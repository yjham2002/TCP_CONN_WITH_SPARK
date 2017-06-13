package server;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import configs.ServerConfig;
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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

import static constants.ConstProtocol.*;

/**
 * @author 함의진
 * 스레드 클래스를 상속하여 클라이언트에 따라 인스턴스를 가변적으로 증가시켜 각각에 대한 응답 기능을
 * 위임받는 클래스로, ServiceProvider에 대해 Aggregation 관계를 가진다.
 * ServiceProvider는 해시맵으로 운영되며, 클라이언트마다 유니크한 값을 해싱하여 이를 키로 사용한다.
 * 또한, 이는 싱크로나이즈되어 운영되기에 이에 대해 레이스 컨디션이 발생하지 않도록 설계한다.
 */
public class ProtocolResponder{

    /**
     * SLF4J 로거
     */
    Logger log;

    private boolean started = false; // 이니셜 프로토콜이 전송되었는지의 여부를 갖는 로컬 변수
    private boolean generated = false; // 유니크키 생성 여부
    private volatile ByteSerial byteSerial;
    private SelectionKey selectionKey;
    private Selector selector;
    private byte[] buffer;
    private String uniqueKey = ""; // 유니크키 초기화 - 클라이언트 해시맵에서 유일성을 가지도록 관리하기 위한 문자열
    private SocketChannel socket; // ServiceProvider로부터 accept된 단위 소켓
    private HashMap<String, ProtocolResponder> clients; // ServiceProvider의 클라이언트 집합의 레퍼런스 포인터

    /**
     * 프로토콜에 따른 응답을 위한 클래스의 생성자로서 단위 소켓과 함께 클라이언트 레퍼런스 포인터를 수용
     * @param socket
     * @param clients
     */
    public ProtocolResponder(SocketChannel socket, HashMap clients, Selector selector){
        super();

        log = LoggerFactory.getLogger(this.getClass());
        this.socket = socket; // 멤버 세팅
        this.selector = selector;

        this.clients = clients; // 멤버 세팅
        try {
            socket.configureBlocking(false);

            SelectionKey selectionKey = socket.register(selector, SelectionKey.OP_READ);
            selectionKey.attach(this);
            this.selectionKey = selectionKey;

            this.socket.socket().setKeepAlive(true);
            this.socket.socket().setSoTimeout(ConstProtocol.SOCKET_TIMEOUT_LIMIT);
        }catch (IOException e){
            e.printStackTrace(); // 소켓 연결 실패
        }
    }

    public boolean receive() throws IOException{
        byteSerial = null;

        try{
            ByteBuffer byteBuffer = ByteBuffer.allocate(ByteSerial.POOL_SIZE);

            int byteCount = socket.read(byteBuffer);
            if(byteCount == -1) return false;

            buffer = byteBuffer.array();

            byteSerial = new ByteSerial(buffer); // 바이트 시리얼 객체로 트리밍과 분석을 위임하기 위한 인스턴스 생성

            if (!byteSerial.isLoss() && !byteSerial.startsWith(SohaProtocolUtil.concat(STX, INITIAL_PROTOCOL_START)))
                started = true;

            buffer = byteSerial.getProcessed(); // 처리된 트림 데이터 추출

            if(buffer.length != LENGTH_REALTIME && buffer.length != LENGTH_INIT){ // 실시간 데이터가 아닌 경우, 동기화 전송 메소드가 이를 참조할 수 있도록 스코프에서 벗어난다
                System.out.println("::::::::: Handler Escape :::::::::::");
                return true;
            }

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

                    clients.put(uniqueKey, this); // 클라이언트 해시맵에 상위에서 추출한 유니크키를 기준으로 삽입

                    // 클라이언트 셋에서 키로 참조하여 이니셜 프로토콜을 전송 - 바이트 시리얼의 수신용 생성자가 아닌 이하의 생성자를 사용하여 자동으로 모드버스로 변환
                    ByteSerial init = new ByteSerial
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
                            );
                    sendOneWay(init);

                    System.out.println(Arrays.toString(init.getProcessed()));

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


        }catch(IOException e){ // 소켓 연결 두절의 경우, 연결을 종료할 경우, 흔히 발생하므로 에러 핸들링을 별도로 하지 않음
            log.info("Connection Finished"); // 커넥션이 마무리 되었음을 디버깅을 위해 출력
            clients.remove(uniqueKey); // 클라이언트 해시맵으로부터 소거함
            return false;
        }finally {
            // DO NOTHING
        }
        return true;
    }

    /**
     * 바이트 시리얼로부터 처리 이후의 바이트 패킷을 추출하여 바이트 기반으로 전송
     * @param msg
     */
    public synchronized ByteSerial send(ByteSerial msg){ // TODO 3회 반복

        byteSerial = null;

        log.info("Sending :: " + Arrays.toString(msg.getProcessed()));
        try {
            int timeouts = 0;

            while(true) {

                socket.write(ByteBuffer.wrap(msg.getProcessed()));
                selectionKey.interestOps(SelectionKey.OP_READ);
                selector.wakeup();

                long startTime = System.currentTimeMillis();

                boolean succ = true;

                while (byteSerial == null || byteSerial.getProcessed().length == LENGTH_REALTIME) {
                    if ((System.currentTimeMillis() - startTime) > ServerConfig.REQUEST_TIMEOUT) {
                        succ = false;
                        System.out.println("[INFO] READ TIMEOUT - " + timeouts);
                        timeouts++;
                        break;
                    }
                }

                if(timeouts >= ConstProtocol.RETRY){
                    byte[] farmBytes = Arrays.copyOfRange(msg.getProcessed(), 2, 6);
                    byte[] dongBytes = Arrays.copyOfRange(msg.getProcessed(), 6, 8);
                    ByteSerial byteSerialAlert = new ByteSerial(SohaProtocolUtil.makeAlertProtocol(farmBytes, dongBytes));
                    System.out.println("[INFO :: Sending Alert Protocol since Read Timeout has been occurred for 3 times]");
                    sendOneWay(byteSerialAlert);
                    return null;
                }

                if(timeouts >= ConstProtocol.RETRY || succ) break;

            }

            return byteSerial;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return byteSerial;
        }

    }

    /**
     * 바이트 시리얼로부터 처리 이후의 바이트 패킷을 추출하여 바이트 기반으로 전송
     * 단방향 전송 - 응답에 대해 대기하지 않음
     * @param msg
     */
    public synchronized void sendOneWay(ByteSerial msg){ // TODO 3회 반복

        byteSerial = null;

        log.info("Sending :: " + Arrays.toString(msg.getProcessed()));
        try {

            socket.write(ByteBuffer.wrap(msg.getProcessed()));
            selectionKey.interestOps(SelectionKey.OP_READ);
            selector.wakeup();

        }

        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @NotNull
    public SocketChannel getSocket() {
        return socket;
    }

    @NotNull
    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

}
