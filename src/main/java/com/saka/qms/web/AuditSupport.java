package com.saka.qms.web;

import com.saka.qms.model.Auditable;
import com.saka.qms.model.Employee;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class AuditSupport {
    private AuditSupport() {
    }

    public static void apply(Auditable entity, Authentication authentication) {
        String username = actorName(authentication);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        entity.setModifiedBy(username);
        entity.setModifiedOn(now);
        entity.setAuthorisedBy(username);
    }

    public static String actorName(Authentication authentication) {
        if (authentication == null) {
            return "anonymous";
        }
        if (authentication.getPrincipal() instanceof Employee employee
                && employee.getUsername() != null
                && !employee.getUsername().isBlank()) {
            return employee.getUsername();
        }
        if (authentication.getPrincipal() instanceof Employee employee
                && employee.getId() != null) {
            return "employee:" + employee.getId();
        }
        if (authentication.getPrincipal() instanceof Employee) {
            return "unknown-employee";
        }
        return authentication.getName();
    }
}
