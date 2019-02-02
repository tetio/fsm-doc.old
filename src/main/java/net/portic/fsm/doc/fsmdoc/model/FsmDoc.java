package net.portic.fsm.doc.fsmdoc.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "doc_version")
    private String docVersion;

    @Column(name = "sender")
    private String sender;

    @Column(name = "state")
    private String state;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id")
    private List<FsmDocReceiver> fsmDocReceivers = new ArrayList<>();

//    history: DocumentHistory[]
//    rejectionErrors: string[]


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocNum() {
        return docNum;
    }

    public void setDocNum(String docNum) {
        this.docNum = docNum;
    }

    public String getDocVersion() {
        return docVersion;
    }

    public void setDocVersion(String docVersion) {
        this.docVersion = docVersion;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<FsmDocReceiver> getFsmDocReceivers() {
        return fsmDocReceivers;
    }

    public void setFsmDocReceivers(List<FsmDocReceiver> fsmDocReceivers) {
        this.fsmDocReceivers = fsmDocReceivers;
    }
}
