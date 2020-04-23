package com.winsun.iot.dao;

import com.winsun.iot.domain.DeviceList;
import com.winsun.iot.domain.LogDeviceEvents;
import com.winsun.iot.domain.SysDevices;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommonDao {

    int addEvent(LogDeviceEvents record);

    List<SysDevices> listDevices();

    int executeRawSql(@Param("sql") String sql);

}