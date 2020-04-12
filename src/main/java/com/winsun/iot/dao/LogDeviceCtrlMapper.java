package com.winsun.iot.dao;

import com.winsun.iot.domain.LogDeviceCtrl;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface LogDeviceCtrlMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(LogDeviceCtrl record);

    int insertSelective(LogDeviceCtrl record);

    LogDeviceCtrl selectByPrimaryKey(Integer id);

    LogDeviceCtrl selectByBizId(String bizId);

    int updateByPrimaryKeySelective(LogDeviceCtrl record);

    int updateByPrimaryKey(LogDeviceCtrl record);

    void updateStatus(@Param("bizId") String bizId,@Param("complete") boolean complete,
                      @Param("result") boolean result,@Param("updateTime") LocalDateTime now);
}