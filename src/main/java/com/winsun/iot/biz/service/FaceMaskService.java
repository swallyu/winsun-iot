package com.winsun.iot.biz.service;

import com.winsun.iot.domain.CmdResult;

public interface FaceMaskService {
    CmdResult<String> sellFaceMak(String baseId, String token);

    CmdResult<String> updateQrCode(String token, String url);
}
