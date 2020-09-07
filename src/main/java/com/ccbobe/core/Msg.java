package com.ccbobe.core;

import lombok.Data;

import java.io.Serializable;
@Data
public class Msg {

    /**
     * 命令code
     */
    private int cmd;

    /**
     * 消息大小
     */
    private int size;
    /**
     * 消息体
     */
    private byte[] data;
}
