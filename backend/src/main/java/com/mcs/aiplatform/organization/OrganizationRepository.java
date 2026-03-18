package com.mcs.aiplatform.organization;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrganizationRepository extends MongoRepository<Organization, String> {
    List<Organization> findByOwnerId(String ownerId);
}