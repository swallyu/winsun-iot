package com.winsun.iot.persistence.redis;

import com.winsun.iot.exception.RedisWrapException;
import com.winsun.iot.utils.ResourceChecker;
import com.winsun.iot.utils.functions.Action;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RedisServiceImpl implements RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    private String redisUri;
    private RedisCommands<String, String> redisCommands;

    private ScheduledExecutorService scheduledExecutorService;

    private CacheCommand cacheCommand;

    public RedisServiceImpl(String url) {
        this.redisUri = url;
        ResourceChecker.addCheckTask(10, new Action<Boolean>() {
            @Override
            public Boolean execute() {
                return connect();
            }
        });
        scheduledExecutorService = Executors.newScheduledThreadPool(100, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("redis-service");
                return t;
            }
        });
        cacheCommand = new CacheCommand();
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
    public boolean isConnect() {
        return redisCommands != null;
    }

    @Override
    public boolean set(String key, String value) {
        if (redisCommands == null) {
            cacheCommand.add(new Action<Boolean>() {
                @Override
                public Boolean execute() {
                    redisCommands.set(key, value);
                    return true;
                }
            });
        } else {
            redisCommands.set(key, value);
        }

        return true;
    }

    @Override
    public boolean hset(String key, String field, String value) {

        if (redisCommands == null) {
            cacheCommand.add(new Action<Boolean>() {
                @Override
                public Boolean execute() {
                    redisCommands.hset(key, field, value);
                    return true;
                }
            });
            return true;
        } else {
            redisCommands.hset(key, field, value);
            return true;
        }
    }

    @Override
    public String hget(String key, String field) {
        return redisCommands.hget(key, field);
    }

    @Override
    public boolean hdel(String key, String... fields) {
        redisCommands.hdel(key, fields);
        return true;
    }

    @Override
    public boolean hset(String key, String field, String value, int expired, TimeUnit timeUnit) {
        redisCommands.hset(key,field,value);
        redisCommands.expire(key,timeUnit.toSeconds(expired));

        return true;
    }

    @Override
    public boolean hset(String key, Map<String, String> value, int expired, TimeUnit timeUnit) {
        redisCommands.hmset(key,value);
        redisCommands.expire(key,timeUnit.toSeconds(expired));

        return true;
    }

    @Override
    public boolean hset(String key, Object value, int expired, TimeUnit timeUnit) {
        try {
            Map<String,String> properties =  BeanUtils.describe(value);
            hset(key, properties, expired, timeUnit);
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RedisWrapException(e);
        }
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return redisCommands.hgetall(key);
    }

    @Override
    public boolean lpush(String key, String... data) {
        redisCommands.rpush(key,data);
        return false;
    }

    @Override
    public long llen(String key) {
        return redisCommands.llen(key);
    }

    @Override
    public List<String> lpopRange(String key,int count) {
        List<String> result = redisCommands.lrange(key,0,count-1);
        redisCommands.ltrim(key,count,-1);

        return result;
    }

    @Override
    public List<String> lgetAll(String key) {
        List<String> result = redisCommands.lrange(key,0,-1);

        return result;
    }

    @Override
    public boolean sadd(String key, String... value) {
        redisCommands.sadd(key,value);
        return true;
    }

    @Override
    public Set<String> sget(String key) {
        return redisCommands.smembers(key);
    }

    private AtomicBoolean resetTimer = new AtomicBoolean(false);

    private void resetCheckTimer() {
        if (!resetTimer.get()) {
            resetTimer.set(true);
            scheduledExecutorService.schedule(cacheCommand, 1, TimeUnit.SECONDS);
        }
    }

    private class CacheCommand implements Runnable {
        private Queue<Action<Boolean>> funcs = new ArrayDeque<>();
        private AtomicBoolean running = new AtomicBoolean(false);

        public void add(Action<Boolean> f) {
            funcs.offer(f);
            resetCheckTimer();
        }

        @Override
        public void run() {
            logger.info("funcs {}", funcs.size());
            if (running.get()) {
                return;
            }
            try {
                resetTimer.set(false);
                running.set(true);
                if (funcs.size() > 0) {
                    Action<Boolean> f;
                    List<Action<Boolean>> shouldReExec = new ArrayList<>();
                    while (true) {
                        if (redisCommands == null) {
                            break;
                        }
                        f = funcs.poll();
                        if (redisCommands == null) {

                            break;
                        }
                        boolean ret = f.execute();
                        if (!ret) {
                            shouldReExec.add(f);
                        }
                        if (funcs.size() == 0) {
                            break;
                        }
                    }
                    shouldReExec.forEach(m -> {
                        funcs.offer(m);
                    });
                }
            } catch (Exception exc) {
                logger.error(exc.getMessage(), exc);
            } finally {
                running.set(false);
                //只有还有需要进行处理的才需要启动检查。
                if (funcs.size() > 0) {
                    resetCheckTimer();
                }
            }
        }

    }
}
