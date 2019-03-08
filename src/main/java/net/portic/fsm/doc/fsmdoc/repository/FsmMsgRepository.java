package net.portic.fsm.doc.fsmdoc.repository;

import net.portic.fsm.doc.fsmdoc.model.FsmMsg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FsmMsgRepository extends JpaRepository<FsmMsg, Long> {
    Optional<FsmMsg> findByKey(String key);
    // Aperak receiver is the original message's sender!!!
    Optional<FsmMsg> findBySenderAndDocTypeAndDocNumAndDocVersion(String aperakReceiver, String msgType, String docNum, String docVersion);
}
