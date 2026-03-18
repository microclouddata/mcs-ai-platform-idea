package com.mcs.aiplatform.knowledgebase;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "knowledge_bases")
public class KnowledgeBase extends BaseEntity {

    @Indexed
    private String userId;

    private String name;
    private String description;
    private int documentCount = 0;
}