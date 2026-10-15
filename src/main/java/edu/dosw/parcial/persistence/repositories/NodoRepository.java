package edu.dosw.parcial.persistence.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.dosw.parcial.persistence.entities.NodoDocument;

public interface NodoRepository extends MongoRepository<NodoDocument, String> {
}
