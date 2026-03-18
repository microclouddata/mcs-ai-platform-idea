package com.mcs.aiplatform.knowledgebase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository repository;

    public KnowledgeBase create(String userId, CreateKnowledgeBaseRequest req) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setUserId(userId);
        kb.setName(req.name());
        kb.setDescription(req.description() != null ? req.description() : "");
        return repository.save(kb);
    }

    public List<KnowledgeBase> listByUser(String userId) {
        return repository.findByUserId(userId);
    }

    public KnowledgeBase getByIdAndUser(String id, String userId) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Knowledge base not found"));
    }

    public void delete(String id, String userId) {
        KnowledgeBase kb = getByIdAndUser(id, userId);
        repository.delete(kb);
    }

    public void incrementDocumentCount(String kbId) {
        repository.findById(kbId).ifPresent(kb -> {
            kb.setDocumentCount(kb.getDocumentCount() + 1);
            repository.save(kb);
        });
    }
}