package edu.dosw.parcial.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.dosw.parcial.controller.dtos.request.LoginRequestDTO;
import edu.dosw.parcial.controller.handlers.GlobalExceptionHandler;
import edu.dosw.parcial.core.validators.ConflictoException;
import edu.dosw.parcial.core.validators.RecursoNoEncontradoException;
import edu.dosw.parcial.core.validators.ReglaNegocioException;
import jakarta.validation.Valid;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorPrueba())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void conservaCodigoYMensajeDeExcepcionesDeNegocio() throws Exception {
        mockMvc.perform(get("/prueba/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REFUGIO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.message").value("El refugio no existe."));
        mockMvc.perform(get("/prueba/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDENTIFICACION_DUPLICADA"));
        mockMvc.perform(get("/prueba/regla"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("TIPO_NODO_INVALIDO"));
    }

    @Test
    void rechazaParametrosNoNumericosYObligatoriosAusentes() throws Exception {
        mockMvc.perform(get("/prueba/parametros").param("page", "texto"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARAMETROS_INVALIDOS"));
        mockMvc.perform(get("/prueba/parametros"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARAMETROS_INVALIDOS"));
    }

    @Test
    void devuelveErrorResponseParaDtoInvalido() throws Exception {
        mockMvc.perform(post("/prueba/dto").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"correo-invalido\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void conservaE02ParaValidacionDtoAlCrearMascota() throws Exception {
        mockMvc.perform(post("/api/v1/mascotas").servletPath("/api/v1/mascotas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"correo-invalido\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("E02"));
    }

    @Test
    void conservaE02ParaJsonInvalidoAlCrearMascota() throws Exception {
        mockMvc.perform(post("/api/v1/mascotas").servletPath("/api/v1/mascotas")
                .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("E02"));
    }

    @RestController
    static class ControladorPrueba {
        @GetMapping("/prueba/no-encontrado")
        public void noEncontrado() {
            throw new RecursoNoEncontradoException("REFUGIO_NO_ENCONTRADO", "El refugio no existe.");
        }

        @GetMapping("/prueba/conflicto")
        public void conflicto() {
            throw new ConflictoException("IDENTIFICACION_DUPLICADA", "La identificación ya existe.");
        }

        @GetMapping("/prueba/regla")
        public void regla() {
            throw new ReglaNegocioException("TIPO_NODO_INVALIDO", "El nodo debe ser un refugio.");
        }

        @GetMapping("/prueba/parametros")
        public void parametros(@RequestParam Integer page) {
        }

        @PostMapping({"/prueba/dto", "/api/v1/mascotas"})
        public void dto(@Valid @RequestBody LoginRequestDTO request) {
        }
    }
}
