package com.ccbobe.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ccbobe
 */
@Slf4j
public class IntegerHandler  extends SimpleChannelInboundHandler<Integer> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Integer msg) throws Exception {
      log.info("收到消息{}",msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常信息{}",cause);
        ctx.close();
    }
}
