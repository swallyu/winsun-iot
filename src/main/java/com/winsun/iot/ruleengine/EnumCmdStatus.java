package com.winsun.iot.ruleengine;

public enum EnumCmdStatus {
    UNKNOWN("unknown"),
    Send("send"),
    Ack("ack"),
    AckConfirm("ack_confirm"),
    Complete("complete"),
    Fail("fail"),

    ;
    private String value;

    EnumCmdStatus(String value) {
        this.value = value;
    }

    EnumCmdStatus parseOf(String value) {
        switch (value) {
            case "send":
                return Send;
            case "ack":
                return Ack;
            case "ack_confirm":
                return AckConfirm;
            case "complete":
                return Complete;
            case "fail":
                return Fail;
        }
        return null;
    }
}
