package net.portic.fsm.doc.fsmdoc.controller;

import net.portic.fsm.doc.fsmdoc.exception.ResourceNotFoundException;
import net.portic.fsm.doc.fsmdoc.model.Document;
import net.portic.fsm.doc.fsmdoc.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DocumentController {
    @Autowired
    private DocumentRepository documentRepository;

    @GetMapping("/documents")
    public List<Document> getDocuments() {
        return documentRepository.findAll();
    }

    @GetMapping("/documents/{documentId}")
    public Document getDocument(@PathVariable Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id = "+documentId));
    }

//    @GetMapping("/documents/key/{key}")
//    public Document getDocumentByKey(@PathVariable String key) {
//        return documentRepository.findOne(documentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id = "+documentId));
//    }
}
