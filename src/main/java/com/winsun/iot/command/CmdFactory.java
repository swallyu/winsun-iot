package com.winsun.iot.command;

import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.domain.DeviceInfo;
import com.winsun.iot.ruleengine.EnumCmdStatus;
import com.winsun.iot.utils.RandomString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmdFactory {

    public static JSONObject buildBizCmd(String sig,EnumQoS qos,String msgtype,JSONObject cmdObj){
        JSONObject ringMsgobj = new JSONObject();
        ringMsgobj.put("sig", sig);
        ringMsgobj.put("qos", qos.getCode());
        ringMsgobj.put("stage", 0);
        ringMsgobj.put("initiator", "cloud/gw");
        ringMsgobj.put("result", false);
        JSONObject detail = new JSONObject();
        detail.put("type", msgtype);
        detail.put("detail", cmdObj);
        ringMsgobj.put("msg", detail);

        return ringMsgobj;
    }

    public static JSONObject buildBizCmdResp(String sig, EnumCmdStatus status,boolean result){
        JSONObject ringMsgobj = new JSONObject();
        ringMsgobj.put("sig", sig);
        ringMsgobj.put("stage", status.getCode());
        ringMsgobj.put("result", result);

        return ringMsgobj;
    }

    public static JSONObject buildAddPairedDev(String baseid, String devicetype, String hwversion, String swversion,
                                               Boolean sensoruploadreality, Boolean autoCtrl, DeviceInfo deviceObj) {
//        String luatVersion = deviceObj.getBaseinfo().getLuatVersion();
//        Map<String,Boolean> autoCtrlSetting = deviceObj.getAutoCtrlSetting();
//
//        JSONObject jcmd = new JSONObject();
//        jcmd.put("config_type", "pairdevs");
//        jcmd.put("opt_type", "add");
//        JSONObject detail = new JSONObject();
//        detail.put("Base_ID", baseid);
//        detail.put("Device_Type", devicetype);
//        detail.put("HW_Verstion", hwversion);
//        detail.put("SW_Verstion", swversion);
//        detail.put("Sensor_Upload_Reality", sensoruploadreality);
//        if(VersionChecker.compare(luatVersion,VersionChecker.V20)>=0){
//            //如果没有设置过，则按照autoopt统一设置
//            List<Map<String,Object>> autoCtrlSync = new ArrayList<>();
//            if(autoCtrlSetting==null){
//                for (Object paramName : deviceObj.getControlrealityp().keySet()) {
//
//                    Map<String,Object> tmpMap = new HashMap<>();
//                    tmpMap.put("param",paramName);
//                    tmpMap.put("value",deviceObj.isAutoOpt());
//                    autoCtrlSync.add(tmpMap);
//                }
//            }else{
//                for (Object entryValue : autoCtrlSetting.entrySet()) {
//                    Map.Entry paramValue = (Map.Entry)entryValue;
//                    String key = paramValue.getKey().toString();
//                    Boolean value = (Boolean)paramValue.getValue();
//
//                    Map<String,Object> tmpMap = new HashMap<>();
//                    tmpMap.put("param",key);
//                    tmpMap.put("value",value);
//                    autoCtrlSync.add(tmpMap);
//                }
//            }
//            detail.put("Is_AutoControl", autoCtrlSync);
//        }else{
//            detail.put("Is_AutoControl", autoCtrl);
//        }
//        jcmd.put("detail", detail);
//        return jcmd;
        return null;
    }
}
