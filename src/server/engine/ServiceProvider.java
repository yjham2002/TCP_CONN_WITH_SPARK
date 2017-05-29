package server.engine;

import configs.ServerConfig;
import server.ProtocolResponder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author 함의진
 * 가변 스레딩을 위한 운영 개체 스레드가 운영되는 곳으로, 이곳으로부터
 * 각 클라이언트에 따른 인스턴스를 생성하고 해시맵을 통해 운영한다.
 * 맵의 키는 클라이언트에 따른 유니크한 스트링을 통해 처리하며,
 * 해당 유니크키 발생을 위한 로직을 미개발 상태이며, 필히 개발되어야 함
 */
public class ServiceProvider extends ServerConfig{

    /**
     * 싱글턴 패턴을 사용하기 위한 인스턴스 레퍼런스
     */
    private static ServiceProvider instance;

    /**
     * 메인 서버 소켓 운영을 위한 스레드
     */
    private Thread thread;

    /**
     * 서버 소켓 인스턴스
     */
    private ServerSocket socket;

    /**
     * 클라이언트 소켓 집합
     */
    private HashMap clients;

    /**
     * 간단한 배치 작업을 위한 스레드로 이후 설계 변경 시 삭제 예정
     * @deprecated
     */
    private Thread batch;
    private static final int BATCH_TIME = 600 * 1000;

    /**
     * 포트를 매개변수로 입력받아 인스턴스를 생성하는 내부 접근 지정 생성자
     * @param port
     */
    private ServiceProvider(int port){
        try {
            socket = new ServerSocket(port);
            clients = new HashMap();
            Collections.synchronizedMap(clients);
        }catch(IOException e){
            e.printStackTrace();
            d("Socket Creation failed");
        }

        batch = new Thread(() -> {
            // Test
            while(true) {

                try {
                    Thread.sleep(BATCH_TIME);
                } catch (InterruptedException e) {
                    System.out.println("Batch Thread Interrupted");
                }

                System.out.println("Batch Executed");

            }
        });

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

    /**
     * 인스턴스 소켓 운영을 위한 스레드와 간이 배치 작업 스레드 호출
     * 명시적 호출이 필요함
     */
    public void start(){
        thread.start();
        batch.start();
    }

    /**
     * 싱글턴 패턴을 위한 인스턴스 레퍼런스 메소드
     * @param port
     * @return
     */
    public static ServiceProvider getInstance(int port){
        if(instance == null) instance = new ServiceProvider(port);
        return instance;
    }

    /**
     * 개발시 디버깅을 위한 로깅 메소드로 이후, Log4j로 대체할 예정 (삭제 필요)
     * @param message
     */
    private void d(String message){
        if(DEBUG_MODE) stream.println(getTime() + " " + message);
    }

}
