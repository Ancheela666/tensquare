package com.tensquare.notice.client;

import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("tensquare-article")
public interface ArticleClient {

    //根据文章ID查询文章数据
    @RequestMapping(value = "article/{articleId}", method = RequestMethod.GET)
    public Result findById(@PathVariable("articleId") String articleId);
}
