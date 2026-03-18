package com.mcs.aiplatform.workflow;

import com.mcs.aiplatform.llm.LlmProviderFactory;
import com.mcs.aiplatform.llm.LlmRequest;
import com.mcs.aiplatform.tool.AgentToolBinding;
import com.mcs.aiplatform.tool.executor.KnowledgeSearchExecutor;
import com.mcs.aiplatform.tool.executor.WebSearchExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    private final WorkflowExecutionRepository executionRepository;
    private final LlmProviderFactory llmProviderFactory;
    private final KnowledgeSearchExecutor knowledgeSearchExecutor;
    private final WebSearchExecutor webSearchExecutor;

    @Async
    public void runAsync(String executionId, Workflow workflow) {
        WorkflowExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalStateException("Execution not found: " + executionId));

        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setStartedAt(Instant.now());
        executionRepository.save(execution);

        Map<String, String> context = new HashMap<>(execution.getInput());
        List<StepResult> results = new ArrayList<>();
        String lastOutput = "";

        try {
            for (WorkflowStep step : workflow.getSteps()) {
                long stepStart = System.currentTimeMillis();
                StepResult result = new StepResult();
                result.setStepId(step.getId());
                result.setStepName(step.getName());

                try {
                    String resolvedInput = resolveTemplate(step.getInputTemplate(), context);
                    String output = executeStep(step, resolvedInput, workflow.getAgentId());

                    String key = step.getOutputKey() != null ? step.getOutputKey() : step.getId();
                    context.put(key, output);
                    lastOutput = output;

                    result.setOutput(output);
                    result.setStatus(StepResultStatus.SUCCESS);
                } catch (Exception e) {
                    log.error("Step '{}' failed in execution {}: {}", step.getName(), executionId, e.getMessage());
                    result.setStatus(StepResultStatus.FAILED);
                    result.setError(e.getMessage());
                    result.setDurationMs(System.currentTimeMillis() - stepStart);
                    results.add(result);

                    execution.setStatus(ExecutionStatus.FAILED);
                    execution.setError("Step '" + step.getName() + "' failed: " + e.getMessage());
                    execution.setStepResults(results);
                    execution.setFinishedAt(Instant.now());
                    executionRepository.save(execution);
                    return;
                }

                result.setDurationMs(System.currentTimeMillis() - stepStart);
                results.add(result);
            }

            execution.setStatus(ExecutionStatus.COMPLETED);
            execution.setFinalOutput(lastOutput);
            execution.setContext(context);

        } catch (Exception e) {
            log.error("Workflow execution {} failed unexpectedly: {}", executionId, e.getMessage());
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setError(e.getMessage());
        }

        execution.setStepResults(results);
        execution.setFinishedAt(Instant.now());
        executionRepository.save(execution);
    }

    private String executeStep(WorkflowStep step, String input, String agentId) {
        Map<String, String> config = step.getConfig() != null ? step.getConfig() : Map.of();

        return switch (step.getType()) {
            case KNOWLEDGE_SEARCH -> {
                AgentToolBinding binding = new AgentToolBinding();
                binding.setAgentId(agentId);
                yield knowledgeSearchExecutor.execute(input, binding, agentId);
            }
            case WEB_SEARCH -> {
                AgentToolBinding binding = new AgentToolBinding();
                yield webSearchExecutor.execute(input, binding, agentId);
            }
            case LLM_PROMPT -> {
                String systemPrompt = config.getOrDefault("systemPrompt", "You are a helpful assistant.");
                String provider = config.getOrDefault("provider", "OPENAI");
                yield llmProviderFactory.get(provider)
                        .chat(new LlmRequest(systemPrompt, input))
                        .content();
            }
            case SUMMARIZE -> {
                String provider = config.getOrDefault("provider", "OPENAI");
                yield llmProviderFactory.get(provider)
                        .chat(new LlmRequest("Summarize the following content concisely in 3-5 sentences.", input))
                        .content();
            }
            case HTTP_REQUEST -> {
                String url = config.get("url");
                if (url == null || url.isBlank()) throw new IllegalArgumentException("HTTP_REQUEST step requires 'url' in config");
                String method = config.getOrDefault("method", "GET");
                yield executeHttp(url, method, input);
            }
        };
    }

    private String executeHttp(String url, String method, String body) {
        try {
            var client = RestClient.create();
            if ("POST".equalsIgnoreCase(method)) {
                return client.post().uri(url)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .retrieve().body(String.class);
            }
            return client.get().uri(url).retrieve().body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("HTTP request to " + url + " failed: " + e.getMessage(), e);
        }
    }

    private String resolveTemplate(String template, Map<String, String> context) {
        if (template == null) return "";
        Matcher m = VAR_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            m.appendReplacement(sb, context.getOrDefault(key, ""));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
