package com.saka.qms.web;

import com.saka.qms.config.AccessControlService;
import com.saka.qms.model.Detail;
import com.saka.qms.repository.DetailRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/details")
public class DetailController {
    private final DetailRepository repository;
    private final AccessControlService accessControl;

    public DetailController(
            DetailRepository repository,
            AccessControlService accessControl) {
        this.repository = repository;
        this.accessControl = accessControl;
    }

    @GetMapping
    public List<Detail> findAll(Authentication authentication) {
        return repository.findAll().stream()
                .filter(detail -> !Boolean.TRUE.equals(detail.getIsDeleted()))
                .filter(detail -> accessControl.canAccessDetail(authentication, detail))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Detail> findById(
            @PathVariable Integer id,
            Authentication authentication) {
        return repository.findById(id)
                .filter(detail -> !Boolean.TRUE.equals(detail.getIsDeleted()))
                .filter(detail -> accessControl.canAccessDetail(authentication, detail))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Detail> create(
            @RequestBody Detail entity,
            Authentication authentication) {
        if (!accessControl.canManageChecklist(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!accessControl.canAccessItemId(authentication, entity.getRelatedItemId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        entity.setId(null);
        entity.setIsDeleted(false);
        AuditSupport.apply(entity, authentication);
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Detail> update(
            @PathVariable Integer id,
            @RequestBody Detail entity,
            Authentication authentication) {
        if (!accessControl.canManageChecklist(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Detail existing = repository.findById(id)
                .filter(detail -> !Boolean.TRUE.equals(detail.getIsDeleted()))
                .orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!accessControl.canAccessDetail(authentication, existing)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!accessControl.canAccessItemId(authentication, entity.getRelatedItemId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        entity.setId(id);
        entity.setIsDeleted(false);
        AuditSupport.apply(entity, authentication);
        return ResponseEntity.ok(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            Authentication authentication) {
            if (!accessControl.canManageChecklist(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Detail detail = repository.findById(id)
                    .filter(candidate -> !Boolean.TRUE.equals(candidate.getIsDeleted()))
                    .orElse(null);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        if (!accessControl.canAccessDetail(authentication, detail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        detail.setIsDeleted(true);
        AuditSupport.apply(detail, authentication);
        repository.save(detail);
        return ResponseEntity.noContent().build();
    }
}
