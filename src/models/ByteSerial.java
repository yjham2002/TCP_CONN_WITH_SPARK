package models;

import constants.ConstProtocol;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.HexUtil;
import utils.SohaProtocolUtil;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author 함의진
 * 송수신되는 바이트를 가공하고 인식하기 위한 캡슐화 클래스
 */
public class ByteSerial implements Serializable{

    /**
     * SLF4J 로거
     */
    Logger log;

    public static final int POOL_SIZE = 256; // 스트림 수신 버퍼 사이즈

    public static final int TYPE_NONE = 0; // 미결정 타입 혹은 손상된 타입 (수신)
    public static final int TYPE_INIT = 10; // 전원 인가 시 접속되는 프로토콜 (수신)
    public static final int TYPE_SET = 20; // 데이터 업로드 주기 설정 프로토콜 타입 (발신)
    public static final int TYPE_WRITE = 30; // 클라이언트로의 쓰기 요청 (발신)
    public static final int TYPE_READ = 40; // 클라이언트로의 읽기 요청 (발신)
    public static final int TYPE_WRITE_SUCC = 50; // 클라이언트로부터 수신되는 쓰기 성공 프로토콜 (수신)
    public static final int TYPE_READ_SUCC = 60; // 클라이언트로부터 수신되는 쓰기 성공 프로토콜 (수신)
    public static final int TYPE_ALERT = 70; // 클라이언트에게 경고를 전송하기 위한 프로토콜 (발신)
    public static final int TYPE_FORCE = 80;


    private boolean loss = false;
    private byte[] original;
    private byte[] processed;
    private int length;
    private int type = TYPE_NONE;

    private long tid;
    private byte addr1;
    private byte addr2;

    @Deprecated
    private ByteSerial(){}

    /**
     * 클라이언트로 전송하기 위한 시리얼을 가공하기 위한 생성자
     * @param bytes
     * @param type
     */
    public ByteSerial(byte[] bytes, int type){
        log = LoggerFactory.getLogger(this.getClass());
        //bytes[bytes.length - 3] = (byte)HexUtil.checkSumByFull(bytes);

//        log.info(Arrays.toString(bytes));

        this.processed = bytes.clone();
        this.original = bytes.clone();

        boolean sound = HexUtil.isCheckSumSound(bytes);

        if(type != TYPE_FORCE) {
            if (sound) System.out.println("Protocol Generated And It is sound");
            else System.out.println("Protocol Generated But it is not sound");
        }else{
            System.out.println("ByteSerial Generated Forcely");
        }

        this.type = type;

    }

    public ByteSerial(byte[] bytes, int type, long tid, byte addr1, byte addr2){
        this(bytes, type);
        this.tid = tid;
        this.addr1 = addr1;
        this.addr2 = addr2;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public byte getAddr1() {
        return addr1;
    }

    public void setAddr1(byte addr1) {
        this.addr1 = addr1;
    }

    public byte getAddr2() {
        return addr2;
    }

    public void setAddr2(byte addr2) {
        this.addr2 = addr2;
    }

    public static byte[] trim(byte[] bytes){
        int newLen = bytes.length - 1;

        byte[] arr;
        for(; newLen >= 0; newLen--){
            if(bytes[newLen] != 0) {
                newLen += 1;
                break;
            }
        }

        arr = Arrays.copyOf(bytes.clone(), newLen >= 0 ? newLen : 0);
        return arr;
    }

    /**
     * 클라이언트로부터 수신된 시리얼을 읽기 위한 생성자
     * @param bytes
     */
    public ByteSerial(byte[] bytes){
        log = LoggerFactory.getLogger(this.getClass());
        int newLen = bytes.length - 1;

        this.original = bytes.clone();
        for(; newLen >= 0; newLen--){
            if(bytes[newLen] != 0) {
                newLen += 1;
                break;
            }
        }

        this.processed = Arrays.copyOf(bytes.clone(), newLen >= 0 ? newLen : 0);
        this.length = newLen;

        String reason = "\n";

        log.info(Arrays.toString(processed));
        log.info(Arrays.toString(bytes));

        if(!HexUtil.isCheckSumSound(this.processed)) {
            loss = true;
            reason += "[Checksum does not match]\n";
        }
        if(bytes.length < 4) {
            loss = true;
            reason += "[byteSerial is too short]\n";
        }
        if(!this.startsWith(ConstProtocol.STX)) {
            loss = true;
            reason += "[no STX found]\n";
        }
        if(!this.endsWith(ConstProtocol.ETX)) {
            loss = true;
            reason += "[no ETX found]\n";
        }

        if(loss) log.info("Packet-Loss Occured or is empty data - Ignoring [" + processed.length + "] " + reason);
        else log.info("Packet has been arrived successfully [" + processed.length + ']');

        setTypeAutomatically();
    }

    /**
     * Byte 배열을 통한 생성 시 호출되며, 길이와 패킷 손실 여부에 따라 자동으로 프로토콜 타입을 판단한다.
     * @return
     */
    @Deprecated
    private int setTypeAutomatically(){
        if(isLoss()) {
            this.type = TYPE_NONE;
            return TYPE_NONE;
        }

        this.type = TYPE_NONE;
        return TYPE_NONE;
    }

    @JsonIgnore
    public byte[] getPureBytes(){
        return Arrays.copyOfRange(processed, ConstProtocol.RANGE_READ_START, processed.length - ConstProtocol.RANGE_READ_END);
    }

    /**
     * 바이트 시리얼 인스턴스를 가변인자로 입력받아 이들의 데이터 범위를 모두 접합하여 반환한다.
     * STX와 ETX 그리고 체크섬과 CRC를 모두 제거한다.
     * @param serials 바이트 시리얼 가변인자
     * @return 접합 데이터
     */
    @JsonIgnore
    public static byte[] getPureDataConcat(ByteSerial... serials){
        byte[] arr = new byte[]{};
        byte temp;
        for(int i = 0; i < serials.length; i++){
            if(arr.length > 0){
                if(serials[i].getPureBytes()[0] == arr[arr.length - 1]) System.out.println("WARNING ::::::: [Check if there is any Data Redundancy while Concatenation]");
            }
            arr = SohaProtocolUtil.concat(arr, serials[i].getPureBytes());
        }

        return arr;
    }

    @JsonIgnore
    public static byte[] getPureDataConcat(List<ByteSerial> serials){
        byte[] arr = new byte[]{};
        for(int i = 0; i < serials.size(); i++){
            if(arr.length > 0){
                if(serials.get(i).getPureBytes()[0] == arr[arr.length - 1]) System.out.println("WARNING ::::::: [Data Redundancy occurred while Concatenation]");
            }
            arr = SohaProtocolUtil.concat(arr, serials.get(i).getPureBytes());
        }

        return arr;
    }

    @JsonIgnore
    public static byte[] getPureDataConcatForRealtime(List<ByteSerial> serials){
        if(serials.size() < 2) return null;

        byte[] padding = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        System.out.println("### " + Arrays.toString(serials.get(0).getProcessed()));
        System.out.println("### " + Arrays.toString(serials.get(1).getProcessed()));
        byte[] arr = SohaProtocolUtil.concat(serials.get(0).getPureBytes(), padding, serials.get(1).getPureBytes());
        return arr;
    }

    /**
     * 바이트 시리얼의 종결 배열을 확인하기 위한 메소드
     * @param bytes
     * @return
     */
    public boolean endsWith(byte[] bytes){
        if(this.processed.length < bytes.length) return false;
        else{
            int inner = 0;
            for(int e = this.processed.length - bytes.length; e < this.processed.length; e++, inner++){
                if(bytes[inner] != this.processed[e]) return false;
            }
            return true;
        }
    }

    /**
     * 바이트 시리얼의 초기 배열을 확인하기 위한 메소드
     * @param bytes
     * @return
     */
    public boolean startsWith(byte[] bytes){
        if(this.processed.length < bytes.length) return false;
        else{
            for(int e = 0; e < bytes.length; e++){
                if(bytes[e] != this.processed[e]) return false;
            }
            return true;
        }
    }

    public static boolean startsWith(byte[] sample, byte[] bytes){
        if(sample.length < bytes.length) return false;
        else{
            for(int e = 0; e < bytes.length; e++){
                if(bytes[e] != sample[e]) return false;
            }
            return true;
        }
    }

    /**
     * 패킷 손실 여부를 반환하기 위한 메소드이며, 수신용 생성자를 이용한 경우에만 유효한 메소드임
     * @return
     */
    public boolean isLoss() {
        return loss;
    }

    public void setLoss(boolean loss) {
        this.loss = loss;
    }

    public byte[] getOriginal() {
        return original;
    }

    public void setOriginal(byte[] original) {
        this.original = original;
    }

    public byte[] getProcessed() {
        return processed;
    }

    public void setProcessed(byte[] processed) {
        this.processed = processed;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString(){
        return Arrays.toString(this.processed);
    }
}
