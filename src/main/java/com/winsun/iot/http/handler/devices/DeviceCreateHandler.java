package com.winsun.iot.http.handler.devices;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.DeviceInfo;
import com.winsun.iot.domain.SysDevices;
import com.winsun.iot.http.common.*;
import com.winsun.iot.utils.BizResult;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@HttpMap(value = "/device/add", method = HttpMethod.Post)
public class DeviceCreateHandler implements HttpController {

    @Inject
    private DeviceManager deviceManager;

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

        JSONObject obj = request.getBodyAsJson();

        JSONObject detail = obj.getJSONObject("detail");

        SysDevices deviceInfo = detail.toJavaObject(SysDevices.class);

        BizResult<Boolean> ret = deviceManager.createDevice(new DeviceInfo(deviceInfo));
        resp.write(ret);
    }
}
