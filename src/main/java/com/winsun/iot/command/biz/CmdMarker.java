package com.winsun.iot.command.biz;

import com.alibaba.fastjson.JSONObject;

public class CmdMarker {
    private String sig;
    private JSONObject data;

    private JSONObject cmd;

    public CmdMarker(String sig, JSONObject cmd) {
        this.sig = sig;
        this.cmd = cmd;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public JSONObject getCmd() {
        return cmd;
    }

    public void setCmd(JSONObject cmd) {
        this.cmd = cmd;
    }
}
