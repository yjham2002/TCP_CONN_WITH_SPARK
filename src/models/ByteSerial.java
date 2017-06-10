package models;

import constants.ConstProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.HexUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static final int POOL_SIZE = 512; // 스트림 수신 버퍼 사이즈

    public static final int TYPE_NONE = 0; // 미결정 타입 혹은 손상된 타입 (수신)
    public static final int TYPE_INIT = 10; // 전원 인가 시 접속되는 프로토콜 (수신)
    public static final int TYPE_SET = 20; // 데이터 업로드 주기 설정 프로토콜 타입 (발신)
    public static final int TYPE_WRITE = 30; // 클라이언트로의 쓰기 요청 (발신)
    public static final int TYPE_READ = 40; // 클라이언트로의 읽기 요청 (발신)
    public static final int TYPE_WRITE_SUCC = 50; // 클라이언트로부터 수신되는 쓰기 성공 프로토콜 (수신)
    public static final int TYPE_READ_SUCC = 60; // 클라이언트로부터 수신되는 쓰기 성공 프로토콜 (수신)
    public static final int TYPE_ALERT = 70; // 클라이언트에게 경고를 전송하기 위한 프로토콜 (발신)


    private boolean loss = false;
    private byte[] original;
    private byte[] processed;
    private int length;
    private int type = TYPE_NONE;

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

        log.info(Arrays.toString(bytes));

        this.processed = bytes.clone();
        this.original = bytes.clone();

        boolean sound = HexUtil.isCheckSumSound(bytes);

        if(sound) System.out.println("Protocol Generated And It is sound");
        else System.out.println("Protocol Generated But it is not sound");

        this.type = type;

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
//        log.info(Arrays.toString(bytes));

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

        if(loss) log.info("Packet-Loss Occured or is empty data - Ignoring" + reason);
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
