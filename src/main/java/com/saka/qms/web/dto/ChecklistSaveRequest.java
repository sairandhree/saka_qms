package com.saka.qms.web.dto;

import jakarta.validation.Valid;

import java.util.List;

public record ChecklistSaveRequest(
        @Valid ItemRequest item,
        List<@Valid DetailRequest> details) {
    public ChecklistSaveRequest {
        details = details == null ? List.of() : List.copyOf(details);
    }
}
