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

    @Column(name = "type")
    private String type;

    @Column(name = "version")
    private String version;

    @Column(name = "state")
    private String state;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id")
    private List<FsmDocReceiver> fsmDocReceivers = new ArrayList<>();

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
