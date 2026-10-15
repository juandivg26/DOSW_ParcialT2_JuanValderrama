package edu.dosw.parcial.persistence.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.dosw.parcial.core.models.Especie;
import edu.dosw.parcial.core.models.EstadoMascota;
import edu.dosw.parcial.core.models.Tamano;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("mascotas")
public class MascotaDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private String identificacion;
    private String nombre;
    private Especie especie;
    private Integer edadMeses;
    private Tamano tamano;
    private EstadoMascota estado;
    private String refugioId;
    private Compatibilidad compatibilidad;
}
