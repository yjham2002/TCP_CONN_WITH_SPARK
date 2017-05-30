package server;

import configs.ServerConfig;
import server.engine.ServiceProvider;

/**
 * 서버 인스턴스 호출 및 실행을 위한 메인 클래스
 */
public class AppMain{

    /**
     * 정적으로 설정된 소켓 포트를 기반으로 싱글턴 패턴 서버 인스턴스를 할당 후 실행
     * 비고 : 서비스 프로바이더의 경우, 가변 스레딩을 운영하기 위한 스레드이며,
     * 필히 싱글턴 설계를 유지하여야 하며, 개발 편의를 위해 데코레이터 패턴으로 작성하는 것이 바람직함
     * @param args
     */
    public static void main(String... args){
        ServiceProvider serviceProvider = ServiceProvider.getInstance(ServerConfig.SOCKET_PORT).start(); // 인스턴스 할당 및 시동
    }

}
