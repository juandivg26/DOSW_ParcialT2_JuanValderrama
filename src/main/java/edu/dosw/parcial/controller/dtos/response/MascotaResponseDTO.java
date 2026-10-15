package edu.dosw.parcial.controller.dtos.response;

import edu.dosw.parcial.core.models.Especie;
import edu.dosw.parcial.core.models.EstadoMascota;
import edu.dosw.parcial.core.models.Tamano;
import edu.dosw.parcial.persistence.entities.Compatibilidad;

public record MascotaResponseDTO(
        String id,
        String identificacion,
        String nombre,
        Especie especie,
        Integer edadMeses,
        Tamano tamano,
        String refugioId,
        String refugioNombre,
        EstadoMascota estado,
        Compatibilidad compatibilidad) {
}
