package net.portic.fsm.doc.fsmdoc.controller;

import net.portic.fsm.doc.fsmdoc.exception.ResourceNotFoundException;
import net.portic.fsm.doc.fsmdoc.model.FsmDoc;
import net.portic.fsm.doc.fsmdoc.model.FsmDocReceiver;
import net.portic.fsm.doc.fsmdoc.model.FsmMsg;
import net.portic.fsm.doc.fsmdoc.repository.FsmDocRepository;
import net.portic.fsm.doc.fsmdoc.repository.FsmMsgRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RestController
public class DocumentController {
    @Autowired
    private FsmDocRepository fsmDocRepository;

    @Autowired
    private FsmMsgRepository fsmMsgRepository;

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

    @Transactional
    private NotifyResult doNotify(MsgDto msgDto) {
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
        FsmDoc doc =  fsmDocRepository.findByKey(docKey)
                .map(fsmDoc ->
                     fsmDocRepository.save(fsmDoc)
                ).orElseGet(() -> newFsmDoc(msg, docKey));

        return processNewDocument(msg, doc);



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
        fsmDoc.setDocVersion(msg.getDocVersion());
        fsmDoc.setSender(msg.getSender());
        fsmDoc.setDocType(msg.getDocType());
        fsmDoc.setKey(docKey);
        fsmDoc.setState("PROCESSING");
        fsmDoc.setDocType(msg.getDocType());
        FsmDocReceiver fsmDocReceiver = new FsmDocReceiver();
        fsmDocReceiver.setReceiver(msg.getReceiver());
        List<FsmDocReceiver> lfdr = new ArrayList<FsmDocReceiver>();
        lfdr.add(fsmDocReceiver);
        fsmDoc.setFsmDocReceivers(lfdr);
        return fsmDocRepository.save(fsmDoc);
    }

    private String makeDocKey(String sender, String docType, String docNum) {
        return String.format("%s###%s###%s", sender, docType, docNum);
    }

    private String makeMsgKey(String trackId, String msgNum) {
        return String.format("%s###%s", trackId, msgNum);
    }
/*


    private  function processMsg(docKey: string, msgFunction: string, docNum: string, docVersion: string, sender: string, docType: string, message: Message, next: Function) {
        // Document processing
        // 1 -new message entry
        // get associated doc info.
        findDocumentByKey(docKey)
                .then((document: DocumentModel) => {
            if (!document) {
                // Document does not exist
                // Step 2
                processNewDocument(docKey, msgFunction, docNum, docVersion, sender, docType, message, next)
            } else {
                // Document exists
                // cases 3, 4, 5
                if (docVersion < document.currentVersion) {
                    // 3. Message version is a previous version of the document
                    next(null, makeResult(ResultCode.OUT_OF_SEQUENCE))
                } else if (docVersion === document.currentVersion) {
                    // 4. message version is equal than document version
                    if (thereIsResponse(document.state)) {
                        next(null, makeResult(ResultCode.ERROR))
                    } else if (document.receivers.indexOf(message.receiver) < 0) {
                        document.receivers.push(message.receiver)
                        saveDocument(prepareForUpdateDocument(document, docVersion, message.receiver), ResultCode.SUCCESS, next)
                    } else {
                        // ??? move to processing ???
                        next(null, makeResult(ResultCode.SUCCESS))
                    }
                } else {
                    // 5. document version is newer than document current version
                    if (msgFunction === FunctionCode.ORIGINAL) {
                        // message with an original is not allowed
                        next(null, makeResult(ResultCode.OUT_OF_SEQUENCE))
                    } else if (document.state === DocStateCode.CANCELLED) {
                        // Document already rejected
                        next(null, makeResult(ResultCode.CANCELLED))
                    } else if (document.state === DocStateCode.PROCESSING) {
                        // Previous version is still processing
                        next(null, makeResult(ResultCode.ON_HOLD))
                    } else {
                        // update document with new version and carry on with msg processing
                        saveDocument(prepareForUpdateDocument(document, docVersion, message.receiver), ResultCode.SUCCESS, next)
                    }
                }
            }
        })
    .catch((error) => next(error))
    }
*/


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
        ACCEPTED("ON_HOLD");

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
        CANCELLED("CANCELLED");

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
        CANCELLED("CANCELLED");

        private final String name;

        ResultCode(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
