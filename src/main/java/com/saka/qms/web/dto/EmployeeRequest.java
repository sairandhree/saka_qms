package com.saka.qms.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record EmployeeRequest(
        Integer employeeId,
        @NotBlank String employeeName,
        @NotBlank String username,
        String password,
        Boolean isAdmin,
        Boolean isDepartmentHead,
        Set<Integer> departmentIds) {
}
