package net.portic.fsm.doc.fsmdoc.model;

import javax.persistence.*;
import java.util.Date;

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

    public String getMsgFunction() {
        return msgFunction;
    }

    public void setMsgFunction(String msgFunction) {
        this.msgFunction = msgFunction;
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

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(String msgNum) {
        this.msgNum = msgNum;
    }

    public Date getMsgDate() {
        return msgDate;
    }

    public void setMsgDate(Date msgDate) {
        this.msgDate = msgDate;
    }

    public Boolean getReprocessed() {
        return reprocessed;
    }

    public void setReprocessed(Boolean reprocessed) {
        this.reprocessed = reprocessed;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
