package edu.dosw.parcial.core.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import edu.dosw.parcial.controller.dtos.response.AdopcionResponseDTO;
import edu.dosw.parcial.controller.dtos.response.ConsultarAdopcionesResponseDTO;
import edu.dosw.parcial.core.models.EstadoSolicitud;
import edu.dosw.parcial.core.validators.RecursoNoEncontradoException;
import edu.dosw.parcial.persistence.entities.MascotaDocument;
import edu.dosw.parcial.persistence.entities.SolicitudAdopcionDocument;
import edu.dosw.parcial.persistence.repositories.MascotaRepository;
import edu.dosw.parcial.persistence.repositories.SolicitudRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdopcionServiceImpl implements IAdopcionService {

    private final SolicitudRepository solicitudRepository;
    private final MascotaRepository mascotaRepository;

    public AdopcionServiceImpl(SolicitudRepository solicitudRepository, MascotaRepository mascotaRepository) {
        this.solicitudRepository = solicitudRepository;
        this.mascotaRepository = mascotaRepository;
    }

    @Override
    public ConsultarAdopcionesResponseDTO consultarAdoptadas(int page, int size) {
        PageRequest paginacion = PageRequest.of(page, size, Sort.Direction.DESC, "fechaRecibimiento");
        Page<SolicitudAdopcionDocument> solicitudes = solicitudRepository.findByEstado(
                EstadoSolicitud.APROBADA, paginacion);

        List<AdopcionResponseDTO> adopciones = new ArrayList<>();
        for (SolicitudAdopcionDocument solicitud : solicitudes) {
            MascotaDocument mascota = mascotaRepository.findById(solicitud.getMascotaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "MASCOTA_NO_ENCONTRADA", "La mascota referenciada por una solicitud aprobada no existe."));
            adopciones.add(new AdopcionResponseDTO(
                    solicitud.getId(), solicitud.getMascotaId(), mascota.getIdentificacion(),
                    mascota.getNombre(), mascota.getEspecie(), solicitud.getAdoptanteId(),
                    solicitud.getEstado(), solicitud.getFechaSolicitud(), solicitud.getFechaRecibimiento()));
        }

        long total = solicitudes.getTotalElements();
        log.info("Consulta de adopciones realizada: {} solicitudes APROBADAS en total", total);
        String mensaje = null;
        if (adopciones.isEmpty()) {
            log.info("La consulta de adopciones no tiene resultados en la página solicitada");
            mensaje = "No existen solicitudes APROBADAS";
        }
        return new ConsultarAdopcionesResponseDTO(total, mensaje, adopciones);
    }
}
