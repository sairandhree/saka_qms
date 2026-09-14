package com.saka.qms.web;

import com.saka.qms.config.AccessControlService;
import com.saka.qms.model.Detail;
import com.saka.qms.model.NameOfItem;
import com.saka.qms.repository.DetailRepository;
import com.saka.qms.repository.NameOfItemRepository;
import com.saka.qms.web.dto.DetailRequest;
import jakarta.validation.Valid;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/details")
public class DetailController {
    private static final Logger logger = LoggerFactory.getLogger(DetailController.class);

    private final DetailRepository repository;
    private final NameOfItemRepository itemRepository;
    private final AccessControlService accessControl;

    public DetailController(
            DetailRepository repository,
            NameOfItemRepository itemRepository,
            AccessControlService accessControl) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.accessControl = accessControl;
    }

    @GetMapping
    public List<Detail> findAll(Authentication authentication) {
        return repository.findAll().stream()
                .filter(detail -> !Boolean.TRUE.equals(detail.getIsDeleted()))
                .filter(detail -> accessControl.canAccessDetail(authentication, detail))
                .toList();
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<Detail>> findByItem(
            @PathVariable Integer itemId,
            Authentication authentication) {
        if (!accessControl.canAccessItemId(authentication, itemId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(repository
                .findByRelatedItemIdAndIsDeletedFalseOrderBySequenceAscIdAsc(itemId));
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

    @PostMapping("/item/{itemId}")
    public ResponseEntity<Detail> create(
            @PathVariable Integer itemId,
            @Valid @RequestBody DetailRequest request,
            Authentication authentication) {
        if (!accessControl.canManageChecklist(authentication)
                || !accessControl.canAccessItemId(authentication, itemId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (itemRepository.findById(itemId)
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Detail detail = new Detail();
        copyRequest(request, detail);
        detail.setRelatedItemId(itemId);
        detail.setIsDeleted(false);
        AuditSupport.apply(detail, authentication);
        logger.info("User '{}' is creating detail for item id={} with values: sequence={}, detailId={}",
                AuditSupport.actorName(authentication), itemId, request.sequence(), request.id());
        return ResponseEntity.ok(repository.save(detail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Detail> update(
            @PathVariable Integer id,
            @Valid @RequestBody DetailRequest request,
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
        copyRequest(request, detail);
        detail.setIsDeleted(false);
        AuditSupport.apply(detail, authentication);
        logger.info("User '{}' is updating detail id={} for item id={} with values: sequence={}",
                AuditSupport.actorName(authentication), id, detail.getRelatedItemId(), request.sequence());
        return ResponseEntity.ok(repository.save(detail));
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
        logger.info("User '{}' is deleting detail id={} for item id={}",
                AuditSupport.actorName(authentication), id, detail.getRelatedItemId());
        repository.save(detail);
        return ResponseEntity.noContent().build();
    }

    private void copyRequest(DetailRequest request, Detail detail) {
        detail.setDetails(request.details());
        detail.setNote(request.note());
        detail.setParameters(request.parameters());
        detail.setLink(request.link());
        detail.setSequence(request.sequence());
    }
}
