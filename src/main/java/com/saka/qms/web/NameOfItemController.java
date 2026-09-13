package com.saka.qms.web;

import com.saka.qms.config.AccessControlService;
import com.saka.qms.model.NameOfItem;
import com.saka.qms.repository.NameOfItemRepository;
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
@RequestMapping("/api/items")
public class NameOfItemController {
    private final NameOfItemRepository repository;
    private final AccessControlService accessControl;

    public NameOfItemController(
            NameOfItemRepository repository,
            AccessControlService accessControl) {
        this.repository = repository;
        this.accessControl = accessControl;
    }

    @GetMapping
    public List<NameOfItem> findAll(Authentication authentication) {
        return accessControl.visibleItems(authentication, repository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NameOfItem> findById(
            @PathVariable Integer id,
            Authentication authentication) {
        return repository.findById(id)
                .filter(item -> accessControl.canAccessItem(authentication, item))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NameOfItem> create(
            @RequestBody NameOfItem entity,
            Authentication authentication) {
        if (!accessControl.canAccessItem(authentication, entity)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        entity.setId(null);
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NameOfItem> update(
            @PathVariable Integer id,
            @RequestBody NameOfItem entity,
            Authentication authentication) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        if (!accessControl.canAccessItem(authentication, entity)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(repository.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            Authentication authentication) {
        NameOfItem item = repository.findById(id).orElse(null);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        if (!accessControl.canAccessItem(authentication, item)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        repository.delete(item);
        return ResponseEntity.noContent().build();
    }
}
