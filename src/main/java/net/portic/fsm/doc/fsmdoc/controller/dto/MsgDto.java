package net.portic.fsm.doc.fsmdoc.controller.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
public class MsgDto {
    String msgFunction;
    String docNum;
    String docVersion;
    String sender;
    String docType;
    String receiver;
    String trackId;
    String msgNum;
    Date when;
}