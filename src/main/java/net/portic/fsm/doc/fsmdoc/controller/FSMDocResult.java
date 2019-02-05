package net.portic.fsm.doc.fsmdoc.controller;

import java.util.Date;

public class FSMDocResult {
    String status;
    Date when;
    String info;

    FSMDocResult(String status, String info) {
        this.info = info;
        this.status = status;
        this.when = new Date();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
