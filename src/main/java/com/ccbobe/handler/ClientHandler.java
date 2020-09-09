package com.ccbobe.handler;

import com.alibaba.fastjson.JSON;
import com.ccbobe.core.Message;
import com.ccbobe.core.Msg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
      log.info("收到消息{}", JSON.toJSONString(msg));
    }
}
