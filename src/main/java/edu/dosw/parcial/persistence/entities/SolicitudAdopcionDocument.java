package edu.dosw.parcial.persistence.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.dosw.parcial.core.models.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("solicitudes")
public class SolicitudAdopcionDocument {
    @Id
    private String id;
    private String mascotaId;
    private String adoptanteId;
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRecibimiento;
    private Integer aplazamientos;
    private Declaracion declaracion;
}
