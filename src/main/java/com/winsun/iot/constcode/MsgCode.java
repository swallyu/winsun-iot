package com.winsun.iot.constcode;

public final class MsgCode {

    public static int CODE_SUCCESS = 0;
    public static int CODE_FAIL = -1;

    //分为两段
    //类型(两位)，错误码（三位）
    //类型 100 设备
    /**
     * 设备已经存在
     */
    public static int DEVICE_EXITS=10_001; //
    public static int DEVICE_NOT_EXITS=10_002; //
}
