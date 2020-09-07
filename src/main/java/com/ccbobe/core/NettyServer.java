package com.ccbobe.core;

import com.ccbobe.codec.IntegerDecoder;
import com.ccbobe.codec.IntegerEncoder;
import com.ccbobe.codec.MsgDecoder;
import com.ccbobe.codec.MsgEncoder;
import com.ccbobe.handler.IntegerHandler;
import com.ccbobe.handler.MessageHandler;
import com.ccbobe.handler.StoreHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author ccbobe
 */
@Order(1)
@Slf4j
@Component
public class NettyServer implements InitializingBean, DisposableBean {

    private EventLoopGroup accept = null;
    private EventLoopGroup worker = null;

    @Override
    public void destroy() throws Exception {
        log.info("netty server destroy.....");
        accept.shutdownGracefully();
        worker.shutdownGracefully();
    }

    private void init(Integer port){
        boolean nioFlag = true;
        //
        if (Epoll.isAvailable()){
            log.info("server start Epoll ...");
            accept = new EpollEventLoopGroup(1);
            worker = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors());
            nioFlag = false;
        }
        if (KQueue.isAvailable()){
            log.info("server start Kqueue ...");
            accept = new KQueueEventLoopGroup(1);
            worker = new KQueueEventLoopGroup(Runtime.getRuntime().availableProcessors());
            nioFlag = false;
        }
        if (nioFlag){
            log.info("server start Nio ...");
            accept = new NioEventLoopGroup(3);
            worker = new NioEventLoopGroup(10);
        }

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(accept,worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childOption(ChannelOption.TCP_NODELAY,true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel sh) throws Exception {
                        ChannelPipeline pipeline =sh.pipeline();
                        //分割符 \n,\r\n 等

                        // pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));
                        //  pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));
                        pipeline.addLast(new MsgDecoder());
                        pipeline.addLast("logging", new LoggingHandler(LogLevel.INFO));
                        pipeline.addLast("heartBeatHandler", new IdleStateHandler(45, 0, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new StoreHandler());
                        pipeline.addLast(new MessageHandler());
                        pipeline.addLast(new MsgEncoder());

                    }
                });

        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("netty binding {}",e.getLocalizedMessage());
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("netty server start.....");
        new Thread(){
            @Override
            public void run() {
                init(8081);
            }
        }.start();

    }
}
