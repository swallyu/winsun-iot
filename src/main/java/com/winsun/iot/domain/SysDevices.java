package com.winsun.iot.domain;

import java.util.Date;

public class SysDevices {
    /**
     *
     */
    private Integer id;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private String baseId;

    /**
     *
     */
    private String mobile;

    /**
     *
     */
    private String nodeType;

    /**
     *
     */
    private String deviceType;

    /**
     *
     */
    private String hardwareVersion;

    /**
     *
     */
    private String softwareVersion;

    /**
     *
     */
    private Boolean isgateway;

    /**
     *
     */
    private String remarks;

    /**
     *
     */
    private Boolean ischecked;

    /**
     *
     */
    private String luatVersion;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date modifiedTime;

    /**
     * 扩展数据
     */
    private String extend;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseId() {
        return baseId;
    }

    public void setBaseId(String baseId) {
        this.baseId = baseId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public Boolean getIsgateway() {
        return isgateway;
    }

    public void setIsgateway(Boolean isgateway) {
        this.isgateway = isgateway;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Boolean getIschecked() {
        return ischecked;
    }

    public void setIschecked(Boolean ischecked) {
        this.ischecked = ischecked;
    }

    public String getLuatVersion() {
        return luatVersion;
    }

    public void setLuatVersion(String luatVersion) {
        this.luatVersion = luatVersion;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
}