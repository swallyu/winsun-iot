package com.winsun.iot.domain;

public class CmdResult<T> {
    private int code;
    private boolean result;
    private String msg;
    private T data;

    public CmdResult(int code, boolean result, String msg) {
        this.code = code;
        this.result = result;
        this.msg = msg;
    }

    public CmdResult(int code, boolean result, String msg, T data) {
        this.code = code;
        this.result = result;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
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
