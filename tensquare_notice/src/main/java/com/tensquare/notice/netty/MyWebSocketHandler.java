package com.tensquare.notice.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tensquare.notice.config.ApplicationContextProvider;
import entity.Result;
import entity.StatusCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static ObjectMapper MAPPER = new ObjectMapper(); //json解析工具类

    //从Spring容器中获取消息监听器容器，处理订阅消息sysNotice
    SimpleMessageListenerContainer sysNoticeContainer =
            (SimpleMessageListenerContainer) ApplicationContextProvider.getApplicationContext().getBean("sysNoticeContainer");

    //从Spring容器中获取消息监听器容器，处理点赞消息userNotice
    SimpleMessageListenerContainer userNoticeContainer =
            (SimpleMessageListenerContainer) ApplicationContextProvider.getApplicationContext().getBean("userNoticeContainer");

    //从Spring容器中获取RabbitTemplate
    RabbitTemplate rabbitTemplate = ApplicationContextProvider.getApplicationContext().getBean(RabbitTemplate.class);

    //存放WebSocket连接map，使用用户ID存放
    //ConcurrentHashMap相比于一般的HashMap，可以保证线程安全
    public static ConcurrentHashMap<String, Channel> userChannelMap = new ConcurrentHashMap<>();

    //用户请求WebSocket服务端时执行的方法
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //约定第一次请求携带的数据为：{"userId": "1"}
        //获取用户请求数据并解析
        String json = msg.text();
        //解析json数据，获取用户ID
        String userId = MAPPER.readTree(json).get("userId").asText();
        //第一次请求时，需要建立WebSocket连接
        Channel channel = userChannelMap.get(userId);
        if(channel == null) {
            channel = ctx.channel(); //获取WebSocket的连接
            userChannelMap.put(userId, channel); //把连接放到容器中
        }

        //获取RabbitMQ的消息内容，并发送给用户
        //只用完成新消息的题型即可，因此只需要获取消息的数量
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate);
        String queueName = "article_subscribe_" + userId; //拼接获取队列名称
        Properties queueProperties = rabbitAdmin.getQueueProperties(queueName); //获取RabbitMQ的Properties容器
        int noticeCount = 0;
        if(queueProperties != null) {
            noticeCount = (int) queueProperties.get("QUEUE_MESSAGE_COUNT");
        }
        //以上获取订阅类消息，以下获取点赞类消息
        String userQueueName = "article_thumbup_" + userId; //拼接获取队列名称
        Properties userQueueProperties = rabbitAdmin.getQueueProperties(userQueueName); //获取RabbitMQ的Properties容器
        int userNoticeCount = 0;
        if(userQueueProperties != null) {
            userNoticeCount = (int) queueProperties.get("QUEUE_MESSAGE_COUNT");
        }

        //封装返回的数据
        HashMap countMap = new HashMap();
        countMap.put("sysNoticeCount", noticeCount); //订阅类消息数量
        countMap.put("userNoticeCount", userNoticeCount); //点赞类消息数量
        Result result = new Result(true, StatusCode.OK, "查询成功", countMap);

        //把数据传给用户
        channel.writeAndFlush(new TextWebSocketFrame(MAPPER.writeValueAsString(result)));

        //把消息从队列中清空，否则MQ消息监听器会再见把消息消费一次
        if(noticeCount > 0){
            rabbitAdmin.purgeQueue(queueName,true);
        }
        if(userNoticeCount > 0){
            rabbitAdmin.purgeQueue(userQueueName,true);
        }

        //为用户的消息通知队列注册监听器，便于用户在线的时候，
        //一旦有消息，可以主动推送给用户，不需要用户请求服务器获取数据
        sysNoticeContainer.addQueueNames(queueName);
        userNoticeContainer.addQueueNames(userQueueName);
    }
}
