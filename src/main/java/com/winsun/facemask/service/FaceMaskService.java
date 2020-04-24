package com.winsun.facemask.service;

import com.winsun.iot.biz.domain.SellInfo;
import com.winsun.iot.domain.CmdResult;

public interface FaceMaskService {
    CmdResult<String> sellFaceMak(SellInfo sellInfo);

    CmdResult<String> updateQrCode(String token, String url);

    void resendQrCode(String baseId);

    void processMissTask(String baseId);

}
