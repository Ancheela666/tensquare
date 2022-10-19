package com.tensquare.article.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.tensquare.article.pojo.Article;
import com.tensquare.article.service.ArticleService;
import entity.PageResult;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("article")
@CrossOrigin //解决跨域问题
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //根据文章ID和用户ID，建立订阅关系（保存的是文章作者ID和用户ID之间的关系）
    // http://127.0.0.1:9004/article/subscribe  POST
    @RequestMapping(value = "subscribe", method = RequestMethod.POST)
    public Result subscribe(@RequestBody Map map){
        //返回状态，如果返回true就是订阅该文章作者，如果返回false就是取消订阅文章作者
        Boolean flag = articleService.subscribe(map.get("articleId"),
                map.get("userId"));
        //判断是订阅还是取消订阅
        if(flag == true){
            return new Result(true, StatusCode.OK, "订阅成功");
        } else {
            return new Result(true, StatusCode.OK, "取消订阅成功");
        }
    }


    //文章分页条件查询
    @RequestMapping(value = "search/{page}/{size}", method = RequestMethod.POST)
    public Result findByPage(@PathVariable Integer page,
                             @PathVariable Integer size,
                             @RequestBody Map<String, Object> map){ //直接使用集合接收数据便于对所有条件进行遍历。如果直接用POJO，遍历POJO的所有属性需要用反射的方式
        //根据条件分页查询
        Page<Article> pageData= articleService.findByPage(page, size, map);
        //封装分页返回对象
        PageResult<Article> pageResult = new PageResult<>(
                pageData.getTotal(), pageData.getRecords()
        );
        //返回数据
        return new Result(true, StatusCode.OK, "查询成功", pageResult);
    }

    //删除文章
    @RequestMapping(value = "{articleId}", method = RequestMethod.DELETE)
    public Result deleteById(@PathVariable("articleId") String articleId){
        articleService.deleteById(articleId);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    //修改文章
    @RequestMapping(value = "{articleId}", method = RequestMethod.PUT)
    public Result updateById(@PathVariable("articleId") String articleId,
                             @RequestBody Article article){
        article.setId(articleId); //修改ID
        //执行修改
        articleService.updateById(article);
        //返回结果
        return new Result(true, StatusCode.OK, "修改成功");
    }

    //增加文章
    @RequestMapping(method = RequestMethod.POST)
    public Result save(@RequestBody Article article){ //@RequestBody注解可以将JSON字符串转换为pojo对象
        articleService.save(article);
        return new Result(true, StatusCode.OK, "新增成功");
    }

    //根据ID查询文章
    @RequestMapping(value = "{articleId}", method = RequestMethod.GET)
    public Result findById(@PathVariable("articleId") String articleId){
        Article article = articleService.findById(articleId);
        return new Result(true, StatusCode.OK, "查询成功", article);
    }

    //查询文章全部列表
    @RequestMapping(method = RequestMethod.GET)
    public Result findAll(){
        List<Article> list = articleService.findAll();
        return new Result(true, StatusCode.OK, "查询成功", list);
    }

    //文章点赞
    @RequestMapping(value = "thumbup/{articleId}", method = RequestMethod.PUT)
    public Result thumbup(@PathVariable String articleId) {
        //模拟用户id
        String userId = "4";
        String key = "thumbup_article_" + userId + "_" + articleId;
        //查询用户点赞信息，根据用户id和文章id
        Object flag = redisTemplate.opsForValue().get(key);
        //判断查询到的结果是否为空
        if (flag == null) { //如果为空，表示用户没有点过赞，可以点赞
            articleService.thumbup(articleId, userId);
            //点赞成功，在redis中保存点赞信息
            redisTemplate.opsForValue().set(key, "1");
            return new Result(true, StatusCode.OK, "点赞成功");
        }
        //如果不为空，表示用户点过赞，不可以重复点赞
        return new Result(false, StatusCode.REPERROR, "不能重复点赞");
    }

    //公共异常处理测试
    @RequestMapping(value = "exception", method = RequestMethod.GET)
    public Result exceptionTest(){
        int a = 1/0;
        return null;
    }

}
