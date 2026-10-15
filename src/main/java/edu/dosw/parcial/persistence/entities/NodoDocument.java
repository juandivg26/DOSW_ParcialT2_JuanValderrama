package edu.dosw.parcial.persistence.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.dosw.parcial.core.models.TipoNodo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("nodos")
public class NodoDocument {
    @Id
    private String id;
    private String nombre;
    private TipoNodo tipo;
    private String parentId;
}
