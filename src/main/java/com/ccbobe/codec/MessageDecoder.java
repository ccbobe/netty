package com.ccbobe.codec;

import com.ccbobe.core.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author  ccbobe
 */
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Message message = new Message();
        if (in.readableBytes() < 4) {
            in.resetWriterIndex();
            return;
        }
        in.markReaderIndex();
        //4byte
        int size = in.readInt();
        //4byte
        int cmd = in.readInt();
        //32byte
        ByteBuf id = in.readBytes(32);
        int version = in.readInt();
        int len =  in.readableBytes();
        byte[] data = new byte[len];
        in.readBytes(data);
        message.setCmd(cmd);
        message.setSize(size);
        message.setId(new String(id.toString(Charset.forName("UTF-8"))));
        message.setVersion(version);
        message.setData(data);
        out.add(message);
    }
}
