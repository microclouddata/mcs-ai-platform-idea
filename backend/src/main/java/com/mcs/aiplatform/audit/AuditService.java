package com.mcs.aiplatform.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(String userId, AuditAction action, String resourceType,
                       String resourceId, String detail) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setDetail(detail);
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                log.setIpAddress(req.getRemoteAddr());
                log.setUserAgent(req.getHeader("User-Agent"));
            }
        } catch (Exception ignored) {}
        auditLogRepository.save(log);
    }

    public List<AuditLog> listForUser(String userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<AuditLog> listForOrg(String orgId) {
        return auditLogRepository.findByOrgIdOrderByCreatedAtDesc(orgId);
    }
}
