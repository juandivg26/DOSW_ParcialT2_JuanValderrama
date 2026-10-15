package edu.dosw.parcial.controller.mappers;

import org.springframework.stereotype.Component;

import edu.dosw.parcial.controller.dtos.request.CompatibilidadRequestDTO;
import edu.dosw.parcial.controller.dtos.request.CrearMascotaRequestDTO;
import edu.dosw.parcial.controller.dtos.response.MascotaResponseDTO;
import edu.dosw.parcial.persistence.entities.Compatibilidad;
import edu.dosw.parcial.persistence.entities.MascotaDocument;

@Component
public class MascotaMapper {

    public MascotaDocument toDocument(CrearMascotaRequestDTO request) {
        MascotaDocument mascota = new MascotaDocument();
        mascota.setIdentificacion(request.identificacion());
        mascota.setNombre(request.nombre());
        mascota.setEspecie(request.especie());
        mascota.setEdadMeses(request.edadMeses());
        mascota.setTamano(request.tamano());
        mascota.setRefugioId(request.refugioId());
        CompatibilidadRequestDTO compatibilidad = request.compatibilidad();
        mascota.setCompatibilidad(new Compatibilidad(compatibilidad.ninos(),
                compatibilidad.otrasMascotas(), compatibilidad.espaciosReducidos()));
        return mascota;
    }

    public MascotaResponseDTO toResponse(MascotaDocument mascota, String refugioNombre) {
        return new MascotaResponseDTO(mascota.getId(), mascota.getIdentificacion(), mascota.getNombre(),
                mascota.getEspecie(), mascota.getEdadMeses(), mascota.getTamano(), mascota.getRefugioId(),
                refugioNombre, mascota.getEstado(), mascota.getCompatibilidad());
    }
}
