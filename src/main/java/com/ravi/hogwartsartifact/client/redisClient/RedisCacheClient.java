package com.ravi.hogwartsartifact.client.redisClient;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import static com.ravi.hogwartsartifact.system.constant.RedisConstant.WHITELIST_USER;


@Component
public class RedisCacheClient {

    private final StringRedisTemplate redisTemplate;

    public RedisCacheClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key,String value, long timeout,TimeUnit timeUnit){
        this.redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    public String get(String key){
        return this.redisTemplate.opsForValue().get(key);
    }

    public void delete(String key){
        this.redisTemplate.delete(key);
    }

    public boolean isUserTokenInWhiteList(String userId,String tokenFromRequest){
        String tokenFromRedis = get(WHITELIST_USER+":"+userId);
        return tokenFromRedis != null && tokenFromRedis.equals(tokenFromRequest);
    }
}
