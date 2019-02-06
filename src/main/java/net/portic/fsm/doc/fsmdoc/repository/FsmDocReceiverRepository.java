package net.portic.fsm.doc.fsmdoc.repository;

import net.portic.fsm.doc.fsmdoc.model.FsmDocReceiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface FsmDocReceiverRepository extends JpaRepository<FsmDocReceiver, Long> {
    List<FsmDocReceiver> findByDocumentId(@Param("documentId") Long documentId);
    @Transactional
    Long deleteByDocumentId(Long documentId);
}
