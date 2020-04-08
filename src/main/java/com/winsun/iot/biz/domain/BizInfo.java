package com.winsun.iot.biz.domain;

import java.time.LocalDateTime;

public class BizInfo {
    private String bizId;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    private boolean finish = false;
    private boolean success = false;

    public BizInfo(String bizId) {
        this.bizId = bizId;
        this.startTime = LocalDateTime.now();
    }

    public BizInfo(String bizId, LocalDateTime startTime) {
        this.bizId = bizId;
        this.startTime = startTime;
    }

    public String getBizId() {
        return bizId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish,boolean result) {
        this.finish = finish;
        this.finishTime = LocalDateTime.now();
        this.success = result;
    }
}
