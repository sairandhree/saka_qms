package com.saka.qms.web.dto;

public record DetailRequest(
        Integer id,
        String details,
        String note,
        String parameters,
        String link,
        Integer sequence,
        String passward) {
}
