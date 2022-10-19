package com.tensquare.article.service;

import com.tensquare.article.pojo.Comment;
import com.tensquare.article.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import util.IdWorker;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Comment> findAll() {
        List<Comment> list = commentRepository.findAll();
        return list;
    }

    public Comment findById(String commentId) {
//        Comment comment = commentRepository.findById(commentId).get();
//        return comment;
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if(commentOptional.isPresent()){ //判断是否有值
            return commentOptional.get();
        }
        return null;
    }

    public void save(Comment comment) {
        //使用分布式id生成器生成id
        String id = idWorker.nextId() + "";
        comment.set_id(id);

        //初始化点赞数据、发布时间……
        comment.setThumbup(0);
        comment.setPublishdate(new Date());

        //保存数据
        commentRepository.save(comment);
    }

    public void updateById(Comment comment) {
        //使用的是MongoRepository中的方法
        //其中的save方法的功能是：如果主键存在，则执行修改操作；若不存在，则执行新增操作
        commentRepository.save(comment);
    }

    public void deleteById(String commentId) {
        commentRepository.deleteById(commentId);
    }

    public List<Comment> findByArticleId(String articleId) {
        //调用持久层自定义的对应方法
        List<Comment> list = commentRepository.findByArticleid(articleId);
        return list;
    }

    public void thumbup(String commentId) {
//        //原始方法：不能保证线程安全  直接解决方法：分布式锁--redis/ZooKeeper
//        //1. 根据评论ID查询评论数据
//        Comment comment = commentRepository.findById(commentId).get();
//        //2. 对评论点赞数+1
//        comment.setThumbup(comment.getThumbup() + 1);
//        //3. 保存评论数据
//        commentRepository.save(comment);

        //点赞功能优化
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(commentId)); //封装修改条件
        Update update = new Update(); //封装修改的数值
        update.inc("thumbup", 1); //使用inc列值增长命令
        mongoTemplate.updateFirst(query, update, "comment"); //直接修改数据，参数：(修改条件, 修改数值, MongoDB中的集合名称)
    }
}
