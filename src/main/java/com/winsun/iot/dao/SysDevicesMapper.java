package com.winsun.iot.dao;

import com.winsun.iot.domain.SysDevices;

public interface SysDevicesMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysDevices record);

    int insertSelective(SysDevices record);

    SysDevices selectByPrimaryKey(Integer id);

    int updateByBaseIdSelective(SysDevices record);

    int updateByPrimaryKey(SysDevices record);

    int deleteDevices(String deviceId);

    int updateByBaseId(SysDevices device);

    SysDevices selectByBaseId(String baseId);
}