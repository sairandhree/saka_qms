package com.saka.qms.web;

import com.saka.qms.model.Employee;
import com.saka.qms.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController extends CrudController<Employee, Integer> {
    private final EmployeeRepository repository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeController(EmployeeRepository repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @GetMapping
    public List<Employee> findAll(Authentication authentication) {
        return repository.findAll().stream()
                .filter(employee -> !Boolean.TRUE.equals(employee.getIsDeleted()))
                .toList();
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Employee> findById(@PathVariable Integer id) {
        return repository.findById(id)
                .filter(employee -> !Boolean.TRUE.equals(employee.getIsDeleted()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            Authentication authentication) {
        Employee employee = repository.findById(id)
                .filter(candidate -> !Boolean.TRUE.equals(candidate.getIsDeleted()))
                .orElse(null);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        if (isProtectedEmployee(employee)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        employee.setIsDeleted(true);
        AuditSupport.apply(employee, authentication);
        repository.save(employee);
        return ResponseEntity.noContent().build();
    }

    @Override
    protected Employee prepareForCreate(Employee employee) {
        employee.setIsDeleted(false);
        encodePassword(employee);
        return employee;
    }

    @Override
    protected Employee prepareForUpdate(Integer id, Employee employee) {
        Employee existing = repository.findById(id)
                .filter(candidate -> !Boolean.TRUE.equals(candidate.getIsDeleted()))
                .orElseThrow(() -> new IllegalStateException("Employee not found: " + id));

        if (employee.getPassword() == null || employee.getPassword().isBlank()) {
            employee.setPassword(existing.getPassword());
        } else {
            encodePassword(employee);
        }
        employee.setEmployeeId(existing.getEmployeeId());
        employee.setUsername(existing.getUsername());
        employee.setIsDeleted(false);
        return employee;
    }

    private void encodePassword(Employee employee) {
        String password = employee.getPassword();
        if (password != null && !isBcryptHash(password)) {
            employee.setPassword(passwordEncoder.encode(password));
        }
    }

    private boolean isBcryptHash(String password) {
        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }

    private boolean isProtectedEmployee(Employee employee) {
        return Integer.valueOf(1).equals(employee.getId())
                && "anand".equalsIgnoreCase(employee.getUsername());
    }
}
