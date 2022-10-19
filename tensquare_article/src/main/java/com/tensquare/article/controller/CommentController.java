package com.tensquare.article.controller;

import com.tensquare.article.pojo.Comment;
import com.tensquare.article.service.CommentService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("comment")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private RedisTemplate redisTemplate;

    //根据评论ID点赞评论
    //PUT  /comment/thumbup/{commentId}
    @RequestMapping(value = "thumbup/{commentId}", method = RequestMethod.PUT)
    public Result thumbup(@PathVariable String commentId){
        /**
         *  把用户点赞信息存到Redis中，每次点赞前先查询点赞信息
         *  如果没有点赞信息，则用户可以点赞
         *  如果有点赞信息，那么用户不能重复点赞
         */
        //模拟用户ID
        String userId = "123";
        //根据用户ID和评论ID，查询用户点赞信息
        Object flag = redisTemplate.opsForValue().get("thumbup_" + userId + "_" + commentId); //加"thumbup_"前缀是为了Redis的数据维护
        //判断查询结果是否为空
        if(flag == null){
            commentService.thumbup(commentId);
            //点赞成功，保存点赞信息
            redisTemplate.opsForValue().set("thumbup_" + userId + "_" + commentId, 1);
            return new Result(true, StatusCode.OK, "点赞成功");
        }
        //如果不为空，则代表用户已经点过赞。不允许重复点赞
        return new Result(false, StatusCode.REPERROR, "不能重复点赞");
    }

    //根据文章ID查询文章评论
    //GET  /comment/article/{articleId}
    @RequestMapping(value = "article/{articleId}", method = RequestMethod.GET)
    public Result findByArticleId(@PathVariable String articleId){
        List<Comment> list = commentService.findByArticleId(articleId);
        return new Result(true, StatusCode.OK, "查询成功", list);
    }

    //查询所有评论 GET
    @RequestMapping(method = RequestMethod.GET)
    public Result findAll(){
        List<Comment> list = commentService.findAll();
        return new Result(true, StatusCode.OK, "查询成功", list);
    }

    //根据评论ID查询评论数据 GET /comment/{commentId}
    @RequestMapping(value = "{commentId}", method = RequestMethod.GET)
    public Result findById(@PathVariable String commentId){
        Comment comment = commentService.findById(commentId);
        return new Result(true, StatusCode.OK, "查询成功", comment);
    }

    //新增评论 POST
    @RequestMapping(method = RequestMethod.POST)
    public Result save(@RequestBody Comment comment){
        commentService.save(comment);
        return new Result(true, StatusCode.OK, "新增成功");
    }

    //修改评论
    //PUT  /comment/{commentId}
    @RequestMapping(value = "{commentId}", method = RequestMethod.PUT)
    public Result updateById(@PathVariable String commentId,
                             @RequestBody Comment comment){
        comment.set_id(commentId); //设置评论的主键
        commentService.updateById(comment); //执行修改
        return new Result(true, StatusCode.OK, "修改成功"); //返回
    }

    //根据ID删除评论
    //DELETE  /comment/{commentId}
    @RequestMapping(value = "{commentId}", method = RequestMethod.DELETE)
    public Result deleteById(@PathVariable("commentId") String commentId){
        commentService.deleteById(commentId);
        return new Result(true, StatusCode.OK, "删除成功");
    }
}
