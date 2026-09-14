package com.saka.qms.web;

import com.saka.qms.config.AccessControlService;
import com.saka.qms.model.Department;
import com.saka.qms.model.Employee;
import com.saka.qms.repository.DepartmentRepository;
import com.saka.qms.repository.EmployeeRepository;
import com.saka.qms.web.dto.EmployeeRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeRepository repository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessControlService accessControl;

    public EmployeeController(
            EmployeeRepository repository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder,
            AccessControlService accessControl) {
        this.repository = repository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessControl = accessControl;
    }

    @GetMapping
    public ResponseEntity<List<Employee>> findAll(Authentication authentication) {
        if (!accessControl.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(repository.findAll().stream()
                .filter(employee -> !Boolean.TRUE.equals(employee.getIsDeleted()))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> findById(
            @PathVariable Integer id,
            Authentication authentication) {
        if (!accessControl.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return repository.findById(id)
                .filter(employee -> !Boolean.TRUE.equals(employee.getIsDeleted()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Employee> create(
            @Valid @RequestBody EmployeeRequest request,
            Authentication authentication) {
        if (!accessControl.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalStateException("Password is required when creating an employee.");
        }
        Employee employee = new Employee();
        copyRequest(request, employee, true);
        employee.setPassword(passwordEncoder.encode(request.password()));
        employee.setIsDeleted(false);
        AuditSupport.apply(employee, authentication);
        logger.info("User '{}' is creating employee '{}' with values: employeeName='{}', departmentIds={}, isAdmin={}, isDepartmentHead={}",
                AuditSupport.actorName(authentication), request.username(), request.employeeName(),
                request.departmentIds(), request.isAdmin(), request.isDepartmentHead());
        return ResponseEntity.ok(repository.save(employee));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Employee> update(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeeRequest request,
            Authentication authentication) {
        if (!accessControl.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Employee employee = repository.findById(id)
                .filter(candidate -> !Boolean.TRUE.equals(candidate.getIsDeleted()))
                .orElse(null);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        copyRequest(request, employee, false);
        if (request.password() != null && !request.password().isBlank()) {
            employee.setPassword(passwordEncoder.encode(request.password()));
        }
        employee.setIsDeleted(false);
        AuditSupport.apply(employee, authentication);
        logger.info("User '{}' is updating employee '{}' (id={}) with values: employeeName='{}', departmentIds={}, isAdmin={}, isDepartmentHead={}",
                AuditSupport.actorName(authentication), employee.getUsername(), id, request.employeeName(),
                request.departmentIds(), request.isAdmin(), request.isDepartmentHead());
        return ResponseEntity.ok(repository.save(employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            Authentication authentication) {
        if (!accessControl.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Employee employee = repository.findById(id)
                .filter(candidate -> !Boolean.TRUE.equals(candidate.getIsDeleted()))
                .orElse(null);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        if (Integer.valueOf(1).equals(employee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        employee.setIsDeleted(true);
        AuditSupport.apply(employee, authentication);
        logger.info("User '{}' is deleting employee '{}' (id={})",
                AuditSupport.actorName(authentication), employee.getUsername(), id);
        repository.save(employee);
        return ResponseEntity.noContent().build();
    }

    private void copyRequest(EmployeeRequest request, Employee employee, boolean creating) {
        if (creating) {
            employee.setEmployeeId(request.employeeId());
            employee.setUsername(request.username());
        }
        employee.setEmployeeName(request.employeeName());
        employee.setIsAdmin(Boolean.TRUE.equals(request.isAdmin()));
        employee.setIsDepartmentHead(Boolean.TRUE.equals(request.isDepartmentHead()));
        employee.setDepartments(resolveDepartments(request.departmentIds()));
    }

    private Set<Department> resolveDepartments(Set<Integer> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Department> departments = departmentRepository.findAllById(departmentIds);
        if (departments.size() != departmentIds.size()) {
            throw new IllegalStateException("One or more departments were not found.");
        }
        return new HashSet<>(departments);
    }
}
