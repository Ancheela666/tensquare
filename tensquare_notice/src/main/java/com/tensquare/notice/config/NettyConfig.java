package com.tensquare.notice.config;

import com.tensquare.notice.netty.NettyServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyConfig {

    @Bean
    public NettyServer createNettyServer(){
        NettyServer nettyServer = new NettyServer();
        //使用新的线程启动Netty服务
        new Thread(){
            @Override
            public void run() {
                nettyServer.start(1234);
            }
        }.start();
        return nettyServer;
    }
}
