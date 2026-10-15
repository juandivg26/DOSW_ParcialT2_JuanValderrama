package edu.dosw.parcial.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;

import edu.dosw.parcial.core.models.Especie;
import edu.dosw.parcial.core.models.EstadoMascota;
import edu.dosw.parcial.core.models.Tamano;
import edu.dosw.parcial.core.models.TipoNodo;
import edu.dosw.parcial.persistence.entities.Compatibilidad;
import edu.dosw.parcial.persistence.entities.MascotaDocument;
import edu.dosw.parcial.persistence.entities.NodoDocument;
import edu.dosw.parcial.persistence.repositories.MascotaRepository;
import edu.dosw.parcial.persistence.repositories.NodoRepository;

class DatosInicialesSeederTest {

    private final NodoRepository nodoRepository = mock(NodoRepository.class);
    private final MascotaRepository mascotaRepository = mock(MascotaRepository.class);
    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final IndexOperations indexOperations = mock(IndexOperations.class);
    private final Map<String, NodoDocument> guardados = new HashMap<>();
    private final Map<String, MascotaDocument> mascotasGuardadas = new HashMap<>();
    private final DatosInicialesSeeder seeder =
            new DatosInicialesSeeder(nodoRepository, mascotaRepository, mongoTemplate);

    @BeforeEach
    void configurarRepositorios() {
        when(mongoTemplate.indexOps(MascotaDocument.class)).thenReturn(indexOperations);
        when(nodoRepository.existsById(anyString())).thenAnswer(invocacion ->
                guardados.containsKey(invocacion.getArgument(0)));
        when(nodoRepository.insert(any(NodoDocument.class))).thenAnswer(invocacion -> {
            NodoDocument nodo = invocacion.getArgument(0);
            guardados.put(nodo.getId(), nodo);
            return nodo;
        });
        when(mascotaRepository.existsById(anyString())).thenAnswer(invocacion ->
                mascotasGuardadas.containsKey(invocacion.getArgument(0)));
        when(mascotaRepository.existsByIdentificacion(anyString())).thenAnswer(invocacion ->
                mascotasGuardadas.values().stream().anyMatch(mascota ->
                        mascota.getIdentificacion().equals(invocacion.getArgument(0))));
        when(mascotaRepository.insert(any(MascotaDocument.class))).thenAnswer(invocacion -> {
            MascotaDocument mascota = invocacion.getArgument(0);
            mascotasGuardadas.put(mascota.getId(), mascota);
            return mascota;
        });
    }

    @Test
    void reiniciarConservaJerarquiaSinDuplicarRegistros() {
        seeder.cargarDatos();
        seeder.cargarDatos();

        assertEquals(8, guardados.size());
        assertEquals(TipoNodo.RED, guardados.get("red-eci").getTipo());
        assertEquals("red-eci", guardados.get("ciu-bogota").getParentId());
        assertEquals("red-eci", guardados.get("ciu-medellin").getParentId());
        assertEquals("red-eci", guardados.get("ciu-cali").getParentId());
        assertEquals("ciu-bogota", guardados.get("ref-bog-norte").getParentId());
        assertEquals("ciu-bogota", guardados.get("ref-bog-sur").getParentId());
        assertEquals("ciu-medellin", guardados.get("ref-med-central").getParentId());
        assertEquals("ciu-cali", guardados.get("ref-cali-oeste").getParentId());
        verify(nodoRepository, times(8)).insert(any(NodoDocument.class));
    }

    @Test
    void reponeUnNodoFaltanteSinSobrescribirModificaciones() {
        seeder.cargarDatos();
        guardados.get("ref-bog-norte").setNombre("Nombre actualizado");
        guardados.remove("ref-cali-oeste");

        seeder.cargarDatos();

        assertEquals(8, guardados.size());
        assertEquals("Nombre actualizado", guardados.get("ref-bog-norte").getNombre());
        assertEquals("Refugio Oeste", guardados.get("ref-cali-oeste").getNombre());
        verify(nodoRepository, times(9)).insert(any(NodoDocument.class));
    }

    @Test
    void cargaExactamenteLasOnceMascotasOficialesDisponibles() {
        seeder.cargarDatos();

        assertThat(mascotasGuardadas.values()).allSatisfy(mascota -> {
            assertEquals(EstadoMascota.DISPONIBLE, mascota.getEstado());
            assertEquals(TipoNodo.REFUGIO, guardados.get(mascota.getRefugioId()).getTipo());
        });
        assertThat(mascotasGuardadas.values()).extracting(
                MascotaDocument::getId, MascotaDocument::getIdentificacion, MascotaDocument::getNombre,
                mascota -> mascota.getEspecie().name(), MascotaDocument::getEdadMeses,
                mascota -> mascota.getTamano().name(),
                mascota -> mascota.getCompatibilidad().getNinos(),
                mascota -> mascota.getCompatibilidad().getOtrasMascotas(),
                mascota -> mascota.getCompatibilidad().getEspaciosReducidos(), MascotaDocument::getRefugioId)
                .containsExactlyInAnyOrder(
                        tuple("masc-001", "PERRO-001", "Max", "PERRO", 24, "MEDIANO", true, true, true, "ref-bog-norte"),
                        tuple("masc-002", "PERRO-002", "Bruno", "PERRO", 72, "GRANDE", true, false, false, "ref-bog-norte"),
                        tuple("masc-003", "GATO-001", "Luna", "GATO", 18, "PEQUENO", true, true, true, "ref-bog-norte"),
                        tuple("masc-004", "PERRO-003", "Toby", "PERRO", 12, "PEQUENO", true, true, true, "ref-bog-sur"),
                        tuple("masc-005", "GATO-002", "Milo", "GATO", 48, "MEDIANO", false, true, true, "ref-bog-sur"),
                        tuple("masc-006", "CONEJO-001", "Copito", "CONEJO", 10, "PEQUENO", true, true, true, "ref-bog-sur"),
                        tuple("masc-007", "PERRO-004", "Rocky", "PERRO", 60, "GRANDE", false, false, false, "ref-med-central"),
                        tuple("masc-008", "GATO-003", "Nala", "GATO", 30, "PEQUENO", true, true, true, "ref-med-central"),
                        tuple("masc-009", "AVE-001", "Kiwi", "AVE", 20, "PEQUENO", true, false, true, "ref-med-central"),
                        tuple("masc-010", "PERRO-005", "Simon", "PERRO", 36, "MEDIANO", true, true, true, "ref-cali-oeste"),
                        tuple("masc-011", "REPTIL-001", "Rocco", "REPTIL", 84, "PEQUENO", false, false, true, "ref-cali-oeste"));
    }

    @Test
    void reiniciarNoDuplicaMascotas() {
        seeder.cargarDatos();
        seeder.cargarDatos();

        assertEquals(11, mascotasGuardadas.size());
        verify(mascotaRepository, times(11)).insert(any(MascotaDocument.class));
    }

    @Test
    void reponeUnaMascotaFaltanteSinSobrescribirDatosNiEstadosExistentes() {
        seeder.cargarDatos();
        MascotaDocument modificada = new MascotaDocument("masc-001", "IDENTIFICACION-ACTUALIZADA",
                "Nombre actualizado", Especie.PERRO, 25, Tamano.GRANDE, EstadoMascota.EN_PROCESO,
                "ref-bog-sur", new Compatibilidad(false, false, false));
        mascotasGuardadas.put(modificada.getId(), modificada);
        mascotasGuardadas.remove("masc-011");

        seeder.cargarDatos();

        assertEquals(11, mascotasGuardadas.size());
        assertEquals(modificada, mascotasGuardadas.get("masc-001"));
        assertEquals("Rocco", mascotasGuardadas.get("masc-011").getNombre());
        verify(mascotaRepository, times(12)).insert(any(MascotaDocument.class));
    }

    @Test
    void conservaMascotaConIdentificacionOficialAunqueTengaOtroIdInterno() {
        MascotaDocument existente = new MascotaDocument("id-existente", "PERRO-001", "Max actualizado",
                Especie.PERRO, 26, Tamano.MEDIANO, EstadoMascota.ADOPTADA,
                "ref-bog-norte", new Compatibilidad(true, true, true));
        mascotasGuardadas.put(existente.getId(), existente);

        seeder.cargarDatos();

        assertEquals(11, mascotasGuardadas.size());
        assertThat(mascotasGuardadas).doesNotContainKey("masc-001");
        assertEquals(existente, mascotasGuardadas.get("id-existente"));
        verify(mascotaRepository, times(10)).insert(any(MascotaDocument.class));
    }
}
