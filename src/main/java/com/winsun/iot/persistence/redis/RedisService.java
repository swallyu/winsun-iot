package com.winsun.iot.persistence.redis;

public interface RedisService {
    boolean isConnect();

    boolean set(String key, String value);

    boolean hset(String key, String field, String value);

    String hget(String key, String field);
}
