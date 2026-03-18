package com.mcs.aiplatform.audit;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "audit_logs")
public class AuditLog extends BaseEntity {
    @Indexed
    private String userId;
    private String orgId;
    private AuditAction action;
    private String resourceType;
    private String resourceId;
    private String detail;
    private String ipAddress;
    private String userAgent;
}
