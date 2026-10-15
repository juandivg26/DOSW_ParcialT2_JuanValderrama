package edu.dosw.parcial.config;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import edu.dosw.parcial.core.models.Especie;
import edu.dosw.parcial.core.models.EstadoMascota;
import edu.dosw.parcial.core.models.Tamano;
import edu.dosw.parcial.core.models.TipoNodo;
import edu.dosw.parcial.persistence.entities.Compatibilidad;
import edu.dosw.parcial.persistence.entities.MascotaDocument;
import edu.dosw.parcial.persistence.entities.NodoDocument;
import edu.dosw.parcial.persistence.repositories.MascotaRepository;
import edu.dosw.parcial.persistence.repositories.NodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatosInicialesSeeder {

    private final NodoRepository nodoRepository;
    private final MascotaRepository mascotaRepository;
    private final MongoTemplate mongoTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void cargarDatos() {
        mongoTemplate.indexOps(MascotaDocument.class)
                .createIndex(new Index().on("identificacion", Sort.Direction.ASC).unique());
        cargarNodos();
        cargarMascotas();
        log.warn("Carga inicial de solicitudes pendiente: faltan sus datos completos. "
                + "Max y Toby deberán pasar a EN_PROCESO cuando se creen sus solicitudes APROBADAS.");
    }

    private void cargarNodos() {
        List<NodoDocument> nodos = List.of(
                new NodoDocument("red-eci", "RED NACIONAL", TipoNodo.RED, null),
                new NodoDocument("ciu-bogota", "Bogotá", TipoNodo.CIUDAD, "red-eci"),
                new NodoDocument("ciu-medellin", "Medellín", TipoNodo.CIUDAD, "red-eci"),
                new NodoDocument("ciu-cali", "Cali", TipoNodo.CIUDAD, "red-eci"),
                new NodoDocument("ref-bog-norte", "Refugio Norte", TipoNodo.REFUGIO, "ciu-bogota"),
                new NodoDocument("ref-bog-sur", "Refugio Sur", TipoNodo.REFUGIO, "ciu-bogota"),
                new NodoDocument("ref-med-central", "Refugio Central", TipoNodo.REFUGIO, "ciu-medellin"),
                new NodoDocument("ref-cali-oeste", "Refugio Oeste", TipoNodo.REFUGIO, "ciu-cali"));
        for (NodoDocument nodo : nodos) {
            if (!nodoRepository.existsById(nodo.getId())) {
                nodoRepository.insert(nodo);
            }
        }
    }

    private void cargarMascotas() {
        List<MascotaDocument> mascotas = List.of(
                new MascotaDocument("masc-001", "PERRO-001", "Max", Especie.PERRO, 24,
                        Tamano.MEDIANO, EstadoMascota.DISPONIBLE, "ref-bog-norte",
                        new Compatibilidad(true, true, true)),
                new MascotaDocument("masc-002", "PERRO-002", "Bruno", Especie.PERRO, 72,
                        Tamano.GRANDE, EstadoMascota.DISPONIBLE, "ref-bog-norte",
                        new Compatibilidad(true, false, false)),
                new MascotaDocument("masc-003", "GATO-001", "Luna", Especie.GATO, 18,
                        Tamano.PEQUENO, EstadoMascota.DISPONIBLE, "ref-bog-norte",
                        new Compatibilidad(true, true, true)),
                new MascotaDocument("masc-004", "PERRO-003", "Toby", Especie.PERRO, 12,
                        Tamano.PEQUENO, EstadoMascota.DISPONIBLE, "ref-bog-sur",
                        new Compatibilidad(true, true, true)),
                new MascotaDocument("masc-005", "GATO-002", "Milo", Especie.GATO, 48,
                        Tamano.MEDIANO, EstadoMascota.DISPONIBLE, "ref-bog-sur",
                        new Compatibilidad(false, true, true)),
                new MascotaDocument("masc-006", "CONEJO-001", "Copito", Especie.CONEJO, 10,
                        Tamano.PEQUENO, EstadoMascota.DISPONIBLE, "ref-bog-sur",
                        new Compatibilidad(true, true, true)),
                new MascotaDocument("masc-007", "PERRO-004", "Rocky", Especie.PERRO, 60,
                        Tamano.GRANDE, EstadoMascota.DISPONIBLE, "ref-med-central",
                        new Compatibilidad(false, false, false)),
                new MascotaDocument("masc-008", "GATO-003", "Nala", Especie.GATO, 30,
                        Tamano.PEQUENO, EstadoMascota.DISPONIBLE, "ref-med-central",
                        new Compatibilidad(true, true, true)),
                new MascotaDocument("masc-009", "AVE-001", "Kiwi", Especie.AVE, 20,
                        Tamano.PEQUENO, EstadoMascota.DISPONIBLE, "ref-med-central",
                        new Compatibilidad(true, false, true)),
                new MascotaDocument("masc-010", "PERRO-005", "Simon", Especie.PERRO, 36,
                        Tamano.MEDIANO, EstadoMascota.DISPONIBLE, "ref-cali-oeste",
                        new Compatibilidad(true, true, true)),
                new MascotaDocument("masc-011", "REPTIL-001", "Rocco", Especie.REPTIL, 84,
                        Tamano.PEQUENO, EstadoMascota.DISPONIBLE, "ref-cali-oeste",
                        new Compatibilidad(false, false, true)));
        for (MascotaDocument mascota : mascotas) {
            if (!mascotaRepository.existsById(mascota.getId())
                    && !mascotaRepository.existsByIdentificacion(mascota.getIdentificacion())) {
                mascotaRepository.insert(mascota);
            }
        }
    }
}
