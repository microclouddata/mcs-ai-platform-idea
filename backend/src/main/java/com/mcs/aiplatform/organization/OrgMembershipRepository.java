package com.mcs.aiplatform.organization;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface OrgMembershipRepository extends MongoRepository<OrgMembership, String> {
    List<OrgMembership> findByOrgId(String orgId);
    List<OrgMembership> findByUserId(String userId);
    Optional<OrgMembership> findByOrgIdAndUserId(String orgId, String userId);
    void deleteByOrgIdAndUserId(String orgId, String userId);
    boolean existsByOrgIdAndUserId(String orgId, String userId);
}
