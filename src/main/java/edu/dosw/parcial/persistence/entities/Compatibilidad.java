package edu.dosw.parcial.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compatibilidad {
    private Boolean ninos;
    private Boolean otrasMascotas;
    private Boolean espaciosReducidos;
}
