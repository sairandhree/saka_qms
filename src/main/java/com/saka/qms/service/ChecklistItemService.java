package com.saka.qms.service;

import com.saka.qms.config.AccessControlService;
import com.saka.qms.model.Department;
import com.saka.qms.model.Detail;
import com.saka.qms.model.NameOfItem;
import com.saka.qms.repository.DepartmentRepository;
import com.saka.qms.repository.DetailRepository;
import com.saka.qms.repository.NameOfItemRepository;
import com.saka.qms.web.AuditSupport;
import com.saka.qms.web.dto.ChecklistSaveRequest;
import com.saka.qms.web.dto.ChecklistSaveResponse;
import com.saka.qms.web.dto.DetailRequest;
import com.saka.qms.web.dto.ItemRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ChecklistItemService {
    private static final Logger logger = LoggerFactory.getLogger(ChecklistItemService.class);

    private final NameOfItemRepository itemRepository;
    private final DetailRepository detailRepository;
    private final DepartmentRepository departmentRepository;
    private final AccessControlService accessControl;

    public ChecklistItemService(
            NameOfItemRepository itemRepository,
            DetailRepository detailRepository,
            DepartmentRepository departmentRepository,
            AccessControlService accessControl) {
        this.itemRepository = itemRepository;
        this.detailRepository = detailRepository;
        this.departmentRepository = departmentRepository;
        this.accessControl = accessControl;
    }

    @Transactional
    public ChecklistSaveResponse save(
            Integer itemId,
            ChecklistSaveRequest request,
            Authentication authentication) {
        if (!accessControl.canManageChecklist(authentication)) {
            throw new AccessDeniedException("Checklist item management is forbidden.");
        }

        NameOfItem item = loadItem(itemId, authentication);

        if (itemId != null && !accessControl.canAccessItem(authentication, item)) {
            throw new AccessDeniedException("Checklist item management is forbidden.");
        }

        Department department = findAuthorizedDepartment(request.item().departmentId(), authentication);

        copyItem(request.item(), item);
        item.setDepartment(department);
        item.setIsDeleted(false);
        AuditSupport.apply(item, authentication);
        logger.info("action=checklist.save actor={} itemId={} itemName={} departmentId={} detailCount={} detailIds={}",
                AuditSupport.actorName(authentication), itemId, request.item().nameOfItem(),
                request.item().departmentId(), request.details().size(),
                request.details().stream().map(DetailRequest::id).filter(id -> id != null).toList());
        NameOfItem savedItem = itemRepository.save(item);

        List<Detail> existingDetails = itemId == null
                ? List.of()
                : detailRepository.findByRelatedItemIdAndIsDeletedFalseOrderBySequenceAscIdAsc(savedItem.getId());
        Map<Integer, Detail> existingById = existingDetails.stream()
                .collect(Collectors.toMap(Detail::getId, Function.identity()));
        Set<Integer> retainedIds = new HashSet<>();

        for (DetailRequest detailRequest : request.details()) {
            Detail detail;
            if (detailRequest.id() == null) {
                detail = new Detail();
                detail.setRelatedItemId(savedItem.getId());
            } else {
                detail = existingById.get(detailRequest.id());
                if (detail == null) {
                    throw new IllegalStateException("Checklist detail was not found for this item.");
                }
                retainedIds.add(detail.getId());
            }
            copyDetail(detailRequest, detail);
            detail.setRelatedItemId(savedItem.getId());
            detail.setIsDeleted(false);
            AuditSupport.apply(detail, authentication);
            detailRepository.save(detail);
        }

        for (Detail detail : existingDetails) {
            if (!retainedIds.contains(detail.getId())
                    && request.details().stream().noneMatch(candidate -> candidate.id() != null
                    && candidate.id().equals(detail.getId()))) {
                detail.setIsDeleted(true);
                AuditSupport.apply(detail, authentication);
                detailRepository.save(detail);
            }
        }

        return new ChecklistSaveResponse(
                savedItem,
                detailRepository.findByRelatedItemIdAndIsDeletedFalseOrderBySequenceAscIdAsc(savedItem.getId()));
    }

    @Transactional
    public NameOfItem saveItem(
            Integer itemId,
            ItemRequest request,
            Authentication authentication) {
        if (!accessControl.canManageChecklist(authentication)) {
            throw new AccessDeniedException("Checklist item management is forbidden.");
        }
        NameOfItem item = loadItem(itemId, authentication);
        item.setDepartment(findAuthorizedDepartment(request.departmentId(), authentication));
        copyItem(request, item);
        item.setIsDeleted(false);
        AuditSupport.apply(item, authentication);
        logger.info("action=checklist.item.save actor={} itemId={} itemName={} departmentId={}",
                AuditSupport.actorName(authentication), itemId, request.nameOfItem(), request.departmentId());
        return itemRepository.save(item);
    }

    private NameOfItem loadItem(Integer itemId, Authentication authentication) {
        if (itemId == null) {
            return new NameOfItem();
        }
        NameOfItem item = itemRepository.findById(itemId)
                .filter(candidate -> !Boolean.TRUE.equals(candidate.getIsDeleted()))
                .orElseThrow(() -> new IllegalStateException("Checklist item was not found."));
        if (!accessControl.canAccessItem(authentication, item)) {
            throw new AccessDeniedException("Checklist item management is forbidden.");
        }
        return item;
    }

    private Department findAuthorizedDepartment(
            Integer departmentId,
            Authentication authentication) {
        if (departmentId == null) {
            throw new IllegalStateException("Department is required.");
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalStateException("Department was not found."));
        if (!accessControl.canAccessDepartment(authentication, department)) {
            throw new AccessDeniedException("Checklist item management is forbidden.");
        }
        return department;
    }

    private void copyItem(ItemRequest source, NameOfItem target) {
        target.setNameOfItem(source.nameOfItem());
        target.setNote(source.note());
        target.setSequence(source.sequence());
        target.setCode1(source.code1());
        target.setCode2(source.code2());
        target.setCode3(source.code3());
        target.setCode4(source.code4());
        target.setCode5(source.code5());
        target.setCdinf1(source.cdinf1());
        target.setCdinf2(source.cdinf2());
        target.setCdinf3(source.cdinf3());
        target.setCdinf4(source.cdinf4());
        target.setCdinf5(source.cdinf5());
    }

    private void copyDetail(DetailRequest source, Detail target) {
        target.setDetails(source.details());
        target.setNote(source.note());
        target.setParameters(source.parameters());
        target.setLink(source.link());
        target.setSequence(source.sequence());
        if (source.passward() != null) {
            target.setPassward(source.passward());
        }
    }
}
