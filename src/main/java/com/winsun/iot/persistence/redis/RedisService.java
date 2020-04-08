package com.winsun.iot.persistence.redis;

public interface RedisService {
    boolean set(String key, String value);
}
