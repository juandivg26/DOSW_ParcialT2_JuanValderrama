package edu.dosw.parcial.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import edu.dosw.parcial.controller.dtos.response.AdopcionResponseDTO;
import edu.dosw.parcial.controller.dtos.response.ConsultarAdopcionesResponseDTO;
import edu.dosw.parcial.core.models.Especie;
import edu.dosw.parcial.core.models.EstadoMascota;
import edu.dosw.parcial.core.models.EstadoSolicitud;
import edu.dosw.parcial.core.services.AdopcionServiceImpl;
import edu.dosw.parcial.core.validators.RecursoNoEncontradoException;
import edu.dosw.parcial.persistence.entities.MascotaDocument;
import edu.dosw.parcial.persistence.entities.SolicitudAdopcionDocument;
import edu.dosw.parcial.persistence.repositories.MascotaRepository;
import edu.dosw.parcial.persistence.repositories.SolicitudRepository;

@ExtendWith(MockitoExtension.class)
class AdopcionServiceImplTest {

    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private MascotaRepository mascotaRepository;
    private AdopcionServiceImpl adopcionService;

    @BeforeEach
    void configurar() {
        adopcionService = new AdopcionServiceImpl(solicitudRepository, mascotaRepository);
    }

    @Test
    void devuelveVariasAprobadasConDatosDeMascotasYDistintosAdoptantes() {
        SolicitudAdopcionDocument reciente = solicitud("sol-prueba-2", "masc-prueba-2", "otro@example.com",
                LocalDateTime.of(2026, 10, 25, 9, 0));
        SolicitudAdopcionDocument anterior = solicitud("sol-prueba-1", "masc-prueba-1", "usuario@example.com",
                LocalDateTime.of(2026, 10, 10, 10, 0));
        MascotaDocument gato = mascota("masc-prueba-2", "GATO-PRUEBA", "Luna", Especie.GATO,
                EstadoMascota.EN_PROCESO);
        MascotaDocument perro = mascota("masc-prueba-1", "PERRO-PRUEBA", "Roco", Especie.PERRO,
                EstadoMascota.DISPONIBLE);
        when(solicitudRepository.findByEstado(eq(EstadoSolicitud.APROBADA), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(reciente, anterior)));
        when(mascotaRepository.findById(gato.getId())).thenReturn(Optional.of(gato));
        when(mascotaRepository.findById(perro.getId())).thenReturn(Optional.of(perro));

        ConsultarAdopcionesResponseDTO respuesta = adopcionService.consultarAdoptadas(0, 20);

        assertEquals(2, respuesta.total());
        assertNull(respuesta.mensaje());
        assertEquals(List.of(
                new AdopcionResponseDTO(reciente.getId(), gato.getId(), "GATO-PRUEBA", "Luna", Especie.GATO,
                        "otro@example.com", EstadoSolicitud.APROBADA,
                        reciente.getFechaSolicitud(), reciente.getFechaRecibimiento()),
                new AdopcionResponseDTO(anterior.getId(), perro.getId(), "PERRO-PRUEBA", "Roco", Especie.PERRO,
                        "usuario@example.com", EstadoSolicitud.APROBADA,
                        anterior.getFechaSolicitud(), anterior.getFechaRecibimiento())), respuesta.adopciones());
        verify(mascotaRepository).findById(gato.getId());
        verify(mascotaRepository).findById(perro.getId());
        verifyNoMoreInteractions(mascotaRepository);
    }

    @Test
    void consultaSoloAprobadasConPaginaTamanoYFechaRecibimientoDescendente() {
        when(solicitudRepository.findByEstado(eq(EstadoSolicitud.APROBADA), any(Pageable.class)))
                .thenReturn(Page.empty());

        adopcionService.consultarAdoptadas(2, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(solicitudRepository).findByEstado(eq(EstadoSolicitud.APROBADA), captor.capture());
        assertEquals(PageRequest.of(2, 5, Sort.Direction.DESC, "fechaRecibimiento"), captor.getValue());
        verifyNoMoreInteractions(solicitudRepository);
        verifyNoInteractions(mascotaRepository);
    }

    @Test
    void consultaVaciaDevuelveTotalCeroListaVaciaYMensaje() {
        when(solicitudRepository.findByEstado(eq(EstadoSolicitud.APROBADA), any(Pageable.class)))
                .thenReturn(Page.empty());

        ConsultarAdopcionesResponseDTO respuesta = adopcionService.consultarAdoptadas(0, 20);

        assertEquals(0, respuesta.total());
        assertTrue(respuesta.adopciones().isEmpty());
        assertEquals("No existen solicitudes APROBADAS", respuesta.mensaje());
        verifyNoInteractions(mascotaRepository);
    }

    @Test
    void totalProvieneDePageYNoDelTamanoDeLaPaginaActual() {
        SolicitudAdopcionDocument solicitud = solicitud("sol-prueba", "masc-prueba", "usuario@example.com",
                LocalDateTime.of(2026, 10, 20, 10, 0));
        when(solicitudRepository.findByEstado(eq(EstadoSolicitud.APROBADA), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(solicitud), PageRequest.of(1, 1), 7));
        when(mascotaRepository.findById("masc-prueba")).thenReturn(Optional.of(
                mascota("masc-prueba", "PERRO-PRUEBA", "Roco", Especie.PERRO, EstadoMascota.EN_PROCESO)));

        ConsultarAdopcionesResponseDTO respuesta = adopcionService.consultarAdoptadas(1, 1);

        assertEquals(7, respuesta.total());
        assertEquals(1, respuesta.adopciones().size());
        assertNull(respuesta.mensaje());
    }

    @Test
    void paginaFueraDeRangoConservaElTotalGlobal() {
        when(solicitudRepository.findByEstado(eq(EstadoSolicitud.APROBADA), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 20), 2));

        ConsultarAdopcionesResponseDTO respuesta = adopcionService.consultarAdoptadas(2, 20);

        assertEquals(2, respuesta.total());
        assertTrue(respuesta.adopciones().isEmpty());
        assertEquals("No existen solicitudes APROBADAS", respuesta.mensaje());
        verifyNoInteractions(mascotaRepository);
    }

    @Test
    void mascotaInexistenteLanzaRecursoNoEncontradoSinInventarDatos() {
        when(solicitudRepository.findByEstado(eq(EstadoSolicitud.APROBADA), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(solicitud("sol-prueba", "masc-ausente",
                        "usuario@example.com", LocalDateTime.of(2026, 10, 20, 10, 0)))));
        when(mascotaRepository.findById("masc-ausente")).thenReturn(Optional.empty());

        RecursoNoEncontradoException error = assertThrows(RecursoNoEncontradoException.class,
                () -> adopcionService.consultarAdoptadas(0, 20));

        assertEquals("MASCOTA_NO_ENCONTRADA", error.getCode());
        verify(mascotaRepository).findById("masc-ausente");
        verifyNoMoreInteractions(mascotaRepository);
    }

    private SolicitudAdopcionDocument solicitud(String id, String mascotaId, String email,
            LocalDateTime recibimiento) {
        return new SolicitudAdopcionDocument(id, mascotaId, email, EstadoSolicitud.APROBADA,
                LocalDateTime.of(2026, 10, 1, 8, 0), recibimiento, 0, null);
    }

    private MascotaDocument mascota(String id, String identificacion, String nombre, Especie especie,
            EstadoMascota estado) {
        MascotaDocument mascota = new MascotaDocument();
        mascota.setId(id);
        mascota.setIdentificacion(identificacion);
        mascota.setNombre(nombre);
        mascota.setEspecie(especie);
        mascota.setEstado(estado);
        return mascota;
    }
}
