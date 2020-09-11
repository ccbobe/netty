package com.ccbobe;

import com.alibaba.fastjson.JSON;
import com.ccbobe.codec.MessageDecoder;
import com.ccbobe.codec.MessageEncoder;
import com.ccbobe.core.Message;
import com.ccbobe.core.Msg;
import com.ccbobe.handler.ClientHandler;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
        for (int i = 0; i <2000 ; i++) {
            msg = new Message();
            System.out.println("次数"+i);
            msg.setVersion(i);
            msg.setData(("wordl-----aaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccccccccccccccc" +
                    "sssddsddssddssssssssssssssssssssssssssssdjfsdndnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn" +
                    "ddddd" +
                    "dddddddddddddddddd我我我我我我呜呜呜呜呜呜呜呜无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无" +
                    "我我我我我我呜呜呜呜呜呜呜呜无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无无" +
                    "" +
                    "少时诵诗书所" +
                    "少时诵诗书" +
                    "" +
                    "少时诵诗书所所" +
                    "少时诵诗书所" +
                    "少时诵诗书所").getBytes());
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


    @Test
    public void testSerial() throws Exception {// java 序列化
        Message msg = new Message();
        msg.setSize(1);
        msg.setCmd(1);
        msg.setId("1234567dssss");
        msg.setData("hollo world".getBytes());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream ops = new ObjectOutputStream(bos);
        ops.writeObject(msg);
        ops.flush();

        File file = new File("D:\\files\\oo.dat");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] bytes = bos.toByteArray();
        fileOutputStream.write(bytes);
        fileOutputStream.flush();

        fileOutputStream.close();
        ops.close();
        bos.close();
        System.out.println(bytes);

        FileInputStream fs = new FileInputStream(file);

        FileChannel fileChannel = fs.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileChannel.size());
        while ((fileChannel.read(byteBuffer)) > 0) {
            // do nothing
            // System.out.println("reading");
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer.array());

        ObjectInputStream obs = new ObjectInputStream(bis);

        Object object = obs.readObject();
        if (object instanceof Message) {
            Message message = (Message) object;
            System.out.println(new String(message.getData()));
        }
    }

    @Test
    public  void  servKryo() throws Exception{
        Kryo kryo = new Kryo();
        kryo.setAutoReset(true);
        kryo.setReferences(true);
        kryo.register(Message.class);
        Message message = new Message();
        message.setData("ha".getBytes());
        message.setId("ddddd");
        message.setVersion(23);
        Output output = new Output(new FileOutputStream("D:\\files\\oo.dat"));
        kryo.writeObject(output, message);
        byte[] buffer = output.getBuffer();
        output.close();

        Input input = new Input(new FileInputStream("D:\\files\\oo2.dat"));
        Message object2 = kryo.readObject(input, Message.class);

        System.out.println(JSON.toJSONString(object2));



        input.close();
    }


    @Test
    public  void  servKryoPool() throws Exception{
        byte[] data = new byte[8];
        int i = 1;
        byte b = 1;
        int bit = (int)((b>>2) & 0x1);
        System.out.println(bit);
         bit = (int)((b>>1)&(0xFF>>(8-5)));
        System.out.println(bit);


    }


    @Test
    public   void  testFile() throws Exception{
        int bufferSize = 20;//字节
        FileChannel src = new FileInputStream("D:\\files\\oo.dat").getChannel();
        FileChannel dest = new FileOutputStream("D:\\files\\oo2.dat").getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        int times = 0;//看读了几次
        while(src.read(buffer) != -1) {//一次读满缓存区，10个字节
            buffer.flip();
            dest.write(buffer);
            buffer.clear();
            System.out.println(++times);
        }
        System.out.println("ok");
    }





}
