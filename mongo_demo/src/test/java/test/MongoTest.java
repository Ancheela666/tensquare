package test;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MongoTest {

    private MongoClient mongoClient; //客户端
    MongoCollection<Document> comment; //集合

    @Before
    public void init(){
        //1. 创建操作MongoDB的客户端
        mongoClient = new MongoClient("192.168.163.131", 27017);

        //2. 选择数据库 use commentdb
        MongoDatabase commentdb = mongoClient.getDatabase("commentdb");

        //3. 获取集合
        comment = commentdb.getCollection("comment");
    }

    @After
    public void after(){
        //关闭连接，释放资源
        mongoClient.close();
    }

    //查询所有数据 db.comment.find()
    @Test
    public void test1(){

        //4. 使用集合进行查询
        FindIterable<Document> documents = comment.find();

        //5. 解析结果集
        for (Document document : documents) {
            System.out.println("-----------------------------");
            System.out.println("_id：" + document.get("_id"));
            System.out.println("内容：" + document.get("content"));
            System.out.println("用户ID:" + document.get("userid"));
            System.out.println("点赞数：" + document.get("thumbup"));
        }

    }

    //根据条件_id查询
    @Test
    public void test2(){
        BasicDBObject bson = new BasicDBObject("_id", "1"); //db.comment.find({"_id": "1"})
        FindIterable<Document> documents = comment.find(bson);
        for (Document document : documents) {
            System.out.println("-----------------------------");
            System.out.println("_id：" + document.get("_id"));
            System.out.println("内容：" + document.get("content"));
            System.out.println("用户ID:" + document.get("userid"));
            System.out.println("点赞数：" + document.get("thumbup"));
        }
    }

    //新增
    @Test
    public void test3(){
        //封装新增数据
        Map<String, Object> map = new HashMap<>();
        map.put("_id", "6");
        map.put("content", "新增测试");
        map.put("userid", "1019");
        map.put("thumbup", "666");

        //封装新增文档对象
        Document document = new Document(map);

        //执行新增操作
        comment.insertOne(document);
    }

    //修改
    @Test
    public void test4(){
        //创建修改条件
        BasicDBObject filter = new BasicDBObject("_id", "6");

        //创建修改值
        BasicDBObject update = new BasicDBObject("$set", new Document("userid","999"));

        //执行修改操作
        comment.updateOne(filter, update);
    }

    //删除
    @Test
    public void test5(){
        BasicDBObject bson = new BasicDBObject("_id", "6"); //创建删除条件
        comment.deleteOne(bson); //执行删除操作
    }
}
