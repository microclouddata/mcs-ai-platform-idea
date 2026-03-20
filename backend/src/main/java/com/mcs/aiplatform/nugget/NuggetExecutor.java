package com.mcs.aiplatform.nugget;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NuggetExecutor {

    @Value("${app.nugget.python-binary:python3}")
    private String pythonBinary;

    @Value("${app.nugget.execution-timeout-seconds:10}")
    private int executionTimeoutSeconds;

    public String execute(Nugget nugget, String input) {
        if (nugget.getCode() == null || nugget.getCode().isBlank()) {
            return "";
        }
        return switch (nugget.getLanguage()) {
            case JAVASCRIPT -> executeJs(nugget.getCode(), input);
            case PYTHON -> executePython(nugget.getCode(), input);
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
            log.warn("JS nugget execution error: {}", e.getMessage());
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
            log.warn("Python nugget execution error: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
