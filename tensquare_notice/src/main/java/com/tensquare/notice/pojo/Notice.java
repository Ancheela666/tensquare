package com.tensquare.notice.pojo;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_notice")
public class Notice implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;//ID

    private String receiverId;//接收消息的用户ID
    private String operatorId;//进行操作的用户ID

    @TableField(exist = false) //代表下面一行的字段在数据库表中是没有的
    private String operatorName;//进行操作的用户昵称（数据库表中没有）
    private String action;//操作类型（评论，点赞等）
    private String targetType;//对象类型（评论，点赞等）

    @TableField(exist = false)
    private String targetName;//对象名称或简介（数据库表中没有）
    private String targetId;//对象id
    private Date createtime;//创建日期
    private String type;    //消息类型：sys 系统消息, user 用户消息
    private String state;   //消息状态（0 未读，1 已读）


}