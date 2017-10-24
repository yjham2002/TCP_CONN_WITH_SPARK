package server;

import constants.ConstProtocol;
import databases.DBManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import models.ByteSerial;
import models.Pair;
import pojo.CropSubPOJO;
import pojo.RealtimePOJO;
import pojo.SettingPOJO;
import pojo.TimerPOJO;
import server.engine.ServiceProvider;
import server.whois.SMSService;
import utils.Log;
import utils.SohaProtocolUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static constants.ConstProtocol.LENGTH_AFTER_TRIM;
import static constants.ConstProtocol.LENGTH_INFO;
import static constants.ConstProtocol.LENGTH_LEN_RANGE;
import static models.ByteSerial.POOL_SIZE;

public class Responder  extends ChannelHandlerAdapter {
    
    protected ByteBuffer byteBuffer;
    protected ChannelHandlerContext ctx;

    protected volatile boolean semaphore = false;

    protected SMSService smsService;
    protected boolean started = false; // 이니셜 프로토콜이 전송되었는지의 여부를 갖는 로컬 변수
    protected boolean generated = false; // 유니크키 생성 여부
    protected volatile ByteSerial byteSerial;
    protected byte[] buffer;
    protected byte[] subBuffer;
    protected String uniqueKey = ""; // 유니크키 초기화 - 클라이언트 해시맵에서 유일성을 가지도록 관리하기 위한 문자열

    protected HashMap<String, Responder> clients; // ServiceProvider의 클라이언트 집합의 레퍼런스 포인터

    protected String farmString;
    protected String harvString;
    protected String farmName;
    protected String harvName;
    protected String info = "";

    public Responder(HashMap clients){
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


    public void sendBlock(ByteSerial msg, int length) throws Exception{
        if(msg.getTid() == 0){
            Log.e("Transaction ID has not been set. Halting this operation.");
            return;
        }

        if(!clients.containsKey(uniqueKey) && uniqueKey != null && !uniqueKey.equals("") && !uniqueKey.equals(SohaProtocolUtil.getMeaninglessUniqueKey())) {
            Log.i("Key Reinserted :: " + uniqueKey);
            clients.put(uniqueKey, this);
        }

        Log.i("Sending message and Locking transaction thread [" + msg.getTid() + "]  : " + Arrays.toString(msg.getProcessed()));

        ByteBuf byteBuf = Unpooled.wrappedBuffer(msg.getProcessed());

        final ChannelHandlerContext tempCtx = this.ctx;
        ctx.flush();

        Thread senderThread = new Thread(() -> {
            ChannelFuture channelFuture = tempCtx.writeAndFlush(byteBuf);

            Log.i(channelFuture);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    Log.i("Sending Operation Complete : " + channelFuture.toString());
                    if(!channelFuture.isSuccess()){
                        Log.i("Sending Failed : " + channelFuture.toString());
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
        Log.i("Sending message without Locking : " + Arrays.toString(msg.getProcessed()));
        ByteBuf byteBuf = Unpooled.wrappedBuffer(msg.getProcessed());
        ctx.writeAndFlush(byteBuf);
    }

    protected void readDailyAge(int order, String farmCode, String harvCode, int id) throws Exception{
        Log.i("Initial Loading - Daily Age Data[" + order + "].");

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
            Log.e("Error occurred while receiving daily age data [" + order + "]");
            return;
        }

        try {
            CropSubPOJO cropPOJO = new CropSubPOJO(recvs, order, farmCode, harvCode);
            cropPOJO.setByteSerial(null);

            String sql = cropPOJO.getInsertSQL();
            DBManager.getInstance().execute(sql);
        }catch(Exception e){
            Log.i("Handled :: CROP");
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

    protected void synchronizeStatus(RealtimePOJO realtimePOJO, String farmC, String harvC, int idC){

//        Log.e("synchronizeStatus");

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
                            Log.i("Initial Loading :: TIMER");
                            protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_TIMER.getHead(), ConstProtocol.RANGE_TIMER.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                            Log.i(Arrays.toString(protocol));

                            recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_TIMER);

                            if(recv == null) {
                                Log.e("An error occurred while auto reading");
                                return;
                            }

                            TimerPOJO timerPOJO = new TimerPOJO(recv, ConstProtocol.RANGE_READ_START, farmC, harvC);
                            String sql = timerPOJO.getInsertSQL();
                            DBManager.getInstance().execute(sql);
                            break;
                        case 1 : // SETTING
                            Log.i("Initial Loading :: SETTING");
                            protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING.getHead(), ConstProtocol.RANGE_SETTING.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                            Log.i("READING SETTINGS - " + Arrays.toString(protocol));

                            recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_SETTING);

                            if(recv == null) throw new Exception("An error occurred while auto reading");

                            SettingPOJO settingPOJO = new SettingPOJO(recv, ConstProtocol.RANGE_READ_START, farmC, harvC);

                            Log.i("SETTING BYTES : " + Arrays.toString(recv.getProcessed()));

                            protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING_TAILS.getHead(), ConstProtocol.RANGE_SETTING_TAILS.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                            Log.i("READING SETTING TAILS - " + Arrays.toString(protocol));

                            recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_SETTING_TAIL);

                            if(recv == null) throw new Exception("An error occurred while auto reading");

                            settingPOJO.initTails(recv, ConstProtocol.RANGE_READ_START);

                            settingPOJO.setByteSerial(null);
                            DBManager.getInstance().execute(settingPOJO.getInsertSQL());
                            break;
                        case 2 : // DAILY 1
                            readDailyAge(1, farmC, harvC, idC);
                            break;
                        case 3 : // DAILY 2
                            readDailyAge(2, farmC, harvC, idC);
                            break;
                        case 4 : // DAILY 3
                            readDailyAge(3, farmC, harvC, idC);
                            break;
                        case 5 : // DAILY 4
                            readDailyAge(4, farmC, harvC, idC);
                            break;
                        case 6 : // DAILY 5
                            readDailyAge(5, farmC, harvC, idC);
                            break;
                        case 7 : // DAILY 6
                            readDailyAge(6, farmC, harvC, idC);
                            break;
                        default: break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("Initial Load Failure :: Error Handled");
        }

        boolean toSend = false;

        try {

            if (realtimePOJO.getOption_changed_setting_a() == ConstProtocol.TRUE) {
                Log.e("Setting Change Detected");
                protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING.getHead(), ConstProtocol.RANGE_SETTING.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                Log.i("READING SETTINGS - " + Arrays.toString(protocol));

                recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_SETTING);

                if(recv == null) throw new Exception("An error occurred while auto reading");

                SettingPOJO settingPOJO = new SettingPOJO(recv, ConstProtocol.RANGE_READ_START, farmC, harvC);

                Log.i("SETTING BYTES : " + Arrays.toString(recv.getProcessed()));

                protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_SETTING_TAILS.getHead(), ConstProtocol.RANGE_SETTING_TAILS.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                Log.i("READING SETTING TAILS - " + Arrays.toString(protocol));

                recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_SETTING_TAIL);

                if(recv == null) throw new Exception("An error occurred while auto reading");

                settingPOJO.initTails(recv, ConstProtocol.RANGE_READ_START);

                settingPOJO.setByteSerial(null);
                DBManager.getInstance().execute(settingPOJO.getInsertSQL());

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_timer_a() == ConstProtocol.TRUE) {
                Log.e("Timer Change Detected");
                protocol = SohaProtocolUtil.makeReadProtocol(ConstProtocol.RANGE_TIMER.getHead(), ConstProtocol.RANGE_TIMER.getTail(), idC, farmC.getBytes(), harvC.getBytes());
                Log.i(Arrays.toString(protocol));

                recv = ServiceProvider.getInstance().send(uniqueKey, new ByteSerial(protocol, ByteSerial.TYPE_NONE), ConstProtocol.RESPONSE_LEN_TIMER);

                if(recv == null) throw new Exception("An error occurred while auto reading");

                TimerPOJO timerPOJO = new TimerPOJO(recv, ConstProtocol.RANGE_READ_START, farmC, harvC);
                String sql = timerPOJO.getInsertSQL();
                DBManager.getInstance().execute(sql);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop1_a() == ConstProtocol.TRUE) {
                Log.e("Crop[1] Change Detected");
                readDailyAge(1, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop2_a() == ConstProtocol.TRUE) {
                Log.e("Crop[2] Change Detected");
                readDailyAge(2, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop3_a() == ConstProtocol.TRUE) {
                Log.e("Crop[3] Change Detected");
                readDailyAge(3, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop4_a() == ConstProtocol.TRUE) {
                Log.e("Crop[4] Change Detected");
                readDailyAge(4, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop5_a() == ConstProtocol.TRUE) {
                Log.e("Crop[5] Change Detected");
                readDailyAge(5, farmC, harvC, idC);

                toSend = true;
            }
            if (realtimePOJO.getOption_changed_crop6_a() == ConstProtocol.TRUE) {
                Log.e("Crop[6] Change Detected");
                readDailyAge(6, farmC, harvC, idC);

                toSend = true;
            }

            byte[] initPrtc = SohaProtocolUtil.makeFlagInitProtocol(idC, farmC.getBytes(), harvC.getBytes());
            ByteSerial ellaborated = new ByteSerial(initPrtc, ByteSerial.TYPE_FORCE);

            if(toSend) {
                ServiceProvider.getInstance().send(uniqueKey, ellaborated, ConstProtocol.RESPONSE_LEN_WRITE);
                Log.e("Initiating Flag Bits");
            }else{
//                Log.i("Nothing Has been detected with changed-flags");
            }

        }catch(Exception e){
            e.printStackTrace();
            Log.e("An error occurred while Initiating Flag Bits but handled.");
        }finally {
            semaphore = false;
        }
    }
}
