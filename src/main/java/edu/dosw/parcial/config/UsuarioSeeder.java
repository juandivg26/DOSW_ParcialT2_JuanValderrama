package edu.dosw.parcial.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import edu.dosw.parcial.core.models.Rol;
import edu.dosw.parcial.persistence.entities.UsuarioDocument;
import edu.dosw.parcial.persistence.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UsuarioSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String passwordInicial;

    public UsuarioSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                         @Value("${seed.password}") String passwordInicial) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordInicial = passwordInicial;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        String hash = passwordEncoder.encode(passwordInicial);
        usuarioRepository.saveAll(List.of(
                new UsuarioDocument("admin@pawconnect.com", hash, Rol.ADMIN),
                new UsuarioDocument("adoptante1@pawconnect.com", hash, Rol.ADOPTANTE),
                new UsuarioDocument("adoptante2@pawconnect.com", hash, Rol.ADOPTANTE)));
        log.info("Usuarios iniciales creados");
    }
}
