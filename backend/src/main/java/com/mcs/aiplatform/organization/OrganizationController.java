package com.mcs.aiplatform.organization;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService orgService;

    @GetMapping
    public ApiResponse<List<Organization>> list() {
        return ApiResponse.ok(orgService.listForUser(CurrentUser.userId()));
    }

    @PostMapping
    public ApiResponse<Organization> create(@RequestBody CreateOrganizationRequest req) {
        return ApiResponse.ok(orgService.create(
                CurrentUser.userId(), CurrentUser.email(), CurrentUser.name(), req));
    }

    @GetMapping("/{orgId}")
    public ApiResponse<Organization> get(@PathVariable String orgId) {
        return ApiResponse.ok(orgService.getOwned(orgId, CurrentUser.userId()));
    }

    @GetMapping("/{orgId}/members")
    public ApiResponse<List<OrgMembership>> members(@PathVariable String orgId) {
        return ApiResponse.ok(orgService.listMembers(orgId, CurrentUser.userId()));
    }

    @PostMapping("/{orgId}/members")
    public ApiResponse<OrgMembership> invite(@PathVariable String orgId,
                                              @RequestBody InviteMemberRequest req) {
        return ApiResponse.ok(orgService.inviteMember(orgId, CurrentUser.userId(), req));
    }

    @DeleteMapping("/{orgId}/members/{userId}")
    public ApiResponse<Void> removeMember(@PathVariable String orgId,
                                           @PathVariable String userId) {
        orgService.removeMember(orgId, CurrentUser.userId(), userId);
        return ApiResponse.ok(null);
    }
}
