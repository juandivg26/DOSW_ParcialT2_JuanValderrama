package edu.dosw.parcial.core.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.dosw.parcial.config.JwtUtil;
import edu.dosw.parcial.controller.dtos.request.LoginRequestDTO;
import edu.dosw.parcial.controller.dtos.response.LoginResponseDTO;
import edu.dosw.parcial.core.validators.CredencialesInvalidasException;
import edu.dosw.parcial.persistence.entities.UsuarioDocument;
import edu.dosw.parcial.persistence.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        UsuarioDocument usuario = usuarioRepository.findById(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> {
                    log.warn("Login fallido para {}", request.email());
                    return new CredencialesInvalidasException();
                });

        String rol = usuario.getRol().name();
        String token = jwtUtil.generarToken(usuario.getId(), rol);
        log.info("Login exitoso: {}", usuario.getId());
        return new LoginResponseDTO(token, "Bearer", jwtUtil.getExpirationSeconds(), rol);
    }
}
