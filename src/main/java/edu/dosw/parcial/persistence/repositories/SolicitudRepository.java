package edu.dosw.parcial.persistence.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import edu.dosw.parcial.core.models.EstadoSolicitud;

import edu.dosw.parcial.persistence.entities.SolicitudAdopcionDocument;

public interface SolicitudRepository extends MongoRepository<SolicitudAdopcionDocument, String> {
    Page<SolicitudAdopcionDocument> findByEstado(EstadoSolicitud estado, Pageable pageable);
}
