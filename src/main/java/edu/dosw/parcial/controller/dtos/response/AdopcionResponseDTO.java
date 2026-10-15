package edu.dosw.parcial.controller.dtos.response;

import java.time.LocalDateTime;

import edu.dosw.parcial.core.models.Especie;
import edu.dosw.parcial.core.models.EstadoSolicitud;

public record AdopcionResponseDTO(
        String solicitudId,
        String mascotaId,
        String identificacionMascota,
        String nombreMascota,
        Especie especie,
        String adoptanteEmail,
        EstadoSolicitud estadoSolicitud,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaRecibimiento) {
}
