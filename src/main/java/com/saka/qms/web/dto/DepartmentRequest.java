package com.saka.qms.web.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(@NotBlank String deptName) {
}
