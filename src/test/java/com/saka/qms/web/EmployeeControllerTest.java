package com.saka.qms.web;

import com.saka.qms.config.AccessControlService;
import com.saka.qms.model.Department;
import com.saka.qms.model.Employee;
import com.saka.qms.repository.DepartmentRepository;
import com.saka.qms.repository.EmployeeRepository;
import com.saka.qms.repository.NameOfItemRepository;
import com.saka.qms.web.dto.EmployeeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeControllerTest {
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AccessControlService accessControl =
            new AccessControlService(mock(NameOfItemRepository.class));
    private final EmployeeController controller = new EmployeeController(
            employeeRepository, departmentRepository, passwordEncoder, accessControl);
    private final Authentication admin = new UsernamePasswordAuthenticationToken(
            "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @Test
    void createPersistsSelectedDepartments() {
        Department department = department(7, "Quality");
        when(departmentRepository.findAllById(Set.of(7))).thenReturn(List.of(department));
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeRequest request = new EmployeeRequest(
                22, "Jane Doe", "jane", "secret", false, true, Set.of(7));

        Employee saved = controller.create(request, admin).getBody();

        assertEquals(Set.of(7), saved.getDepartments().stream()
                .map(Department::getId)
                .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void updateReplacesSelectedDepartments() {
        Department oldDepartment = department(3, "Engineering");
        Department newDepartment = department(7, "Quality");
        Employee employee = new Employee();
        employee.setId(12);
        employee.setUsername("jane");
        employee.setDepartments(Set.of(oldDepartment));

        when(employeeRepository.findById(12)).thenReturn(Optional.of(employee));
        when(departmentRepository.findAllById(Set.of(7))).thenReturn(List.of(newDepartment));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeRequest request = new EmployeeRequest(
                null, "Jane Updated", "ignored", null, false, true, Set.of(7));

        Employee saved = controller.update(12, request, admin).getBody();

        assertTrue(saved.getDepartments().stream()
                .anyMatch(department -> Integer.valueOf(7).equals(department.getId())));
        assertEquals(1, saved.getDepartments().size());
    }

    private Department department(Integer id, String name) {
        Department department = new Department();
        department.setId(id);
        department.setDeptName(name);
        return department;
    }
}
