package net.portic.fsm.doc.fsmdoc.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "fsm_doc")
public class FsmDoc extends AuditModel {
    @Id
    @GeneratedValue(generator = "fsm_doc_generator")
    @SequenceGenerator(
            name =  "fsm_doc_generator",
            sequenceName = "seq_fsm_doc",
            initialValue = 1000
    )
    private Long id;

    @Column(name = "key", unique = true)
    private String key;

    @Column(name = "tdoc_ype")
    private String docType;

    @Column(name = "doc_num")
    private String docNum;

    @Column(name = "doc_current_version")
    private String docCurrentVersion;

    @Column(name = "sender")
    private String sender;

    @Column(name = "state")
    private String state;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "fsm_doc_id")
    private List<FsmDocReceiver> fsmDocReceivers = new ArrayList<>();

//    history: DocumentHistory[]
//    rejectionErrors: string[]

}
