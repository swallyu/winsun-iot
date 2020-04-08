package com.winsun.iot.command;

import com.winsun.iot.ruleengine.CmdRule;

import java.util.List;

public interface CmdCallback {

    void complete(String bizId, CmdRule cmdMsg);
}
