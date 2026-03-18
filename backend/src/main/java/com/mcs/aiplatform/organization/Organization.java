package com.mcs.aiplatform.organization;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "organizations")
public class Organization extends BaseEntity {
    private String name;
    private String description;
    private String slug;
    private String plan = "FREE"; // FREE, PRO, ENTERPRISE
    private String ownerId;
}