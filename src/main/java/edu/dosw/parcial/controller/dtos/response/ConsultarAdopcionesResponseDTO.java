package edu.dosw.parcial.controller.dtos.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ConsultarAdopcionesResponseDTO(
        long total,
        @JsonInclude(JsonInclude.Include.NON_NULL) String mensaje,
        List<AdopcionResponseDTO> adopciones) {
}
