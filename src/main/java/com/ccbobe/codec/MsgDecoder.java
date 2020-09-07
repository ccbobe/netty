package com.ccbobe.codec;

import com.ccbobe.core.Msg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

public class MsgDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        int cmd = in.readInt();
        int size = in.readInt();
        if (in.readableBytes() < size) {
            //消息不完整，无法处理，将readerIndex复位
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[size];
        in.readBytes(data);
        Msg msg = new Msg();
        msg.setCmd(cmd);
        msg.setSize(size);
        msg.setData(data);
        out.add(msg);
    }
}
