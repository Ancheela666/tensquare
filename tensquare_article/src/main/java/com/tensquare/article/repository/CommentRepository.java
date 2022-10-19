package com.tensquare.article.repository;

import com.tensquare.article.pojo.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> { //<操作对象, 主键类型>
    /**
     *  SpringDataMogoDB支持通过设置查询方法名进行查询定义的方式
     *  eg：
     *  //根据发布时间和点赞数查询
     *  List<Comment> findByPublishdateAndThumbup(Date publishdate, Integer thumbup);
     *
     *  //根据用户ID查询，并根据发布时间倒序排序
     *  List<Comment> findByUseridOOrderByPublishdateDesc(String userid);
     */

    //根据文章ID查询文章评论数据
    List<Comment> findByArticleid(String articleId);
}
