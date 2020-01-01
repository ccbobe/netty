package com.ccbobe.handler;

import com.ccbobe.core.ClientStore;
import com.ccbobe.utils.ApplicationContextUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
/**
 * @author ccbobe
 */
@Slf4j
public class StoreHandler extends ChannelInboundHandlerAdapter {

    ClientStore clientStore = ApplicationContextUtils.getBean(ClientStore.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        clientStore.add(ctx.channel().remoteAddress().toString(),ctx.channel());
        log.info("当前保存客户端信息{}",ctx.channel().remoteAddress().toString());
        ctx.fireChannelActive();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        clientStore.removed(ctx.channel().remoteAddress().toString());
    }
}
