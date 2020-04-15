package com.winsun.iot.persistence.redis;

import com.winsun.iot.config.Config;
import com.winsun.iot.utils.ResourceChecker;
import com.winsun.iot.utils.functions.Action;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisServiceImpl implements RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    private String redisUri;
    private RedisCommands<String, String> redisCommands;

    public RedisServiceImpl(String url) {
        this.redisUri = url;
        ResourceChecker.addCheckTask(10, new Action<Boolean>() {
            @Override
            public Boolean execute() {
                return connect();
            }
        });

    }

    public boolean connect() {
        try {
            RedisClient redisClient = RedisClient.create(redisUri);   // <2> 创建客户端
            StatefulRedisConnection<String, String> connection = redisClient.connect();     // <3> 创建线程安全的连接
            redisCommands = connection.sync();                // <4> 创建同步命令
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean isConnect(){
        return redisCommands!=null;
    }

    @Override
    public boolean set(String key, String value) {
        redisCommands.set(key,value);
        return true;
    }

    @Override
    public boolean hset(String key, String field, String value) {

        return redisCommands.hset(key,field,value);
    }

    @Override
    public String hget(String key, String field) {
        return redisCommands.hget(key,field);
    }
}
