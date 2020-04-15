package com.winsun.iot.device;

import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.utils.MsgConsumer;

import java.util.function.Consumer;

public interface CommandServer {
    void receive(CmdMsg cmdMsg);
    void start();
    void setReceiveMsgConsumer(MsgConsumer cmdMsg);

    boolean isconnect();
}
