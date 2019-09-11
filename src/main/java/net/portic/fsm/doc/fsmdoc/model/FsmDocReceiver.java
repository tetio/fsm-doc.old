package net.portic.fsm.doc.fsmdoc.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Entity
@Table(name = "fsm_doc_receiver")
public class FsmDocReceiver extends AuditModel {
    @Id
    @GeneratedValue(generator = "doc_receiver_generator")
    @SequenceGenerator(
            name = "doc_receiver_generator",
            sequenceName = "seq_fsm_doc_receiver",
            initialValue = 1000
    )
    private Long id;

    @NotBlank
    @Size(min = 3, max = 32)
    private String receiver;

    @Column(name = "fsm_doc_id", nullable = false)
    private Long documentId;

}
