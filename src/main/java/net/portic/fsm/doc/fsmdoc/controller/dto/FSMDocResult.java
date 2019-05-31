package net.portic.fsm.doc.fsmdoc.controller.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
public class FSMDocResult {
    String status;
    Date when;
    String info;

    public FSMDocResult(String status, String info) {
        this.info = info;
        this.status = status;
        this.when = new Date();
    }
}
