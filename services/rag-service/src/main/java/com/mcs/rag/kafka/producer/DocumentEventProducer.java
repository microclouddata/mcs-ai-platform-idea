package com.mcs.rag.kafka.producer;

import com.mcs.rag.kafka.KafkaTopics;
import com.mcs.rag.kafka.event.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDocumentUploaded(DocumentUploadedEvent event) {
        kafkaTemplate.send(KafkaTopics.DOCUMENT_UPLOADED, event.documentId(), event);
        log.info("Published document.uploaded event for documentId={}", event.documentId());
    }
}
