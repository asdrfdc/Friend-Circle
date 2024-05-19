package com.zm.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zm.usercenter.mapper.UserMapper;
import com.zm.usercenter.model.domain.User;
import com.zm.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 */
@Slf4j
@Component
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //重点用户
    private List<Long> mainUserList = Arrays.asList(1L);


    //每天加载，推荐预热用户
    @Scheduled(cron = "0 56 23 * * *")
    public void doCacheRecommendUser()  {
        RLock lock=redissonClient.getLock("user-center:precachejob:docache:lock");
        try{
            //只有一个线程能获取到锁
            //waitTime指的是其他线程拿不到锁最多等多久，到点了就返回false
            //leaseTime指的是锁的过期时间
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("getLock"+Thread.currentThread().getId());

                for(Long userId:mainUserList){
                    QueryWrapper<User> queryWrapper=new QueryWrapper<>();
                    //todo  没有设置针对性查询条件
                    Page<User> userPage=userService.page(new Page<>(1,20),queryWrapper);

                    //准备操作缓存
                    String redisKey=String.format("user-center:user:recommend:%s",userId);
                    ValueOperations<String,Object> valueOperations= redisTemplate.opsForValue();

                    //写缓存
                    try{
                        valueOperations.set(redisKey,userPage,360, TimeUnit.SECONDS);
                    }catch (Exception e){
                        log.info("redis set key error",e);
                    }
                }
            }
        }catch(InterruptedException e){
            log.error("doCacheRecommendUser error",e);
        } finally {
            //只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                System.out.println("unlock" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
