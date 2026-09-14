package com.saka.qms.web;

import com.saka.qms.config.AccessControlService;
import com.saka.qms.model.Department;
import com.saka.qms.model.Employee;
import com.saka.qms.repository.DepartmentRepository;
import com.saka.qms.web.dto.DepartmentRequest;
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
@RequestMapping("/api/departments")
public class DepartmentController {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentRepository repository;
    private final AccessControlService accessControl;

    public DepartmentController(
            DepartmentRepository repository,
            AccessControlService accessControl) {
        this.repository = repository;
        this.accessControl = accessControl;
    }

    @GetMapping
    public ResponseEntity<List<Department>> findAll(Authentication authentication) {
        if (accessControl.isAdmin(authentication)) {
            return ResponseEntity.ok(repository.findAll());
        }
        if (authentication != null
                && authentication.getPrincipal() instanceof Employee employee) {
            return ResponseEntity.ok(employee.getDepartments().stream().toList());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    public ResponseEntity<Department> create(
            @Valid @RequestBody DepartmentRequest request,
            Authentication authentication) {
        if (!accessControl.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Department department = new Department();
        department.setDeptName(request.deptName());
        AuditSupport.apply(department, authentication);
        logger.info("action=department.create actor={} departmentName={}",
                AuditSupport.actorName(authentication), request.deptName());
        return ResponseEntity.ok(repository.save(department));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> update(
            @PathVariable Integer id,
            @Valid @RequestBody DepartmentRequest request,
            Authentication authentication) {
        if (!accessControl.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Department department = repository.findById(id).orElse(null);
        if (department == null) {
            return ResponseEntity.notFound().build();
        }
        department.setDeptName(request.deptName());
        AuditSupport.apply(department, authentication);
        logger.info("action=department.update actor={} departmentId={} departmentName={}",
                AuditSupport.actorName(authentication), id, request.deptName());
        return ResponseEntity.ok(repository.save(department));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            Authentication authentication) {
        if (!accessControl.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        logger.info("action=department.delete actor={} departmentId={}",
                AuditSupport.actorName(authentication), id);
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
