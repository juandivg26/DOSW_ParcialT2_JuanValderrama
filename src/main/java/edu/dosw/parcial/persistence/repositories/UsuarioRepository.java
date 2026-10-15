package edu.dosw.parcial.persistence.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.dosw.parcial.persistence.entities.UsuarioDocument;

public interface UsuarioRepository extends MongoRepository<UsuarioDocument, String> {
}
