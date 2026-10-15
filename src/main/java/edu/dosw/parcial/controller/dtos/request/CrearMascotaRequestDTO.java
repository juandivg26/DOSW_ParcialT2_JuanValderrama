package edu.dosw.parcial.controller.dtos.request;

import edu.dosw.parcial.core.models.Especie;
import edu.dosw.parcial.core.models.Tamano;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearMascotaRequestDTO(
        @NotBlank String identificacion,
        @NotBlank String nombre,
        @NotNull Especie especie,
        @NotNull @Min(0) @Max(360) Integer edadMeses,
        @NotNull Tamano tamano,
        @NotBlank String refugioId,
        @NotNull @Valid CompatibilidadRequestDTO compatibilidad) {
}
