package com.saka.qms.web;

import com.saka.qms.config.AccessControlService;
import com.saka.qms.model.NameOfItem;
import com.saka.qms.repository.NameOfItemRepository;
import com.saka.qms.service.ChecklistItemService;
import com.saka.qms.web.dto.ChecklistSaveRequest;
import com.saka.qms.web.dto.ChecklistSaveResponse;
import com.saka.qms.web.dto.ItemRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class NameOfItemController {
    private static final Logger logger = LoggerFactory.getLogger(NameOfItemController.class);

    private final NameOfItemRepository repository;
    private final AccessControlService accessControl;
    private final ChecklistItemService checklistItemService;

    public NameOfItemController(
            NameOfItemRepository repository,
            AccessControlService accessControl,
            ChecklistItemService checklistItemService) {
        this.repository = repository;
        this.accessControl = accessControl;
        this.checklistItemService = checklistItemService;
    }

    @GetMapping
    public List<NameOfItem> findAll(Authentication authentication) {
        return accessControl.visibleItems(authentication, repository.findAll().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .toList());
    }

    @GetMapping("/search")
    public List<NameOfItem> search(
            @RequestParam String name,
            Authentication authentication) {
        return accessControl.visibleItems(
                authentication,
                repository.findByNameOfItemContainingIgnoreCase(name).stream()
                        .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NameOfItem> findById(
            @PathVariable Integer id,
            Authentication authentication) {
        return repository.findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .filter(item -> accessControl.canAccessItem(authentication, item))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NameOfItem> create(
            @Valid @RequestBody ItemRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(checklistItemService.saveItem(null, request, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NameOfItem> update(
            @PathVariable Integer id,
            @Valid @RequestBody ItemRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(checklistItemService.saveItem(id, request, authentication));
    }

    @PostMapping("/with-details")
    public ResponseEntity<ChecklistSaveResponse> createWithDetails(
            @Valid @RequestBody ChecklistSaveRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(checklistItemService.save(null, request, authentication));
    }

    @PutMapping("/{id}/with-details")
    public ResponseEntity<ChecklistSaveResponse> updateWithDetails(
            @PathVariable Integer id,
            @Valid @RequestBody ChecklistSaveRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(checklistItemService.save(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            Authentication authentication) {
        if (!accessControl.canManageChecklist(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        NameOfItem item = repository.findById(id)
                .filter(candidate -> !Boolean.TRUE.equals(candidate.getIsDeleted()))
                .orElse(null);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        if (!accessControl.canAccessItem(authentication, item)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        item.setIsDeleted(true);
        AuditSupport.apply(item, authentication);
        logger.info("User '{}' is deleting checklist item '{}' (id={}) from department id={}",
                AuditSupport.actorName(authentication), item.getNameOfItem(), id,
                item.getDepartment() == null ? null : item.getDepartment().getId());
        repository.save(item);
        return ResponseEntity.noContent().build();
    }
}
