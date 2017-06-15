package server;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import configs.ServerConfig;
import constants.ConstProtocol;
import constants.ConstRest;
import models.ByteSerial;
import models.DataMap;
import models.Pair;
import models.RestProcessor;
import mysql.DBManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.CropDaySubPOJO;
import pojo.CropSubPOJO;
import pojo.SettingPOJO;
import pojo.TimerPOJO;
import server.engine.ServiceProvider;
import server.response.Response;
import server.response.ResponseConst;
import spark.Spark;
import utils.SohaProtocolUtil;

import java.io.IOException;
import java.util.*;

/**
 * 서버 인스턴스 호출 및 실행을 위한 메인 클래스
 */
public class AppMain{

    private static Logger log;
    private static ServiceProvider serviceProvider;
    private static DBManager dbManager;

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
        /**
         * 서비스 프로바이더 인스턴스 할당 및 시동
         */
        serviceProvider = ServiceProvider.getInstance(); // 인스턴스 할당

        serviceProvider.setCustomTime(1000 * 10);
        /**
         * 주기성 배치 작업 콜백 인터페이스 위임
         */
        serviceProvider.offer(() -> {
            System.out.println("[DB Migration has started]");
            DBManager.getInstance().migrateFromRedis();
            System.out.println("[DB Migration has done]");
        });

        serviceProvider.start(); // 인스턴스 시동

        /**
         * Spark Framework Implementation
         * 스파크 프레임워크를 이용하여 경량 REST를 이용함으로써 WAS와 연동하도록 함
         */
        Spark.port(ConstRest.REST_PORT);

        Spark.get(ConstRest.REST_CONNECT_TEST, (req, res) -> {
            DataMap map = RestProcessor.makeProcessData(req.raw());

            for(String s : map.keySet()) System.out.println(s + " : " + map.get(s));

            log.info(ConstRest.REST_CONNECT_TEST);

            return RestProcessor.makeResultJson(0, "test", map);
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
            byte[][] protocols = null;
            ByteSerial recv;
            List<ByteSerial> recvs;
            String retVal = null;

            switch(mode){
                case ConstRest.MODE_READ_SETTING:

                    protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING.getHead(), ConstProtocol.RANGE_SETTING.getTail(), id, farmCode, harvCode);
                    System.out.println("READING SETTINGS - " + Arrays.toString(protocol));

                    recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol);
                    if(recv == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);
                    SettingPOJO settingPOJO = new SettingPOJO(recv, ConstProtocol.RANGE_READ_START, rawFarm, rawHarv);

                    System.out.println("SETTING BYTES : " + Arrays.toString(recv.getProcessed()));

                    protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING_TAILS.getHead(), ConstProtocol.RANGE_SETTING_TAILS.getTail(), id, farmCode, harvCode);
                    System.out.println("READING SETTING TAILS - " + Arrays.toString(protocol));

                    recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol);

                    settingPOJO.initTails(recv, ConstProtocol.RANGE_READ_START);

                    settingPOJO.setByteSerial(null);

                    retVal = objectMapper.writeValueAsString(settingPOJO);

                    DBManager.getInstance().execute(settingPOJO.getInsertSQL());

                    break;
                case ConstRest.MODE_READ_TIMER:
                    protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_TIMER.getHead(), ConstProtocol.RANGE_TIMER.getTail(), id, farmCode, harvCode);
                    System.out.println(Arrays.toString(protocol));

                    recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol);
                    if(recv == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);
                    TimerPOJO timerPOJO = new TimerPOJO(recv, ConstProtocol.RANGE_READ_START, rawFarm, rawHarv);

                    try {
                        String sql = timerPOJO.getInsertSQL();
                        DBManager.getInstance().execute(sql);
                        retVal = Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SUCCESS);
                    }catch (IOException e){
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

                    recvs = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocols);
                    if(recvs.size() <= 0) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_NONE);

                    CropSubPOJO cropPOJO = new CropSubPOJO(recvs, order, rawFarm, rawHarv);
                    cropPOJO.setByteSerial(null);

                    try {
                        String sql = cropPOJO.getInsertSQL();
                        DBManager.getInstance().execute(sql);
                        retVal = Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SUCCESS);
                    }catch (IOException e){
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
         * mode : read_timer, read_dayage(order 필요/누락 시 전체 일령)
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
                case ConstRest.MODE_WRITE_TIMER: {
                    // TODO START POINT
                    /**
                     * 일령 리스트 쓰기
                     * 실시간 데이터 쓰기
                     * 경보 쓰기
                     * 플래그 초기화
                     * 06/15 일일보고서 작성
                     */

                    try {
                        System.out.println("WRITING TIMER " + rawFarm + ":" + rawHarv);
                        TimerPOJO timerPOJO = objectMapper.readValue(rawJson, TimerPOJO.class);
                        byte[] pure = new ByteSerial(timerPOJO.getBytes(), ByteSerial.TYPE_FORCE).getPureBytes();
                        protocol = SohaProtocolUtil.makeWriteProtocol(ConstProtocol.RANGE_TIMER.getHead(), ConstProtocol.RANGE_TIMER.getTail(), id, farmCode, harvCode, pure);
                        recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol);

                        if(recv == null) return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_INVALID_PARAM);
                        if(recv.isLoss()) {
                            return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                        }else{
                            protocol = SohaProtocolUtil.makeFlagNotifyProtocol(id, farmCode, harvCode, ConstProtocol.FLAG_TIMER);
                            serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol);
                        }

                        System.out.println("WRITE ::::::::::::::::: " + Arrays.toString(recv.getProcessed()));

                        return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SAVE_SUCC);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                    }
                }
                case ConstRest.MODE_WRITE_DAYAGE: {

                    System.out.println("WRITING DAILY AGE " + rawFarm + ":" + rawHarv);
                    try{
                        CropSubPOJO cropSubPOJO = objectMapper.readValue(rawJson, CropSubPOJO.class);
                        return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SAVE_SUCC);
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
                        recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol);

                        if(recv != null && !recv.isLoss()) multiCount++;

                        pure = new ByteSerial(settingPOJO.getTailBytes(), ByteSerial.TYPE_FORCE).getPureBytes();
                        protocol = SohaProtocolUtil.makeWriteProtocol(ConstProtocol.RANGE_SETTING_TAILS.getHead(), ConstProtocol.RANGE_SETTING_TAILS.getTail(), id, farmCode, harvCode, pure);
                        recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol);

                        if(recv != null && !recv.isLoss()) multiCount++;

                        if(multiCount != confirmCount) {
                            return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                        }else{
                            protocol = SohaProtocolUtil.makeFlagNotifyProtocol(id, farmCode, harvCode, ConstProtocol.FLAG_SETTING);
                            serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol);
                        }

                        System.out.println("WRITE ::::::::::::::::: " + Arrays.toString(recv.getProcessed()));

                        return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SAVE_SUCC);
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
