package server;

import agent.AlertAgent;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import configs.ServerConfig;
import constants.ConstProtocol;
import constants.ConstRest;
import databases.DBManager;
import models.ByteSerial;
import models.DataMap;
import models.Pair;
import models.RestProcessor;
import mysql.Cache;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.*;
import server.engine.ServiceProvider;
import server.response.Response;
import server.response.ResponseConst;
import spark.Spark;
import utils.DataMapValidationUtil;
import utils.SohaProtocolUtil;
import java.util.*;

import static constants.ConstProtocol.LENGTH_JUMP_DAYAGE_DETAIL;
import static constants.ConstProtocol.LENGTH_JUMP_DAYAGE_NAME;

/**
 * 서버 인스턴스 호출 및 실행을 위한 메인 클래스
 */
public class AppMain{

    private static Logger log;
    private static ServiceProvider serviceProvider;
    private static DBManager dbManager;
    private static Cache cache;
    private static AlertAgent alertAgent;

    /**
     * 정적으로 설정된 소켓 포트를 기반으로 싱글턴 패턴 서버 인스턴스를 할당 후 실행
     * 비고 : 서비스 프로바이더의 경우, 가변 스레딩을 운영하기 위한 스레드이며,
     * 필히 싱글턴 설계를 유지하여야 하며, 개발 편의를 위해 데코레이터 패턴으로 작성하는 것이 바람직함
     * @param args
     */
    public static void main(String... args){

        /**
         * 로거 초기화
         */
        log = LoggerFactory.getLogger("TCP/IP ServerEngine App");
        dbManager = DBManager.getInstance();
//        dbManager.setDebug(true);
        cache = Cache.getInstance();
        alertAgent = AlertAgent.getInstance();
        /**
         * 서비스 프로바이더 인스턴스 할당 및 시동
         */
        serviceProvider = ServiceProvider.getInstance(); // 인스턴스 할당

        serviceProvider.setCustomTime(1000 * 30);
        /**
         * 주기성 배치 작업 콜백 인터페이스 위임
         */
        serviceProvider.offer(() -> {
            System.out.println("[DB Migration has started]");
//            DBManager.getInstance().migrateFromRedis();
            System.out.println("[DB Migration has done]");
        });

        alertAgent.start(20);
        serviceProvider.start(); // 인스턴스 시동

        /**
         * Spark Framework Implementation
         * 스파크 프레임워크를 이용하여 경량 REST를 이용함으로써 WAS와 연동하도록 함
         */
        Spark.port(ConstRest.REST_PORT);

        Spark.get(ConstRest.REST_CACHE, (req, res) -> {
            Cache.getInstance().recache();
            return RestProcessor.makeResultJson(1, "Cache Done.");
        });

        Spark.get(ConstRest.REST_CONNECT_TEST, (req, res) -> {
            DataMap map = RestProcessor.makeProcessData(req.raw());

            String rawFarm = map.getString(ConstRest.FARM_CODE);
            String rawHarv = map.getString(ConstRest.HARV_CODE);
            byte[] farmCode = rawFarm.getBytes();

            if(serviceProvider.getClients().containsKey(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode))){
                return RestProcessor.makeResultJson(1, "TRUE");
            }else{
                return RestProcessor.makeResultJson(0, "FALSE");
            }

        });

        /**
         * READ 요청 API
         * id, mode, farm, harv, order(optional)
         * mode : read_timer, read_dayage(order 필요/누락 시 전체 일령)
         */
        Spark.get(ConstRest.REST_READ_REQUEST, (req, res) -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            DataMap map = RestProcessor.makeProcessData(req.raw());
            log.info(ConstRest.REST_READ_REQUEST);

            ObjectMapper objectMapper = new ObjectMapper();

            String mode = map.getString("mode");
            String rawFarm = map.getString(ConstRest.FARM_CODE);
            String rawHarv = map.getString(ConstRest.HARV_CODE);
            int id = 0;
            if(map.get("id") == null && !rawHarv.equals("")) id = Integer.parseInt(rawHarv);
            else id = map.getInt("id");

            if(id == 0){
                System.out.println("No appropriate machine code for " + rawFarm + ":" + rawHarv);
                return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);
            }else{
                System.out.println("Machine Code :: " + id);
            }

            byte[] farmCode = rawFarm.getBytes();
            byte[] harvCode = rawHarv.getBytes();
            int order = map.getInt("order");

            if(map.get("mode") == null || map.get(ConstRest.FARM_CODE) == null || map.get(ConstRest.HARV_CODE) == null){
                return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_PARAM);
            }

            byte[] protocol = null;
            byte[] protocol_sub = null;
            byte[][] protocols = null;
            ByteSerial recv;
            List<ByteSerial> recvs;
            String retVal = null;

            switch(mode){
                case ConstRest.MODE_READ_REALTIME:

                    protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_REALTIME_READABLE.getHead(), ConstProtocol.RANGE_REALTIME_READABLE.getTail(), id, farmCode, harvCode, 1);
                    protocol_sub = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_REALTIME_READABLE_TAILS.getHead(), ConstProtocol.RANGE_REALTIME_READABLE_TAILS.getTail(), id, farmCode, harvCode, 2);
                    protocols = new byte[][]{protocol_sub.clone(), protocol.clone()};
                    recvs = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocols, new int[]{ConstProtocol.RESPONSE_LEN_REAL_SUB, ConstProtocol.RESPONSE_LEN_REAL});

                    if(recvs == null || recvs.size() != 2) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);
                    else{
                        try {
                            byte[] pure = ByteSerial.getPureDataConcatForRealtime(recvs);
                            if(pure == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);

                            ByteSerial rSerial = new ByteSerial(SohaProtocolUtil.concat(ConstProtocol.STX, farmCode, harvCode, pure, new byte[]{0}, ConstProtocol.ETX), ByteSerial.TYPE_FORCE);


                            System.out.println("=============================================================");
                            System.out.println("REALTIME DATA :: " + rSerial.getProcessed().length + " / " + Arrays.toString(rSerial.getProcessed()));
                            System.out.println("=============================================================");

                            RealtimePOJO realtimePOJO = new RealtimePOJO(rSerial);

                            WrappedPOJO wrappedPOJO = new WrappedPOJO(realtimePOJO, rawFarm, rawHarv);

                            AlertAgent.getInstance().getBlockingQueue().put(wrappedPOJO);
                            try {
                                DBManager.getInstance().execute(realtimePOJO.getInsertSQL());
                                return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SUCCESS);
                            }catch (Exception e){
                                return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);
                            }
                        }catch (ArrayIndexOutOfBoundsException e){
                            e.printStackTrace();
                            return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);
                        }

                    }

                case ConstRest.MODE_READ_SETTING:

                    protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING.getHead(), ConstProtocol.RANGE_SETTING.getTail(), id, farmCode, harvCode);
                    System.out.println("READING SETTINGS - " + Arrays.toString(protocol));

                    recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_SETTING);
                    if(recv == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);
                    SettingPOJO settingPOJO = new SettingPOJO(recv, ConstProtocol.RANGE_READ_START, rawFarm, rawHarv);

                    System.out.println("SETTING BYTES : " + Arrays.toString(recv.getProcessed()));

                    protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING_TAILS.getHead(), ConstProtocol.RANGE_SETTING_TAILS.getTail(), id, farmCode, harvCode);
                    System.out.println("READING SETTING TAILS - " + Arrays.toString(protocol));

                    recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_SETTING_TAIL);

                    if(recv == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_FAILURE);

                    System.out.println("============================================================================================");
                    System.out.println(Arrays.toString(recv.getProcessed()));
                    System.out.println("============================================================================================");

                    settingPOJO.initTails(recv, ConstProtocol.RANGE_READ_START);

                    try {
                        settingPOJO.setByteSerial(null);
                        DBManager.getInstance().execute(settingPOJO.getInsertSQL());
                        retVal = Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SUCCESS);
                    }catch (Exception e){
                        retVal = Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_FAILURE);
                    }

                    break;
                case ConstRest.MODE_READ_TIMER:
                    protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_TIMER.getHead(), ConstProtocol.RANGE_TIMER.getTail(), id, farmCode, harvCode);
                    System.out.println(Arrays.toString(protocol));

                    recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_TIMER);

                    if(recv == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);
                    TimerPOJO timerPOJO = new TimerPOJO(recv, ConstProtocol.RANGE_READ_START, rawFarm, rawHarv);

                    try {
                        String sql = timerPOJO.getInsertSQL();
                        DBManager.getInstance().execute(sql);
                        retVal = Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SUCCESS);
                    }catch (Exception e){
                        retVal = Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_FAILURE);
                    }

                    break;
                case ConstRest.MODE_READ_DAYAGE:
                    Pair<Integer> range = ConstProtocol.RANGE_DAYAGE;
                    switch (order){
                        case 1: range = ConstProtocol.RANGE_DAYAGE_01; break;
                        case 2: range = ConstProtocol.RANGE_DAYAGE_02; break;
                        case 3: range = ConstProtocol.RANGE_DAYAGE_03; break;
                        case 4: range = ConstProtocol.RANGE_DAYAGE_04; break;
                        case 5: range = ConstProtocol.RANGE_DAYAGE_05; break;
                        case 6: range = ConstProtocol.RANGE_DAYAGE_06; break;
                        default: order = -1; break;
                    }

                    if(order == -1 || order > 6) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_PARAM);

                    protocols = SohaProtocolUtil.makeReadProtocols(range.getHead(), range.getTail(), id, farmCode, harvCode);

                    int[] lens = new int[]{
                            ConstProtocol.RESPONSE_LEN_DAILY,
                            ConstProtocol.RESPONSE_LEN_DAILY,
                            ConstProtocol.RESPONSE_LEN_DAILY,
                            ConstProtocol.RESPONSE_LEN_DAILY,
                            ConstProtocol.RESPONSE_LEN_DAILY,
                            ConstProtocol.RESPONSE_LEN_DAILY,
                            ConstProtocol.RESPONSE_LEN_DAILY,
                            ConstProtocol.RESPONSE_LEN_DAILY_TAIL
                    };

                    recvs = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocols, lens);
                    if(recvs == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);
                    if(recvs.size() <= 0) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);

                    CropSubPOJO cropPOJO = new CropSubPOJO(recvs, order, rawFarm, rawHarv);
                    cropPOJO.setByteSerial(null);

                    try {
                        String sql = cropPOJO.getInsertSQL();
                        DBManager.getInstance().execute(sql);
                        retVal = Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SUCCESS);
                    }catch (Exception e){
                        retVal = Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_FAILURE);
                    }

                    break;
                default: protocols = null; break;
            }

            if((protocols == null && protocol == null) || retVal == null){
                log.info("Mode has not been designated - Do nothing");
                return Response.response(ResponseConst.CODE_INVALID_PARAM, ResponseConst.MSG_INVALID_PARAM);
            }else{
                return retVal;
            }

        });

        /**
         * WRITE 요청 API
         * id, mode, farm, harv, order(optional)
         * mode : read_timer, read_dayage(order 필요)
         */
        Spark.post(ConstRest.REST_WRITE_REQUEST, (req, res) -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            DataMap map = RestProcessor.makeProcessData(req.raw());

            log.info(ConstRest.REST_WRITE_REQUEST);

            ObjectMapper objectMapper = new ObjectMapper();

            if(map.get("mode") == null || map.get(ConstRest.FARM_CODE) == null || map.get(ConstRest.HARV_CODE) == null){
                return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_PARAM);
            }

            String mode = map.getString("mode");
            String rawFarm = map.getString(ConstRest.FARM_CODE);
            String rawHarv = map.getString(ConstRest.HARV_CODE);
            String rawJson = map.getString(ConstRest.JSON_CODE);

            int id = 0;
            if(map.get("id") == null && !rawHarv.equals("")) id = Integer.parseInt(rawHarv);
            else id = map.getInt("id");

            byte[] farmCode = rawFarm.getBytes();
            byte[] harvCode = rawHarv.getBytes();
            int order = map.getInt("order");

            if(map.get("mode") == null){
                return Response.response(ResponseConst.CODE_INVALID_PARAM, ResponseConst.MSG_INVALID_PARAM);
            }

            byte[] protocol = null;
            byte[][] protocols = null;
            ByteSerial recv;
            List<ByteSerial> recvs;
            String retVal = null;

            switch(mode) {
                case ConstRest.MODE_WRITE_REALTIME:{

                    boolean initExist = false;
                    int initFlag = 0;

                    if(DataMapValidationUtil.isValid(map, "init")){
                        initExist = true;
                        initFlag = map.getInt("init");
                    }

                    boolean isIcon = (initExist) && (initFlag == 1);

                    try {
                        System.out.println("WRITING REALTIME " + rawFarm + ":" + rawHarv);
                        RealtimePOJO realPOJO = objectMapper.readValue(rawJson, RealtimePOJO.class);

                        System.out.println("==============================================================================");
                        System.out.println("MCN_CTRL_WEB_ORDER_CO2      : " + realPOJO.getMcnctrl_web_order_co2());
                        System.out.println("MCN_CTRL_WEB_ORDER_TEMP     : " + realPOJO.getMcnctrl_web_order_temp());
                        System.out.println("MCN_CTRL_WEB_ORDER_HUMID    : " + realPOJO.getMcnctrl_web_order_humidity());
                        System.out.println("MCN_CTRL_WEB_ORDER_ILLUM    : " + realPOJO.getMcnctrl_web_order_ilum());
                        System.out.println("==============================================================================");
                        System.out.println("MCN_CTRL_WEB_STAT_FAN       : " + realPOJO.getMcnctrl_web_stat_fan());
                        System.out.println("MCN_CTRL_WEB_STAT_HEATER    : " + realPOJO.getMcnctrl_web_stat_heater());
                        System.out.println("MCN_CTRL_WEB_STAT_FREEZER   : " + realPOJO.getMcnctrl_web_stat_freezer());
                        System.out.println("MCN_CTRL_WEB_STAT_HUMID     : " + realPOJO.getMcnctrl_web_stat_humidifier());
                        System.out.println("MCN_CTRL_WEB_STAT_DEHUM     : " + realPOJO.getMcnctrl_web_stat_dehumidifier());
                        System.out.println("MCN_CTRL_WEB_STAT_ILLUM     : " + realPOJO.getMcnctrl_web_stat_ilum());
                        System.out.println("MCN_CTRL_WEB_STAT_ALARM     : " + realPOJO.getMcnctrl_web_stat_alarm());
                        System.out.println("==============================================================================");

                        byte[] pure = realPOJO.getWritableData();
                        protocol = SohaProtocolUtil.makeWriteProtocol(ConstProtocol.RANGE_REALTIME_WRITABLE.getHead(), ConstProtocol.RANGE_REALTIME_WRITABLE.getTail(), id, farmCode, harvCode, pure);
                        recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);

                        if(recv == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_PARAM);
                        if(recv.isLoss()) {
                            return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                        }
                        /**
                         * 110 Setting Flag 제거
                         */
                        else{
                            if(!isIcon) {
                                try{
                                    Thread.sleep(1 * (protocol.length - 19));
                                }catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                                protocol = SohaProtocolUtil.makeFlagNotifyProtocol(id, farmCode, harvCode, ConstProtocol.FLAG_SETTING);
                                serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);
                            }
                        }

                        System.out.println("WRITE ::::::::::::::::: " + Arrays.toString(recv.getProcessed()));

                        return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SAVE_SUCC);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                    }
                }
                case ConstRest.MODE_WRITE_TIMER: {

                    // TODO START POINT
                    /**
                     * 실시간 데이터 쓰기
                     * 경보 쓰기
                     * 플래그를 통한 새로고침 이후 이니셜라이징
                     * 플래그를 통한 새로고침 로직
                     */

                    try {
                        System.out.println("WRITING TIMER " + rawFarm + ":" + rawHarv);
                        TimerPOJO timerPOJO = objectMapper.readValue(rawJson, TimerPOJO.class);
                        byte[] pure = new ByteSerial(timerPOJO.getBytes(), ByteSerial.TYPE_FORCE).getPureBytes();
                        protocol = SohaProtocolUtil.makeWriteProtocol(ConstProtocol.RANGE_TIMER.getHead(), ConstProtocol.RANGE_TIMER.getTail(), id, farmCode, harvCode, pure);
                        recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);

                        if(recv == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_PARAM);
                        if(recv.isLoss()) {
                            return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                        }else{
                            try{
                                Thread.sleep(1 * (protocol.length - 19));
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                            protocol = SohaProtocolUtil.makeFlagNotifyProtocol(id, farmCode, harvCode, ConstProtocol.FLAG_TIMER);
                            recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);
                        }

                        System.out.println("WRITE ::::::::::::::::: " + Arrays.toString(recv.getProcessed()));

                        return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SAVE_SUCC);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                    }
                }
                case ConstRest.MODE_WRITE_DAYAGE_ONCE: {

                    System.out.println("WRITING DAILY AGE ONCE " + rawFarm + ":" + rawHarv);
                    try{
                        if(!DataMapValidationUtil.isValid(map, "order", "day", "index")) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_PARAM);

                        int dayOrder = map.getInt("day");
                        int index = map.getInt("index");

                        if(dayOrder < 0 || dayOrder > 49 || index > 2 || index < 0) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_RANGE);

                        Pair<Integer> range;
                        int dailyFlag = -1;

                        switch (order){
                            case 1: range = ConstProtocol.RANGE_DAYAGE_01; dailyFlag = ConstProtocol.FLAG_DAILY_1; break;
                            case 2: range = ConstProtocol.RANGE_DAYAGE_02; dailyFlag = ConstProtocol.FLAG_DAILY_2; break;
                            case 3: range = ConstProtocol.RANGE_DAYAGE_03; dailyFlag = ConstProtocol.FLAG_DAILY_3; break;
                            case 4: range = ConstProtocol.RANGE_DAYAGE_04; dailyFlag = ConstProtocol.FLAG_DAILY_4; break;
                            case 5: range = ConstProtocol.RANGE_DAYAGE_05; dailyFlag = ConstProtocol.FLAG_DAILY_5; break;
                            case 6: range = ConstProtocol.RANGE_DAYAGE_06; dailyFlag = ConstProtocol.FLAG_DAILY_6; break;
                            default: range = null; order = -1; break;
                        }

                        if(order == -1 || range == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_PARAM);

                        CropSubPOJO cropSubPOJO = objectMapper.readValue(rawJson, CropSubPOJO.class);

                        System.out.println("RAW :: " + rawJson);
                        System.out.println("PARSE :: " + objectMapper.writeValueAsString(cropSubPOJO));

                        /**
                         * dayOrder는 0~49 범위의 정수(의미상의 일령 순서가 아님)
                         * index는 0~2 범위의 정수(의미상의 차수 정보가 아님)
                         */
                        CropDaySubPOJO cropUnit = cropSubPOJO.getCropDaySubPOJOs().get(dayOrder);
                        byte[] unitByte = cropUnit.getUnitBytes(index);

                        int toWrite = range.getHead() + LENGTH_JUMP_DAYAGE_NAME + (LENGTH_JUMP_DAYAGE_DETAIL * 3 * dayOrder) + (LENGTH_JUMP_DAYAGE_DETAIL * index);
                        int wordLen = LENGTH_JUMP_DAYAGE_DETAIL / 2;

                        protocol = SohaProtocolUtil.makeWriteProtocol(toWrite, wordLen, id, farmCode, harvCode, unitByte);
                        recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);

                        if(recv != null){
                            System.out.println("WRITE ::::::::::::::::: DAILY AGE (UNIT) SUCCEEDED");
                            try{
                                Thread.sleep(1 * (protocol.length - 19));
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                            protocol = SohaProtocolUtil.makeFlagNotifyProtocol(id, farmCode, harvCode, dailyFlag);
                            serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);

                            try {
                                String sql = cropSubPOJO.getInsertSQL();
                                DBManager.getInstance().execute(sql);
                                return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SUCCESS);
                            }catch (Exception e){
                                return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_FAILURE);
                            }
                        }
                        else return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                    }catch(Exception e){
                        e.printStackTrace();
                        return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                    }
                }

                case ConstRest.MODE_WRITE_DAYAGE: {

                    System.out.println("WRITING DAILY AGE " + rawFarm + ":" + rawHarv);
                    try{
                        if(map.get("order") == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_PARAM);

                        Pair<Integer> range;
                        int dailyFlag = -1;

                        switch (order){
                            case 1: range = ConstProtocol.RANGE_DAYAGE_01; dailyFlag = ConstProtocol.FLAG_DAILY_1; break;
                            case 2: range = ConstProtocol.RANGE_DAYAGE_02; dailyFlag = ConstProtocol.FLAG_DAILY_2; break;
                            case 3: range = ConstProtocol.RANGE_DAYAGE_03; dailyFlag = ConstProtocol.FLAG_DAILY_3; break;
                            case 4: range = ConstProtocol.RANGE_DAYAGE_04; dailyFlag = ConstProtocol.FLAG_DAILY_4; break;
                            case 5: range = ConstProtocol.RANGE_DAYAGE_05; dailyFlag = ConstProtocol.FLAG_DAILY_5; break;
                            case 6: range = ConstProtocol.RANGE_DAYAGE_06; dailyFlag = ConstProtocol.FLAG_DAILY_6; break;
                            default: range = null; order = -1; break;
                        }

                        if(order == -1 || range == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_PARAM);

                        CropSubPOJO cropSubPOJO = objectMapper.readValue(rawJson, CropSubPOJO.class);

//                        System.out.println("RAW :: " + rawJson);

                        int resCount = 0;

                        byte[] pure = cropSubPOJO.getPureBytes();
                        protocols = SohaProtocolUtil.makeWriteProtocols(range.getHead(), range.getTail(), id, farmCode, harvCode, pure);

                        for(int c = 0; c < protocols.length; c++){
                            protocol = protocols[c];
                            recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);
                            try{
                                Thread.sleep(1 * (protocol.length - 19));
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                            if(recv != null) resCount++;
                        }

                        if(resCount == protocols.length){
                            System.out.println("WRITE ::::::::::::::::: DAILY AGE SUCC");
                            protocol = SohaProtocolUtil.makeFlagNotifyProtocol(id, farmCode, harvCode, dailyFlag);
                            serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);

                            try {
                                String sql = cropSubPOJO.getInsertSQL();
                                DBManager.getInstance().execute(sql);
                                return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SUCCESS);
                            }catch (Exception e){
                                return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_FAILURE);
                            }
                        }
                        else return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                    }catch(Exception e){
                        e.printStackTrace();
                        return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                    }
                }
                case ConstRest.MODE_WRITE_SETTING:{

                    System.out.println("WRITING SETTING " + rawFarm + ":" + rawHarv);
                    try{
                        int confirmCount = 2;
                        int multiCount = 0;

                        SettingPOJO settingPOJO = objectMapper.readValue(rawJson, SettingPOJO.class);

                        byte[] pure = new ByteSerial(settingPOJO.getBytes(), ByteSerial.TYPE_FORCE).getPureBytes();
                        protocol = SohaProtocolUtil.makeWriteProtocol(ConstProtocol.RANGE_SETTING.getHead(), ConstProtocol.RANGE_SETTING.getTail(), id, farmCode, harvCode, pure);
                        recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);

                        if(recv != null && !recv.isLoss()) multiCount++;

                        try{
                            Thread.sleep(1 * (protocol.length - 19));
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }

                        pure = new ByteSerial(settingPOJO.getTailBytes(), ByteSerial.TYPE_FORCE).getPureBytes();
                        protocol = SohaProtocolUtil.makeWriteProtocol(ConstProtocol.RANGE_SETTING_TAILS.getHead(), ConstProtocol.RANGE_SETTING_TAILS.getTail(), id, farmCode, harvCode, pure);
                        recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);

                        if(recv != null && !recv.isLoss()) multiCount++;

                        try{
                            Thread.sleep(1 * (protocol.length - 19));
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }

                        if(multiCount != confirmCount) {
                            return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                        }else{
                            protocol = SohaProtocolUtil.makeFlagNotifyProtocol(id, farmCode, harvCode, ConstProtocol.FLAG_SETTING, ConstProtocol.FLAG_DAILY_6);
                            System.out.println("Sending Flag :::::::::::::::: " + Arrays.toString(protocol));
                            serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol, ConstProtocol.RESPONSE_LEN_WRITE);
                        }

                        System.out.println("WRITE ::::::::::::::::: " + Arrays.toString(recv.getProcessed()));

                        try {
                            settingPOJO.setByteSerial(null);
                            DBManager.getInstance().execute(settingPOJO.getInsertSQL());
                            return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SUCCESS);
                        }catch (Exception e){
                            return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_FAILURE);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                    }
                }
                default: {
                    return Response.response(ResponseConst.CODE_INVALID_PARAM, ResponseConst.MSG_INVALID_PARAM);
                }
            }

        });

        /**
         * 스파크 프레임워크 REST 정의 종결
         */

    }

}
