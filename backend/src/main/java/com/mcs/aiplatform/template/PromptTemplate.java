package com.mcs.aiplatform.template;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "prompt_templates")
public class PromptTemplate extends BaseEntity {

    @Indexed
    private String userId;

    private String name;
    private String description;
    /** Template content with {{variableName}} placeholders. */
    private String content;
    /** Variable names extracted from the template. */
    private List<String> variables = new ArrayList<>();
}
