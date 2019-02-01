package net.portic.fsm.doc.fsmdoc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "document_receiver")
public class Receiver extends AuditModel {
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Document document;


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

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
