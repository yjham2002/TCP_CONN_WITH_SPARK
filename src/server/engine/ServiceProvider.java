package server.engine;

import configs.ServerConfig;
import javafx.application.Platform;
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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import static constants.ConstProtocol.SOCKET_TIMEOUT_LIMIT;
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
     * 서버 소켓 채널 인스턴스
     */
    private ServerSocketChannel socket;

    private Selector selector;

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
     * 서버 소켓 채널 바인딩 포트
     */
    private int port;

    private void startServer(){
        try {
            resetServer();

            selector = Selector.open();

            socket = ServerSocketChannel.open(); // 서버 소켓 인스턴스 생성
            socket.configureBlocking(false);
            socket.bind(new InetSocketAddress(port));
            socket.register(selector, SelectionKey.OP_ACCEPT);

            clients = new HashMap<>(); // 클라이언트 해시맵 생성
            Collections.synchronizedMap(clients); // 가변 스레드 환경에서 아토믹하게 해시맵을 이용하기 위한 동기화 명시 호출

        }catch(IOException e){
            e.printStackTrace();
            d("Socket Creation failed - The port set in const is in use or internet connection is not established.");
        }
    }

    private void resetServer() throws IOException{
        if(socket != null){
            if(socket.isOpen()) socket.close();
        }
        if(selector != null){
            if(selector.isOpen()) selector.close();
        }
    }

    /**
     * 포트를 매개변수로 입력받아 인스턴스를 생성하는 내부 접근 지정 생성자
     * @param port
     */
    private ServiceProvider(int port){
        this.port = port;

        jobs = new ArrayList<>();

        log = LoggerFactory.getLogger(this.getClass());

        log.info("Initiating Service Provider");

        startServer();

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

        batch.setPriority(Thread.NORM_PRIORITY);

        thread = new Thread(() -> {
            boolean recv = true;
            while(true){
                try {
                    int keyCount = selector.select(SOCKET_TIMEOUT_LIMIT);
                    if(keyCount == 0) continue;

                    d("STATUS :: [Channel is now Pending until Selector is inactive]");

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();

                        if (selectionKey.isAcceptable()) {
                            accept(selectionKey);
                            System.out.println("ServiceProvider :: [Accept]");
                        } else if (selectionKey.isReadable()) {
                            ProtocolResponder client = (ProtocolResponder) selectionKey.attachment();
                            System.out.println("ServiceProvider :: [Receive]");
                            recv = client.receive();
                        } else if (selectionKey.isWritable()) {
                            ProtocolResponder client = (ProtocolResponder) selectionKey.attachment();
                            System.out.println("ServiceProvider :: [Write]");
                            //client.send(selectionKey);
                        }

                        iterator.remove();
                    }

                    if(!recv) break;

                }catch(IOException e){
                    e.printStackTrace();
                    d("ERROR :: CHANNEL SELECTOR LEVEL ERROR");
                }

            }

            if(!recv) {
                d("WARNING ::::::::::::::::::::::::: [Connection Expired - Restarting :: " + getTimestamp() + "]");
                emergencyStart();
            }

        });

        d("Server is ready to respond");

    }

    void accept(SelectionKey selectionKey) {

        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();

            d("STATUS :: [Connection Requested from [" + socketChannel.getRemoteAddress() + "]]");

            ProtocolResponder protocolResponder = new ProtocolResponder(socketChannel, clients, selector);

        } catch (Exception e) {
            e.printStackTrace();
            d("ERROR :: NOT ABLE TO GENERATE SOCKET CHANNEL AND AN ERROR OCCURED WHILE ACCEPTING");
        }
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

    private ServiceProvider emergencyStart(){
        startServer();
        thread.start();

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
        for(int e = 0; e < msgs.length; e++) {
            ByteSerial entry = send(client, new ByteSerial(msgs[e], ByteSerial.TYPE_NONE));
            byteSerials.add(entry);
        }

        return byteSerials;
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
