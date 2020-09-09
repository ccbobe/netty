package com.ccbobe.codec;

import com.ccbobe.core.Message;
import com.ccbobe.core.Msg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author ccbobe
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        out.writeInt(msg.getSize());
        out.writeInt(msg.getCmd());
        out.writeBytes(msg.getId().getBytes());
        out.writeInt(msg.getVersion());
        out.writeBytes(msg.getData());
        out.writeBytes("$_".getBytes());
    }
}
