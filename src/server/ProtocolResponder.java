package server;

import agent.AlertAgent;
import agent.RealtimeAgent;
import constants.ConstProtocol;
import databases.DBManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import models.ByteSerial;
import models.Pair;
import models.TIDBlock;
import mysql.Cache;
import pojo.*;
import redis.RedisManager;
import server.engine.ServiceProvider;
import server.whois.SMSService;
import utils.HexUtil;
import utils.SohaProtocolUtil;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import static constants.ConstProtocol.*;
import static models.ByteSerial.POOL_SIZE;

/**
 * @author EuiJin.Ham
 * @version 1.5.0
 * @description 클라이언트별 실시간 데이터 처리
 */
public class ProtocolResponder extends ChannelHandlerAdapter{

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
        this.byteBuffer = ByteBuffer.allocate(POOL_SIZE);
        this.byteBuffer.clear();
        this.smsService = new SMSService();
        this.clients = clients;

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

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

        byteSerial = null;

        try{
            ByteBuf in = (ByteBuf) msgObj;
            byte[] bytes = new byte[in.readableBytes()];
            in.readBytes(bytes);
            in.release();

            buffer = bytes;

            byteSerial = new ByteSerial(buffer);

            String originText = Arrays.toString(byteSerial.getProcessed());

            System.out.println("[INFO] RealTime Data received [" + Arrays.toString(byteSerial.getProcessed())  + "]");

            if(byteSerial.getProcessed().length == 15){
                byte[] d = byteSerial.getProcessed();
                byte[] fmarr = Arrays.copyOfRange(d, 6, 10);
                byte[] hvarr = Arrays.copyOfRange(d, 10, 12);
                String fStr = HexUtil.getNumericStringFromAscii(fmarr);
                String hStr = HexUtil.getNumericStringFromAscii(hvarr);
                String tFName = Cache.getInstance().farmNames.get(fStr);
                String tName;

                System.err.println("[ALERT PROTOCOL] : " + fStr + ":" + hStr);
                List<String> phones = SohaProtocolUtil.getPhoneNumbers(fStr, fStr);
                tName = Cache.getInstance().harvNames.get(Cache.getHarvKey(fStr, hStr));
                if(tName == null || tName.equals("null") || tName.equals("")) {
                    if(fStr.length() == 4 && hStr.length() == 2) System.err.println(info);
                    tName = "단말기 ID : " + hStr;
                }

                if(hStr.trim().length() == 2 || fStr.trim().length() == 4){
                    for (String tel : phones) {
                        if(tel != null && !tel.trim().equals("") && !tel.trim().equals("--")) smsService.sendSMS(tel, String.format(ConstProtocol.CONNECTION_MESSAGE, tFName, tName));
                    }
                }

            }

            long tid = 0;
            byte addr1 = 0;
            byte addr2 = 0;

            try {
                if (!byteSerial.isLoss() && !byteSerial.startsWith(SohaProtocolUtil.concat(STX, INITIAL_PROTOCOL_START))) started = true;
                buffer = byteSerial.getProcessed(); // 처리된 트림 데이터 추출

                if(buffer.length >= 16) {
                    tid = ByteSerial.bytesToLong(Arrays.copyOfRange(buffer, 12, 20));
                    addr1 = buffer[14];
                    addr2 = buffer[15];
                }

            }catch(NullPointerException e){
                e.printStackTrace();
            }

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

            if(buffer.length != LENGTH_REALTIME && buffer.length != LENGTH_INIT && buffer.length != LENGTH_ALERT_PRTC){ // 실시간 데이터가 아닌 경우, 동기화 전송 메소드가 이를 참조할 수 있도록 스코프에서 벗어난다
                byteSerial = new ByteSerial(buffer, ByteSerial.TYPE_NONE, tid, addr1, addr2);
                if(ServiceProvider.blockMap.containsKey(tid)){
                    System.out.println("[INFO] Transaction Caught.");
                    TIDBlock tidBlock = ServiceProvider.blockMap.get(tid);

                    synchronized (tidBlock) {
                        ServiceProvider.blockMap.get(tid).setByteSerial(byteSerial.clone());
                        if (byteSerial.isLoss()) ServiceProvider.blockMap.get(tid).setByteSerial(null);
                        tidBlock.notify();
                        ServiceProvider.blockMap.remove(tid);
                    }
                }

                System.out.println("[INFO] Transaction Finalized with TID [" + tid + "] LOC ["+ addr1 + "/" + addr2 +"].");
                return;
            }

            if (!generated) {
                generated = true;
                if(buffer.length == LENGTH_INIT) uniqueKey = SohaProtocolUtil.getUniqueKeyByInit(subBuffer); // 유니크키를 농장코드로 설정하여 추출
                if (uniqueKey.equals(SohaProtocolUtil.getMeaninglessUniqueKey()) || buffer.length != LENGTH_INIT)
                    uniqueKey = SohaProtocolUtil.getUniqueKeyByFarmCode(SohaProtocolUtil.getFarmCodeByProtocol(subBuffer));
                clients.put(uniqueKey, this);
            }

            System.out.println(buffer.length);

            if(byteSerial == null) return;
            if (!byteSerial.isLoss()) { // 바이트 시리얼 내에서 인스턴스 할당 시 작동한 손실 여부 파악 로직에 따라 패킷 손실 여부를 파악

                if (!started || (buffer.length != LENGTH_ALERT_PRTC && buffer.length != LENGTH_REALTIME)) { // 이니셜 프로토콜에 따른 처리 여부를 확인하여 최초 연결일 경우, 본 로직을 수행
                    started = true;
                    clients.put(uniqueKey, this);
                    System.out.println("[INFO] Unique Key Generated. [" + uniqueKey + "].");
                    String farmInit = SohaProtocolUtil.getFarmCodeFromInit(subBuffer);

                    ByteSerial init = SohaProtocolUtil.makeIntervalProtocol(farmInit, subBuffer);

                    this.sendOneWay(init);

                    System.out.println(Arrays.toString(init.getProcessed()));

                    System.out.println("[INFO] Connected Farm - " + farmInit + "[" + uniqueKey + "] :: Totally " + clients.size() + " connections are being maintained.");

                } else if(buffer.length == LENGTH_ALERT_PRTC){ // 경보 프로토콜 수신 시
                    System.err.println("[ALERT PROTOCOL] : " + farmString + ":" + harvString);
                    List<String> phones = SohaProtocolUtil.getPhoneNumbers(farmString, farmString);

                    if(harvName == null || harvName.equals("null") || harvName.equals("")) {
                        if(farmString.length() == 4 && harvString.length() == 2) System.err.println(info);
                        harvName = "단말기 ID : " + harvString;
                    }

                    if(harvString.trim().length() == 2 || farmString.trim().length() == 4){
                        for (String tel : phones) {
                            if(tel != null && !tel.trim().equals("") && !tel.trim().equals("--")) smsService.sendSMS(tel, String.format(ConstProtocol.CONNECTION_MESSAGE, farmName, harvName));
                        }
                    }
                }else {
                    if (!clients.containsKey(uniqueKey)) {
                        System.out.println("[INFO] Unique Key inserted : " + uniqueKey);
                        clients.put(uniqueKey, this); // 클라이언트 해시맵에 상위에서 추출한 유니크키를 기준으로 삽입
                    }

                    RedisManager redisManager = RedisManager.getInstance();
                    String farm = SohaProtocolUtil.getSimpleKey(SohaProtocolUtil.getFarmCodeByProtocol(buffer));
                    String key = farm + "@" + RedisManager.getTimestamp();
                    String millis = Long.toString(RedisManager.getMillisFromRedisKey(key));

                    RealtimePOJO realtimePOJO = new RealtimePOJO(byteSerial);
                    realtimePOJO.setOrigin(originText);
                    realtimePOJO.setRedisTime(millis);

                    RealtimeAgent.getInstance().getOfferList().put(realtimePOJO);
                    boolean succ = redisManager.put(key, realtimePOJO);

                    System.out.println("[INFO] Tried to insert a realTime data - " + succ);

                    /**
                     * 경보내역 조건 검사를 에이전트에 위임
                     */
                    WrappedPOJO wrappedPOJO = new WrappedPOJO(realtimePOJO, farmString, harvString);
                    AlertAgent.getInstance().getBlockingQueue().put(wrappedPOJO);

                    Thread synchronizer = new Thread(() -> {
                        semaphore = true;
                        try {
                            synchronizeStatus(realtimePOJO, farmString, harvString, HexUtil.getNumericValue(harvCodeTemp));
                        }catch(Exception e){
                            System.err.println("[WARN] Exception on the Auto Reading Process handled.");
                        }
                    });

                    if(!semaphore) synchronizer.start();

                }
            }

        }catch(IOException e) { // Connection Finished OR Error Occurred
            e.printStackTrace();
            return;
        }catch(Exception e) {
            e.printStackTrace();
            System.err.println("[WARN] Exception Handled - " + e.getMessage());
        }finally {
            byteBuffer.compact();
        }
        return;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if(farmString.length() == 4 && harvString.length() == 2) System.out.println("[INFO] Channel Inactivated at [" + farmString + "/" + harvString + "].");
        try {
            super.channelInactive(ctx);
            try {
                List<String> phones = SohaProtocolUtil.getPhoneNumbers(farmString, farmString);
                if(harvName == null || harvName.equals("null") || harvName.equals("")) {
                    if(farmString.length() == 4 && harvString.length() == 2) System.err.println(info);
                    harvName = "단말기 ID : " + harvString;
                }
                if(harvString.trim().length() == 2 || farmString.trim().length() == 4) {
                    for (String tel : phones)
                        if(tel != null && !tel.trim().equals("") && !tel.trim().equals("--")) smsService.sendSMS(tel, String.format(ConstProtocol.CONNECTION_MESSAGE, farmName, harvName));
                }
                System.out.println("Connection Finished"); // 커넥션이 마무리 되었음을 디버깅을 위해 출력
                clients.remove(uniqueKey); // 클라이언트 해시맵으로부터 소거함
            } catch (Exception e2) {
                System.out.println("통신 이상 SMS 전송 중 에러 :: \n" + e2.toString());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            super.channelActive(ctx);
            this.ctx = ctx;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void synchronizeStatus(RealtimePOJO realtimePOJO, String farmC, String harvC, int idC){

        ByteSerial recv = null;
        byte[] protocol = null;

        try{
            List<String> sets = SohaProtocolUtil.countExistingData(farmC, harvC);

            for(int sync = 0; sync < sets.size(); sync++){
                String str = sets.get(sync);
                int num = Integer.parseInt(str);
                if(num == 0){
                    switch (sync){
                        case 0 : // TIMER
                            System.out.println("[INFO] Initial Loading :: TIMER");
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
                            System.out.println("[INFO] Initial Loading :: SETTING");
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
                            readDailyAge(1, farmC, harvC, idC);
                        case 3 : // DAILY 2
                            readDailyAge(2, farmC, harvC, idC);
                        case 4 : // DAILY 3
                            readDailyAge(3, farmC, harvC, idC);
                        case 5 : // DAILY 4
                            readDailyAge(4, farmC, harvC, idC);
                        case 6 : // DAILY 5
                            readDailyAge(5, farmC, harvC, idC);
                        case 7 : // DAILY 6
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

        try {
            if (realtimePOJO.getOption_changed_setting_a() == ConstProtocol.TRUE) {
                System.out.println("[INFO] Setting Change Detected");
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
                System.out.println("[INFO] Timer Change Detected");
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
                System.out.println("[INFO] Crop[1] Change Detected");
                readDailyAge(1, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop2_a() == ConstProtocol.TRUE) {
                System.out.println("[INFO] Crop[2] Change Detected");
                readDailyAge(2, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop3_a() == ConstProtocol.TRUE) {
                System.out.println("[INFO] Crop[3] Change Detected");
                readDailyAge(3, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop4_a() == ConstProtocol.TRUE) {
                System.out.println("[INFO] Crop[4] Change Detected");
                readDailyAge(4, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop5_a() == ConstProtocol.TRUE) {
                System.out.println("[INFO] Crop[5] Change Detected");
                readDailyAge(5, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop6_a() == ConstProtocol.TRUE) {
                System.out.println("[INFO] Crop[6] Change Detected");
                readDailyAge(6, farmC, harvC, idC);

                toSend = true;
            }

            byte[] initPrtc = SohaProtocolUtil.makeFlagInitProtocol(idC, farmC.getBytes(), harvC.getBytes());
            ByteSerial ellaborated = new ByteSerial(initPrtc, ByteSerial.TYPE_FORCE);

            if(toSend) {
                ServiceProvider.getInstance().send(uniqueKey, ellaborated, ConstProtocol.RESPONSE_LEN_WRITE);
                System.out.println("[INFO] Initiating Flag Bits");
            }else{
//                System.out.println("[INFO] Nothing Has been detected with changed-flags");
            }

        }catch(Exception e){
            e.printStackTrace();
            System.err.println("[WARN] An error occurred while Initiating Flag Bits but handled.");
        }finally {
            semaphore = false;
        }
    }

    private void readDailyAge(int order, String farmCode, String harvCode, int id) throws Exception{
        System.out.println("[INFO] Initial Loading - Daily Age Data[" + order + "].");

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
            System.err.println("[WARN] Error occurred while receiving daily age data [" + order + "]");
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
            System.err.println("[WARN] Transaction ID has not been set. Halting this operation.");
            return;
        }

        if(!clients.containsKey(uniqueKey) && uniqueKey != null && !uniqueKey.equals("") && !uniqueKey.equals(SohaProtocolUtil.getMeaninglessUniqueKey())) {
            System.out.println("[INFO] Key Reinserted :: " + uniqueKey);
            clients.put(uniqueKey, this);
        }

        System.out.println("[INFO] Sending message and Locking transaction thread [" + msg.getTid() + "]  : " + Arrays.toString(msg.getProcessed()));

        ByteBuf byteBuf = Unpooled.wrappedBuffer(msg.getProcessed());

        final ChannelHandlerContext tempCtx = this.ctx;
        ctx.flush();

        Thread senderThread = new Thread(() -> {
            ChannelFuture channelFuture = tempCtx.writeAndFlush(byteBuf);

            System.out.println(channelFuture);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    System.out.println("[INFO] Sending Operation Complete : " + channelFuture.toString());
                    if(!channelFuture.isSuccess()){
                        System.out.println("[INFO] Sending Failed : " + channelFuture.toString());
                    }
                }
            });
        });

        senderThread.start();

    }

    /**
     * 바이트 시리얼로부터 처리 이후의 바이트 패킷을 추출하여 바이트 기반으로 전송
     * 단방향 전송 - 응답에 대해 대기하지 않음
     * @param msg
     */
    public void sendOneWay(ByteSerial msg){
        System.out.println("[INFO] Sending message without Locking : " + Arrays.toString(msg.getProcessed()));
        ByteBuf byteBuf = Unpooled.wrappedBuffer(msg.getProcessed());
        ctx.writeAndFlush(byteBuf);
    }

}
