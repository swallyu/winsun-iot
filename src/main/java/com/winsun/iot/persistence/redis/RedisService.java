package com.winsun.iot.persistence.redis;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface RedisService {
    boolean isConnect();

    boolean set(String key, String value);

    boolean hset(String key, String field, String value);

    String hget(String key, String field);

    boolean hdel(String key, String... field);

    boolean hset(String key, String field, String value, int expired, TimeUnit timeUnit);

    boolean hset(String key, Map<String, String> value, int expired, TimeUnit timeUnit);

    boolean hset(String key, Object value, int expired, TimeUnit timeUnit);

    Map<String, String> hgetAll(String bizId);

    boolean lpush(String key, String... data);

    long llen(String key);

    List<String> lpopRange(String key, int count);

    List<String> lgetAll(String key);

    boolean sadd(String key,String... value);

    Set<String> sget(String key);
}
