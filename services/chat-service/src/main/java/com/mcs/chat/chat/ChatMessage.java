package com.mcs.chat.chat;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "chat_messages")
@CompoundIndex(def = "{'sessionId': 1, 'createdAt': 1}")
public class ChatMessage {
    @Id
    private String id;
    private String sessionId;
    private String agentId;
    private String userId;
    private String role;
    private String content;
    private List<Object> toolCalls;
    private List<Object> toolResults;
    @CreatedDate
    private Instant createdAt;
}
