package server.engine;

import configs.ServerConfig;
import models.ByteSerial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.ICallback;
import server.ProtocolResponder;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static spark.route.HttpMethod.get;

/**
 * @author 함의진
 * @version 1.0.0
 * 가변 스레딩을 위한 운영 개체 스레드셋이 운영되는 곳으로, 이곳으로부터
 * 각 클라이언트에 따른 인스턴스를 생성하고 해시맵을 통해 운영한다.
 * 맵의 키는 클라이언트에 따른 유니크한 스트링을 통해 처리(정수형 농장 코드 및 특정 설정 문자열 기반)
 */
public class ServiceProvider extends ServerConfig{

    /**
     * SLF4J 로거
     */
    Logger log;
    /**
     * 싱글턴 패턴을 사용하기 위한 인스턴스 레퍼런스
     */
    private static ServiceProvider instance;

    private int customTime = -1;

    /**
     * 메인 서버 소켓 운영을 위한 스레드
     */
    private Thread thread;

    /**
     * 서버 소켓 인스턴스
     */
    private ServerSocket socket;

    private List<ICallback> jobs;

    /**
     * 클라이언트 소켓 집합
     */
    private HashMap<String, ProtocolResponder> clients;

    /**
     * 간단한 배치 작업을 위한 스레드로 이후 설계 변경 시 삭제 예정
     * @deprecated
     */
    private Thread batch;

    /**
     * 포트를 매개변수로 입력받아 인스턴스를 생성하는 내부 접근 지정 생성자
     * @param port
     */
    private ServiceProvider(int port){
        jobs = new ArrayList<>();

        log = LoggerFactory.getLogger(this.getClass());

        log.info("Initiating Service Provider");

        try {
            socket = new ServerSocket(port); // 서버 소켓 인스턴스 생성
            clients = new HashMap<>(); // 클라이언트 해시맵 생성
            Collections.synchronizedMap(clients); // 가변 스레드 환경에서 아토믹하게 해시맵을 이용하기 위한 동기화 명시 호출
        }catch(IOException e){
            e.printStackTrace();
            d("Socket Creation failed - The port set in const is in use or internet connection is not established.");
        }

        batch = new Thread(() -> {
            // Test
            while(true) {
                try {
                    int time = BATCH_TIME;
                    if(customTime != -1 && customTime > 0) time = customTime;
                    Thread.sleep(time);
                    for(ICallback callback : jobs){
                        callback.postExecuted();
                    }
                } catch (InterruptedException e) {
                    d("Batch Thread Interrupted");
                }

                d("Batch Executed");

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

    public void offer(ICallback callback){
        this.jobs.add(callback);
    }

    /**
     * 인스턴스 소켓 운영을 위한 스레드와 간이 배치 작업 스레드 호출
     * 명시적 호출이 필요함
     */
    public ServiceProvider start(){
        thread.start();
        batch.start();

        return instance;
    }

    public ByteSerial send(String client, ByteSerial msg){
        ByteSerial ret = null;
        try {
            if(clients.containsKey(client)) ret = clients.get(client).send(msg);
            else{
                log.info("Client just requested does not exist. [KEY : " + client + "]");
            }
        }catch(Exception e){
            // DO NOTHING
        }finally {
            return ret;
        }
    }

    public ByteSerial send(String client, byte[] msg){
        return send(client, new ByteSerial(msg, ByteSerial.TYPE_NONE));
    }

    public List<ByteSerial> send(String client, byte[][] msgs){
        List<ByteSerial> byteSerials = new ArrayList<>();
        for(int e = 0; e < msgs.length; e++) byteSerials.add(send(client, new ByteSerial(msgs[e], ByteSerial.TYPE_NONE)));

        return byteSerials;
    }

    /**
     * 바이트 시리얼로부터 처리 이후의 바이트 패킷을 추출하여 전체 클라이언트에게 바이트 기반으로 전송
     * @param msg
     */
    @Deprecated
    private void sendToAll(ByteSerial msg){
        log.info("Sending :: " + Arrays.toString(msg.getProcessed()));

        Iterator it = clients.keySet().iterator();
        while(it.hasNext()){
            try{
                DataOutputStream out = (DataOutputStream)clients.get(it.next()).getOut();
                out.write(msg.getProcessed());
            }catch(IOException e){

            }
        }
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

    public static ServiceProvider getInstance(){
        return getInstance(SOCKET_PORT);
    }

    /**
     * 개발시 디버깅을 위한 로깅 메소드 단축
     * @param message
     */
    private void d(String message){
        if(DEBUG_MODE) {
            log.info(getTime() + " " + message);
        }
    }

    /**
     * 배치 작업 주기 시간을 커스텀 설정함
    * @param customTime
     */
    public void setCustomTime(int customTime) {
        this.customTime = customTime;
    }
}
