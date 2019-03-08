package net.portic.fsm.doc.fsmdoc.controller;

public class AperakDto extends MsgDto {
    String refDocNum;
    String refDocVersion;
    String refDocType;
    String aperakType;

    public String getRefDocNum() {
        return refDocNum;
    }

    public void setRefDocNum(String refDocNum) {
        this.refDocNum = refDocNum;
    }

    public String getRefDocVersion() {
        return refDocVersion;
    }

    public void setRefDocVersion(String refDocVersion) {
        this.refDocVersion = refDocVersion;
    }

    public String getRefDocType() {
        return refDocType;
    }

    public void setRefDocType(String refDocType) {
        this.refDocType = refDocType;
    }

    public String getAperakType() {
        return aperakType;
    }

    public void setAperakType(String aperakType) {
        this.aperakType = aperakType;
    }
}
