package com.ccbobe.core;

import com.ccbobe.codec.IntegerDecoder;
import com.ccbobe.codec.IntegerEncoder;
import com.ccbobe.handler.IntegerHandler;
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
            accept = new NioEventLoopGroup(1);
            worker = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
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
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024*10*10,Delimiters.lineDelimiter()));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast("logging",new LoggingHandler(LogLevel.INFO));

                        pipeline.addLast(new StoreHandler());
                        //编解码Integer
                        pipeline.addLast(new IntegerEncoder());
                        pipeline.addLast(new IntegerDecoder());
                        pipeline.addLast(new IntegerHandler());
                        pipeline.addLast("idle",new IdleStateHandler(1,1,
                                0));
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
