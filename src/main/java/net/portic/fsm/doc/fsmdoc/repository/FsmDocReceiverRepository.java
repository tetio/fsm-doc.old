package net.portic.fsm.doc.fsmdoc.repository;

import net.portic.fsm.doc.fsmdoc.model.FsmDocReceiver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FsmDocReceiverRepository extends JpaRepository<FsmDocReceiver, Long> {
    List<FsmDocReceiver> findByDocumentId(Long documentId);
}
