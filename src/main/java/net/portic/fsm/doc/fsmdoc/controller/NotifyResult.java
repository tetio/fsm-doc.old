package net.portic.fsm.doc.fsmdoc.controller;

import java.util.Date;

public class NotifyResult {
    String status;
    Date when;
    String info;

    NotifyResult(String status, String info) {
        this.info = info;
        this.status = status;
        this.when = new Date();
    }
}
