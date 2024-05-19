package com.zm.usercenter;

import com.zm.usercenter.config.FastjsonCodec;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import javax.xml.datatype.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() throws InterruptedException {
        //list，数据存在本地JVM内存中
//        List<String> list=new ArrayList<>();
//        list.add("zm");
//        System.out.println("list:"+list.get(0));
        //list.remove(0);

        //数据存在redis的内存中
        //参数传入的test-list是redis的key
        RList<String> rList= redissonClient.getList("test-list", new FastjsonCodec());
        //rList.expire(10, TimeUnit.SECONDS);
        rList.add("zm");
        Thread.sleep(10000);
        rList.add("zm");
        rList.expire(10, TimeUnit.SECONDS);
        System.out.println("rList:"+rList.get(0));
        //rList.remove(0);
    }
}
