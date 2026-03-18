package com.mcs.aiplatform.tool.executor;

import com.mcs.aiplatform.tool.AgentToolBinding;
import com.mcs.aiplatform.tool.ToolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Slf4j
@Component
public class CalculatorExecutor implements ToolExecutor {

    @Override
    public ToolType supportedType() {
        return ToolType.CALCULATOR;
    }

    @Override
    public String execute(String expression, AgentToolBinding binding, String agentId) {
        // Allow only digits, basic operators, parentheses, decimals
        String sanitized = expression.replaceAll("[^0-9+\\-*/().\\s]", "").trim();
        if (sanitized.isEmpty()) return "Invalid expression";

        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            if (engine == null) return "Calculator unavailable";
            return String.valueOf(engine.eval(sanitized));
        } catch (Exception e) {
            log.warn("Calculator error for '{}': {}", expression, e.getMessage());
            return "Calculation error: " + e.getMessage();
        }
    }
}