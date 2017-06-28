package server;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import configs.ServerConfig;
import constants.ConstProtocol;
import models.ByteSerial;
import models.Pair;
import mysql.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.CropSubPOJO;
import pojo.RealtimePOJO;
import pojo.SettingPOJO;
import pojo.TimerPOJO;
import redis.RedisManager;
import server.response.Response;
import server.response.ResponseConst;
import server.whois.SMSService;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static constants.ConstProtocol.*;
import static models.ByteSerial.POOL_SIZE;

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

    private ByteBuffer byteBuffer;

    private volatile boolean semaphore = false;

    private SMSService smsService;
    private boolean started = false; // 이니셜 프로토콜이 전송되었는지의 여부를 갖는 로컬 변수
    private boolean generated = false; // 유니크키 생성 여부
    private volatile ByteSerial byteSerial;
    private SelectionKey selectionKey;
    private Selector selector;
    private byte[] buffer;
    private String uniqueKey = ""; // 유니크키 초기화 - 클라이언트 해시맵에서 유일성을 가지도록 관리하기 위한 문자열
    private SocketChannel socket; // ServiceProvider로부터 accept된 단위 소켓
    private HashMap<String, ProtocolResponder> clients; // ServiceProvider의 클라이언트 집합의 레퍼런스 포인터

    private int[] prevErrorData = null;

    /**
     * 프로토콜에 따른 응답을 위한 클래스의 생성자로서 단위 소켓과 함께 클라이언트 레퍼런스 포인터를 수용
     * @param socket
     * @param clients
     */
    public ProtocolResponder(SocketChannel socket, HashMap clients, Selector selector){
        super();

        log = LoggerFactory.getLogger(this.getClass());
        this.socket = socket; // 멤버 세팅

//        this.socket.socket().setReuseAddress(true);

        this.selector = selector;
        this.byteBuffer = ByteBuffer.allocate(POOL_SIZE);
        /**
         * SMS 전송 클래스 생성
         */
        this.smsService = new SMSService();

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

    private static boolean tempVar = false; // 디버깅용 변수

    public boolean receive() throws IOException{
//        System.out.println("RECEIVE[ENTERED] :: " + socket.isConnected() + " :: " + socket.isOpen() + " :: " + socket.getRemoteAddress() + " :: " + socket.getLocalAddress());
        byteSerial = null;

        try{
//          byteBuffer.clear();

            byteBuffer = ByteBuffer.allocate(POOL_SIZE); // ByteBuffer Limit has to be considered

            System.out.println("RECEIVE[ALLOC] :: " + socket.isConnected() + " :: " + socket.isOpen() + " :: " + socket.getRemoteAddress() + " :: " + socket.getLocalAddress());

            int byteCount = socket.read(byteBuffer);
            if(byteCount == -1) {
                System.out.println("RECEIVE[-1] :: " + socket.isConnected() + " :: " + socket.isOpen() + " :: " + socket.getRemoteAddress() + " :: " + socket.getLocalAddress());
                throw new IOException();
            }

            System.out.println("RECEIVE[READ] :: " + socket.isConnected() + " :: " + socket.isOpen() + " :: " + socket.getRemoteAddress() + " :: " + socket.getLocalAddress());

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
                if (uniqueKey.equals(SohaProtocolUtil.getMeaninglessUniqueKey()) || buffer.length != LENGTH_INIT)
                    uniqueKey = SohaProtocolUtil.getUniqueKeyByFarmCode(SohaProtocolUtil.getFarmCodeByProtocol(buffer));
            }

            if (!byteSerial.isLoss()) { // 바이트 시리얼 내에서 인스턴스 할당 시 작동한 손실 여부 파악 로직에 따라 패킷 손실 여부를 파악
                if (!started || buffer.length != LENGTH_REALTIME) { // 이니셜 프로토콜에 따른 처리 여부를 확인하여 최초 연결일 경우, 본 로직을 수행
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

                    byte[] farmCodeTemp = SohaProtocolUtil.getFarmCodeByProtocol(buffer);
                    byte[] harvCodeTemp = SohaProtocolUtil.getHarvCodeByProtocol(buffer);

                    String farmString = HexUtil.getNumericStringFromAscii(farmCodeTemp);
                    String harvString = HexUtil.getNumericStringFromAscii(harvCodeTemp);

                    if(SohaProtocolUtil.getErrorCount(realtimePOJO) > 0){
                        int errArray[] = SohaProtocolUtil.getErrorArray(realtimePOJO);
                        int errSMSarray[] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

                        boolean haveToSend = false;

                        if(prevErrorData != null){
                            for(int err = 0; err < errArray.length; err++){
                                if(errArray[err] != prevErrorData[err]){
                                    if(errArray[err] == ConstProtocol.TRUE) {

                                        haveToSend = true;
                                        errSMSarray[err] = ConstProtocol.TRUE;

                                        String sql = SohaProtocolUtil.getErrorSQL(farmString, harvString, err, "Y");
                                        DBManager.getInstance().execute(sql);
                                    }else{
                                        String sql = SohaProtocolUtil.getErrorSQL(farmString, harvString, err, "N");
                                        DBManager.getInstance().execute(sql);
                                    }
                                }
                            }
                        }

                        if(haveToSend) {
                            String farmName = DBManager.getInstance().getString(String.format(ConstProtocol.SQL_FARMNAME_FORMAT, farmString), ConstProtocol.SQL_COL_FARMNAME);
                            String harvName = DBManager.getInstance().getString(String.format(ConstProtocol.SQL_DONGNAME_FORMAT, farmString, harvString), ConstProtocol.SQL_COL_DONGNAME);
                            String tel = DBManager.getInstance().getString(String.format(ConstProtocol.SQL_FARM_TEL, farmString), ConstProtocol.SQL_COL_FARM_TEL);

                            String msg = SohaProtocolUtil.getErrorMessage(errSMSarray, farmName, harvName);
                            smsService.sendSMS(tel, msg);
                        }

                        prevErrorData = errArray;

                    }

                    log.info("Farm Code :: " + Arrays.toString(farmCodeTemp) + " / HarvCode :: " + Arrays.toString(harvCodeTemp));

                    Thread synchronizer = new Thread(() -> {
                        semaphore = true;
                        synchronizeStatus(realtimePOJO, farmString, harvString, HexUtil.getNumericValue(harvCodeTemp));
                    });

                    if(!semaphore) synchronizer.start();

                }
            }


        }catch(IOException e){ // 소켓 연결 두절의 경우, 연결을 종료할 경우, 흔히 발생하므로 에러 핸들링을 별도로 하지 않음
            e.printStackTrace();
            if(selectionKey.isValid()) selectionKey.cancel();
            selectionKey.channel().close();
//            socket.finishConnect();
            log.info("Connection Finished"); // 커넥션이 마무리 되었음을 디버깅을 위해 출력
            clients.remove(uniqueKey); // 클라이언트 해시맵으로부터 소거함
            return false;
        }finally {
            // DO NOTHING
        }
        return true;
    }

    public void synchronizeStatus(RealtimePOJO realtimePOJO, String farmC, String harvC, int idC){

        boolean toSend = false;

        ByteSerial recv;
        byte[] protocol;

        try {
            if (realtimePOJO.getOption_changed_setting_a() == ConstProtocol.TRUE) {
                System.out.println("INFO :: Setting Change Detected");
                protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING.getHead(), ConstProtocol.RANGE_SETTING.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                System.out.println("READING SETTINGS - " + Arrays.toString(protocol));

                recv = send(new ByteSerial(protocol, ByteSerial.TYPE_NONE));

                if(recv == null) throw new Exception("An error occurred while auto reading");

                SettingPOJO settingPOJO = new SettingPOJO(recv, ConstProtocol.RANGE_READ_START, farmC, harvC);

                System.out.println("SETTING BYTES : " + Arrays.toString(recv.getProcessed()));

                protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING_TAILS.getHead(), ConstProtocol.RANGE_SETTING_TAILS.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                System.out.println("READING SETTING TAILS - " + Arrays.toString(protocol));

                recv = send(new ByteSerial(protocol, ByteSerial.TYPE_NONE));

                if(recv == null) throw new Exception("An error occurred while auto reading");

                settingPOJO.initTails(recv, ConstProtocol.RANGE_READ_START);

                settingPOJO.setByteSerial(null);
                DBManager.getInstance().execute(settingPOJO.getInsertSQL());

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_timer_a() == ConstProtocol.TRUE) {
                System.out.println("INFO :: Timer Change Detected");
                protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_TIMER.getHead(), ConstProtocol.RANGE_TIMER.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                System.out.println(Arrays.toString(protocol));

                recv = send(new ByteSerial(protocol, ByteSerial.TYPE_NONE));

                if(recv == null) throw new Exception("An error occurred while auto reading");

                TimerPOJO timerPOJO = new TimerPOJO(recv, ConstProtocol.RANGE_READ_START, farmC, harvC);
                String sql = timerPOJO.getInsertSQL();
                DBManager.getInstance().execute(sql);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop1_a() == ConstProtocol.TRUE) {
                System.out.println("INFO :: Crop[1] Change Detected");
                readDailyAge(1, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop2_a() == ConstProtocol.TRUE) {
                System.out.println("INFO :: Crop[2] Change Detected");
                readDailyAge(2, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop3_a() == ConstProtocol.TRUE) {
                System.out.println("INFO :: Crop[3] Change Detected");
                readDailyAge(3, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop4_a() == ConstProtocol.TRUE) {
                System.out.println("INFO :: Crop[4] Change Detected");
                readDailyAge(4, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop5_a() == ConstProtocol.TRUE) {
                System.out.println("INFO :: Crop[5] Change Detected");
                readDailyAge(5, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop6_a() == ConstProtocol.TRUE) {
                System.out.println("INFO :: Crop[6] Change Detected");
                readDailyAge(6, farmC, harvC, idC);

                toSend = true;
            }

            byte[] initPrtc = SohaProtocolUtil.makeFlagInitProtocol(idC, farmC.getBytes(), harvC.getBytes());
            ByteSerial ellaborated = new ByteSerial(initPrtc, ByteSerial.TYPE_FORCE);

            if(toSend) {
                send(ellaborated);
                System.out.println("INFO :: Initiating Flag Bits");
            }else{
                System.out.println("INFO :: Nothing Has been detected with changed-flags");
            }

        }catch(Exception e){
            System.out.println("=============================================================");
            System.out.println("WARN :: An error occurred while Initiating Flag Bits");
            System.out.println("Message :: " + e.getMessage());
            System.out.println("=============================================================");
        }finally {
            semaphore = false;
        }
    }

    private void readDailyAge(int order, String farmCode, String harvCode, int id) throws Exception{
        List<ByteSerial> recvs;
        byte[][] protocols;

        Pair<Integer> range = ConstProtocol.RANGE_DAYAGE;
        switch (order){
            case 1: range = ConstProtocol.RANGE_DAYAGE_01; break;
            case 2: range = ConstProtocol.RANGE_DAYAGE_02; break;
            case 3: range = ConstProtocol.RANGE_DAYAGE_03; break;
            case 4: range = ConstProtocol.RANGE_DAYAGE_04; break;
            case 5: range = ConstProtocol.RANGE_DAYAGE_05; break;
            case 6: range = ConstProtocol.RANGE_DAYAGE_06; break;
            default: order = -1; break;
        }

        protocols = SohaProtocolUtil.makeReadProtocols(range.getHead(), range.getTail(), id, farmCode.getBytes(), harvCode.getBytes());

        recvs = new ArrayList<>();
        for(int e = 0; e < protocols.length; e++) {
            ByteSerial entry = send(new ByteSerial(protocols[e], ByteSerial.TYPE_NONE));
            if(entry != null) {
                recvs.add(entry);
            }else{
                break;
            }
        }

        if(recvs.size() <= 0) throw new Exception("auto reading error on daily age :: " + order);

        CropSubPOJO cropPOJO = new CropSubPOJO(recvs, order, farmCode, harvCode);
        cropPOJO.setByteSerial(null);

        String sql = cropPOJO.getInsertSQL();
        DBManager.getInstance().execute(sql);
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
            if(selectionKey.isValid()) selectionKey.cancel();
            selectionKey.channel().close();
//            socket.finishConnect();
            log.info("Connection Finished - Send"); // 커넥션이 마무리 되었음을 디버깅을 위해 출력
            clients.remove(uniqueKey); // 클라이언트 해시맵으로부터 소거함
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

        } catch (IOException e) {
            e.printStackTrace();
            if(selectionKey.isValid()) selectionKey.cancel();
            try{selectionKey.channel().close();}catch(Exception ee){ee.printStackTrace();}
//            socket.finishConnect();
            log.info("Connection Finished - Send Oneway"); // 커넥션이 마무리 되었음을 디버깅을 위해 출력
            clients.remove(uniqueKey); // 클라이언트 해시맵으로부터 소거함
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
