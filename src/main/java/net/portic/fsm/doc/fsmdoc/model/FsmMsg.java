package net.portic.fsm.doc.fsmdoc.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "fsm_msg")
public class FsmMsg extends AuditModel{
    @Id
    @GeneratedValue(generator = "fsm_msg_generator")
    @SequenceGenerator(
            name =  "fsm_msg_generator",
            sequenceName = "seq_fsm_msg",
            initialValue = 1000
    )
    private Long id;

    @Column(name = "key", unique = true)
    private String key;

    @Column(name = "msg_function")
    private String msgFunction;

    @Column(name = "doc_num")
    private String docNum;

    @Column(name = "doc_version")
    private String docVersion;

    @Column(name = "sender")
    private String sender;

    @Column(name = "doc_type")
    private String docType;

    @Column(name = "receiver")
    private String receiver;

    @Column(name = "track_id")
    private String trackId;

    @Column(name = "msg_num")
    private String msgNum;

    @Column(name = "msgDate")
    private Date msgDate;

    @Column(name = "state")
    private String state;

    @Column(name = "reprocessed")
    private Boolean reprocessed = false;

}
