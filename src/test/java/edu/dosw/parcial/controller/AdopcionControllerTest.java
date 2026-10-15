package edu.dosw.parcial.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.dosw.parcial.config.JwtFilter;
import edu.dosw.parcial.config.JwtUtil;
import edu.dosw.parcial.config.SecurityConfig;
import edu.dosw.parcial.controller.dtos.response.AdopcionResponseDTO;
import edu.dosw.parcial.controller.dtos.response.ConsultarAdopcionesResponseDTO;
import edu.dosw.parcial.controller.handlers.GlobalExceptionHandler;
import edu.dosw.parcial.core.models.Especie;
import edu.dosw.parcial.core.models.EstadoSolicitud;
import edu.dosw.parcial.core.services.IAdopcionService;
import edu.dosw.parcial.core.validators.RecursoNoEncontradoException;
import io.jsonwebtoken.JwtException;

@WebMvcTest(AdopcionController.class)
@ContextConfiguration(classes = {AdopcionController.class, GlobalExceptionHandler.class,
        SecurityConfig.class, JwtFilter.class})
class AdopcionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private IAdopcionService adopcionService;
    @MockitoBean
    private JwtUtil jwtUtil;

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "ADOPTANTE"})
    void ambosRolesObtienen200ConDatosDelContratoYValoresPredeterminados(String rol) throws Exception {
        AdopcionResponseDTO adopcion = new AdopcionResponseDTO(
                "sol-prueba", "masc-prueba", "PERRO-PRUEBA", "Roco", Especie.PERRO,
                "otro@example.com", EstadoSolicitud.APROBADA,
                LocalDateTime.of(2026, 10, 1, 8, 0), LocalDateTime.of(2026, 10, 20, 10, 0));
        when(adopcionService.consultarAdoptadas(0, 20))
                .thenReturn(new ConsultarAdopcionesResponseDTO(1, null, List.of(adopcion)));

        mockMvc.perform(get("/api/v1/adopciones").with(user("usuario@example.com").roles(rol)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.mensaje").doesNotExist())
                .andExpect(jsonPath("$.adopciones.length()").value(1))
                .andExpect(jsonPath("$.adopciones[0].length()").value(9))
                .andExpect(jsonPath("$.adopciones[0].solicitudId").value("sol-prueba"))
                .andExpect(jsonPath("$.adopciones[0].mascotaId").value("masc-prueba"))
                .andExpect(jsonPath("$.adopciones[0].identificacionMascota").value("PERRO-PRUEBA"))
                .andExpect(jsonPath("$.adopciones[0].nombreMascota").value("Roco"))
                .andExpect(jsonPath("$.adopciones[0].especie").value("PERRO"))
                .andExpect(jsonPath("$.adopciones[0].adoptanteEmail").value("otro@example.com"))
                .andExpect(jsonPath("$.adopciones[0].estadoSolicitud").value("APROBADA"))
                .andExpect(jsonPath("$.adopciones[0].fechaSolicitud").value("2026-10-01T08:00:00"))
                .andExpect(jsonPath("$.adopciones[0].fechaRecibimiento").value("2026-10-20T10:00:00"));
        verify(adopcionService).consultarAdoptadas(0, 20);
    }

    @Test
    void consultaVaciaDevuelve200ConMensajeYListaVacia() throws Exception {
        when(adopcionService.consultarAdoptadas(0, 20)).thenReturn(
                new ConsultarAdopcionesResponseDTO(0, "No existen solicitudes APROBADAS", List.of()));

        mockMvc.perform(get("/api/v1/adopciones").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.mensaje").value("No existen solicitudes APROBADAS"))
                .andExpect(jsonPath("$.adopciones").isEmpty());
    }

    @Test
    void paginaNegativaDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/adopciones").param("page", "-1")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.code").value("PARAMETROS_INVALIDOS"))
                .andExpect(jsonPath("$.message").isString());
        verifyNoInteractions(adopcionService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "101"})
    void tamanoFueraDeLimitesDevuelve400(String size) throws Exception {
        mockMvc.perform(get("/api/v1/adopciones").param("size", size)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARAMETROS_INVALIDOS"))
                .andExpect(jsonPath("$.message").isString());
        verifyNoInteractions(adopcionService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"page", "size"})
    void parametroNoNumericoDevuelve400(String parametro) throws Exception {
        mockMvc.perform(get("/api/v1/adopciones").param(parametro, "texto")
                .with(user("adoptante").roles("ADOPTANTE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARAMETROS_INVALIDOS"));
        verifyNoInteractions(adopcionService);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100})
    void aceptaLimitesDeTamanoYPropagaLaPaginacion(int size) throws Exception {
        when(adopcionService.consultarAdoptadas(1, size)).thenReturn(
                new ConsultarAdopcionesResponseDTO(0, "No existen solicitudes APROBADAS", List.of()));

        mockMvc.perform(get("/api/v1/adopciones").param("page", "1").param("size", String.valueOf(size))
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        verify(adopcionService).consultarAdoptadas(1, size);
    }

    @Test
    void sinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/adopciones"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"));
        verifyNoInteractions(adopcionService);
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        when(jwtUtil.validar("token-prueba-invalido")).thenThrow(new JwtException("Token inválido"));

        mockMvc.perform(get("/api/v1/adopciones").header("Authorization", "Bearer token-prueba-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"));
        verifyNoInteractions(adopcionService);
    }

    @Test
    void usuarioSinPermisosDevuelve403() throws Exception {
        mockMvc.perform(get("/api/v1/adopciones").with(user("visitante").roles("VISITANTE")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
        verifyNoInteractions(adopcionService);
    }

    @Test
    void mascotaReferenciadaInexistenteDevuelve404ConFormatoExistente() throws Exception {
        when(adopcionService.consultarAdoptadas(0, 20)).thenThrow(new RecursoNoEncontradoException(
                "MASCOTA_NO_ENCONTRADA", "La mascota referenciada por una solicitud aprobada no existe."));

        mockMvc.perform(get("/api/v1/adopciones").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MASCOTA_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.message")
                        .value("La mascota referenciada por una solicitud aprobada no existe."));
    }
}
