package com.mcs.aiplatform.template;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PromptTemplateService {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    private final PromptTemplateRepository repository;

    public PromptTemplate create(String userId, CreateTemplateRequest req) {
        PromptTemplate t = new PromptTemplate();
        t.setUserId(userId);
        t.setName(req.name());
        t.setDescription(req.description() != null ? req.description() : "");
        t.setContent(req.content());
        t.setVariables(extractVariables(req.content()));
        return repository.save(t);
    }

    public List<PromptTemplate> listByUser(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public PromptTemplate getOwned(String id, String userId) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));
    }

    public String render(String id, String userId, Map<String, String> variables) {
        PromptTemplate t = getOwned(id, userId);
        return applyVariables(t.getContent(), variables);
    }

    public void delete(String id, String userId) {
        repository.delete(getOwned(id, userId));
    }

    public String applyVariables(String template, Map<String, String> variables) {
        if (template == null || variables == null) return template;
        for (Map.Entry<String, String> e : variables.entrySet()) {
            template = template.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return template;
    }

    private List<String> extractVariables(String content) {
        List<String> vars = new ArrayList<>();
        if (content == null) return vars;
        Matcher m = VAR_PATTERN.matcher(content);
        while (m.find()) {
            String v = m.group(1);
            if (!vars.contains(v)) vars.add(v);
        }
        return vars;
    }
}
