package com.tensquare.notice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.tensquare.notice.netty.MyWebSocketHandler;
import entity.Result;
import entity.StatusCode;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import java.util.HashMap;

public class UserNoticeListener implements ChannelAwareMessageListener {

    private static ObjectMapper MAPPER = new ObjectMapper(); //json解析工具类

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        //通过队列名称获取用户ID
        String queueName = message.getMessageProperties().getConsumerQueue();
        String userId = queueName.substring(queueName.lastIndexOf("_") + 1);

        io.netty.channel.Channel wsChannel = MyWebSocketHandler.userChannelMap.get(userId);

        //判断用户是否在线
        if(wsChannel != null){ //如果连接不为空，表示用户在线
            //封装返回数据
            HashMap countMap = new HashMap<>();
            countMap.put("userNoticeCount", 1);
            Result result = new Result(true, StatusCode.OK, "查询成功", countMap);

            //把数据通过WebSocket连接主动推送给用户
            wsChannel.writeAndFlush(new TextWebSocketFrame(MAPPER.writeValueAsString(result)));
        }
    }
}
