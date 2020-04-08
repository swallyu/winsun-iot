package com.winsun.iot.utils;

import com.winsun.iot.command.CmdMsg;

public interface MsgConsumer {
    default void before(CmdMsg cmdMsg){

    }

    default void after(CmdMsg cmdMsg){

    }
}
