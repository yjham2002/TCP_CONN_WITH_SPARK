package utils;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import constants.ConstProtocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author 함의진
 * 소하테크에 최적화된 모드버스 프로토콜을 생성하는 유틸리티 정적 클래스
 */
public class SohaProtocolUtil {

    /**
     * 이니셜 프로토콜로부터 농장에 따른 유니크한 키를 생성하는 메소드
     * @param bytes
     * @return
     */
    public static String getUniqueKeyByInit(byte[] bytes){
        byte[] farm = getFarmCodeByInit(bytes);
        return getUniqueKeyByFarmCode(farm);
    }

    public static String getMeaninglessUniqueKey(){
        return getUniqueKeyByInit(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
    }

    public static String getUniqueKeyByFarmCode(byte[] farm){
        String uKey = "HEADER_" + getBytesConcatWithDeilmiter(farm, "_") + "_SOHAUNIFARM";
        return uKey;
    }

    public static String getBytesConcatWithDeilmiter(byte[] bytes, String delimiter){
        String ret = "";
        for(int e = 0 ; e < bytes.length; e++){
            ret += bytes[e];
            if(e + 1 < bytes.length) ret += delimiter;
        }

        return ret;
    }

    /**
     * 이니셜 프로토콜로부터 농장코드를 추출하는 메소드
     * @param bytes 버퍼 바이트
     * @return 농장 코드
     */
    public static byte[] getFarmCodeByInit(byte[] bytes){
        byte[] ret = new byte[]{bytes[13], bytes[14], bytes[15], bytes[16]};
        return ret;
    }

    /**
     * 일반 프로토콜로부터 농장코드를 추출하는 메소드
     * @param bytes 버퍼 바이트
     * @return 농장 코드
     */
    public static byte[] getFarmCodeByProtocol(byte[] bytes){
        byte[] ret = new byte[]{bytes[2], bytes[3], bytes[4], bytes[5]};
        return ret;
    }

    public static byte[] getHexLocation(int location){
        if(location < 0) location = location & 0xff;

        String format = "0000";
        String hex = Integer.toHexString(location);
        String result = hex;
        int len = 0;
        if(hex.length() < 4){
            len = 4 - hex.length();
            result = format.substring(0, len) + hex;
        }

        int header = Integer.parseInt(result.substring(0, 2), 16);
        int footer = Integer.parseInt(result.substring(2, 4), 16);

        if(header < 0) header = header & 0xff;
        if(footer < 0) footer = footer & 0xff;

        byte[] ret = new byte[]{(byte)header, (byte)footer};

        return ret;
    }

    public static byte[] getNBytes(int n){
        byte[] arr = new byte[n];
        Arrays.fill(arr, (byte)0);

        return arr;
    }

    public static byte[] makeWriteProtocol(int location, int length, int id, byte[] farmCode, byte[] harvCode, byte[] data){
        Modbus modbus = new Modbus();
        byte[] protocol;
        byte[] loc = getHexLocation(location);
        byte[] len = concat(new byte[]{0x00}, getHexLocation(length));
        byte[] deviceId = new byte[]{(byte)id};
        byte[] crc16 = modbus.fn_makeCRC16(concat(deviceId, ConstProtocol.FUNCTION_WRITE, loc, len, data));
        byte[] checkSum = new byte[]{HexUtil.checkSum(concat(ConstProtocol.STX, farmCode, harvCode, deviceId, ConstProtocol.FUNCTION_WRITE, loc, len, data, crc16))};
        protocol = concat(ConstProtocol.STX, farmCode, harvCode, deviceId, ConstProtocol.FUNCTION_WRITE, loc, len, data, crc16, checkSum, ConstProtocol.ETX);

        return protocol;
    }

    public static byte[] makeReadProtocol(int location, int length, int id, byte[] farmCode, byte[] harvCode){
        Modbus modbus = new Modbus();
        byte[] protocol = null;
        byte[] loc = getHexLocation(location);
        byte[] len = getHexLocation(length);
        byte[] deviceId = new byte[]{(byte)id};
        byte[] crc16 = modbus.fn_makeCRC16(concat(deviceId, ConstProtocol.FUNCTION_READ, loc, len));
        byte[] checkSum = new byte[]{HexUtil.checkSum(concat(ConstProtocol.STX, farmCode, harvCode, deviceId, ConstProtocol.FUNCTION_READ, loc, len, crc16))};
        protocol = concat(ConstProtocol.STX, farmCode, harvCode, deviceId, ConstProtocol.FUNCTION_READ, loc, len, crc16, checkSum, ConstProtocol.ETX);

        return protocol;
    }

    public static byte[][] makeReadProtocols(int location, int length, int id, byte[] farmCode, byte[] harvCode){

        int start = location;
        int ceil = (int)Math.ceil((double)length / (double)ConstProtocol.READ_LIMIT);

        byte[][] bulk = new byte[ceil][];

        for(int e = 1; e <= ceil; e++){
            int jump = (ConstProtocol.READ_LIMIT * e) - 1;
            if(e == ceil) jump = length - 1;
            int newLen = (jump - start + 1);
            bulk[e - 1] = makeReadProtocol(start, newLen, id, farmCode, harvCode);
            start = jump + 1;
        }

        return bulk;
    }

    /**
     * 일반 프로토콜로부터 재배동 코드를 추출하는 메소드
     * @param bytes 버퍼 바이트
     * @return 재배동 코드
     */
    public static byte[] getHarvCodeByProtocol(byte[] bytes){
        byte[] ret = new byte[]{bytes[6], bytes[7]};
        return ret;
    }

    public static byte[] getLocationCode(byte[] bytes){
        return concat(getFarmCodeByProtocol(bytes), getHarvCodeByProtocol(bytes));
    }

    public static String getLocationCodeAsString(byte[] bytes){
        return Arrays.toString(getLocationCode(bytes));
    }

    /**
     * 이니셜 프로토콜과 재배동 번호, 데이터 업로드 주기를 통해 초기화 프로토콜을 생성한다.
     * 재배동 번호와 데이터 업로드 주기는 10진수로 입력받으며, 내부적으로 아스키로 변환한다.
     * @param initBuffer
     * @return
     */
    public static byte[] getInitProtocol(byte[] initBuffer, int harvCode1, int harvCode2, int min10, int min, int sec10, int sec){
        byte[] farmCode = getFarmCodeByInit(initBuffer);
        byte[] header = concat(ConstProtocol.STX, farmCode, intsToAscii(harvCode1, harvCode2, min10, min, sec10, sec));
        byte[] checkSum = new byte[]{HexUtil.checkSum(header)};

        return concat(header, checkSum, ConstProtocol.ETX);
    }

//    public static byte[] getWriteProtocol(byte[] farmCode, int harvCode1, int harvCode2, int ID, int )

    /**
     * 가변인자 바이트 어레이 컨켓네이션 메소드
     * @param arrays
     * @return
     */
    public static byte[] concat(byte[]... arrays){
        int len = 0;
        for(byte[] arr : arrays) len += arr.length;

        byte[] concat = new byte[len];

        for(int e = arrays.length - 1; e >= 0; e--){
            byte[] unit = arrays[e];
            for(int s = unit.length - 1; s >= 0; s--){
                concat[--len] = arrays[e][s];
            }
        }

        return concat;
    }

    /**
     * 정수로부터 바이트형 아스키코드를 계산하는 메소드
     * @param number
     * @return
     */
    public static byte intToAscii(int number){
        int ascii = number + '0';
        return (byte)ascii;
    }

    /**
     * 정수들로부터 바이트형 아스키코드 배열을 추출하는 메소드
     * @param numbers
     * @return
     */
    public static byte[] intsToAscii(int... numbers){
        byte[] array = new byte[numbers.length];
        for(int e = 0; e < numbers.length; e++) array[e] = intToAscii(numbers[e]);

        return array;
    }

}
