package com.tensquare.notice.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.tensquare.notice.client.ArticleClient;
import com.tensquare.notice.client.UserClient;
import com.tensquare.notice.dao.NoticeDao;
import com.tensquare.notice.dao.NoticeFreshDao;
import com.tensquare.notice.pojo.Notice;
import com.tensquare.notice.pojo.NoticeFresh;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import util.IdWorker;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class NoticeService {

    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private NoticeFreshDao noticeFreshDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private ArticleClient articleClient;

    @Autowired
    private UserClient userClient;

    //完善消息内容
    private void getInfo(Notice notice){
        //查询用户昵称
        Result userResult = userClient.selectById(notice.getOperatorId());
        HashMap userMap = (HashMap) userResult.getData();
        notice.setOperatorName(userMap.get("nickname").toString()); //设置操作者的用户昵称到消息通知中

        //查询对象名称
        Result articleResult = articleClient.findById(notice.getTargetId());
        HashMap articleMap = (HashMap) articleResult.getData();
        notice.setTargetName(articleMap.get("title").toString()); //设置对象名称到消息通知中
    }

    public Notice selectById(String id) {
        Notice notice = noticeDao.selectById(id);
        getInfo(notice); //完善消息
        return notice;
    }

    public Page<Notice> selectByPage(Notice notice, Integer page, Integer size) {
        Page<Notice> pageData = new Page<>(page, size); //封装分页对象
        List<Notice> noticeList = noticeDao.selectPage(pageData, new EntityWrapper<>(notice)); //执行分页查询
        for (Notice n : noticeList) {
            getInfo(n);
        }
        pageData.setRecords(noticeList); //设置结果集到分页对象中
        return pageData; //返回结果
    }

    public void save(Notice notice) {
        //设置初始值
        notice.setState("0"); //设置状态：0表示未读，1表示已读
        notice.setCreatetime(new Date());
        String id = idWorker.nextId() + ""; //使用分布式ID生成器生成ID
        notice.setId(id);
        //待推送消息入库（新消息提醒）：用了RabbitMQ之后这部分就不需要了
        NoticeFresh noticeFresh = new NoticeFresh();
        noticeFresh.setNoticeId(id); //消息ID
        noticeFresh.setUserId(notice.getReceiverId()); //待通知用户的ID
        //保存
        noticeDao.insert(notice);
        noticeFreshDao.insert(noticeFresh);
    }

    public void updateById(Notice notice) {
        noticeDao.updateById(notice);
    }

    public Page<NoticeFresh> freshPage(String userId, Integer page, Integer size) {
        NoticeFresh noticeFresh = new NoticeFresh(); //封装查询条件
        Page<NoticeFresh> pageData = new Page<>(page, size); //封装分页对象
        List<NoticeFresh> list = noticeFreshDao.selectPage(pageData, new EntityWrapper<>(noticeFresh)); //执行分页查询
        pageData.setRecords(list); //设置结果集到分页对象中
        return pageData; //返回结果
    }

    public void freshDelete(NoticeFresh noticeFresh) {
        noticeFreshDao.delete(new EntityWrapper<>(noticeFresh));
    }
}
