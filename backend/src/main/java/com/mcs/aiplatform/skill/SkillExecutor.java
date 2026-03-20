package com.mcs.aiplatform.skill;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
            case JAVA -> "Java execution not supported yet";
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
        try {
            ProcessBuilder pb = new ProcessBuilder(pythonBinary, "-c", code);
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
        }
    }
}
