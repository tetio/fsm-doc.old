package net.portic.fsm.doc.fsmdoc.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "fsm_doc_receiver")
public class FsmDocReceiver extends AuditModel {
    @Id
    @GeneratedValue(generator = "doc_receiver_generator")
    @SequenceGenerator(
            name = "doc_receiver_generator",
            sequenceName = "seq_doc_receiver",
            initialValue = 1000
    )
    private Long id;

    @NotBlank
    @Size(min = 3, max = 32)
    private String receiver;

    @Column(name = "fsm_doc_id", nullable = false)
    private Long documentId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
}
