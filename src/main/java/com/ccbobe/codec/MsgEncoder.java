package com.ccbobe.codec;

import com.ccbobe.core.Msg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgEncoder extends MessageToByteEncoder<Msg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Msg msg, ByteBuf out) throws Exception {
        int cmd = msg.getCmd(); // 1
        out.writeInt(cmd);
        byte[] data = msg.getData();
        msg.setSize(data.length);
        out.writeInt(msg.getSize());// 2
        out.writeBytes(data); // 3
    }
}
