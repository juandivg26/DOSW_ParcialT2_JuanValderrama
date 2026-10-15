package edu.dosw.parcial.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.dosw.parcial.config.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil("MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=", 3600000);

    @Test
    void tokenGeneradoSeValidaConSusDatos() {
        // Act
        Claims claims = jwtUtil.validar(jwtUtil.generarToken("admin@pawconnect.com", "ADMIN"));

        // Assert
        assertEquals("admin@pawconnect.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("rol", String.class));
        assertEquals(3600, jwtUtil.getExpirationSeconds());
    }

    @Test
    void tokenAlteradoEsRechazado() {
        // Arrange
        String alterado = jwtUtil.generarToken("admin@pawconnect.com", "ADMIN") + "x";

        // Act + Assert
        assertThrows(JwtException.class, () -> jwtUtil.validar(alterado));
    }
}
