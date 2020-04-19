package com.winsun.iot.http.handler.devices;

import com.google.inject.Inject;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.http.common.*;

@HttpMap(value = "/device/refresh",method = HttpMethod.Post)
public class DeviceRefreshHandler implements HttpController {

    @Inject
    private DeviceManager deviceManager;
    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {
        deviceManager.reload();

        CmdResult<Boolean> result = new CmdResult<>(0,true,"重新加载设备信息成功");

        resp.write(result);
    }
}
