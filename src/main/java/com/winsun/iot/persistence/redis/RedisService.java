package com.winsun.iot.persistence.redis;

public interface RedisService {
    boolean isConnect();

    boolean set(String key, String value);
}
