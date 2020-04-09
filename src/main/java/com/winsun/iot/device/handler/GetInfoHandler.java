package com.winsun.iot.device.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.CmdCallback;
import com.winsun.iot.command.CmdHandler;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.CmdRuleInfo;
import com.winsun.iot.command.biz.BizCmdHandler;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.DeviceInfo;
import com.winsun.iot.persistence.PersistenceService;
import com.winsun.iot.ruleengine.CmdRule;
import com.winsun.iot.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetInfoHandler implements CmdHandler {

    public static final String TOPIC = "/E2ES/GateWay/GetInfo/+";

    public static final int QOS = 1;

    private static final Logger logger = LoggerFactory.getLogger(GetInfoHandler.class);

    @Inject
    private PersistenceService persistenceService;
    @Inject
    private CommonDao dao;
    @Inject
    private DeviceManager deviceManager;

    @Inject
    private BizCmdHandler cmdHandler;
    public GetInfoHandler() {
    }

    public GetInfoHandler(PersistenceService persistenceService, CommonDao dao, DeviceManager deviceManager) {
        this.persistenceService = persistenceService;
        this.dao = dao;
        this.deviceManager = deviceManager;
    }

    /**
     * 获取设备信息
     * {
     * {
     * "getinfoType" : "getdevinfo",
     * "detail" : {
     * "pairDevBaseid" : "00000001812D506D00000394",
     * "gatewayId" : "866262040009895"
     * }
     * }
     *
     * @param topic
     * @param data
     */
    @Override
    public void execute(String topic, CmdMsg data) {

        cmdHandler.addCmdRule(new CmdRuleInfo(data),new InnerCmdCallBack());
//        JSONObject msg = data.getData();
//        String getinfotype = msg.getString("getinfoType");
//        JSONObject detail = msg.getJSONObject("detail");
//
//        switch (getinfotype) {
//            case "getdevinfo":
//                processGetDevInfo(detail);
//                break;
//        }
    }

    private class InnerCmdCallBack implements CmdCallback {

        @Override
        public void complete(String bizId, CmdRule cmdMsg) {

        }

        @Override
        public String getRespTopic(CmdMsg cmdMsg) {
            return "/E2ES/GateWay/Config/Response/"+ PathUtil.getPathLast(cmdMsg.getTopic());
        }
    }

    private void processGetDevInfo(JSONObject detail){
//        if (detail == null) {
//            return;
//        }
//        String gatewayid = detail.getString("gatewayId");
//        String pdb = detail.getString("pairDevBaseid");
//        DeviceInfo gatewayobj = deviceManager.getDeviceObj(gatewayid);
//        if (gatewayobj != null) {
//            DeviceInfo dobj = deviceManager.getDeviceObj(pdb);
//            if (dobj == null) return;
//            String gwold = dobj.getGatewayid();
//
//            if (gatewayobj.getPaireddevices().contains(pdb)) {
//
//                JSONObject cmd = CmdFactory.buildAddPairedDev(pdb,
//                        dobj.getBaseinfo().getDevicetype().toString(),
//                        dobj.getBaseinfo().getHardwareV(),
//                        dobj.getBaseinfo().getSoftwareV(),
//                        dobj.getBaseinfo().isSaverealtimedata(), dobj.isAutoOpt(),
//                        dobj);
////	    	            	    				logger.info(cmd.toString());
//                MyPublicObjs.dm.sendConfigCmd(gatewayid, cmd);
//
//                JSONObject cmd2 = CmdFactory.buildModifyDevAdvConfig(pdb, dobj.getBaseinfo().isAutoopt(), dobj.getBaseinfo().isSaverealtimedata());
////                             System.out.println(cmd.toString());
//                MyPublicObjs.dm.sendConfigCmd(gatewayid, cmd2);
//            } else {
//                if (!dobj.isIsonline() || "".equals(gwold) || gwold == null) {
//                    DevManResultBean dmrb = MyPublicObjs.dm.modifyGateWayId(pdb, gatewayid);
//                    if (dmrb.isSuccessed()) {
//                        if (!"".equals(gwold) && gwold != null) {
//                            JSONObject cmdremove = CmdFactory.buildRemovePairedDev(pdb);
//                            MyPublicObjs.dm.sendConfigCmd(gwold, cmdremove);
//                        }
//
//                        JSONObject cmd = CmdFactory.buildAddPairedDev(pdb,
//                                dobj.getBaseinfo().getDevicetype().toString(),
//                                dobj.getBaseinfo().getHardwareV(),
//                                dobj.getBaseinfo().getSoftwareV(),
//                                dobj.getBaseinfo().isSaverealtimedata(), dobj.isAutoOpt(),
//                                dobj);
////			    	            	    				logger.info(cmd.toString());
//                        MyPublicObjs.dm.sendConfigCmd(gatewayid, cmd);
//
//                        JSONObject cmd2 = CmdFactory.buildModifyDevAdvConfig(pdb, dobj.getBaseinfo().isAutoopt(), dobj.getBaseinfo().isSaverealtimedata());
////                             System.out.println(cmd.toString());
//                        MyPublicObjs.dm.sendConfigCmd(gatewayid, cmd2);
//                    }
//                } else {
//
//                }
//            }
//        }
    }

}
