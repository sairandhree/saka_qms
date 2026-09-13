package com.saka.qms.web;

import com.saka.qms.model.Employee;
import com.saka.qms.config.JwtService;
import com.saka.qms.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(EmployeeRepository employeeRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.username() == null || request.password() == null) {
            return unauthorized();
        }

        Optional<Employee> authenticatedEmployee = employeeRepository.findByUsername(request.username())
                .filter(employee -> employee.getPassword() != null
                        && passwordEncoder.matches(request.password(), employee.getPassword()));
        if (authenticatedEmployee.isEmpty()) {
            return unauthorized();
        }
        return ResponseEntity.ok(createLoginResponse(authenticatedEmployee.get()));
    }

    private ResponseEntity<LoginError> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginError("Invalid username or password"));
    }

    public record LoginError(String message) {
    }

    private LoginResponse createLoginResponse(Employee employee) {
        return new LoginResponse(
                jwtService.createToken(employee),
                employee.getId(),
                employee.getUsername(),
                employee.getEmployeeName(),
                employee.getIsAdmin());
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(
            String token,
            Integer employeeId,
            String username,
            String employeeName,
            Boolean isAdmin) {
    }
}
