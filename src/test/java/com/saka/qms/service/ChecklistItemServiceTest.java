package com.saka.qms.service;

import com.saka.qms.config.AccessControlService;
import com.saka.qms.model.Department;
import com.saka.qms.model.Detail;
import com.saka.qms.model.Employee;
import com.saka.qms.model.NameOfItem;
import com.saka.qms.repository.DepartmentRepository;
import com.saka.qms.repository.DetailRepository;
import com.saka.qms.repository.NameOfItemRepository;
import com.saka.qms.web.dto.ChecklistSaveRequest;
import com.saka.qms.web.dto.DetailRequest;
import com.saka.qms.web.dto.ItemRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChecklistItemServiceTest {
    private final NameOfItemRepository itemRepository = mock(NameOfItemRepository.class);
    private final DetailRepository detailRepository = mock(DetailRepository.class);
    private final DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
    private final AccessControlService accessControl = new AccessControlService(itemRepository);
    private final Authentication authentication = new UsernamePasswordAuthenticationToken(
            new Employee(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    private final ChecklistItemService service = new ChecklistItemService(
            itemRepository, detailRepository, departmentRepository, accessControl);

    @Test
    void savingItemSoftDeletesPersistedDetailsOmittedFromRequest() {
        NameOfItem item = new NameOfItem();
        item.setId(10);
        Department department = new Department();
        department.setId(3);
        Detail removed = new Detail();
        removed.setId(20);
        removed.setRelatedItemId(10);

        when(itemRepository.findById(10)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(NameOfItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(departmentRepository.findById(3)).thenReturn(Optional.of(department));
        when(detailRepository.findByRelatedItemIdAndIsDeletedFalseOrderBySequenceAscIdAsc(10))
                .thenReturn(List.of(removed));
        when(detailRepository.save(any(Detail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemRequest itemRequest = new ItemRequest(
                "Item", null, null, null, null, null, null, null,
                null, null, null, null, null, 3);
        service.save(10, new ChecklistSaveRequest(itemRequest, List.of()), authentication);

        verify(detailRepository, atLeastOnce()).save(argThat(detail -> Boolean.TRUE.equals(detail.getIsDeleted())));
    }

    @Test
    void savingItemRejectsUnauthorizedChecklistManagement() {
        Authentication unauthorized = new UsernamePasswordAuthenticationToken("user", null, List.of());

        ItemRequest itemRequest = new ItemRequest(
                "Item", null, null, null, null, null, null, null,
                null, null, null, null, null, 3);

        assertThrows(AccessDeniedException.class,
                () -> service.save(null, new ChecklistSaveRequest(
                        itemRequest, List.of(new DetailRequest(null, "detail", null, null, null, 1, null))),
                        unauthorized));
        verifyNoInteractions(itemRepository, detailRepository, departmentRepository);
    }
}
