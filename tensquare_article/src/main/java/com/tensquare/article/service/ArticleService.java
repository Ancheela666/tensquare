package com.tensquare.article.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.tensquare.article.client.NoticeClient;
import com.tensquare.article.dao.ArticleDao;
import com.tensquare.article.pojo.Article;
import com.tensquare.article.pojo.Notice;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import util.IdWorker;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ArticleService {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private NoticeClient noticeClient;

    @Autowired
//    private RedisTemplate redisTemplate; //这样存入的key和value会乱码
    private StringRedisTemplate redisTemplate; //用StringRedisTemplate类型，这样不会乱码

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public List<Article> findAll() {
        return articleDao.selectList(null);
    }

    public Article findById(String articleId) {
        return articleDao.selectById(articleId);
    }

    public void save(Article article) {
        //使用分布式ID生成器
        String id = idWorker.nextId() + "";
        article.setId(id);
        //初始化数据
        article.setVisits(0); //浏览量
        article.setThumbup(0); //点赞数
        article.setComment(0); //评论数
        //新增
        articleDao.insert(article);
        //新增文章后，创建消息，通知给订阅者
        //TODO: 使用jwt鉴权获取当前用户的信息，用户ID，也就是文章作者ID
        String authorId = article.getUserid();
        //存放作者的订阅者信息的集合key，里面存放订阅者ID
        String authorKey = "article_author_" + authorId;
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
        Set<String> set = redisTemplate.boundSetOps(authorKey).members();
        if(null != set && set.size() > 0){
            Notice notice = null;
            for (String uid : set) {
                //创建消息对象
                //消息通知
                notice = new Notice();
                notice.setReceiverId(uid);
                notice.setOperatorId(authorId);
                notice.setAction("publish");
                notice.setTargetType("article");
                notice.setTargetId(id);
                notice.setCreatetime(new Date());
                notice.setType("sys");
                notice.setState("0");

                noticeClient.save(notice);
            }
        }
        //发消息给RabbitMQ，也就是新消息的通知
        //参数：1. 交换机名（使用之前完成的订阅功能的路由键）；2. 路由键；3. 消息内容
        rabbitTemplate.convertAndSend("article_subscribe", authorId, id);

    }

    public void updateById(Article article) {
        // 1. 根据主键ID修改
//        articleDao.updateById(article);

        // 2. 根据条件修改
        EntityWrapper<Article> wrapper = new EntityWrapper<>(); //创建条件对象
        wrapper.eq("id", article.getId()); // 设置条件
        articleDao.update(article, wrapper);
    }

    public void deleteById(String articleId) {
        articleDao.deleteById(articleId);
    }

    public Page<Article> findByPage(Integer page, Integer size, Map<String, Object> map) {
        //设置查询条件
        EntityWrapper<Article> wrapper = new EntityWrapper<>();
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
//            if(map.get(key) != null){
//                wrapper.eq(key, map.get(key));
//            }
            wrapper.eq(map.get(key) != null, key, map.get(key)); //和上面的if判断语句效果相同
        }
        //设置分页参数
        Page<Article> pageData = new Page<>(page, size);
        //执行查询
        List<Article> list = articleDao.selectPage(pageData, wrapper);
        pageData.setRecords(list);
        //返回
        return pageData;
    }

    public Boolean subscribe(Object articleId, Object userId) {
        //根据文章ID查询文章作者ID
        String authorId = articleDao.selectById((String) articleId).getUserid();

        //1. 创建RabbitMQ管理器
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate.getConnectionFactory());

        //2. 声明Direct类型的交换机（exchange），处理新增文章消息
        DirectExchange exchange = new DirectExchange("article_subscribe");
        rabbitAdmin.declareExchange(exchange);

        //3. 创建队列（queue），每个用户都有自己的队列，通过用户ID进行区分
        Queue queue = new Queue("article_subscribe_" + userId, true);

        //4. 声明exchange和queue的绑定关系，需要确保每个队列都只收到对应作者的新增文章消息。设置路由键(Routing Key)为作者ID，通过路由键进行对作者的绑定
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(authorId);

        //存放用户订阅信息的集合key，里面存放作者ID
        String userKey = "article_subscribe_" + (String) userId;

        //存放作者的订阅者信息的集合key，里面存放订阅者ID
        String authorKey = "article_author_" + authorId;

        //查询用户的订阅关系，看之前是否订阅了该作者
        Boolean flag = redisTemplate.boundSetOps(userKey).isMember(authorId);
        if(flag == true){ //如果订阅了作者，则取消订阅
            //在用户订阅信息的集合中删除，删除订阅的作者
            redisTemplate.boundSetOps(userKey).remove(authorId);
            //在作者订阅者信息的集合中，删除订阅者
            redisTemplate.boundSetOps(authorKey).remove((String) userId);
            //如果取消订阅，则删除队列的绑定关系
            rabbitAdmin.removeBinding(binding);
            //返回false
            return false;
        } else { //如果没有订阅作者，则进行订阅
            //在用户订阅信息的集合中添加订阅的作者
            redisTemplate.boundSetOps(userKey).add(authorId);
            //在作者订阅者信息的集合中添加订阅者
            redisTemplate.boundSetOps(authorKey).add((String) userId);
            //声明要绑定的队列
            rabbitAdmin.declareQueue(queue);
            //如果订阅，则添加绑定关系
            rabbitAdmin.declareBinding(binding);
            //返回true
            return true;
        }
    }

    public void thumbup(String articleId,String userid) {
        //文章点赞
        Article article = articleDao.selectById(articleId);
        article.setThumbup(article.getThumbup() + 1);
        articleDao.updateById(article);

        //消息通知：点赞成功后，需要发送“点对点”消息给文章作者
        Notice notice = new Notice();
        notice.setReceiverId(article.getUserid()); //接收消息的用户的ID
        notice.setOperatorId(userid); //进行操作的用户的ID
        notice.setAction("thumbup"); //操作类型：评论、点赞等
        notice.setTargetType("article"); //被操作的对象：文章、评论等
        notice.setTargetId(articleId); //被操作对象的ID
        notice.setCreatetime(new Date());
        notice.setType("user"); //通知类型
        notice.setState("0");

        noticeClient.save(notice); //保存消息

        //1 创建Rabbit管理器
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate.getConnectionFactory());

        //2 创建队列，每个作者都有自己的队列，通过用户id进行区分
        Queue queue = new Queue("article_thumbup_" + article.getUserid(), true);
        rabbitAdmin.declareQueue(queue);

        //3 发送消息
        rabbitTemplate.convertAndSend("article_thumbup_"+article.getUserid(),articleId);
    }
}
