package com.winsun.iot.utils;

import static com.winsun.iot.constcode.MsgCode.CODE_FAIL;
import static com.winsun.iot.constcode.MsgCode.CODE_SUCCESS;

public class BizResult<T> {
    private int code;
    private String msg;
    private T data;


    public static <T> BizResult<T> Success(String msg){
        return new BizResult<>(CODE_SUCCESS,msg,null);
    }


    public static <T> BizResult<T> Fail(String msg, T data) {
        return new BizResult<>(CODE_FAIL,msg,data);
    }

    public static <T> BizResult<T> Fail(T data){
        return new BizResult<>(CODE_FAIL,"fail",data);
    }


    public static <T> BizResult<T> Success(String msg,T data){
        return new BizResult<>(CODE_SUCCESS,msg,data);
    }

    public static <T> BizResult<T> Success(T data){
        return new BizResult<>(CODE_SUCCESS,"ok",data);
    }

    public BizResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public BizResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
