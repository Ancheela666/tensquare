package com.tensquare.notice.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class NettyServer {

    public void start(int port){
        System.out.println("准备启动Netty----------");
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        EventLoopGroup boos = new NioEventLoopGroup(); //用来处理新连接
        EventLoopGroup worker = new NioEventLoopGroup(); //用来处理业务逻辑，主要是读写等操作

        serverBootstrap.group(boos, worker)
                .localAddress(port)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        //给流水线添加“工人”
                        channel.pipeline().addLast(new HttpServerCodec()); //请求消息解码器
                        channel.pipeline().addLast(new HttpObjectAggregator(65536)); //将多个消息转换为单一的request或者response对象
                        channel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws")); //处理WebSocket的消息事件
                        //创建自己的WebSocket处理器，用来编写业务逻辑
                        MyWebSocketHandler myWebSocketHandler = new MyWebSocketHandler();
                        channel.pipeline().addLast(myWebSocketHandler);
                    }
                }).bind();
    }
}
