package edu.dosw.parcial.persistence.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.dosw.parcial.core.models.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("usuarios")
public class UsuarioDocument {
    @Id
    private String id;
    private String passwordHash;
    private Rol rol;
}
