package net.portic.fsm.doc.fsmdoc.controller.dto;

import lombok.*;

@Getter
@Setter
public class AperakDto extends MsgDto {
    String refDocNum;
    String refDocVersion;
    String refDocType;
    String aperakType;
}
