package com.tensquare.notice.config;

//import com.rabbitmq.client.ConnectionFactory; //错误的包
import org.springframework.amqp.rabbit.connection.ConnectionFactory; //正确的包
import com.tensquare.notice.listener.SysNoticeListener;
import com.tensquare.notice.listener.UserNoticeListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean("sysNoticeContainer")
    //注意，此处的参数类型应该是org.springframework.amqp.rabbit.connection.ConnectionFactory而不是com.rabbitmq.client.ConnectionFactory，不然会报错
    public SimpleMessageListenerContainer createSys(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        //使用Channel进行监听
        container.setExposeListenerChannel(true);
        //设置自己的监听器
        container.setMessageListener(new SysNoticeListener());

        return container;
    }

    @Bean("userNoticeContainer")
    public SimpleMessageListenerContainer createUser(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        //使用Channel进行监听
        container.setExposeListenerChannel(true);
        //设置自己的监听器
        container.setMessageListener(new UserNoticeListener());

        return container;
    }
}
