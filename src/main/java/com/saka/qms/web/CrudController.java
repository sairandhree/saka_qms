package com.saka.qms.web;

import com.saka.qms.model.Identifiable;
import com.saka.qms.model.Auditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

public abstract class CrudController<T extends Identifiable<ID>, ID> {
    private final JpaRepository<T, ID> repository;

    protected CrudController(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    protected T prepareForCreate(T entity) {
        return entity;
    }

    protected T prepareForUpdate(ID id, T entity) {
        return entity;
    }

    @GetMapping
    public List<T> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> findById(@PathVariable ID id) {
        return repository.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<T> create(
            @RequestBody T entity,
            Authentication authentication) {
        entity.setId(null);
        applyAudit(entity, authentication);
        return ResponseEntity.ok(repository.save(prepareForCreate(entity)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> update(
            @PathVariable ID id,
            @RequestBody T entity,
            Authentication authentication) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        applyAudit(entity, authentication);
        return ResponseEntity.ok(repository.save(prepareForUpdate(id, entity)));
    }

    private void applyAudit(T entity, Authentication authentication) {
        if (entity instanceof Auditable auditable) {
            AuditSupport.apply(auditable, authentication);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
