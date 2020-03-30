package com.winsun.iot.dao;

import com.winsun.iot.domain.DeviceList;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DeviceDao {

    @Select("select * from DeviceList")
    List<DeviceList> listAll();
}
