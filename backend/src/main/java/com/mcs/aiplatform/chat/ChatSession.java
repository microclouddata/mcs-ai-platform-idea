package com.mcs.aiplatform.chat;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "chat_sessions")
@CompoundIndex(def = "{'userId': 1, 'agentId': 1, 'updatedAt': -1}")
public class ChatSession extends BaseEntity {
    private String userId;
    private String agentId;
    private String title;
}
