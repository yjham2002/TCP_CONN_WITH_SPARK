package server;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import constants.ConstProtocol;
import models.ByteSerial;
import mysql.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.RealtimePOJO;
import redis.RedisManager;
import utils.HexUtil;
import utils.SerialUtil;
import utils.SohaProtocolUtil;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

import static constants.ConstProtocol.INITIAL_PROTOCOL_START;
import static constants.ConstProtocol.SOCKET_TIMEOUT_COUNT;
import static constants.ConstProtocol.STX;

/**
 * @author 함의진
 * 스레드 클래스를 상속하여 클라이언트에 따라 인스턴스를 가변적으로 증가시켜 각각에 대한 응답 기능을
 * 위임받는 클래스로, ServiceProvider에 대해 Aggregation 관계를 가진다.
 * ServiceProvider는 해시맵으로 운영되며, 클라이언트마다 유니크한 값을 해싱하여 이를 키로 사용한다.
 * 또한, 이는 싱크로나이즈되어 운영되기에 이에 대해 레이스 컨디션이 발생하지 않도록 설계한다.
 */
public class ProtocolResponder extends Thread{

    private boolean flag = false;

    /**
     * SLF4J 로거
     */
    Logger log;

    private byte[] buffer;
    private String uniqueKey = ""; // 유니크키 초기화 - 클라이언트 해시맵에서 유일성을 가지도록 관리하기 위한 문자열
    private Socket socket; // ServiceProvider로부터 accept된 단위 소켓
    private DataInputStream in; // 입력 스트림
    private DataOutputStream out; // 출력 스트림
    private HashMap<String, ProtocolResponder> clients; // ServiceProvider의 클라이언트 집합의 레퍼런스 포인터

    /**
     * 프로토콜에 따른 응답을 위한 클래스의 생성자로서 단위 소켓과 함께 클라이언트 레퍼런스 포인터를 수용
     * @param socket
     * @param clients
     */
    public ProtocolResponder(Socket socket, HashMap clients){
        super();

        log = LoggerFactory.getLogger(this.getClass());
        this.socket = socket; // 멤버 세팅

        this.clients = clients; // 멤버 세팅
        try {
            this.socket.setKeepAlive(true);
            this.socket.setSoTimeout(ConstProtocol.SOCKET_TIMEOUT_LIMIT);
            in = new DataInputStream(socket.getInputStream()); // 소켓으로부터 입력 스트림 추출
            out = new DataOutputStream(socket.getOutputStream()); // 소켓으로부터 출력 스트림 추출
        }catch (IOException e){
            e.printStackTrace(); // 소켓 연결 실패
        }
    }



    /**
     * 스레드 상속 클래스로서 본 메소드에 로직을 구현
     */
    @Override
    public void run(){

        boolean started = false; // 이니셜 프로토콜이 전송되었는지의 여부를 갖는 로컬 변수
        boolean generated = false; // 유니크키 생성 여부

        try{
            buffer = new byte[ByteSerial.POOL_SIZE]; // 버퍼 사이즈 할당

            while ((in.read(buffer)) != -1) { // 응답이 없을 때까지 입력 스트림으로부터 바이트 버퍼를 읽음

                ByteSerial byteSerial = new ByteSerial(buffer); // 바이트 시리얼 객체로 트리밍과 분석을 위임하기 위한 인스턴스 생성

                if (!byteSerial.isLoss() && !byteSerial.startsWith(SohaProtocolUtil.concat(STX, INITIAL_PROTOCOL_START)))
                    started = true;

                buffer = byteSerial.getProcessed(); // 처리된 트림 데이터 추출

//                if (buffer.length == 0) System.exit(122);// TODO 디버깅용

                if (!generated) {
                    generated = true;
                    uniqueKey = SohaProtocolUtil.getUniqueKeyByInit(buffer); // 유니크키를 농장코드로 설정하여 추출
                    if (uniqueKey.equals(SohaProtocolUtil.getMeaninglessUniqueKey()))
                        uniqueKey = SohaProtocolUtil.getUniqueKeyByFarmCode(SohaProtocolUtil.getFarmCodeByProtocol(buffer));
                }

                if (!byteSerial.isLoss()) { // 바이트 시리얼 내에서 인스턴스 할당 시 작동한 손실 여부 파악 로직에 따라 패킷 손실 여부를 파악
                    if (!started) { // 이니셜 프로토콜에 따른 처리 여부를 확인하여 최초 연결일 경우, 본 로직을 수행
                        started = true; // 이니셜 프로토콜 전송 여부 갱신

                        System.out.println(Arrays.toString(buffer) + " :::::::::::::::::::::::::::::::::::::::::::::::::::");

                        clients.put(uniqueKey, this); // 클라이언트 해시맵에 상위에서 추출한 유니크키를 기준으로 삽입

                        // 클라이언트 셋에서 키로 참조하여 이니셜 프로토콜을 전송 - 바이트 시리얼의 수신용 생성자가 아닌 이하의 생성자를 사용하여 자동으로 모드버스로 변환
                        send(new ByteSerial
                                (
                                        SohaProtocolUtil.getInitProtocol(
                                                buffer,
                                                0,
                                                0,
                                                ConstProtocol.INIT_TERM_MIN10,
                                                ConstProtocol.INIT_TERM_MIN,
                                                ConstProtocol.INIT_TERM_SEC10,
                                                ConstProtocol.INIT_TERM_SEC
                                        ),
                                        ByteSerial.TYPE_SET
                                )
                        );

                        log.info("Responder :: [" + uniqueKey + "] :: Totally " + clients.size() + " connections are being maintained");
                        // 현재 연결된 클라이언트 소켓수와 유니크키를 디버깅을 위해 출력함
                    } else {
                        if (!clients.containsKey(uniqueKey)) {
                            log.info("Unique Key inserted : " + uniqueKey);
                            clients.put(uniqueKey, this); // 클라이언트 해시맵에 상위에서 추출한 유니크키를 기준으로 삽입
                        }

                        RedisManager redisManager = RedisManager.getInstance();
                        String farm = SohaProtocolUtil.getSimpleKey(SohaProtocolUtil.getFarmCodeByProtocol(buffer));
                        String key = farm + "@" + RedisManager.getTimestamp();

                        String millis = Long.toString(RedisManager.getMillisFromRedisKey(key));

                        RealtimePOJO realtimePOJO = new RealtimePOJO(byteSerial);
                        realtimePOJO.setRedisTime(millis);

                        boolean succ = redisManager.put(key, realtimePOJO);

                        log.info("JEDIS REALTIME DATA PUT : " + succ);

                        log.info("Farm Code :: " + Arrays.toString(SohaProtocolUtil.getFarmCodeByProtocol(buffer)) + " / HarvCode :: " + Arrays.toString(SohaProtocolUtil.getHarvCodeByProtocol(buffer)));
                    }
                }
            }

        }catch(IOException e){ // 소켓 연결 두절의 경우, 연결을 종료할 경우, 흔히 발생하므로 에러 핸들링을 별도로 하지 않음
            // Ignore
        }finally {
            log.info("Connection Finished"); // 커넥션이 마무리 되었음을 디버깅을 위해 출력
            if(!this.currentThread().isInterrupted()) clients.remove(uniqueKey); // 클라이언트 해시맵으로부터 소거함
        }
    }

    /**
     * 바이트 시리얼로부터 처리 이후의 바이트 패킷을 추출하여 바이트 기반으로 전송
     * @param msg
     */
    public ByteSerial send(ByteSerial msg){

        log.info("Sending :: " + Arrays.toString(msg.getProcessed()));
        ByteSerial byteSerial = null;
        try {
            DataOutputStream out = clients.get(uniqueKey).out;
            out.write(msg.getProcessed());

            int timeOutCount = 0;
            boolean succ = false;

            while (timeOutCount < SOCKET_TIMEOUT_COUNT) {
                try {
                    byte[] bufferSend = new byte[ByteSerial.POOL_SIZE]; // 버퍼 사이즈 할당

                    while ((in.read(bufferSend)) != -1) {
                        log.info("Message Handler Overrode the response successfully");
                        byteSerial = new ByteSerial(bufferSend);
                        succ = true;
                        break;
                    }
                } catch (SocketTimeoutException timeout) {
                    System.out.println();
                    timeOutCount++;
                } finally {
                    if (succ) break;
                    if (timeOutCount >= SOCKET_TIMEOUT_COUNT) {
                        System.out.println("Timeout occured for 3 times :: Need to send alert message");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return byteSerial;
        }

    }

    @NotNull
    public Socket getSocket() {
        return socket;
    }

    @NotNull
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @NotNull
    public DataInputStream getIn() {
        return in;
    }

    @NotNull
    public void setIn(DataInputStream in) {
        this.in = in;
    }

    @NotNull
    public DataOutputStream getOut() {
        return out;
    }

    @NotNull
    public void setOut(DataOutputStream out) {
        this.out = out;
    }
}
