package com.tensquare.article.controller;

import entity.Result;
import entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice //对controller进行增强
public class BaseExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handler(Exception e){
        //对不同类型的异常进行相应的处理
        if(e instanceof NullPointerException){ //如果是空指针异常
            System.out.println("空指针异常");
        }
        System.out.println("异常处理");
        return new Result(false, StatusCode.ERROR, e.getMessage());
    }

}
