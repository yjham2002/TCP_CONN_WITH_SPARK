package models;

import constants.ConstProtocol;
import utils.HexUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 함의진
 * 송수신되는 바이트를 가공하고 인식하기 위한 캡슐화 클래스
 */
public class ByteSerial{

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

    /**
     * 클라이언트로 전송하기 위한 시리얼을 가공하기 위한 생성자
     * @param bytes
     * @param type
     */
    public ByteSerial(byte[] bytes, int type){

        bytes[bytes.length - 3] = HexUtil.checkSum(Arrays.copyOf(bytes, bytes.length - 3));

        System.out.println(Arrays.toString(bytes));

        this.processed = bytes.clone();
        this.original = bytes.clone();
        this.type = type;

    }

    /**
     * 클라이언트로부터 수신된 시리얼을 읽기 위한 생성자
     * @param bytes
     */
    public ByteSerial(byte[] bytes){

        int newLen = 0;

        this.original = bytes.clone();
        for(newLen = 0; newLen < bytes.length; newLen++){
            if(bytes[newLen] == 0) break;
        }

        System.out.println(newLen);

        this.processed = Arrays.copyOf(bytes.clone(), newLen);
        this.length = newLen;

        System.out.println(Arrays.toString(processed));

        if(bytes.length < 4) loss = true;
        if(!this.startsWith(ConstProtocol.STX)) loss = true;
        if(!this.endsWith(ConstProtocol.ETX)) loss = true;

        System.out.println(loss);

        setTypeAutomatically();
    }

    /**
     * Byte 배열을 통한 생성 시 호출되며, 길이와 패킷 손실 여부에 따라 자동으로 프로토콜 타입을 판단한다.
     * @return
     */
    private int setTypeAutomatically(){
        if(isLoss()) {
            this.type = TYPE_NONE;
            return TYPE_NONE;
        }



        this.type = TYPE_NONE;
        return TYPE_NONE;
    }

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

    public boolean startsWith(byte[] bytes){
        if(this.processed.length < bytes.length) return false;
        else{
            for(int e = 0; e < bytes.length; e++){
                if(bytes[e] != this.processed[e]) return false;
            }
            return true;
        }
    }

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
        return "";
    }
}
