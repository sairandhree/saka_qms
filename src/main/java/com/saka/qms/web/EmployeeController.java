package com.saka.qms.web;

import com.saka.qms.model.Employee;
import com.saka.qms.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    protected Employee prepareForCreate(Employee employee) {
        encodePassword(employee);
        return employee;
    }

    @Override
    protected Employee prepareForUpdate(Integer id, Employee employee) {
        Employee existing = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Employee not found: " + id));

        if (employee.getPassword() == null || employee.getPassword().isBlank()) {
            employee.setPassword(existing.getPassword());
        } else {
            encodePassword(employee);
        }
        employee.setEmployeeId(existing.getEmployeeId());
        employee.setUsername(existing.getUsername());
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
}
