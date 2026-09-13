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
        String username = authentication.getPrincipal() instanceof Employee employee
                ? employee.getUsername()
                : authentication.getName();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        entity.setModifiedBy(username);
        entity.setModifiedOn(now);
        entity.setAuthorisedBy(username);
    }
}
