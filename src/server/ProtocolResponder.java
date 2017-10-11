package server;

import agent.AlertAgent;
import agent.RealtimeAgent;
import constants.ConstProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import models.ByteSerial;
import models.TIDBlock;
import mysql.Cache;
import pojo.*;
import redis.RedisManager;
import server.engine.ServiceProvider;
import server.whois.SMSService;
import utils.HexUtil;
import utils.Log;
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
public class ProtocolResponder extends Responder{

    public ProtocolResponder(HashMap clients){
        super(clients);
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
            final String originText = Arrays.toString(byteSerial.getProcessed());
            Log.i("RealTime Data received. [" + Arrays.toString(byteSerial.getProcessed())  + "]");

            if(byteSerial.getProcessed().length == 15){
                byte[] d = byteSerial.getProcessed();
                byte[] fmarr = Arrays.copyOfRange(d, 6, 10);
                byte[] hvarr = Arrays.copyOfRange(d, 10, 12);
                String fStr = HexUtil.getNumericStringFromAscii(fmarr);
                String hStr = HexUtil.getNumericStringFromAscii(hvarr);
                String tFName = Cache.getInstance().farmNames.get(fStr);
                String tName;

                Log.e("[ALERT PROTOCOL] : " + fStr + ":" + hStr);
                List<String> phones = SohaProtocolUtil.getPhoneNumbers(fStr, fStr);
                tName = Cache.getInstance().harvNames.get(Cache.getHarvKey(fStr, hStr));
                if(tName == null || tName.equals("null") || tName.equals("")) {
                    if(fStr.length() == 4 && hStr.length() == 2) Log.e(info);
                    tName = "단말기 ID : " + hStr;
                }

                if(hStr.trim().length() == 2 || fStr.trim().length() == 4){
                    for (String tel : phones) {
                        if(tel != null && !tel.trim().equals("") && !tel.trim().equals("--")) smsService.sendSMS(tel, String.format(ConstProtocol.CONNECTION_MESSAGE, tFName, tName));
                    }
                }
                return;
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
                    Log.i("Transaction Caught.");
                    TIDBlock tidBlock = ServiceProvider.blockMap.get(tid);

                    synchronized (tidBlock) {
                        ServiceProvider.blockMap.get(tid).setByteSerial(byteSerial.clone());
                        if (byteSerial.isLoss()) ServiceProvider.blockMap.get(tid).setByteSerial(null);
                        tidBlock.notify();
                        ServiceProvider.blockMap.remove(tid);
                    }
                }

                Log.i("Transaction Finalized with TID [" + tid + "] LOC ["+ addr1 + "/" + addr2 +"].");
                return;
            }

            if (!generated) {
                generated = true;
                if(buffer.length == LENGTH_INIT) uniqueKey = SohaProtocolUtil.getUniqueKeyByInit(subBuffer); // 유니크키를 농장코드로 설정하여 추출
                if (uniqueKey.equals(SohaProtocolUtil.getMeaninglessUniqueKey()) || buffer.length != LENGTH_INIT)
                    uniqueKey = SohaProtocolUtil.getUniqueKeyByFarmCode(SohaProtocolUtil.getFarmCodeByProtocol(subBuffer));
                clients.put(uniqueKey, this);
            }

            Log.i("The Packet Length was " + buffer.length + ".");

            if(byteSerial == null) return;
            if (!byteSerial.isLoss()) { // 바이트 시리얼 내에서 인스턴스 할당 시 작동한 손실 여부 파악 로직에 따라 패킷 손실 여부를 파악

                if (!started || (buffer.length != LENGTH_ALERT_PRTC && buffer.length != LENGTH_REALTIME)) { // 이니셜 프로토콜에 따른 처리 여부를 확인하여 최초 연결일 경우, 본 로직을 수행
                    started = true;
                    clients.put(uniqueKey, this);
                    Log.i("Unique Key Generated. [" + uniqueKey + "].");
                    String farmInit = SohaProtocolUtil.getFarmCodeFromInit(subBuffer);

                    ByteSerial init = SohaProtocolUtil.makeIntervalProtocol(farmInit, subBuffer);

                    this.sendOneWay(init);

                    Log.i(Arrays.toString(init.getProcessed()));

                    Log.i("Connected Farm - " + farmInit + "[" + uniqueKey + "] :: Totally " + clients.size() + " connections are being maintained.");

                } else if(buffer.length == LENGTH_ALERT_PRTC){ // 경보 프로토콜 수신 시
                    Log.e("[ALERT PROTOCOL] : " + farmString + ":" + harvString);
                    List<String> phones = SohaProtocolUtil.getPhoneNumbers(farmString, farmString);

                    if(harvName == null || harvName.equals("null") || harvName.equals("")) {
                        if(farmString.length() == 4 && harvString.length() == 2) Log.e(info);
                        harvName = "단말기 ID : " + harvString;
                    }

                    if(harvString.trim().length() == 2 || farmString.trim().length() == 4){
                        for (String tel : phones) {
                            if(tel != null && !tel.trim().equals("") && !tel.trim().equals("--")) smsService.sendSMS(tel, String.format(ConstProtocol.CONNECTION_MESSAGE, farmName, harvName));
                        }
                    }
                }else {
                    if (!clients.containsKey(uniqueKey)) {
                        Log.i("Unique Key inserted : " + uniqueKey);
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

                    Log.i("Tried to insert a realTime data into redis - " + succ);

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
                            Log.e("Exception on the Auto Reading Process handled.");
                        }
                    });

                    if(!semaphore) synchronizer.start();

                }
            }
        }catch(Exception e) {
            e.printStackTrace();
            Log.e("Exception Handled - " + e.getMessage());
        }finally {
            byteBuffer.compact();
        }
        return;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            super.channelInactive(ctx);
            if(farmString.length() == 4 && harvString.length() == 2) Log.i("Channel Inactivated at [" + farmString + "/" + harvString + "].");

            List<String> phones = SohaProtocolUtil.getPhoneNumbers(farmString, farmString);
            if(harvName == null || harvName.equals("null") || harvName.equals("")) {
                if(farmString.length() == 4 && harvString.length() == 2) Log.e(info);
                harvName = "단말기 ID : " + harvString;
            }
            if(harvString.trim().length() == 2 || farmString.trim().length() == 4) {
                for (String tel : phones)
                    if(tel != null && !tel.trim().equals("") && !tel.trim().equals("--")) smsService.sendSMS(tel, String.format(ConstProtocol.CONNECTION_MESSAGE, farmName));
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.e("통신 이상 SMS 전송 중 에러 : " + e.getMessage());
        }finally {
            Log.i("Connection Finished."); // 커넥션이 마무리 되었음을 디버깅을 위해 출력
            clients.remove(uniqueKey); // 클라이언트 해시맵으로부터 소거함
        }
    }

}
