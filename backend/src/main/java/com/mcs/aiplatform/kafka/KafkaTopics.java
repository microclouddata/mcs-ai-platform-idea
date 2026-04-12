package com.mcs.aiplatform.kafka;

/**
 * Centralised Kafka topic name constants.
 *
 * <pre>
 * document.uploaded  — produced by DocumentService on file save;
 *                      consumed by DocumentProcessingConsumer (extract → chunk → embed).
 *
 * usage.recorded     — produced by ChatService after every LLM response;
 *                      consumed by UsageConsumer (persists to MongoDB).
 * </pre>
 */
public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String DOCUMENT_UPLOADED = "document.uploaded";
    public static final String USAGE_RECORDED    = "usage.recorded";
}
