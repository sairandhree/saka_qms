package com.saka.qms.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetailSerializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void passwordIsAcceptedForInputButExcludedFromResponses() throws Exception {
        Detail detail = objectMapper.readValue(
                "{\"details\":\"check\",\"passward\":\"secret\"}",
                Detail.class);

        assertTrue("secret".equals(detail.getPassward()));
        assertFalse(objectMapper.writeValueAsString(detail).contains("secret"));
        assertFalse(objectMapper.writeValueAsString(detail).contains("passward"));
    }
}
