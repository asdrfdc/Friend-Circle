package com.zm.usercenter.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redission配置
 */
@Configuration
@ConfigurationProperties()
@Data
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.redisson.database}")
    private Integer redissonDatabase;

    @Bean
    public RedissonClient redissonClient() {
        //1.创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(redissonDatabase);
        config.setCodec(new org.redisson.codec.JsonJacksonCodec());
        //2.创建实例
        RedissonClient redisson= Redisson.create(config);
        return redisson;
    }
}
