package net.portic.fsm.doc.fsmdoc.controller;

import net.portic.fsm.doc.fsmdoc.exception.ResourceNotFoundException;
import net.portic.fsm.doc.fsmdoc.model.FsmDoc;
import net.portic.fsm.doc.fsmdoc.model.FsmDocReceiver;
import net.portic.fsm.doc.fsmdoc.model.FsmMsg;
import net.portic.fsm.doc.fsmdoc.repository.FsmDocReceiverRepository;
import net.portic.fsm.doc.fsmdoc.repository.FsmDocRepository;
import net.portic.fsm.doc.fsmdoc.repository.FsmMsgRepository;
import org.hibernate.JDBCException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;

import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.List;

@RestController
public class DocumentController {
    @Autowired
    private FsmDocRepository fsmDocRepository;

    @Autowired
    private FsmMsgRepository fsmMsgRepository;

    @Autowired
    private FsmDocReceiverRepository fsmDocReceiverRepository;

    @GetMapping("/fsmdoc")
    public List<FsmDoc> getDocuments() {
        return fsmDocRepository.findAll();
    }

    @GetMapping("/fsmdoc/{documentId}")
    public FsmDoc getDocument(@PathVariable Long documentId) {
        return fsmDocRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id = " + documentId));
    }

//    @GetMapping("/documents/key/{key}")
//    public Document getDocumentByKey(@PathVariable String key) {
//        return documentRepository.findOne(documentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id = "+documentId));
//    }

    @PostMapping("/notify")
    public NotifyResult notify(@RequestBody MsgDto msgDto) {

        return doNotify(msgDto);
    }

    private FsmMsg makeFsmMsg(MsgDto msgDto, String key) {
        FsmMsg msg = new FsmMsg();

        msg.setDocNum(msgDto.docNum);
        msg.setDocType(msgDto.docType);
        msg.setDocVersion(msgDto.docVersion);
        msg.setKey(key);
        msg.setMsgFunction(msgDto.msgFunction);
        msg.setMsgNum(msgDto.msgNum);
        msg.setReceiver(msgDto.receiver);
        msg.setSender(msgDto.sender);
        msg.setTrackId(msgDto.trackId);
        msg.setMsgDate(msgDto.when);
        msg.setState(MsgStateCode.PROCESSING.getName());
        return msg;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    private NotifyResult doNotify(MsgDto msgDto) {
        NotifyResult result = null;
        String docKey = makeDocKey(msgDto.getSender(), msgDto.getDocType(), msgDto.getDocNum());
        String msgKey = makeMsgKey(msgDto.trackId, msgDto.msgNum);
        FsmMsg msg = fsmMsgRepository.findByKey(msgKey)
                .map(aMsg -> {
                    aMsg.setReprocessed(true);
                    return fsmMsgRepository.save(aMsg);
                }).orElseGet(() -> {
                    FsmMsg auxMsg = makeFsmMsg(msgDto, msgKey);
                    return fsmMsgRepository.save(auxMsg);
                });
        try {
            result = fsmDocRepository.findByKey(docKey)
                    .map(fsmDoc -> processDocument(msg, fsmDocRepository.save(fsmDoc)))
                    .orElseGet(() -> processNewDocument(msg, fsmDocRepository.save(newFsmDoc(msg, docKey))));
        } catch (DataIntegrityViolationException e) {
            // Race condition, no doc existed and 2 messages tried to create the new document simultaneously
            return doNotify(msgDto);
        }
        return result;
    }


    private NotifyResult processDocument(FsmMsg msg, FsmDoc doc) {
        // Document exists
        // cases 3, 4, 5
        if (msg.getDocVersion().compareTo(doc.getDocCurrentVersion()) < 0) {
            // 3. Message version is a previous version of the document
            return new NotifyResult(ResultCode.OUT_OF_SEQUENCE.getName(), "Message references an older version of the document");
        } else if (msg.getDocVersion().compareTo(doc.getDocCurrentVersion()) == 0) {
            // 4. message version is equal than document version
            if (thereIsResponse(doc.getState())) {
                return new NotifyResult(ResultCode.ERROR.getName(), "There is a response for this version of the document");
            } else if (doc.getFsmDocReceivers().stream().filter(r -> r.getReceiver().equals(msg.getReceiver())).count() == 0) {
                FsmDocReceiver fsmDocReceiver = new FsmDocReceiver();
                fsmDocReceiver.setDocumentId(doc.getId());
                fsmDocReceiver.setReceiver(msg.getReceiver());
                fsmDocReceiverRepository.save(fsmDocReceiver);
                return new NotifyResult(ResultCode.SUCCESS.getName(), "Same message but for another receiver");
            } else {
                return new NotifyResult(ResultCode.SUCCESS.getName(), "Message reprocessed ???");
            }
        } else {
            // 5. message version is newer than document current version
            if (msg.getMsgFunction().equals(FunctionCode.ORIGINAL.getName())) {
                // message with an original is not allowed
                return new NotifyResult(ResultCode.OUT_OF_SEQUENCE.getName(), "Message with an original is not allowed");
            } else if (doc.getState().equals(DocStateCode.CANCELLED.getName())) {
                // Document already rejected
                return new NotifyResult(ResultCode.CANCELLED.getName(), "Document already rejected");
            } else if (doc.getState().equals(DocStateCode.PROCESSING.getName())) {
                // Previous version is still processing
                msg.setState(MsgStateCode.ON_HOLD.getName());
                fsmMsgRepository.save(msg);
                return new NotifyResult(ResultCode.ON_HOLD.getName(), "Document's previous version is still processing");
            } else {
                // update document with new version and carry on with msg processing
                fsmDocReceiverRepository.deleteByDocumentId(doc.getId());
                FsmDocReceiver fsmDocReceiver = new FsmDocReceiver();
                fsmDocReceiver.setDocumentId(doc.getId());
                fsmDocReceiver.setReceiver(msg.getReceiver());
                fsmDocReceiverRepository.save(fsmDocReceiver);
                doc.setDocCurrentVersion(msg.getDocVersion());
                fsmDocRepository.save(doc);
                return new NotifyResult(ResultCode.SUCCESS.getName(), "Update document with new version and carry on with msg processing");
            }
        }
    }

    private Boolean thereIsResponse(String state) {
        return (state.equals(DocStateCode.ACCEPTED.getName()) || state.equals(DocStateCode.REJECTED.getName()));
    }

    private NotifyResult processNewDocument(FsmMsg msg, FsmDoc doc) {
        // Document does not exist
        // Step 2
        if (msg.getMsgFunction().equals(FunctionCode.ORIGINAL.getName())) {
            fsmDocRepository.save(doc);
            return new NotifyResult(ResultCode.SUCCESS.getName(), "New document created");
        } else if (msg.getMsgFunction().equals(FunctionCode.REPLACEMENT.getName()) ||
                msg.getMsgFunction().equals(FunctionCode.CANCELLATION.getName())) {
            return new NotifyResult(ResultCode.ON_HOLD.getName(), "Message it's a replacement or cancellation and no original message has been found. it must be put on hold");
        }
        return new NotifyResult(ResultCode.UNKNOWN_DOCUMENT_FUNCTION.getName(), "Message function is unknown");
    }

    private FsmDoc newFsmDoc(FsmMsg msg, String docKey) {
        FsmDoc fsmDoc = new FsmDoc();
        fsmDoc.setDocNum(msg.getDocNum());
        fsmDoc.setDocCurrentVersion(msg.getDocVersion());
        fsmDoc.setSender(msg.getSender());
        fsmDoc.setDocType(msg.getDocType());
        fsmDoc.setKey(docKey);
        fsmDoc.setState("PROCESSING");
        fsmDoc.setDocType(msg.getDocType());
        FsmDocReceiver fsmDocReceiver = new FsmDocReceiver();
        fsmDocReceiver.setReceiver(msg.getReceiver());
//        List<FsmDocReceiver> lfdr = new ArrayList<FsmDocReceiver>();
//        lfdr.add(fsmDocReceiver);
//        fsmDoc.setFsmDocReceivers(lfdr);

        FsmDoc newDoc =  fsmDocRepository.save(fsmDoc);
        fsmDocReceiver.setDocumentId(newDoc.getId());
        fsmDocReceiverRepository.save(fsmDocReceiver);

        return newDoc;
    }

    private String makeDocKey(String sender, String docType, String docNum) {
        return String.format("%s###%s###%s", sender, docType, docNum);
    }

    private String makeMsgKey(String trackId, String msgNum) {
        return String.format("%s###%s", trackId, msgNum);
    }



    ////////////////////////////////////////////////
    public enum FunctionCode {
        ORIGINAL("9"),
        REPLACEMENT("5"),
        CANCELLATION("1");

        private final String name;

        private FunctionCode(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum MsgStateCode {
        PROCESSING("PROCESSING"),
        DELIVERED("DELIVERED"),
        CONFIRMED("CONFIRMED"),
        ACKNOWLEDGED("ACKNOWLEDGED"),
        ERROR("ERROR"),
        ON_HOLD("ON_HOLD");

        private final String name;

        MsgStateCode(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum DocStateCode {
        PROCESSING("PROCESSING"),
        DELIVERED("DELIVERED"),
        CONFIRMED("CONFIRMED"),
        ACKNOWLEDGED("ACKNOWLEDGED"),
        ERROR("ERROR"),
        ACCEPTED("ACCEPTED"),
        REJECTED("REJECTED"),
        CANCELLED("CANCELLATION");

        private final String name;

        DocStateCode(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum ResultCode {
        SUCCESS("SUCCESS"),
        OUT_OF_SEQUENCE("OUT_OF_SEQUENCE"), // doc already exists, so another 'original' msg is out of sequence
        ON_HOLD("ON_HOLD"),
        ERROR("ERROR"),
        UNKNOWN_DOCUMENT_FUNCTION("UNKNOWN_DOCUMENT_FUNCTION"),
        CANCELLED("CANCELLATION");

        private final String name;

        ResultCode(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
