package com.ccbobe.core;

import com.ccbobe.codec.*;
import com.ccbobe.handler.IntegerHandler;
import com.ccbobe.handler.MessageHandler;
import com.ccbobe.handler.StoreHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
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
                .option(ChannelOption.SO_BACKLOG,1024*1024)
                .childOption(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel sh) throws Exception {
                        ChannelPipeline pipeline =sh.pipeline();
                        //分割符 \n,\r\n 等
                        //这里使用自定义分隔符
                        pipeline.addLast(new DelimiterBasedFrameDecoder(65536, Unpooled.copiedBuffer("$_".getBytes())));
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageEncoder());
                        pipeline.addLast(new StoreHandler());
                        pipeline.addLast(new MessageHandler());

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
