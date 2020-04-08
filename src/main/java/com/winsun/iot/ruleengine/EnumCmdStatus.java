package com.winsun.iot.ruleengine;

public enum EnumCmdStatus {
    UNKNOWN(-1),
    Stage_0(0),
    Stage_1(1),
    Stage_2(2),
    ;
    private int code;

    EnumCmdStatus(int value) {
        this.code = value;
    }

    public int getCode() {
        return code;
    }

    public static EnumCmdStatus parseOf(int value) {
        switch (value) {
            case 0:
                return Stage_0;
            case 1:
                return Stage_1;
            case 2:
                return Stage_2;
        }
        return UNKNOWN;
    }
}
