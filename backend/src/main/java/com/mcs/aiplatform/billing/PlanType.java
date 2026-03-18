package com.mcs.aiplatform.billing;

public enum PlanType {
    FREE(5, 100, 50),
    PRO(50, 5000, 1000),
    ENTERPRISE(500, 100000, 10000);

    public final int maxAgents;
    public final int maxRequestsPerDay;
    public final int maxDocuments;

    PlanType(int maxAgents, int maxRequestsPerDay, int maxDocuments) {
        this.maxAgents = maxAgents;
        this.maxRequestsPerDay = maxRequestsPerDay;
        this.maxDocuments = maxDocuments;
    }
}
