package edu.dosw.parcial.core.services;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import edu.dosw.parcial.controller.dtos.request.CrearMascotaRequestDTO;
import edu.dosw.parcial.controller.dtos.response.MascotaResponseDTO;
import edu.dosw.parcial.controller.mappers.MascotaMapper;
import edu.dosw.parcial.core.models.EstadoMascota;
import edu.dosw.parcial.core.models.TipoNodo;
import edu.dosw.parcial.core.validators.ConflictoException;
import edu.dosw.parcial.core.validators.RecursoNoEncontradoException;
import edu.dosw.parcial.core.validators.ReglaNegocioException;
import edu.dosw.parcial.persistence.entities.MascotaDocument;
import edu.dosw.parcial.persistence.entities.NodoDocument;
import edu.dosw.parcial.persistence.repositories.MascotaRepository;
import edu.dosw.parcial.persistence.repositories.NodoRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MascotaServiceImpl implements IMascotaService {

    private final NodoRepository nodoRepository;
    private final MascotaRepository mascotaRepository;
    private final MascotaMapper mascotaMapper;

    public MascotaServiceImpl(NodoRepository nodoRepository, MascotaRepository mascotaRepository,
            MascotaMapper mascotaMapper) {
        this.nodoRepository = nodoRepository;
        this.mascotaRepository = mascotaRepository;
        this.mascotaMapper = mascotaMapper;
    }

    @Override
    public MascotaResponseDTO crearMascota(CrearMascotaRequestDTO request) {
        NodoDocument refugio = nodoRepository.findById(request.refugioId())
                .orElseThrow(() -> {
                    log.warn("Creación de mascota rechazada: el refugio no existe");
                    return new RecursoNoEncontradoException("REFUGIO_NO_ENCONTRADO", "El refugio no existe.");
                });

        if (refugio.getTipo() != TipoNodo.REFUGIO) {
            log.warn("Creación de mascota rechazada: el nodo no es un refugio");
            throw new ReglaNegocioException("E03", "El nodo debe ser un refugio.");
        }
        if (mascotaRepository.existsByIdentificacion(request.identificacion())) {
            throw mascotaDuplicada();
        }

        MascotaDocument mascota = mascotaMapper.toDocument(request);
        mascota.setEstado(EstadoMascota.DISPONIBLE);
        MascotaDocument guardada;
        try {
            guardada = mascotaRepository.save(mascota);
        } catch (DuplicateKeyException ex) {
            throw mascotaDuplicada();
        }

        log.info("Mascota creada: {}", guardada.getIdentificacion());
        return mascotaMapper.toResponse(guardada, refugio.getNombre());
    }

    private ConflictoException mascotaDuplicada() {
        log.warn("Creación de mascota rechazada: identificación duplicada");
        return new ConflictoException("MASCOTA_DUPLICADA", "La identificación ya existe.");
    }
}
