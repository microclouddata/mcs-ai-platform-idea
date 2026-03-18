package com.mcs.aiplatform.organization;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "org_memberships")
@CompoundIndex(def = "{'orgId': 1, 'userId': 1}", unique = true)
public class OrgMembership extends BaseEntity {
    private String orgId;
    private String userId;
    private String userEmail;
    private String userName;
    private OrgRole role = OrgRole.MEMBER;
    private String invitedBy;
}
