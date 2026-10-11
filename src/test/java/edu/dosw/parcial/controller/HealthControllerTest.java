package edu.dosw.parcial.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

class HealthControllerTest {

    @Test
    void healthReturnsUp() {
        Map<String, String> body = new HealthController().health();
        assertEquals("UP", body.get("status"));
        assertEquals("DOSW-ParcialT2", body.get("service"));
    }
}
