package server;

import configs.ServerConfig;
import server.engine.ServiceProvider;

public class AppMain{

    public static void main(String... args){
        ServiceProvider serviceProvider = ServiceProvider.getInstance(ServerConfig.SOCKET_PORT);
        serviceProvider.start();
    }

}
