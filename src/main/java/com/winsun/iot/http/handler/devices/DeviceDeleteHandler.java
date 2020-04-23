package com.winsun.iot.http.handler.devices;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.DeviceInfo;
import com.winsun.iot.http.common.*;
import com.winsun.iot.utils.BizResult;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@HttpMap(value = "/device/delete", method = HttpMethod.Post)
public class DeviceDeleteHandler implements HttpController {

    @Inject
    private DeviceManager deviceManager;

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

        JSONObject obj = request.getBodyAsJson();
        String deviceId = obj.getString("baseId");

        BizResult<Boolean> ret = deviceManager.deleteDevice(deviceId);
        resp.write(ret);
    }
}
