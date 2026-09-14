package com.saka.qms.web;

import com.saka.qms.config.AccessControlService;
import com.saka.qms.model.Employee;
import com.saka.qms.model.Department;
import com.saka.qms.repository.DepartmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController extends CrudController<Department, Integer> {
    private final DepartmentRepository repository;
    private final AccessControlService accessControl;

    public DepartmentController(
            DepartmentRepository repository,
            AccessControlService accessControl) {
        super(repository);
        this.repository = repository;
        this.accessControl = accessControl;
    }

    @Override
    @GetMapping
    public List<Department> findAll(Authentication authentication) {
        if (accessControl.isAdmin(authentication)) {
            return repository.findAll();
        }
        if (authentication != null
                && authentication.getPrincipal() instanceof Employee employee) {
            return employee.getDepartments().stream().toList();
        }
        return List.of();
    }
}
