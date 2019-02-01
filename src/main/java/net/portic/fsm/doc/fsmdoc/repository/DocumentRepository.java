package net.portic.fsm.doc.fsmdoc.repository;

import net.portic.fsm.doc.fsmdoc.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
