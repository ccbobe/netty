package com.ccbobe;

import com.alibaba.fastjson.JSON;
import com.ccbobe.codec.MessageDecoder;
import com.ccbobe.codec.MessageEncoder;
import com.ccbobe.codec.MsgDecoder;
import com.ccbobe.codec.MsgEncoder;
import com.ccbobe.core.Message;
import com.ccbobe.core.Msg;
import com.ccbobe.handler.ClientHandler;
import com.ccbobe.handler.MessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

class NettyApplicationTests {

    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)
                // 使用NioSocketChannel来作为连接用的channel类
                .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("正在连接中...");
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        //这里使用自定义分隔符
                        pipeline.addLast(new DelimiterBasedFrameDecoder(65536, Unpooled.copiedBuffer("$_".getBytes())));

                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageEncoder());
                        pipeline.addLast(new ClientHandler());

                    }
                });



        // 发起异步连接请求，绑定连接端口和host信息
        Channel channel = b.connect("172.18.0.68", 8081).sync().channel();
        Message msg = null;
        for (int i = 0; i <1000000 ; i++) {
            msg = new Message();
            System.out.println("次数"+i);
            msg.setVersion(i);
            msg.setData("wordl-----".getBytes());
            msg.setId(UUID.randomUUID().toString().replace("-","").toUpperCase());
            msg.setCmd(1);
            msg.setSize(msg.getData().length);
            channel.writeAndFlush(msg);
            Thread.sleep(1);
        }
        ChannelFuture future = channel.closeFuture();
        future.await();

    }


    @Test
    public void testParse(){
        String data = "{\"cmd\":1,\"data\":\"aGVsbG8gd29ybGQ=\",\"size\":11}";
        Msg msg = JSON.parseObject(data, Msg.class);

        System.out.println(new String(msg.getData()));
    }


}
