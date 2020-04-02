package com.winsun.iot.exception;

public class SendException extends RuntimeException {
    public SendException() {
    }

    public SendException(String message) {
        super(message);
    }

    public SendException(String message, Throwable cause) {
        super(message, cause);
    }

    public SendException(Throwable cause) {
        super(cause);
    }

    public SendException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
