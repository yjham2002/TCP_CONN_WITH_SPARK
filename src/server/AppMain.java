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
import pojo.CropWrappingPOJO;
import pojo.SettingPOJO;
import pojo.TimerPOJO;
import redis.ICallback;
import server.engine.ServiceProvider;
import server.response.Response;
import server.response.ResponseConst;
import spark.Spark;
import utils.SohaProtocolUtil;

import java.util.*;

import static constants.ConstRest.RESPONSE_INVALID;
import static constants.ConstRest.RESPONSE_NONE;
import static spark.route.HttpMethod.get;

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
            }else{
                System.out.println("Machine Code :: " + id);
            }

            byte[] farmCode = rawFarm.getBytes();
            byte[] harvCode = rawHarv.getBytes();
            int order = map.getInt("order");

            if(map.get("mode") == null || map.get(ConstRest.FARM_CODE) == null || map.get(ConstRest.HARV_CODE) == null){
                return RESPONSE_INVALID;
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
                    if(recv == null) return RESPONSE_NONE;
                    SettingPOJO settingPOJO = new SettingPOJO(recv, ConstProtocol.RANGE_READ_START, rawFarm, rawHarv);

                    // TODO For Debugging
                    System.out.println("ORIGIN : " + Arrays.toString(recv.getProcessed()));
                    System.out.println("BYTES  : " + Arrays.toString(settingPOJO.getBytes()));
                    // TODO For Debugging

                    protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING_TAILS.getHead(), ConstProtocol.RANGE_SETTING_TAILS.getTail(), id, farmCode, harvCode);
                    System.out.println("READING SETTING TAILS - " + Arrays.toString(protocol));

                    recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol);

                    settingPOJO.initTails(recv, ConstProtocol.RANGE_READ_START);

                    settingPOJO.setByteSerial(null);

                    retVal = objectMapper.writeValueAsString(settingPOJO);

                    break;
                case ConstRest.MODE_READ_TIMER:
                    protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_TIMER.getHead(), ConstProtocol.RANGE_TIMER.getTail(), id, farmCode, harvCode);
                    System.out.println(Arrays.toString(protocol));

                    recv = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocol);
                    if(recv == null) return RESPONSE_NONE;
                    TimerPOJO timerPOJO = new TimerPOJO(recv, ConstProtocol.RANGE_READ_START);
                    retVal = objectMapper.writeValueAsString(timerPOJO);
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

                    protocols = SohaProtocolUtil.makeReadProtocols(range.getHead(), range.getTail(), id, farmCode, harvCode);

                    for(byte[] aaa : protocols){
                        System.out.println(Arrays.toString(aaa));
                    }

                    if(true) return "";

                    recvs = serviceProvider.send(SohaProtocolUtil.getUniqueKeyByFarmCode(farmCode), protocols);
                    if(recvs.size() <= 0) return RESPONSE_NONE;

                    CropWrappingPOJO cropWrappingPOJO = new CropWrappingPOJO(recvs, order);
                    retVal = objectMapper.writeValueAsString(cropWrappingPOJO);
                    break;
                default: protocols = null; break;
            }

            // TODO START POINT
            /**
             * MySQL 컨버터가 필요함
             * 쓰기 명령 작성해야 함
             * 쓰기 명령 시 프로토콜을 나누어 처리하는 로직이 필요함
             * 인덱싱 이슈 정확히 살펴봐야 함
             * 기계 작동 여부 확인
             * MySQL 업서트 패턴 구현
             * 제어 플래그 업데이트 - 필히 Bit Clear
             * 전달 3단 제어 체크
             * 쓰기 명령 프로토콜 생성 시 역방향 파싱이 필요
             * 타임아웃 에러 핸들링 코드에서 경보 문자 로직 작성
             * 기계 연결 - 저장 - 에러 이슈 별도 저장(MySQL) - 이후 필요 시 REST 요청에 응답
             *
             * [작업 진행 계획]
             * 위 TCP/IP 프로토콜 통신 모두 완료
             * 각 UI단에서 MySQL과 REST 중 데이터를 받을 곳을 확정해야 함
             * REST 연결 및 소켓 연결 유니크키 테스트
             * 유니크키 로직 공유
             */

            if((protocols == null && protocol == null) || retVal == null){
                log.info("Mode has not been designated - Do nothing");
                return Response.response(ResponseConst.CODE_INVALID_PARAM, ResponseConst.MSG_INVALID_PARAM);
            }else{
                return retVal;
            }

        });

        /**
         * WRITE 요청 API
         */
        Spark.post(ConstRest.REST_WRITE_REQUEST, (req, res) -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            DataMap map = RestProcessor.makeProcessData(req.raw());

            log.info(ConstRest.REST_WRITE_REQUEST);

            ObjectMapper objectMapper = new ObjectMapper();

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

            switch(mode) {
                case ConstRest.MODE_WRITE_TIMER: {
                    try {
                        TimerPOJO timerPOJO = objectMapper.readValue(rawJson, TimerPOJO.class);
                        return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SAVE_SUCC);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Response.response(ResponseConst.CODE_FAILURE, ResponseConst.MSG_SAVE_FAIL);
                    }
                }
                case ConstRest.MODE_WRITE_DAYAGE: {
                    return Response.response(ResponseConst.CODE_SUCCESS, ResponseConst.MSG_SUCCESS);
                }
                case ConstRest.MODE_WRITE_SETTING:{
                    try{
                        SettingPOJO settingPOJO = objectMapper.readValue(rawJson, SettingPOJO.class);

                        // TODO setting Tails to byte
                        //SohaProtocolUtil.makeWriteProtocol(ConstProtocol.RANGE_SETTING.getHead(), ConstProtocol.RANGE_SETTING.getTail(), id, farmCode, harvCode, settingPOJO.getBytes());
                        // TODO sending protocol for 2 times for writing value on different address

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
