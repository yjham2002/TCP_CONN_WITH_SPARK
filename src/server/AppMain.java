package server;

import configs.ServerConfig;
import constants.ConstRest;
import models.ByteSerial;
import models.DataMap;
import models.RestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.engine.ServiceProvider;
import spark.Spark;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import static spark.route.HttpMethod.get;

/**
 * 서버 인스턴스 호출 및 실행을 위한 메인 클래스
 */
public class AppMain{

    private static Logger log;
    private static ServiceProvider serviceProvider;

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
        /**
         * 서비스 프로바이더 인스턴스 할당 및 시동
         */
        serviceProvider = ServiceProvider.getInstance().start(); // 인스턴스 할당 및 시동

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

        Spark.get(ConstRest.REST_READ_REQUEST, (req, res) -> {
            DataMap map = RestProcessor.makeProcessData(req.raw());
            log.info(ConstRest.REST_READ_REQUEST);
            serviceProvider.send(map.getString("cli"), new ByteSerial(new byte[]{0,0,0,0,0, 1}));
            return "{\"json\":1}";
        });

        Spark.get(ConstRest.REST_WRITE_REQUEST, (req, res) -> {
            log.info(ConstRest.REST_WRITE_REQUEST);
            return "{\"json\":1}";
        });

        /**
         * 스파크 프레임워크 REST 정의 종결
         */

    }

}
