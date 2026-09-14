package com.saka.qms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemRequest(
        @NotBlank String nameOfItem,
        String note,
        String sequence,
        String code1,
        String code2,
        String code3,
        String code4,
        String code5,
        String cdinf1,
        String cdinf2,
        String cdinf3,
        String cdinf4,
        String cdinf5,
        @NotNull Integer departmentId) {
}
