package com.saka.qms.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");

    @Test
    void illegalStateDoesNotExposeDatabaseDetails() {
        GlobalExceptionHandler.ApiError error = handler
                .handleIllegalState(new IllegalStateException("Department was not found."), request)
                .getBody();

        assertEquals(400, error.status());
        assertEquals("The request could not be completed.", error.message());
        assertFalse(error.message().contains("select"));
    }

    @Test
    void unexpectedExceptionUsesSafeMessage() {
        GlobalExceptionHandler.ApiError error = handler
                .handleUnexpected(new RuntimeException("password=secret"), request)
                .getBody();

        assertEquals(500, error.status());
        assertEquals("An unexpected error occurred.", error.message());
        assertFalse(error.message().contains("secret"));
    }
}
