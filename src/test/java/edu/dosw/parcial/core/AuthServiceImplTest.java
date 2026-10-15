package edu.dosw.parcial.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.dosw.parcial.config.JwtUtil;
import edu.dosw.parcial.controller.dtos.request.LoginRequestDTO;
import edu.dosw.parcial.controller.dtos.response.LoginResponseDTO;
import edu.dosw.parcial.core.models.Rol;
import edu.dosw.parcial.core.services.AuthServiceImpl;
import edu.dosw.parcial.core.validators.CredencialesInvalidasException;
import edu.dosw.parcial.persistence.entities.UsuarioDocument;
import edu.dosw.parcial.persistence.repositories.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthServiceImpl authService;

    private final UsuarioDocument admin = new UsuarioDocument("admin@pawconnect.com", "hash", Rol.ADMIN);

    @Test
    void loginCorrectoDevuelveTokenYRol() {
        // Arrange
        when(usuarioRepository.findById("admin@pawconnect.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("clave", "hash")).thenReturn(true);
        when(jwtUtil.generarToken("admin@pawconnect.com", "ADMIN")).thenReturn("token-falso");
        when(jwtUtil.getExpirationSeconds()).thenReturn(3600L);

        // Act
        LoginResponseDTO respuesta = authService.login(new LoginRequestDTO("admin@pawconnect.com", "clave"));

        // Assert
        assertEquals("token-falso", respuesta.accessToken());
        assertEquals("Bearer", respuesta.tokenType());
        assertEquals(3600L, respuesta.expiresIn());
        assertEquals("ADMIN", respuesta.rol());
    }

    @Test
    void contrasenaIncorrectaLanzaCredencialesInvalidas() {
        // Arrange
        when(usuarioRepository.findById("admin@pawconnect.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);
        LoginRequestDTO request = new LoginRequestDTO("admin@pawconnect.com", "mala");

        // Act + Assert
        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
        verify(jwtUtil, never()).generarToken(anyString(), anyString());
    }

    @Test
    void correoInexistenteLanzaCredencialesInvalidas() {
        // Arrange
        when(usuarioRepository.findById("nadie@pawconnect.com")).thenReturn(Optional.empty());
        LoginRequestDTO request = new LoginRequestDTO("nadie@pawconnect.com", "clave");

        // Act + Assert
        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
        verify(jwtUtil, never()).generarToken(anyString(), anyString());
    }
}
