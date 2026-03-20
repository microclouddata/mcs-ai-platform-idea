package com.mcs.aiplatform.skill;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SkillExecutor {

    /** Ordered candidates tried at startup; first one that exits 0 wins. */
    private static final List<String> PYTHON_CANDIDATES = List.of("py", "python", "python3");

    @Value("${app.skill.python-binary:}")
    private String configuredPythonBinary;

    @Value("${app.skill.execution-timeout-seconds:10}")
    private int executionTimeoutSeconds;

    private String pythonBinary;

    @PostConstruct
    void resolvePythonBinary() {
        // If the user explicitly configured a binary, trust it.
        if (configuredPythonBinary != null && !configuredPythonBinary.isBlank()) {
            pythonBinary = configuredPythonBinary.trim();
            log.info("Python binary (configured): {}", pythonBinary);
            return;
        }
        // Otherwise probe each candidate and pick the first that exits 0.
        for (String candidate : PYTHON_CANDIDATES) {
            try {
                Process p = new ProcessBuilder(candidate, "--version")
                        .redirectErrorStream(true)
                        .start();
                boolean done = p.waitFor(5, TimeUnit.SECONDS);
                if (done && p.exitValue() == 0) {
                    pythonBinary = candidate;
                    log.info("Python binary (auto-detected): {}", pythonBinary);
                    return;
                }
            } catch (Exception ignored) {
                // candidate not found — try next
            }
        }
        pythonBinary = "python"; // last-resort default
        log.warn("No Python binary detected; falling back to '{}'. " +
                 "Set app.skill.python-binary in application.properties to override.", pythonBinary);
    }

    public String execute(Skill skill, String input) {
        if (skill.getCode() == null || skill.getCode().isBlank()) {
            return "";
        }
        return switch (skill.getLanguage()) {
            case JAVASCRIPT -> executeJs(skill.getCode(), input);
            case PYTHON -> executePython(skill.getCode(), input);
            case JAVA -> executeJava(skill.getCode(), input);
        };
    }

    private String executeJs(String code, String input) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            if (engine == null) {
                log.warn("JavaScript engine unavailable — add nashorn-core to classpath");
                return "JavaScript engine unavailable";
            }
            engine.put("input", input);
            Object result = engine.eval(code);
            return result != null ? String.valueOf(result) : "";
        } catch (Exception e) {
            log.warn("JS skill execution error: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String executePython(String code, String input) {
        Path tempFile = null;
        try {
            // Write code to a temp file so that newlines, quotes, and special
            // characters are never mangled by shell argument parsing (a common
            // failure mode on Windows when using the -c flag).
            tempFile = Files.createTempFile("skill_", ".py");
            Files.writeString(tempFile, code, StandardCharsets.UTF_8);

            ProcessBuilder pb = new ProcessBuilder(pythonBinary, tempFile.toString());
            pb.environment().put("INPUT", input != null ? input : "");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(executionTimeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Timeout: Python execution exceeded " + executionTimeoutSeconds + "s";
            }

            String output = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            int exitCode = process.exitValue();
            if (exitCode != 0 && output.isBlank()) {
                return "Python process exited with code " + exitCode;
            }
            return output;
        } catch (Exception e) {
            log.warn("Python skill execution error: {}", e.getMessage());
            return "Error: " + e.getMessage();
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
            }
        }
    }

    private static final Pattern CLASS_NAME_PATTERN =
            Pattern.compile("public\\s+class\\s+(\\w+)");

    private String executeJava(String code, String input) {
        Path tempDir = null;
        try {
            // Extract the public class name — Java requires file name == class name.
            Matcher matcher = CLASS_NAME_PATTERN.matcher(code);
            if (!matcher.find()) {
                return "Error: no public class found in Java skill code";
            }
            String className = matcher.group(1);

            tempDir = Files.createTempDirectory("skill_java_");
            Path sourceFile = tempDir.resolve(className + ".java");
            Files.writeString(sourceFile, code, StandardCharsets.UTF_8);

            // Compile in-process using the JDK compiler (no need for javac on PATH).
            javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                return "Error: Java compiler unavailable — the app must run on a JDK, not a JRE";
            }
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            try (StandardJavaFileManager fm =
                         compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
                Iterable<? extends JavaFileObject> units = fm.getJavaFileObjects(sourceFile.toFile());
                boolean success = compiler
                        .getTask(null, fm, diagnostics, List.of("-d", tempDir.toString()), null, units)
                        .call();
                if (!success) {
                    String errors = diagnostics.getDiagnostics().stream()
                            .filter(d -> d.getKind() == javax.tools.Diagnostic.Kind.ERROR)
                            .map(d -> d.getMessage(null))
                            .collect(Collectors.joining("\n"));
                    return "Compilation error:\n" + errors;
                }
            }

            // Resolve the java binary from java.home so we never depend on PATH.
            String javaExe = System.getProperty("java.home") +
                             File.separator + "bin" + File.separator + "java";

            ProcessBuilder pb = new ProcessBuilder(
                    javaExe, "-cp", tempDir.toString(),
                    "-DINPUT=" + (input != null ? input : ""),
                    className);
            pb.environment().put("INPUT", input != null ? input : "");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(executionTimeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Timeout: Java execution exceeded " + executionTimeoutSeconds + "s";
            }

            String output = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            int exitCode = process.exitValue();
            if (exitCode != 0 && output.isBlank()) {
                return "Java process exited with code " + exitCode;
            }
            return output;
        } catch (Exception e) {
            log.warn("Java skill execution error: {}", e.getMessage());
            return "Error: " + e.getMessage();
        } finally {
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                         .sorted(Comparator.reverseOrder())
                         .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
                } catch (Exception ignored) {}
            }
        }
    }
}
