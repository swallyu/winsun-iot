package com.winsun.iot.dao;

import com.winsun.iot.domain.SysDevices;

public interface SysDevicesMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysDevices record);

    int insertSelective(SysDevices record);

    SysDevices selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysDevices record);

    int updateByPrimaryKey(SysDevices record);
}