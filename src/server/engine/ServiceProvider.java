package server.engine;

import configs.ServerConfig;
import server.ProtocolResponder;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * @author 함의진
 */
public class ServiceProvider extends ServerConfig{

    private static ServiceProvider instance;

    private Thread thread;
    private ServerSocket socket;
    private HashMap clients;

    private ServiceProvider(int port){
        try {
            socket = new ServerSocket(port);
            clients = new HashMap();
            Collections.synchronizedMap(clients);
        }catch(IOException e){
            e.printStackTrace();
            d("Socket Creation failed");
        }

        thread = new Thread(() -> {
            while(true){
                try {
                    d("Socket is pending until receiving");
                    Socket sock = socket.accept();
                    d("Connection Requested from [" + sock.getRemoteSocketAddress() + "]");

                    ProtocolResponder protocolResponder = new ProtocolResponder(sock, clients);
                    protocolResponder.start();

                }catch(IOException e){
                    e.printStackTrace();
                    d("ACK Failed");
                }

            }
        });

        d("Server is ready to respond");
    }

    public void start(){
        thread.start();
    }

    public static ServiceProvider getInstance(int port){
        if(instance == null) instance = new ServiceProvider(port);
        return instance;
    }

    private void d(String message){
        if(DEBUG_MODE) stream.println(getTime() + " " + message);
    }

}
