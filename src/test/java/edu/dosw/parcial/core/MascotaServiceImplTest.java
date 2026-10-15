package edu.dosw.parcial.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.dosw.parcial.config.JwtFilter;
import edu.dosw.parcial.config.JwtUtil;
import edu.dosw.parcial.config.SecurityConfig;
import edu.dosw.parcial.controller.MascotaController;
import edu.dosw.parcial.controller.dtos.request.CompatibilidadRequestDTO;
import edu.dosw.parcial.controller.dtos.request.CrearMascotaRequestDTO;
import edu.dosw.parcial.controller.dtos.response.MascotaResponseDTO;
import edu.dosw.parcial.controller.handlers.GlobalExceptionHandler;
import edu.dosw.parcial.controller.mappers.MascotaMapper;
import edu.dosw.parcial.core.models.Especie;
import edu.dosw.parcial.core.models.EstadoMascota;
import edu.dosw.parcial.core.models.Tamano;
import edu.dosw.parcial.core.models.TipoNodo;
import edu.dosw.parcial.core.services.MascotaServiceImpl;
import edu.dosw.parcial.core.validators.ConflictoException;
import edu.dosw.parcial.core.validators.RecursoNoEncontradoException;
import edu.dosw.parcial.core.validators.ReglaNegocioException;
import edu.dosw.parcial.persistence.entities.Compatibilidad;
import edu.dosw.parcial.persistence.entities.MascotaDocument;
import edu.dosw.parcial.persistence.entities.NodoDocument;
import edu.dosw.parcial.persistence.repositories.MascotaRepository;
import edu.dosw.parcial.persistence.repositories.NodoRepository;

@ExtendWith(MockitoExtension.class)
class MascotaServiceImplTest {

    private static final CrearMascotaRequestDTO REQUEST = new CrearMascotaRequestDTO(
            "PERRO-006", "Maya", Especie.PERRO, 18, Tamano.MEDIANO, "ref-bog-norte",
            new CompatibilidadRequestDTO(true, true, false));
    private static final NodoDocument REFUGIO = new NodoDocument(
            "ref-bog-norte", "Refugio Norte", TipoNodo.REFUGIO, "ciu-bogota");
    private static final String JSON = """
            {"identificacion":"PERRO-006","nombre":"Maya","especie":"PERRO",
             "edadMeses":18,"tamano":"MEDIANO","refugioId":"ref-bog-norte",
             "compatibilidad":{"ninos":true,"otrasMascotas":true,"espaciosReducidos":false}}
            """;

    @Mock
    private NodoRepository nodoRepository;
    @Mock
    private MascotaRepository mascotaRepository;
    private MascotaServiceImpl mascotaService;

    @BeforeEach
    void configurar() {
        mascotaService = new MascotaServiceImpl(nodoRepository, mascotaRepository, new MascotaMapper());
    }

    @Test
    void crearMascotaGuardaUnaVezConEstadoDisponible() {
        // Arrange
        when(nodoRepository.findById(REQUEST.refugioId())).thenReturn(Optional.of(REFUGIO));
        when(mascotaRepository.existsByIdentificacion(REQUEST.identificacion())).thenReturn(false);
        when(mascotaRepository.save(any(MascotaDocument.class))).thenAnswer(invocacion -> {
            MascotaDocument mascota = invocacion.getArgument(0);
            mascota.setId("masc-013");
            return mascota;
        });

        // Act
        MascotaResponseDTO respuesta = mascotaService.crearMascota(REQUEST);

        // Assert
        ArgumentCaptor<MascotaDocument> captor = ArgumentCaptor.forClass(MascotaDocument.class);
        InOrder orden = inOrder(nodoRepository, mascotaRepository);
        orden.verify(nodoRepository).findById(REQUEST.refugioId());
        orden.verify(mascotaRepository).existsByIdentificacion(REQUEST.identificacion());
        orden.verify(mascotaRepository).save(captor.capture());
        verifyNoMoreInteractions(nodoRepository, mascotaRepository);
        assertEquals(EstadoMascota.DISPONIBLE, captor.getValue().getEstado());
        assertEquals(new MascotaResponseDTO("masc-013", "PERRO-006", "Maya", Especie.PERRO,
                18, Tamano.MEDIANO, "ref-bog-norte", "Refugio Norte", EstadoMascota.DISPONIBLE,
                new Compatibilidad(true, true, false)), respuesta);
    }

    @Test
    void refugioInexistenteLanzaRecursoNoEncontradoSinGuardar() {
        // Arrange
        when(nodoRepository.findById(REQUEST.refugioId())).thenReturn(Optional.empty());

        // Act
        RecursoNoEncontradoException error = assertThrows(RecursoNoEncontradoException.class,
                () -> mascotaService.crearMascota(REQUEST));

        // Assert
        assertEquals("REFUGIO_NO_ENCONTRADO", error.getCode());
        verify(nodoRepository).findById(REQUEST.refugioId());
        verifyNoInteractions(mascotaRepository);
    }

    @Test
    void nodoCiudadLanzaE03SinGuardar() {
        // Arrange
        NodoDocument ciudad = new NodoDocument(REQUEST.refugioId(), "Bogotá", TipoNodo.CIUDAD, "red");
        when(nodoRepository.findById(REQUEST.refugioId())).thenReturn(Optional.of(ciudad));

        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> mascotaService.crearMascota(REQUEST));

        // Assert
        assertEquals("E03", error.getCode());
        verify(nodoRepository).findById(REQUEST.refugioId());
        verifyNoInteractions(mascotaRepository);
    }

    @Test
    void identificacionDuplicadaLanzaConflictoSinGuardar() {
        // Arrange
        when(nodoRepository.findById(REQUEST.refugioId())).thenReturn(Optional.of(REFUGIO));
        when(mascotaRepository.existsByIdentificacion(REQUEST.identificacion())).thenReturn(true);

        // Act
        ConflictoException error = assertThrows(ConflictoException.class,
                () -> mascotaService.crearMascota(REQUEST));

        // Assert
        assertEquals("MASCOTA_DUPLICADA", error.getCode());
        InOrder orden = inOrder(nodoRepository, mascotaRepository);
        orden.verify(nodoRepository).findById(REQUEST.refugioId());
        orden.verify(mascotaRepository).existsByIdentificacion(REQUEST.identificacion());
        verify(mascotaRepository, never()).save(any(MascotaDocument.class));
        verifyNoMoreInteractions(nodoRepository, mascotaRepository);
    }

    @Test
    void duplicadoDetectadoAlGuardarSeConvierteEnConflicto() {
        // Arrange
        when(nodoRepository.findById(REQUEST.refugioId())).thenReturn(Optional.of(REFUGIO));
        when(mascotaRepository.existsByIdentificacion(REQUEST.identificacion())).thenReturn(false);
        when(mascotaRepository.save(any(MascotaDocument.class)))
                .thenThrow(new DuplicateKeyException("Índice único de identificación"));

        // Act
        ConflictoException error = assertThrows(ConflictoException.class,
                () -> mascotaService.crearMascota(REQUEST));

        // Assert
        assertEquals("MASCOTA_DUPLICADA", error.getCode());
        InOrder orden = inOrder(nodoRepository, mascotaRepository);
        orden.verify(nodoRepository).findById(REQUEST.refugioId());
        orden.verify(mascotaRepository).existsByIdentificacion(REQUEST.identificacion());
        orden.verify(mascotaRepository).save(any(MascotaDocument.class));
        verifyNoMoreInteractions(nodoRepository, mascotaRepository);
    }

    @Nested
    @WebMvcTest(MascotaController.class)
    @ContextConfiguration(classes = {MascotaController.class, MascotaServiceImpl.class, MascotaMapper.class,
            GlobalExceptionHandler.class, SecurityConfig.class, JwtFilter.class})
    class RespuestasHttp {

        @Autowired
        private MockMvc mockMvc;
        @MockitoBean
        private NodoRepository nodoRepository;
        @MockitoBean
        private MascotaRepository mascotaRepository;
        @MockitoBean
        private JwtUtil jwtUtil;

        @Test
        void adminCreaMascotaCon201YCamposDelContrato() throws Exception {
            // Arrange
            when(nodoRepository.findById(REQUEST.refugioId())).thenReturn(Optional.of(REFUGIO));
            when(mascotaRepository.save(any(MascotaDocument.class))).thenAnswer(invocacion -> {
                MascotaDocument mascota = invocacion.getArgument(0);
                mascota.setId("masc-013");
                return mascota;
            });

            // Act + Assert
            mockMvc.perform(post("/api/v1/mascotas").with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON).content(JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.length()").value(10))
                    .andExpect(jsonPath("$.id").value("masc-013"))
                    .andExpect(jsonPath("$.identificacion").value("PERRO-006"))
                    .andExpect(jsonPath("$.nombre").value("Maya"))
                    .andExpect(jsonPath("$.especie").value("PERRO"))
                    .andExpect(jsonPath("$.edadMeses").value(18))
                    .andExpect(jsonPath("$.tamano").value("MEDIANO"))
                    .andExpect(jsonPath("$.refugioId").value("ref-bog-norte"))
                    .andExpect(jsonPath("$.refugioNombre").value("Refugio Norte"))
                    .andExpect(jsonPath("$.estado").value("DISPONIBLE"))
                    .andExpect(jsonPath("$.compatibilidad.ninos").value(true))
                    .andExpect(jsonPath("$.compatibilidad.otrasMascotas").value(true))
                    .andExpect(jsonPath("$.compatibilidad.espaciosReducidos").value(false));
            verify(mascotaRepository).save(any(MascotaDocument.class));
        }

        @Test
        void datosInvalidosDevuelven400E02SinConsultarRepositorios() throws Exception {
            // Arrange
            String[] invalidos = {
                    JSON.replace("\"PERRO-006\"", "\" \""),
                    JSON.replace("\"Maya\"", "\"\""),
                    JSON.replace("\"PERRO\"", "null"),
                    JSON.replace("\"PERRO\"", "\"OTRA\""),
                    JSON.replace("\"edadMeses\":18", "\"edadMeses\":null"),
                    JSON.replace("\"edadMeses\":18", "\"edadMeses\":-1"),
                    JSON.replace("\"edadMeses\":18", "\"edadMeses\":361"),
                    JSON.replace("\"MEDIANO\"", "null"),
                    JSON.replace("\"MEDIANO\"", "\"OTRO\""),
                    JSON.replace("\"ref-bog-norte\"", "\"\""),
                    JSON.replace("{\"ninos\":true,\"otrasMascotas\":true,\"espaciosReducidos\":false}", "null"),
                    JSON.replace("\"ninos\":true", "\"ninos\":null"),
                    JSON.replace("\"otrasMascotas\":true", "\"otrasMascotas\":null"),
                    JSON.replace("\"espaciosReducidos\":false", "\"espaciosReducidos\":null"),
                    JSON.replace("\"ninos\":true,", ""),
                    JSON.replace("\"otrasMascotas\":true,", ""),
                    JSON.replace(",\"espaciosReducidos\":false", ""),
                    "{}", "{", ""
            };

            // Act + Assert
            for (String contenido : invalidos) {
                mockMvc.perform(post("/api/v1/mascotas").servletPath("/api/v1/mascotas")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(contenido))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("E02"));
            }
            verifyNoInteractions(nodoRepository, mascotaRepository);
        }

        @Test
        void sinAutenticacionDevuelve401() throws Exception {
            // Act + Assert
            mockMvc.perform(post("/api/v1/mascotas").contentType(MediaType.APPLICATION_JSON).content(JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("NO_AUTENTICADO"));
            verifyNoInteractions(nodoRepository, mascotaRepository);
        }

        @Test
        void adoptanteRecibe403() throws Exception {
            // Act + Assert
            mockMvc.perform(post("/api/v1/mascotas").with(user("adoptante").roles("ADOPTANTE"))
                    .contentType(MediaType.APPLICATION_JSON).content(JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
            verifyNoInteractions(nodoRepository, mascotaRepository);
        }

        @Test
        void refugioInexistenteDevuelve404() throws Exception {
            // Act + Assert
            mockMvc.perform(post("/api/v1/mascotas").with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON).content(JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("REFUGIO_NO_ENCONTRADO"));
            verifyNoInteractions(mascotaRepository);
        }

        @Test
        void nodoCiudadDevuelve422E03() throws Exception {
            // Arrange
            when(nodoRepository.findById(REQUEST.refugioId())).thenReturn(Optional.of(
                    new NodoDocument(REQUEST.refugioId(), "Bogotá", TipoNodo.CIUDAD, "red")));

            // Act + Assert
            mockMvc.perform(post("/api/v1/mascotas").with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON).content(JSON))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("E03"));
            verifyNoInteractions(mascotaRepository);
        }

        @Test
        void identificacionDuplicadaDevuelve409() throws Exception {
            // Arrange
            when(nodoRepository.findById(REQUEST.refugioId())).thenReturn(Optional.of(REFUGIO));
            when(mascotaRepository.existsByIdentificacion(REQUEST.identificacion())).thenReturn(true);

            // Act + Assert
            mockMvc.perform(post("/api/v1/mascotas").with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON).content(JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("MASCOTA_DUPLICADA"));
            verify(mascotaRepository, never()).save(any(MascotaDocument.class));
        }

        @Test
        void indiceUnicoDuplicadoDevuelve409() throws Exception {
            // Arrange
            when(nodoRepository.findById(REQUEST.refugioId())).thenReturn(Optional.of(REFUGIO));
            when(mascotaRepository.save(any(MascotaDocument.class)))
                    .thenThrow(new DuplicateKeyException("Índice único de identificación"));

            // Act + Assert
            mockMvc.perform(post("/api/v1/mascotas").with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON).content(JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("MASCOTA_DUPLICADA"));
            verify(mascotaRepository).save(any(MascotaDocument.class));
        }
    }
}
