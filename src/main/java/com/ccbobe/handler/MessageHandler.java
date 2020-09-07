package com.ccbobe.handler;

import com.alibaba.fastjson.JSON;
import com.ccbobe.core.Msg;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MessageHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof String){
            log.info("收到消息{}",msg.toString());
            ReferenceCountUtil.release(msg);
        }else if (msg instanceof Msg){
          log.info("msg===>:{}", JSON.toJSONString(msg));
            System.out.println(new String(((Msg) msg).getData()));
            ReferenceCountUtil.release(msg);
        }else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("error");
        cause.printStackTrace();
        ctx.close();
    }
}
