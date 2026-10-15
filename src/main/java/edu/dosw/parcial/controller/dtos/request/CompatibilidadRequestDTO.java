package edu.dosw.parcial.controller.dtos.request;

import jakarta.validation.constraints.NotNull;

public record CompatibilidadRequestDTO(
        @NotNull Boolean ninos,
        @NotNull Boolean otrasMascotas,
        @NotNull Boolean espaciosReducidos) {
}
