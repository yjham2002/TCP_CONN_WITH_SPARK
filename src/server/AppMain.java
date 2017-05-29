package server;

import configs.ServerConfig;
import server.engine.ServiceProvider;

/**
 * 서버 인스턴스 호출 및 실행을 위한 메인 클래스
 */
public class AppMain{

    public static void main(String... args){
        ServiceProvider serviceProvider = ServiceProvider.getInstance(ServerConfig.SOCKET_PORT);
        serviceProvider.start();
    }

}
