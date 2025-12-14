package com.secretsanta.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.secretsanta.backend.model.SessionDocument;

public interface SessionRepository extends MongoRepository<SessionDocument, String> {
}
