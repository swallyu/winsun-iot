package com.winsun.iot.http.handler.devices;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.DeviceInfo;
import com.winsun.iot.http.common.*;
import com.winsun.iot.utils.BizResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@HttpMap(value = "/device/list", method = HttpMethod.Post)
public class DeviceListHandler implements HttpController {

    @Inject
    private DeviceManager deviceManager;

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {
        Collection<DeviceInfo> deviceInfos = deviceManager.getDeviceObjList();
        JSONObject obj = request.getBodyAsJson();
        Integer page = obj.getInteger("page");
        page = (page == null) ? 1 : page;

        Integer pageSize = obj.getInteger("pageSize");
        pageSize = (pageSize == null) ? 10 : pageSize;

        List<DeviceInfo> resultList = deviceInfos.stream().skip((page-1)*pageSize)
                .limit(pageSize).collect(Collectors.toList());

        BizResult<List<DeviceInfo>> ret = BizResult.Success(resultList);
        resp.write(ret);
    }
}
