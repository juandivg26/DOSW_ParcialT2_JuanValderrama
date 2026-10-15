package edu.dosw.parcial.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.dosw.parcial.controller.dtos.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String ADOPTANTE = "ADOPTANTE";

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/health", "/error",
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/mascotas").hasRole(ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/v1/adopciones").hasAnyRole(ADMIN, ADOPTANTE)
                .requestMatchers(HttpMethod.POST, "/api/v1/mascotas/*/adopciones").hasRole(ADOPTANTE)
                .requestMatchers(HttpMethod.PATCH, "/api/v1/adopciones/*/recibimiento").hasRole(ADOPTANTE)
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> escribirError(res, 401,
                        new ErrorResponseDTO("NO_AUTENTICADO", "Token de autenticación no proporcionado o expirado.")))
                .accessDeniedHandler((req, res, ex) -> escribirError(res, 403,
                        new ErrorResponseDTO("ACCESO_DENEGADO", "No tiene permisos para esta operación."))))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void escribirError(HttpServletResponse res, int status, ErrorResponseDTO error) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}
