package com.mcs.aiplatform.billing;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "subscriptions")
public class Subscription extends BaseEntity {
    @Indexed(unique = true)
    private String userId;
    private String orgId;
    private PlanType plan = PlanType.FREE;
    private String status = "ACTIVE"; // ACTIVE, CANCELLED, PAST_DUE
    private Instant periodStart;
    private Instant periodEnd;
    private String stripeCustomerId;
    private String stripeSubscriptionId;
}
