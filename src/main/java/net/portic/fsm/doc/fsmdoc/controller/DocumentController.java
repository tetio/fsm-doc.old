package net.portic.fsm.doc.fsmdoc.controller;

import net.portic.fsm.doc.fsmdoc.controller.dto.AperakDto;
import net.portic.fsm.doc.fsmdoc.controller.dto.FSMDocResult;
import net.portic.fsm.doc.fsmdoc.controller.dto.MsgDto;
import net.portic.fsm.doc.fsmdoc.exception.ResourceNotFoundException;
import net.portic.fsm.doc.fsmdoc.model.FsmDoc;
import net.portic.fsm.doc.fsmdoc.model.FsmDocReceiver;
import net.portic.fsm.doc.fsmdoc.model.FsmMsg;
import net.portic.fsm.doc.fsmdoc.service.FsmServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("${spring.data.rest.base-path}")
public class DocumentController {

    @Autowired
    private FsmServices services;

    @GetMapping("/fsmdoc")
    public List<FsmDoc> findAllDocuments() {
        return services.findAllDocuments();
    }

    @GetMapping("/fsmdoc/{documentId}")
    public FsmDoc getDocument(@PathVariable Long documentId) {
        return services.getDocument(documentId);
    }

//    @GetMapping("/documents/key/{key}")
//    public Document getDocumentByKey(@PathVariable String key) {
//        return documentRepository.findOne(documentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id = "+documentId));
//    }

    @PostMapping("/msg/notify")
    public FSMDocResult notify(@RequestBody MsgDto msgDto) {

        return services.prepareNotify(msgDto);
    }

    @PostMapping("/msg/deliver")
    public FSMDocResult deliver(@RequestBody MsgDto msgDto) {

        return services.prepareDeliver(msgDto);
    }

    @PostMapping("/aperak/notify")
    public FSMDocResult notifyAperak(@RequestBody AperakDto aperakDto) {
        return services.prepareNotifyAperak(aperakDto);
    }

    @PostMapping("/aperak/deliver")
    public FSMDocResult deliverAperak(@RequestBody AperakDto aperakDto) {
        return services.prepareDeliverAperak(aperakDto);
    }
}
