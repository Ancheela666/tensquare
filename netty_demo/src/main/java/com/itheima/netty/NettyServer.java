package com.itheima.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class NettyServer {
    public static void main(String[] args) {
        // 用于接受客户端的连接以及为已接受的连接创建子通道，一般用于服务端。
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        // EventLoopGroup包含有多个EventLoop的实例，用来管理event Loop的组件
        // 接受新连接线程
        NioEventLoopGroup boos = new NioEventLoopGroup();
        // 读取数据的线程
        NioEventLoopGroup worker = new NioEventLoopGroup();

        //服务端执行
        serverBootstrap
                .group(boos, worker)
                // Channel对网络套接字的I/O操作，
                // 例如读、写、连接、绑定等操作进行适配和封装的组件。
                .channel(NioServerSocketChannel.class)
                // ChannelInitializer用于对刚创建的channel进行初始化
                // 将ChannelHandler添加到channel的ChannelPipeline处理链路中。
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {
                        // 组件从流水线头部进入，流水线上的工人按顺序对组件进行加工
                        // 流水线相当于ChannelPipeline
                        // 流水线工人相当于ChannelHandler
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            //这个工人有点麻烦，需要我们告诉他干啥事
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                System.out.println(msg);
                            }
                        });
                    }
                })
                .bind(8000);
    }
}