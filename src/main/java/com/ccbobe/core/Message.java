package com.ccbobe.core;

import lombok.Data;

@Data
public class Message {

    /**
     * 消息大小 4
     */
    private int size;
    /**
     * 命令类型 4
     */
    private int cmd;

    /**
     * 消息ID 32
     */
    private String id;
    /**
     * 消息版本 4
     */
    private int version;

    /**
     * 消息体
     */
    private byte[] data;
}
