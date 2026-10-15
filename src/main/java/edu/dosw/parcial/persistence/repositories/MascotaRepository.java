package edu.dosw.parcial.persistence.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.dosw.parcial.persistence.entities.MascotaDocument;

public interface MascotaRepository extends MongoRepository<MascotaDocument, String> {
    boolean existsByIdentificacion(String identificacion);
}
