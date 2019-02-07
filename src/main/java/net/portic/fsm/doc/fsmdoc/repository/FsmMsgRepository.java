package net.portic.fsm.doc.fsmdoc.repository;

import net.portic.fsm.doc.fsmdoc.model.FsmMsg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FsmMsgRepository extends JpaRepository<FsmMsg, Long> {
    Optional<FsmMsg> findByKey(String key);
    Optional<FsmMsg> findBySenderAndDocTypeAndDocNumAndDocVersion(String aperakReceiver, String msgType, String docNum, String docVersion);
}
