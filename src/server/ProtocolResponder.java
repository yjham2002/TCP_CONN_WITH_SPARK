package server;

import agent.AlertAgent;
import agent.RealtimeAgent;
import configs.ServerConfig;
import constants.ConstProtocol;
import databases.DBManager;
import databases.exception.NothingToTakeException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.concurrent.Promise;
import models.ByteSerial;
import models.DataMap;
import models.Pair;
import models.TIDBlock;
import mysql.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.*;
import redis.RedisManager;
import server.engine.ServiceProvider;
import server.whois.SMSService;
import utils.HexUtil;
import utils.SohaProtocolUtil;

import java.io.*;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import static constants.ConstProtocol.*;
import static models.ByteSerial.POOL_SIZE;

/**
 * @author 함의진
 * 스레드 클래스를 상속하여 클라이언트에 따라 인스턴스를 가변적으로 증가시켜 각각에 대한 응답 기능을
 * 위임받는 클래스로, ServiceProvider에 대해 Aggregation 관계를 가진다.
 * ServiceProvider는 해시맵으로 운영되며, 클라이언트마다 유니크한 값을 해싱하여 이를 키로 사용한다.
 * 또한, 이는 싱크로나이즈되어 운영되기에 이에 대해 레이스 컨디션이 발생하지 않도록 설계한다.
 */
public class ProtocolResponder extends ChannelHandlerAdapter{

    /**
     * SLF4J 로거
     */
    Logger log;

    private ByteBuffer byteBuffer;
    private ChannelHandlerContext ctx;

    private volatile boolean semaphore = false;

    private SMSService smsService;
    private boolean started = false; // 이니셜 프로토콜이 전송되었는지의 여부를 갖는 로컬 변수
    private boolean generated = false; // 유니크키 생성 여부
    private volatile ByteSerial byteSerial;
    private byte[] buffer;
    private byte[] subBuffer;
    private String uniqueKey = ""; // 유니크키 초기화 - 클라이언트 해시맵에서 유일성을 가지도록 관리하기 위한 문자열

    private HashMap<String, ProtocolResponder> clients; // ServiceProvider의 클라이언트 집합의 레퍼런스 포인터

    private String farmString;
    private String harvString;
    private String farmName;
    private String harvName;
    private String info = "";

    /**
     * 프로토콜에 따른 응답을 위한 클래스의 생성자로서 단위 소켓과 함께 클라이언트 레퍼런스 포인터를 수용
     * @param clients
     */
    public ProtocolResponder(HashMap clients){
        super();

        log = LoggerFactory.getLogger(this.getClass());

        this.byteBuffer = ByteBuffer.allocate(POOL_SIZE);
        this.byteBuffer.clear();
        /**
         * allocate 를 통해 새롭게 메모리를 할당함
         */

        /**
         * SMS 전송 클래스 생성
         */
        this.smsService = new SMSService();

        this.clients = clients; // 멤버 세팅

    }

    int aa = 0;

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        System.out.println(":::::::::::::::::::::::CONTEXT SWITCHED:::::::::::::::::::::::::::");
        ctx.flush(); // 컨텍스트의 내용을 플러쉬합니다.
    };

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public static byte[] trimLength(byte[] arr){
        return SohaProtocolUtil.concat(ConstProtocol.STX, Arrays.copyOfRange(arr, LENGTH_LEN_RANGE, arr.length));
    }

    public static byte[] trimHead(byte[] arr){
        byte[] temp = trimLength(arr);
        return SohaProtocolUtil.concat(Arrays.copyOf(temp, LENGTH_AFTER_TRIM), Arrays.copyOfRange(temp, LENGTH_AFTER_TRIM + LENGTH_INFO, temp.length));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msgObj) throws Exception {

//        System.out.println("RECEIVE[ENTERED] :: " + socket.isConnected() + " :: " + socket.isOpen() + " :: " + socket.getRemoteAddress() + " :: " + socket.getLocalAddress());
        byteSerial = null;

        try{
            ByteBuf in = (ByteBuf) msgObj;
            byte[] bytes = new byte[in.readableBytes()];
            in.readBytes(bytes);
            in.release();

            buffer = bytes;

            byteSerial = new ByteSerial(buffer); // 바이트 시리얼 객체로 트리밍과 분석을 위임하기 위한 인스턴스 생성

            String originText = Arrays.toString(byteSerial.getProcessed());

            System.out.println("RECV [" + Arrays.toString(byteSerial.getProcessed())  + "]");

//            if(byteSerial.isLoss()) aa++;
//            System.out.println("INFO :: PACKET LOSS OCCURED FOR [" + aa + "] TIME(S)");

            long tid = 0;
            byte addr1 = 0;
            byte addr2 = 0;

            try {
                if (!byteSerial.isLoss() && !byteSerial.startsWith(SohaProtocolUtil.concat(STX, INITIAL_PROTOCOL_START))) started = true;
                buffer = byteSerial.getProcessed(); // 처리된 트림 데이터 추출

                if(buffer.length >= 16) {
                    tid = ByteSerial.bytesToLong(Arrays.copyOfRange(buffer, 12, 20));

//                    System.out.println("TRANSACTION ID :: " + tid);
                    addr1 = buffer[14];
                    addr2 = buffer[15];
                }

            }catch(NullPointerException e){
                e.printStackTrace();
            }

            byte[] origin = buffer.clone();

            subBuffer = trimLength(buffer);
            buffer = trimHead(buffer);

            byteSerial.setProcessed(buffer);
            byteSerial.setOriginal(buffer);

            if(buffer.length == 0) throw new IOException();

            byte[] farmCodeTemp = SohaProtocolUtil.getFarmCodeByProtocol(buffer);
            byte[] harvCodeTemp = SohaProtocolUtil.getHarvCodeByProtocol(buffer);

            farmString = HexUtil.getNumericStringFromAscii(farmCodeTemp);
            harvString = HexUtil.getNumericStringFromAscii(harvCodeTemp);
            info = "[농가코드 정보] : " + farmString + ":" + Arrays.toString(farmCodeTemp) + "\n" + "[재배동 코드 정보] : " + harvString + ":" + Arrays.toString(harvCodeTemp);

            farmName = Cache.getInstance().farmNames.get(farmString);
            harvName = Cache.getInstance().harvNames.get(Cache.getHarvKey(farmString, harvString));

//            System.out.println(Arrays.toString(buffer));

//            farmName = DBManager.getInstance().getString(String.format(ConstProtocol.SQL_FARMNAME_FORMAT, farmString), ConstProtocol.SQL_COL_FARMNAME);
//            harvName = DBManager.getInstance().getString(String.format(ConstProtocol.SQL_DONGNAME_FORMAT, farmString, harvString), ConstProtocol.SQL_COL_DONGNAME);

//            System.out.println(buffer.length + "/" + subBuffer.length);

            if(buffer.length != LENGTH_REALTIME && buffer.length != LENGTH_INIT && buffer.length != LENGTH_ALERT_PRTC){ // 실시간 데이터가 아닌 경우, 동기화 전송 메소드가 이를 참조할 수 있도록 스코프에서 벗어난다
                byteSerial = new ByteSerial(buffer, ByteSerial.TYPE_NONE, tid, addr1, addr2);
                System.out.println("############################################ TID START");

                if(ServiceProvider.blockMap.containsKey(tid)){
                    System.out.println("###################################### :: CONTAINS TRUE");
                    TIDBlock tidBlock = ServiceProvider.blockMap.get(tid);

                    synchronized (tidBlock) {
                        ServiceProvider.blockMap.get(tid).setByteSerial(byteSerial.clone());
                        if (byteSerial.isLoss()) ServiceProvider.blockMap.get(tid).setByteSerial(null);
                        tidBlock.notify();
                        ServiceProvider.blockMap.remove(tid);
                    }
                }else{
                    System.out.println("###################################### :: CONTAINS FALSE");
                }

                System.out.println("::::::::: Escaping From RealTime Handler - TID [" + tid + "] LOC ["+ addr1 + "/" + addr2 +"] ::::::::::: ");
                return;
            }

            if (!generated) {
                generated = true;
                if(buffer.length == LENGTH_INIT) uniqueKey = SohaProtocolUtil.getUniqueKeyByInit(subBuffer); // 유니크키를 농장코드로 설정하여 추출
                if (uniqueKey.equals(SohaProtocolUtil.getMeaninglessUniqueKey()) || buffer.length != LENGTH_INIT)
                    uniqueKey = SohaProtocolUtil.getUniqueKeyByFarmCode(SohaProtocolUtil.getFarmCodeByProtocol(subBuffer));
                clients.put(uniqueKey, this);
            }

            if(byteSerial == null) return;
            if (!byteSerial.isLoss()) { // 바이트 시리얼 내에서 인스턴스 할당 시 작동한 손실 여부 파악 로직에 따라 패킷 손실 여부를 파악

                if (!started || (buffer.length != LENGTH_ALERT_PRTC && buffer.length != LENGTH_REALTIME)) { // 이니셜 프로토콜에 따른 처리 여부를 확인하여 최초 연결일 경우, 본 로직을 수행
                    started = true; // 이니셜 프로토콜 전송 여부 갱신

                    clients.put(uniqueKey, this); // 클라이언트 해시맵에 상위에서 추출한 유니크키를 기준으로 삽입

                    System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: " + uniqueKey);

                    // 클라이언트 셋에서 키로 참조하여 이니셜 프로토콜을 전송 - 바이트 시리얼의 수신용 생성자가 아닌 이하의 생성자를 사용하여 자동으로 모드버스로 변환

                    String farmInit = SohaProtocolUtil.getFarmCodeFromInit(subBuffer);

                    System.out.println("[FARM CONNECTED] " + farmInit);

                    long interval = 0;

                    try {
                        interval = DBManager.getInstance().getNumber("SELECT inter_time FROM farm_list WHERE farm_code = '" + farmInit + "'", "inter_time");
                    }catch(NothingToTakeException e){}

                    int initM10 = ConstProtocol.INIT_TERM_MIN10;
                    int initM = ConstProtocol.INIT_TERM_MIN;
                    int initS10 = ConstProtocol.INIT_TERM_SEC10;
                    int initS = ConstProtocol.INIT_TERM_SEC;

                    switch ((int)interval){
                        case 30:
                            initM10 = 0;
                            initM = 0;
                            initS10 = 3;
                            initS = 0;
                            break;
                        case 60:
                            initM10 = 0;
                            initM = 0;
                            initS10 = 6;
                            initS = 0;
                            break;
                        case 300:
                            initM10 = 0;
                            initM = 5;
                            initS10 = 0;
                            initS = 0;
                            break;
                        case 600:
                            initM10 = 0;
                            initM = 10;
                            initS10 = 0;
                            initS = 0;
                            break;
                        case 1200:
                            initM10 = 2;
                            initM = 0;
                            initS10 = 0;
                            initS = 0;
                            break;
                        case 1800:
                            initM10 = 3;
                            initM = 0;
                            initS10 = 0;
                            initS = 0;
                            break;
                        default: break;
                    }

                    ByteSerial init = new ByteSerial
                            (
                                    SohaProtocolUtil.getInitProtocol(
                                            subBuffer,
                                            0,
                                            0,
                                            initM10,
                                            initM,
                                            initS10,
                                            initS
                                    ),
                                    ByteSerial.TYPE_SET
                            );
                    this.sendOneWay(init);

                    System.out.println(Arrays.toString(init.getProcessed()));

                    log.info("Responder :: [" + uniqueKey + "] :: Totally " + clients.size() + " connections are being maintained");
                    // 현재 연결된 클라이언트 소켓수와 유니크키를 디버깅을 위해 출력함

                } else if(buffer.length == LENGTH_ALERT_PRTC){ // 경보 프로토콜 수신 시
                    List<String> phones = new Vector<>();
                    List<DataMap> pList = DBManager.getInstance().getList("SELECT farm_code, a_tel, b_tel, c_tel, d_tel FROM user_list WHERE farm_code='"+farmString+"' OR user_auth='A'");

                    for(DataMap dmap : pList){
                        try {
                            phones.add(dmap.getString("a_tel"));
                            phones.add(dmap.getString("b_tel"));
                            phones.add(dmap.getString("c_tel"));
                            phones.add(dmap.getString("d_tel"));
                        }catch(Exception e){
                            System.out.println("WARN :: Phone LIst Error :: Skipping");
                        }
                    }

                    if(harvName == null || harvName.equals("null") || harvName.equals("")) {
                        System.err.println(info);
                        harvName = "단말기 ID : " + harvString;
                    }

                    for(String tel : phones) {
                        smsService.sendSMS(tel, String.format(ConstProtocol.CONNECTION_MESSAGE, farmName, harvName));
                    }
                }else {
                    if (!clients.containsKey(uniqueKey)) {
                        log.info("Unique Key inserted : " + uniqueKey);
                        clients.put(uniqueKey, this); // 클라이언트 해시맵에 상위에서 추출한 유니크키를 기준으로 삽입
                    }

                    RedisManager redisManager = RedisManager.getInstance();
                    String farm = SohaProtocolUtil.getSimpleKey(SohaProtocolUtil.getFarmCodeByProtocol(buffer));
                    String key = farm + "@" + RedisManager.getTimestamp();

//                    System.out.println("KEY " + key);

                    String millis = Long.toString(RedisManager.getMillisFromRedisKey(key));

                    RealtimePOJO realtimePOJO = new RealtimePOJO(byteSerial);
                    realtimePOJO.setOrigin(originText);
                    realtimePOJO.setRedisTime(millis);

                    RealtimeAgent.getInstance().getOfferList().put(realtimePOJO);
                    boolean succ = redisManager.put(key, realtimePOJO);

                    log.info("JEDIS REALTIME DATA PUT : " + succ);

                    /**
                     * 경보내역 조건 검사를 에이전트에 위임
                     */

                    WrappedPOJO wrappedPOJO = new WrappedPOJO(realtimePOJO, farmString, harvString);

                    AlertAgent.getInstance().getBlockingQueue().put(wrappedPOJO);

//                    log.info("Farm Code :: " + Arrays.toString(farmCodeTemp) + " / HarvCode :: " + Arrays.toString(harvCodeTemp));

                    Thread synchronizer = new Thread(() -> {
                        semaphore = true;
                        try {
                            // TODO 주석처리 해제 필히 해야 함
                            synchronizeStatus(realtimePOJO, farmString, harvString, HexUtil.getNumericValue(harvCodeTemp));
                        }catch(Exception e){
                            System.out.println("Auto Reading handled");
                        }
                    });

                    if(!semaphore) synchronizer.start();

                }
            }


        }catch(IOException e) { // Connection Finished OR Error Occurred

            e.printStackTrace();

            return;
        }catch(NullPointerException ne) {
            System.out.println("Null Pointer Handled");
            ne.printStackTrace();
        }catch(ArrayIndexOutOfBoundsException ae){
            ae.printStackTrace();
            System.out.println("Array Index error handled");
        }finally {
            byteBuffer.compact();
        }
        return;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Channel Inactivated");
        try {
            super.channelInactive(ctx);
            try {
                List<String> phones = DBManager.getInstance().getStrings("SELECT farm_code, a_tel, b_tel, c_tel, d_tel FROM user_list WHERE (farm_code='" + farmString + "' OR user_auth='A' OR manage_farm LIKE '%" + farmString + "%') AND delete_flag = 'N'", "a_tel", "b_tel", "c_tel", "d_tel");
                if(harvName == null || harvName.equals("null") || harvName.equals("")) {
                    System.err.println(info);
                    harvName = "단말기 ID : " + harvString;
                }
                for (String tel : phones)
                    smsService.sendSMS(tel, String.format(ConstProtocol.CONNECTION_MESSAGE, farmName, harvName));
//            socket.finishConnect();
                log.info("Connection Finished"); // 커넥션이 마무리 되었음을 디버깅을 위해 출력
                clients.remove(uniqueKey); // 클라이언트 해시맵으로부터 소거함
            } catch (Exception e2) {
                System.out.println("통신 이상 SMS 전송 중 에러 :: \n" + e2.toString());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private BlockingQueue<Promise<String>> messageList = new ArrayBlockingQueue<>(16);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            super.channelActive(ctx);
            this.ctx = ctx;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // TODO 주석처리 해제
    public void synchronizeStatus(RealtimePOJO realtimePOJO, String farmC, String harvC, int idC){

        ByteSerial recv = null;
        byte[] protocol = null;

        try{
            String sqlNums = "SELECT \n" +
                    "(SELECT COUNT(*) FROM tblTimerData WHERE farmCode='"+ farmC +"' AND dongCode='" + harvC + "') AS timer,\n" +
                    "(SELECT COUNT(*) FROM tblSettingData WHERE farmCode='"+ farmC +"' AND dongCode='"+ harvC +"') AS setting,\n" +
                    "(SELECT COUNT(*) FROM tblDaily WHERE farmCode='"+ farmC +"' AND dongCode='"+ harvC +"' AND `order`=1) AS daily1,\n" +
                    "(SELECT COUNT(*) FROM tblDaily WHERE farmCode='"+ farmC +"' AND dongCode='"+ harvC +"' AND `order`=2) AS daily2,\n" +
                    "(SELECT COUNT(*) FROM tblDaily WHERE farmCode='"+ farmC +"' AND dongCode='"+ harvC +"' AND `order`=3) AS daily3,\n" +
                    "(SELECT COUNT(*) FROM tblDaily WHERE farmCode='"+ farmC +"' AND dongCode='"+ harvC +"' AND `order`=4) AS daily4,\n" +
                    "(SELECT COUNT(*) FROM tblDaily WHERE farmCode='"+ farmC +"' AND dongCode='"+ harvC +"' AND `order`=5) AS daily5,\n" +
                    "(SELECT COUNT(*) FROM tblDaily WHERE farmCode='"+ farmC +"' AND dongCode='"+ harvC +"' AND `order`=6) AS daily6;\n" +
                    "\n";
            List<String> sets = DBManager.getInstance().getStrings(sqlNums, "timer", "setting", "daily1", "daily2", "daily3", "daily4", "daily5", "daily6");

            for(int sync = 0; sync < sets.size(); sync++){
                String str = sets.get(sync);
                int num = Integer.parseInt(str);
                if(num == 0){
                    switch (sync){
                        case 0 : // TIMER
                            System.out.println("INFO :: Initial Loading :: TIMER");
                            protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_TIMER.getHead(), ConstProtocol.RANGE_TIMER.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                            System.out.println(Arrays.toString(protocol));

                            recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_TIMER);

                            if(recv == null) {
                                System.out.println("An error occurred while auto reading");
                                return;
                            }

                            TimerPOJO timerPOJO = new TimerPOJO(recv, ConstProtocol.RANGE_READ_START, farmC, harvC);
                            String sql = timerPOJO.getInsertSQL();
                            DBManager.getInstance().execute(sql);
                            break;
                        case 1 : // SETTING
                            System.out.println("INFO :: Initial Loading :: SETTING");
                            protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING.getHead(), ConstProtocol.RANGE_SETTING.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                            System.out.println("READING SETTINGS - " + Arrays.toString(protocol));

                            recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_SETTING);

                            if(recv == null) throw new Exception("An error occurred while auto reading");

                            SettingPOJO settingPOJO = new SettingPOJO(recv, ConstProtocol.RANGE_READ_START, farmC, harvC);

                            System.out.println("SETTING BYTES : " + Arrays.toString(recv.getProcessed()));

                            protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING_TAILS.getHead(), ConstProtocol.RANGE_SETTING_TAILS.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                            System.out.println("READING SETTING TAILS - " + Arrays.toString(protocol));

                            recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_SETTING_TAIL);

                            if(recv == null) throw new Exception("An error occurred while auto reading");

                            settingPOJO.initTails(recv, ConstProtocol.RANGE_READ_START);

                            settingPOJO.setByteSerial(null);
                            DBManager.getInstance().execute(settingPOJO.getInsertSQL());
                            break;
                        case 2 : // DAILY 1
                            System.out.println("INFO :: Initial Loading :: DAILY 1");
                            readDailyAge(1, farmC, harvC, idC);
                        case 3 : // DAILY 2
                            System.out.println("INFO :: Initial Loading :: DAILY 2");
                            readDailyAge(2, farmC, harvC, idC);
                        case 4 : // DAILY 3
                            System.out.println("INFO :: Initial Loading :: DAILY 3");
                            readDailyAge(3, farmC, harvC, idC);
                        case 5 : // DAILY 4
                            System.out.println("INFO :: Initial Loading :: DAILY 4");
                            readDailyAge(4, farmC, harvC, idC);
                        case 6 : // DAILY 5
                            System.out.println("INFO :: Initial Loading :: DAILY 5");
                            readDailyAge(5, farmC, harvC, idC);
                        case 7 : // DAILY 6
                            System.out.println("INFO :: Initial Loading :: DAILY 6");
                            readDailyAge(6, farmC, harvC, idC);
                            break;
                        default: break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Initial Load Failure :: Error Handled");
        }

        boolean toSend = false;

        recv = null;
        protocol = null;

        try {
            if (realtimePOJO.getOption_changed_setting_a() == ConstProtocol.TRUE) {
                System.out.println("INFO :: Setting Change Detected");
                protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING.getHead(), ConstProtocol.RANGE_SETTING.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                System.out.println("READING SETTINGS - " + Arrays.toString(protocol));

                recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_SETTING);

                if(recv == null) throw new Exception("An error occurred while auto reading");

                SettingPOJO settingPOJO = new SettingPOJO(recv, ConstProtocol.RANGE_READ_START, farmC, harvC);

                System.out.println("SETTING BYTES : " + Arrays.toString(recv.getProcessed()));

                protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING_TAILS.getHead(), ConstProtocol.RANGE_SETTING_TAILS.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                System.out.println("READING SETTING TAILS - " + Arrays.toString(protocol));

                recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_SETTING_TAIL);

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

                recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_TIMER);

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
                ServiceProvider.getInstance().send(uniqueKey, ellaborated, ConstProtocol.RESPONSE_LEN_WRITE);
                System.out.println("INFO :: Initiating Flag Bits");
            }else{
//                System.out.println("INFO :: Nothing Has been detected with changed-flags");
            }

        }catch(Exception e){
            System.out.println("=============================================================");
            System.out.println("WARN :: An error occurred while Initiating Flag Bits");
            System.out.println("Message :: " + e.toString());
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
            ByteSerial entry = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocols[e], ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_DAILY);
            if(entry != null) {
                recvs.add(entry);
            }else{
                break;
            }
        }

        if(recvs.size() <= 0) {
            System.out.println("auto reading error on daily age :: " + order);
            return;
        }

        try {
            CropSubPOJO cropPOJO = new CropSubPOJO(recvs, order, farmCode, harvCode);
            cropPOJO.setByteSerial(null);

            String sql = cropPOJO.getInsertSQL();
            DBManager.getInstance().execute(sql);
        }catch(Exception e){
            System.out.println("Handled :: CROP");
        }
    }

    public void sendBlock(ByteSerial msg, int length) throws Exception{
        if(msg.getTid() == 0){
            System.out.println("No TID SET");
            return;
        }

        if(!clients.containsKey(uniqueKey) && uniqueKey != null && !uniqueKey.equals("") && !uniqueKey.equals(SohaProtocolUtil.getMeaninglessUniqueKey())) {
            System.out.println("Key Reinserted :: " + uniqueKey);
            clients.put(uniqueKey, this);
        }

        log.info("Sending (BLOCKED)[" + msg.getTid() + "]:: " + Arrays.toString(msg.getProcessed()));

        ByteBuf byteBuf = Unpooled.wrappedBuffer(msg.getProcessed());

        final ChannelHandlerContext tempCtx = this.ctx;
        ctx.flush();

        Thread senderThread = new Thread(() -> {
                ChannelFuture channelFuture = tempCtx.writeAndFlush(byteBuf);

                System.out.println(channelFuture);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        System.out.println("operationComplete :: " + channelFuture.toString());
                        if(!channelFuture.isSuccess()){
                            System.out.println("FAILED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        }
                    }
                });
        });

        senderThread.start();

    }

    /**
     * 바이트 시리얼로부터 처리 이후의 바이트 패킷을 추출하여 바이트 기반으로 전송
     * @param msg
     */
    public ByteSerial send0(ByteSerial msg, int length) throws Exception{

        if(msg.getTid() == 0){
            System.out.println("No TID SET");
            return null;
        }

        if(!clients.containsKey(uniqueKey) && uniqueKey != null && !uniqueKey.equals("") && !uniqueKey.equals(SohaProtocolUtil.getMeaninglessUniqueKey())) {
            System.out.println("Key Reinserted :: " + uniqueKey);
            clients.put(uniqueKey, this);
        }

//        byteSerial = null;

        log.info("Sending [" + msg.getTid() + "]:: " + Arrays.toString(msg.getProcessed()));
        int timeouts = 0;

        ByteSerial retVal = null;

        while(true) {

            ByteBuf byteBuf = Unpooled.wrappedBuffer(msg.getProcessed());

            ChannelFuture channelFuture = ctx.writeAndFlush(byteBuf);

            long startTime = System.currentTimeMillis();

            boolean succ = true;

            while (byteSerial == null || byteSerial.getTid() != msg.getTid()) {

//                if(byteSerial != null && byteSerial.getProcessed().length != LENGTH_REALTIME && length == ConstProtocol.RESPONSE_LEN_WRITE) break;
                if ((System.currentTimeMillis() - startTime) > ServerConfig.REQUEST_TIMEOUT) {
                    succ = false;
                    System.out.println("[INFO] READ TIMEOUT OCCURRED - " + timeouts + " TIME(S) FROM " + farmName);
                    timeouts++;
                    break;
                }
            }

            retVal = byteSerial.clone();

            if(timeouts >= ConstProtocol.RETRY){
                System.out.println("[INFO] READ TIMEOUT OCCURRED FOR 3 TIMES - HALTING SEND REQUEST");
//                sendOneWay(byteSerialAlert);
                return null;
            }

            /**
             * 2017-08-06
             * 타임아웃 시 정상적이지 않은 값을 반환하던 버그 수정
             */
            if(timeouts >= ConstProtocol.RETRY) return null;
            if(succ) break;

        }

        return retVal;

    }

    /**
     * 바이트 시리얼로부터 처리 이후의 바이트 패킷을 추출하여 바이트 기반으로 전송
     * 단방향 전송 - 응답에 대해 대기하지 않음
     * @param msg
     */
    public void sendOneWay(ByteSerial msg){
        log.info("Sending :: " + Arrays.toString(msg.getProcessed()));
        ByteBuf byteBuf = Unpooled.wrappedBuffer(msg.getProcessed());
        ChannelFuture cf = ctx.writeAndFlush(byteBuf);
    }

}
