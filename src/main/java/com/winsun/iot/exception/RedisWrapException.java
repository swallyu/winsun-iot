package com.winsun.iot.exception;

public class RedisWrapException extends RuntimeException {
    public RedisWrapException() {
    }

    public RedisWrapException(String message) {
        super(message);
    }

    public RedisWrapException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisWrapException(Throwable cause) {
        super(cause);
    }

    public RedisWrapException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
