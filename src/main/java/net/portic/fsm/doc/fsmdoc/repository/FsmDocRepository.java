package net.portic.fsm.doc.fsmdoc.repository;

import net.portic.fsm.doc.fsmdoc.model.FsmDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FsmDocRepository extends JpaRepository<FsmDoc, Long> {
    Optional<FsmDoc> findByKey(String key);
}
