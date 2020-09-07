package com.ccbobe;

import com.alibaba.fastjson.JSON;
import com.ccbobe.codec.MsgDecoder;
import com.ccbobe.codec.MsgEncoder;
import com.ccbobe.core.Msg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

class NettyApplicationTests {

    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)  // 使用NioSocketChannel来作为连接用的channel类
                .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("正在连接中...");
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MsgDecoder());
                        pipeline.addLast(new MsgEncoder());

                    }
                });

        Msg msg = new Msg();
        msg.setCmd(4);
        String data = "hello world";
        msg.setData(data.getBytes());
        msg.setSize(data.length());
        // 发起异步连接请求，绑定连接端口和host信息
        final ChannelFuture future = b.connect("172.18.0.68", 8081).sync().channel().writeAndFlush(msg);
        future.await();

    }


    @Test
    public void testParse(){
        String data = "{\"cmd\":1,\"data\":\"aGVsbG8gd29ybGQ=\",\"size\":11}";
        Msg msg = JSON.parseObject(data, Msg.class);

        System.out.println(new String(msg.getData()));
    }


}
