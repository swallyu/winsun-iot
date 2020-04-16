package com.winsun.iot.biz.domain;

public class SellInfo {
    private String baseId;
    private String ticket;
    private String qrCodeToken;
    private String qrCodeUrl;

    private String rawToken;

    public SellInfo(String ticket, String qrCodeToken, String qrCodeUrl) {
        this.qrCodeToken = qrCodeToken;
        this.qrCodeUrl = qrCodeUrl;
        String[] tmps = ticket.split(",");
        this.baseId = tmps[0];
        this.ticket = tmps[1];
        this.rawToken = ticket;
    }

    public String getTicket() {
        return ticket;
    }

    public String getQrCodeToken() {
        return qrCodeToken;
    }

    public void setQrCodeToken(String qrCodeToken) {
        this.qrCodeToken = qrCodeToken;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getBaseId() {
        return baseId;
    }

    public String getRawToken() {
        return rawToken;
    }
}
