package com.mcs.aiplatform.kafka.producer;

import com.mcs.aiplatform.kafka.KafkaTopics;
import com.mcs.aiplatform.kafka.event.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a {@code document.uploaded} event.
     * Uses {@code documentId} as the partition key so all events for the same
     * document land on the same partition and are processed in order.
     */
    public void publishDocumentUploaded(DocumentUploadedEvent event) {
        kafkaTemplate.send(KafkaTopics.DOCUMENT_UPLOADED, event.documentId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] Failed to publish document.uploaded for documentId={}: {}",
                                event.documentId(), ex.getMessage());
                    } else {
                        log.info("[Kafka] Published document.uploaded: documentId={}, partition={}, offset={}",
                                event.documentId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
