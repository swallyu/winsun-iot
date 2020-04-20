package com.winsun.iot.biz.service;

import com.winsun.iot.biz.domain.SellInfo;
import com.winsun.iot.domain.CmdResult;

public interface FaceMaskService {
    CmdResult<String> sellFaceMak(SellInfo sellInfo);

    CmdResult<String> updateQrCode(String token, String url);

    void resendQrCode(String baseId);
}
