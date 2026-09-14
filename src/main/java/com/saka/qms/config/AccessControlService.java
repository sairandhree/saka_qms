package com.saka.qms.config;

import com.saka.qms.model.Detail;
import com.saka.qms.model.Employee;
import com.saka.qms.model.NameOfItem;
import com.saka.qms.repository.NameOfItemRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccessControlService {
    private final NameOfItemRepository itemRepository;

    public AccessControlService(NameOfItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public boolean canAccessItem(Authentication authentication, NameOfItem item) {
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }
        if (!(authentication.getPrincipal() instanceof Employee employee)
                || item.getDepartment() == null) {
            return false;
        }
        return employee.getDepartments().stream()
                .anyMatch(department -> department.getId().equals(item.getDepartment().getId()));
    }

    public boolean canAccessItemId(Authentication authentication, Integer itemId) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (itemId == null) {
            return false;
        }
        return itemRepository.findById(itemId)
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .map(item -> canAccessItem(authentication, item))
                .orElse(false);
    }

    public boolean canAccessDetail(Authentication authentication, Detail detail) {
        return canAccessItemId(authentication, detail.getRelatedItemId());
    }

    public List<NameOfItem> visibleItems(Authentication authentication, List<NameOfItem> items) {
        return items.stream()
                .filter(item -> canAccessItem(authentication, item))
                .toList();
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean canManageChecklist(Authentication authentication) {
        if (!(authentication != null
                && authentication.getPrincipal() instanceof Employee employee)) {
            return false;
        }
        return isAdmin(authentication) || Boolean.TRUE.equals(employee.getIsDepartmentHead());
    }
}
