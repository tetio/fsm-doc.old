package net.portic.fsm.doc.fsmdoc.service;

import net.portic.fsm.doc.fsmdoc.controller.dto.AperakDto;
import net.portic.fsm.doc.fsmdoc.controller.dto.FSMDocResult;
import net.portic.fsm.doc.fsmdoc.controller.dto.MsgDto;
import net.portic.fsm.doc.fsmdoc.exception.ResourceNotFoundException;
import net.portic.fsm.doc.fsmdoc.model.FsmDoc;
import net.portic.fsm.doc.fsmdoc.model.FsmDocReceiver;
import net.portic.fsm.doc.fsmdoc.model.FsmMsg;
import net.portic.fsm.doc.fsmdoc.repository.FsmDocReceiverRepository;
import net.portic.fsm.doc.fsmdoc.repository.FsmDocRepository;
import net.portic.fsm.doc.fsmdoc.repository.FsmMsgRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class FsmServices {
    @Autowired
    private FsmDocRepository fsmDocRepository;

    @Autowired
    private FsmMsgRepository fsmMsgRepository;

    @Autowired
    private FsmDocReceiverRepository fsmDocReceiverRepository;

    public List<FsmDoc> findAllDocuments() {
        return fsmDocRepository.findAll();
    }

    public FsmDoc getDocument(Long documentId) {
        return fsmDocRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id = " + documentId));

    }

    public FSMDocResult prepareNotify(MsgDto msgDto) {
        FSMDocResult result;
        String docKey = makeDocKey(msgDto.getSender(), msgDto.getDocType(), msgDto.getDocNum());
        FsmMsg msg = getFsmMsg(msgDto);
        try {
            result = prepareDocument(docKey, msg);
        } catch (DataIntegrityViolationException e) {
//            Caused by: org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint "uk_hq0ox5o2fsu70lvamkq04ht3g"
//            Detail: Key (key)=(ESA787878###COPINOE04###23383073) already exists.
            // Race condition, no doc existed and at least two messages tried to create the new document simultaneously
            return prepareNotify(msgDto);
        } catch(ConstraintViolationException ex) {
            // ORA-00001: unique constraint (PORTIC.UK_HQ0OX5O2FSU70LVAMKQ04HT3G) violated
            return prepareNotify(msgDto);
        }
        return result;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    private FSMDocResult prepareDocument(String docKey, FsmMsg msg) {
        FSMDocResult result;
        result = fsmDocRepository.findByKey(docKey)
                .map(fsmDoc -> processDocument(msg, fsmDoc))
                .orElseGet(() -> processNewDocument(msg, fsmDocRepository.save(newFsmDoc(msg, docKey))));
        return result;
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public FSMDocResult prepareDeliver(MsgDto msgDto) {
        String docKey = makeDocKey(msgDto.getSender(), msgDto.getDocType(), msgDto.getDocNum());
        String msgKey = makeMsgKey(msgDto.getTrackId(), msgDto.getMsgNum());
        return fsmMsgRepository.findByKey(msgKey)
                .map(msg -> fsmDocRepository.findByKey(docKey)
                        .map(doc -> doDeliver(msg, doc, msgKey, docKey))
                        .orElseGet(() -> new FSMDocResult(FsmServices.ResultCode.ERROR.getName(), String.format("doDeliver: No document Found key=[%s]", docKey)))
                ).orElseGet(() -> new FSMDocResult(FsmServices.ResultCode.ERROR.getName(), String.format("doDeliver: No message Found key=[%s]", msgKey)));
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public FSMDocResult prepareNotifyAperak(AperakDto aperakDto) {
        FsmMsg msg = getFsmMsg(aperakDto);

        return fsmMsgRepository.findBySenderAndDocTypeAndDocNumAndDocVersion(aperakDto.getReceiver(), aperakDto.getRefDocType(), aperakDto.getRefDocNum(), aperakDto.getRefDocVersion())
                .map(originalMsg ->
                        doNotifyAperak(aperakDto, originalMsg, msg)
                ).orElseGet(() ->
                        new FSMDocResult(FsmServices.ResultCode.ERROR.getName(), String.format("prepareNotifyAperak: Original message not found: sender(%s), docType(%s), docNum(%s), docVersion(%s)", aperakDto.getReceiver(), aperakDto.getRefDocType(), aperakDto.getRefDocNum(), aperakDto.getRefDocVersion()))
                );
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public FSMDocResult prepareDeliverAperak(AperakDto aperakDto) {
        FsmMsg aperakMsg = getFsmMsg(aperakDto);
        aperakMsg.setState(FsmServices.MsgStateCode.DELIVERED.getName());
        return fsmMsgRepository.findBySenderAndDocTypeAndDocNumAndDocVersion(aperakDto.getReceiver(), aperakDto.getRefDocType(), aperakDto.getRefDocNum(), aperakDto.getRefDocVersion())
                .map(originalMsg ->
                        doDeliverAperak(aperakDto, originalMsg, aperakMsg)
                ).orElseGet(() ->
                        new FSMDocResult(FsmServices.ResultCode.ERROR.getName(), String.format("prepareNotifyAperak: Original message not found: sender(%s), docType(%s), docNum(%s), docVersion(%s)", aperakDto.getReceiver(), aperakDto.getRefDocType(), aperakDto.getRefDocNum(), aperakDto.getRefDocVersion()))
                );
    }


    private FsmMsg makeFsmMsg(MsgDto msgDto, String key) {
        FsmMsg msg = new FsmMsg();
        msg.setDocNum(msgDto.getDocNum());
        msg.setDocType(msgDto.getDocType());
        msg.setDocVersion(msgDto.getDocVersion());
        msg.setKey(key);
        msg.setMsgFunction(msgDto.getMsgFunction());
        msg.setMsgNum(msgDto.getMsgNum());
        msg.setReceiver(msgDto.getReceiver());
        msg.setSender(msgDto.getSender());
        msg.setTrackId(msgDto.getTrackId());
        msg.setMsgDate(msgDto.getWhen());
        msg.setState(MsgStateCode.PROCESSING.getName());
        return msg;
    }


    private FsmMsg getFsmMsg(MsgDto msgDto) {
        String msgKey = makeMsgKey(msgDto.getTrackId(), msgDto.getMsgNum());
        return fsmMsgRepository.findByKey(msgKey)
                .map(aMsg -> {
                    aMsg.setReprocessed(true);
                    return fsmMsgRepository.save(aMsg);
                }).orElseGet(() -> {
                    FsmMsg auxMsg = makeFsmMsg(msgDto, msgKey);
                    return fsmMsgRepository.save(auxMsg);
                });
    }


    private FSMDocResult doDeliver(FsmMsg msg, FsmDoc doc, String msgKey, String docKey) {
        if (msg.getState().equals(MsgStateCode.ON_HOLD.getName())) {
            // Message on Hold, nothing to do on msg and doc, just continue
            return new FSMDocResult(ResultCode.ON_HOLD.getName(), String.format("Message [%s] is ON HOLD, can't be processed", msgKey));
        }

        if (msg.getDocVersion().equals(doc.getDocCurrentVersion())) {
            if (doc.getState().equals(DocStateCode.PROCESSING.getName()) ||
                    doc.getState().equals(DocStateCode.ERROR.getName())) {
                doc.setState(DocStateCode.DELIVERED.getName());
                msg.setState(MsgStateCode.DELIVERED.getName());
                fsmDocRepository.save(doc);
                fsmMsgRepository.save(msg);
                return new FSMDocResult(ResultCode.SUCCESS.getName(), String.format("Message [%s] and Document [%s] updated ", msgKey, docKey));
            } else {
                // Could be a message being reprocessed and it's beeing delivered again
                // Only msg data is updated!!!
                msg.setState(MsgStateCode.DELIVERED.getName());
                fsmMsgRepository.save(msg);
                return new FSMDocResult(ResultCode.SUCCESS.getName(), String.format("Only message [%s] is updated", msgKey));
            }
        } else if (msg.getDocVersion().compareTo(doc.getDocCurrentVersion()) > 0) {
            doc.setState(DocStateCode.DELIVERED.getName());
            doc.setDocCurrentVersion(msg.getDocVersion());
            msg.setState(MsgStateCode.DELIVERED.getName());
            fsmDocRepository.save(doc);
            fsmMsgRepository.save(msg);
            return new FSMDocResult(ResultCode.SUCCESS.getName(), String.format("Message [%s] and Document [%s] updated and version too ", msgKey, docKey));
        } else {
            msg.setState(MsgStateCode.DELIVERED.getName());
            fsmMsgRepository.save(msg);
            return new FSMDocResult(ResultCode.SUCCESS.getName(), String.format("Only message [%s] is updated, version as older then current version", msgKey));
        }
    }

    private FSMDocResult processDocument(FsmMsg msg, FsmDoc doc) {
        // Document exists
        // cases 3, 4, 5
        if (msg.getDocVersion().compareTo(doc.getDocCurrentVersion()) < 0) {
            // 3. Message version is a previous version of the document
            return new FSMDocResult(ResultCode.OUT_OF_SEQUENCE.getName(), "Message references an older version of the document");
        } else if (msg.getDocVersion().compareTo(doc.getDocCurrentVersion()) == 0) {
            // 4. message version is equal than document version
            if (thereIsResponse(doc.getState())) {
                return new FSMDocResult(ResultCode.ERROR.getName(), "There is a response for this version of the document");
            } else if (doc.getFsmDocReceivers().stream().noneMatch(r -> r.getReceiver().equals(msg.getReceiver()))) {
                FsmDocReceiver fsmDocReceiver = new FsmDocReceiver();
                fsmDocReceiver.setDocumentId(doc.getId());
                fsmDocReceiver.setReceiver(msg.getReceiver());
                fsmDocReceiverRepository.save(fsmDocReceiver);
                return new FSMDocResult(ResultCode.SUCCESS.getName(), "Same message but for another receiver");
            } else if (msg.getReprocessed()) {
                // Message is being reprocessed
                doc.setState(DocStateCode.PROCESSING.getName());
                fsmDocRepository.save(doc);
                return new FSMDocResult(ResultCode.SUCCESS.getName(), "Message reprocessed ???. Change state to processing");
            } else {
                return new FSMDocResult(ResultCode.ERROR.getName(), "Message with  existing key ans it's not being reprocessed. CHECK THIS CASE.");
            }
        } else {
            // 5. message version is newer than document current version
            if (msg.getMsgFunction().equals(FunctionCode.ORIGINAL.getName())) {
                // message with an original is not allowed
                return new FSMDocResult(ResultCode.OUT_OF_SEQUENCE.getName(), "Message with an original is not allowed");
            } else if (doc.getState().equals(DocStateCode.CANCELLED.getName())) {
                // Document already rejected
                return new FSMDocResult(ResultCode.CANCELLED.getName(), "Document already rejected");
            } else if (doc.getState().equals(DocStateCode.PROCESSING.getName())) {
                // Previous version is still processing
                msg.setState(MsgStateCode.ON_HOLD.getName());
                fsmMsgRepository.save(msg);
                return new FSMDocResult(ResultCode.ON_HOLD.getName(), "Document's previous version is still processing");
            } else {
                // update document with new version and carry on with msg processing
                doc.setDocCurrentVersion(msg.getDocVersion());
                doc.setState(DocStateCode.PROCESSING.getName());
                fsmDocRepository.save(doc);

                fsmDocReceiverRepository.deleteByDocumentId(doc.getId());
                FsmDocReceiver fsmDocReceiver = new FsmDocReceiver();
                fsmDocReceiver.setDocumentId(doc.getId());
                fsmDocReceiver.setReceiver(msg.getReceiver());
                fsmDocReceiverRepository.save(fsmDocReceiver);

                return new FSMDocResult(ResultCode.SUCCESS.getName(), "Update document with new version and carry on with msg processing");
            }
        }
    }

    private Boolean thereIsResponse(String state) {
        return (state.equals(DocStateCode.ACCEPTED.getName()) || state.equals(DocStateCode.REJECTED.getName()));
    }

    private FSMDocResult processNewDocument(FsmMsg msg, FsmDoc doc) {
        // Document does not exist
        // Step 2
        if (msg.getMsgFunction().equals(FunctionCode.ORIGINAL.getName())) {
            fsmDocRepository.save(doc);
            return new FSMDocResult(ResultCode.SUCCESS.getName(), "New document created");
        } else if (msg.getMsgFunction().equals(FunctionCode.REPLACEMENT.getName()) ||
                msg.getMsgFunction().equals(FunctionCode.CANCELLATION.getName())) {
            return new FSMDocResult(ResultCode.ON_HOLD.getName(), "Message it's a replacement or cancellation and no original message has been found. it must be put on hold");
        }
        return new FSMDocResult(ResultCode.UNKNOWN_DOCUMENT_FUNCTION.getName(), "Message function is unknown");
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

        FsmDoc newDoc = fsmDocRepository.save(fsmDoc);
        fsmDocReceiver.setDocumentId(newDoc.getId());
        fsmDocReceiverRepository.save(fsmDocReceiver);

        return newDoc;
    }


    private FSMDocResult doNotifyAperak(AperakDto aperakDto, FsmMsg originalMsg, FsmMsg msg) {
        msg.setState(MsgStateCode.PROCESSING.getName());
        fsmMsgRepository.save(msg);
        return new FSMDocResult(ResultCode.SUCCESS.getName(), String.format("prepareNotifyAperak: aperak is beeing processed, key:  originalMessageSender(%s), docType(%s), docNum(%s), docVersion(%s)",
                aperakDto.getReceiver(), aperakDto.getRefDocType(), aperakDto.getRefDocNum(), aperakDto.getRefDocVersion()));
    }


    private FSMDocResult doDeliverAperak(AperakDto aperakDto, FsmMsg originalMsg, FsmMsg aperakMsg) {
        String aperakFunc = aperakDto.getMsgFunction();
        String aperakType = aperakDto.getAperakType();
        aperakMsg.setState(MsgStateCode.DELIVERED.getName());
        // if it's an error aperak generated by the platform, then the original message should be marked as error.
        if (aperakType.equals(AperakFunctionCode.APERAK_ERROR.getName()) &&
                aperakFunc.equals(AperakFunctionCode.REJECTED.getName())) {
            originalMsg.setState(MsgStateCode.ERROR.getName());
        } else if (aperakType.equals(AperakFunctionCode.APERAK_RESPONSE.getName()) &&
                (aperakFunc.equals(AperakFunctionCode.ACKNOWLEDGED.getName()) ||
                        aperakFunc.equals(AperakFunctionCode.NOTIFIED.getName()))) {
            originalMsg.setState(MsgStateCode.CONFIRMED.getName());
        } else if (aperakType.equals(AperakFunctionCode.APERAK_RESPONSE.getName()) &&
                aperakFunc.equals(AperakFunctionCode.REJECTED.getName())) {
            originalMsg.setState(MsgStateCode.ACKNOWLEDGED.getName());
        } else if (aperakType.equals(AperakFunctionCode.APERAK_RESPONSE.getName()) &&
                aperakFunc.equals(AperakFunctionCode.ACCEPTED.getName())) {
            originalMsg.setState(MsgStateCode.ACKNOWLEDGED.getName());
        } else {
            originalMsg.setState(MsgStateCode.ERROR.getName());
        }
        fsmMsgRepository.save(aperakMsg);
        fsmMsgRepository.save(originalMsg);
        String docKey = makeDocKey(aperakDto.getReceiver(), aperakDto.getDocType(), aperakDto.getDocNum());
        return fsmDocRepository.findByKey(docKey)
                .map(fsmDoc -> processAperak(aperakMsg, originalMsg, fsmDoc, aperakType, aperakFunc))
                .orElseGet(() ->
                        new FSMDocResult(ResultCode.SUCCESS.getName(), String.format("doDeliverAperak: aperak delivered, keys: originalMsgSender(%s), aperakSender(%s), docType(%s), docNum(%s), docVersion(%s)",
                                aperakDto.getReceiver(), aperakDto.getSender(), aperakDto.getRefDocType(), aperakDto.getRefDocNum(), aperakDto.getRefDocVersion())
                        ));
    }

    private FSMDocResult processAperak(FsmMsg aperakMsg, FsmMsg originalMsg, FsmDoc fsmDoc, String aperakType, String aperakFunc) {
        fsmMsgRepository.save(originalMsg);
        fsmMsgRepository.save(aperakMsg);

        // TODO update Document state!!!

        return new FSMDocResult(ResultCode.SUCCESS.getName(), String.format("doDeliverAperak: aperak delivered, keys: originalMsgSender(%s), aperakSender(%s), msgKey(%s)",
                aperakMsg.getReceiver(), aperakMsg.getSender(), aperakMsg.getKey()));
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

    public enum AperakFunctionCode {
        // 963 + 27 => error RESPONSE
        // 962 + 27 => REJECTION RESPONSE
        // 962 + 44 => accepted response
        // 962 + 11 => ACK response
        APERAK_ERROR("963"),
        APERAK_RESPONSE("962"),
        REJECTED("27"),
        ACCEPTED("44"),
        NOTIFIED("55"),
        ACKNOWLEDGED("11");

        private final String name;

        private AperakFunctionCode(final String name) {
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
