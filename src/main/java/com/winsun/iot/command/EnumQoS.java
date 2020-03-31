package com.winsun.iot.command;

public enum EnumQoS {
    Once(0),
    AtleastOnce(1),
    ExtractOnce(2),

    ;
    private int code;

    EnumQoS(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static EnumQoS valueOf(int code){
        switch (code){
            case 0:
                return Once;
            case 1:
                return AtleastOnce;
            case 2:
                return ExtractOnce;
        }
        return null;
    }
}
