package com.winsun.iot.command;

public interface CmdHandler {

    void execute(String topic,CmdMsg data);
}
