package com.mcs.aiplatform.organization;

import com.mcs.aiplatform.user.User;
import com.mcs.aiplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository orgRepository;
    private final OrgMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public Organization create(String ownerId, String ownerEmail, String ownerName,
                               CreateOrganizationRequest req) {
        Organization org = new Organization();
        org.setName(req.name());
        org.setDescription(req.description());
        org.setSlug(toSlug(req.name()));
        org.setOwnerId(ownerId);
        org = orgRepository.save(org);

        OrgMembership membership = new OrgMembership();
        membership.setOrgId(org.getId());
        membership.setUserId(ownerId);
        membership.setUserEmail(ownerEmail);
        membership.setUserName(ownerName);
        membership.setRole(OrgRole.OWNER);
        membershipRepository.save(membership);

        return org;
    }

    public List<Organization> listForUser(String userId) {
        List<String> orgIds = membershipRepository.findByUserId(userId)
                .stream().map(OrgMembership::getOrgId).toList();
        return orgRepository.findAllById(orgIds);
    }

    public Organization getOwned(String orgId, String userId) {
        Organization org = orgRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        if (!membershipRepository.existsByOrgIdAndUserId(orgId, userId)) {
            throw new RuntimeException("Access denied");
        }
        return org;
    }

    public OrgMembership inviteMember(String orgId, String inviterId, InviteMemberRequest req) {
        requireRole(orgId, inviterId, OrgRole.ADMIN);
        User user = userRepository.findByEmail(req.email().toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + req.email()));
        if (membershipRepository.existsByOrgIdAndUserId(orgId, user.getId())) {
            throw new RuntimeException("User is already a member");
        }
        OrgMembership m = new OrgMembership();
        m.setOrgId(orgId);
        m.setUserId(user.getId());
        m.setUserEmail(user.getEmail());
        m.setUserName(user.getName());
        m.setRole(req.role() != null ? req.role() : OrgRole.MEMBER);
        m.setInvitedBy(inviterId);
        return membershipRepository.save(m);
    }

    public void removeMember(String orgId, String requesterId, String targetUserId) {
        requireRole(orgId, requesterId, OrgRole.ADMIN);
        Organization org = orgRepository.findById(orgId).orElseThrow();
        if (org.getOwnerId().equals(targetUserId)) {
            throw new RuntimeException("Cannot remove the organization owner");
        }
        membershipRepository.deleteByOrgIdAndUserId(orgId, targetUserId);
    }

    public List<OrgMembership> listMembers(String orgId, String requesterId) {
        if (!membershipRepository.existsByOrgIdAndUserId(orgId, requesterId)) {
            throw new RuntimeException("Access denied");
        }
        return membershipRepository.findByOrgId(orgId);
    }

    public Organization updatePlan(String orgId, String plan) {
        Organization org = orgRepository.findById(orgId).orElseThrow();
        org.setPlan(plan);
        return orgRepository.save(org);
    }

    private void requireRole(String orgId, String userId, OrgRole minimumRole) {
        OrgMembership m = membershipRepository.findByOrgIdAndUserId(orgId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
        if (m.getRole() == OrgRole.MEMBER && minimumRole != OrgRole.MEMBER) {
            throw new RuntimeException("Insufficient organization role");
        }
    }

    private String toSlug(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        return Pattern.compile("[^\\w\\s-]").matcher(normalized).replaceAll("")
                .trim().toLowerCase().replaceAll("[\\s]+", "-");
    }
}
