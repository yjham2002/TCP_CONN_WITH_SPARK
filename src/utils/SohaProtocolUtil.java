package utils;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import constants.ConstProtocol;
import models.ByteSerial;
import mysql.DBManager;
import pojo.RealtimePOJO;
import java.util.Arrays;
import java.util.List;
import static constants.ConstProtocol.LENGTH_LEN_RANGE;

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

    public static String getSimpleKey(byte[] farm){
        String uKey = "[" + getBytesConcatWithDeilmiter(farm, "_") + "]";
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

        if(location < 0) {
            //location = location & 0xff;
            location += ConstProtocol.NEGATIVE_OFFSET;
        }

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

    /**
     * 쓰기 프로토콜을 생성한다
     * @param location
     * @param length WORD 단위 길이
     * @param id
     * @param farmCode
     * @param harvCode
     * @param data
     * @return
     */
    public static byte[] makeWriteProtocol(int location, int length, int id, byte[] farmCode, byte[] harvCode, byte[] data){
        Modbus modbus = new Modbus();
        byte[] protocol;
        byte[] loc = getHexLocation(location);
        byte[] len = new byte[]{0x00, (byte)length, (byte)(length * 2)};
        byte[] deviceId = new byte[]{(byte)id};
        byte[] crc16 = modbus.fn_makeCRC16(concat(deviceId, ConstProtocol.FUNCTION_WRITE, loc, len, data));
        byte[] checkSum = new byte[]{HexUtil.checkSum(concat(ConstProtocol.STX, farmCode, harvCode, deviceId, ConstProtocol.FUNCTION_WRITE, loc, len, data, crc16))};
        protocol = concat(ConstProtocol.STX, farmCode, harvCode, deviceId, ConstProtocol.FUNCTION_WRITE, loc, len, data, crc16, checkSum, ConstProtocol.ETX);

        return protocol;
    }

    /**
     * 플래그 비트를 세팅하기 위한 프로토콜을 생성하는 메소드이다.
     * 본 메소드는 서버로부터 기기에 변경을 알리기 위한 프로토콜을 생성하며, 기기로부터 변경 플래그를 받기 위한 비트를 모두 클리어한다.
     * 동기화 지연을 발생시킬 수 있고, 이는 쓰기 빈도가 실시간 데이터 수신의 빈도보다 낮다는 가정하의 로직이다.
     * @param id 기기 아이디
     * @param farmCode 농가코드
     * @param harvCode 재배동 코드
     * @param flags 플래그 가변인자
     * @return 플래그 프로토콜
     */
    public static byte[] makeFlagNotifyProtocol(int id, byte[] farmCode, byte[] harvCode, int... flags){
        byte[] data = new byte[]{0, ConstProtocol.makeFlagSet(flags)};
        byte[] prtc = makeWriteProtocol(ConstProtocol.RANGE_FLAG_BIT.getHead(), ConstProtocol.RANGE_FLAG_BIT.getTail(), id, farmCode, harvCode, data);
        return prtc;
    }

    public static byte[] makeFlagInitProtocol(int id, byte[] farmCode, byte[] harvCode){
        byte[] data = new byte[]{0, 0};
        byte[] prtc = makeWriteProtocol(ConstProtocol.RANGE_FLAG_BIT.getHead(), ConstProtocol.RANGE_FLAG_BIT.getTail(), id, farmCode, harvCode, data);
        return prtc;
    }

    /**
     * 읽기 프로토콜을 생성한다
     * @param location
     * @param length WORD 단위 길이
     * @param id
     * @param farmCode
     * @param harvCode
     * @return
     */
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

    public static byte[] makeAlertProtocol(byte[] farmCode, byte[] harvCode){
        byte[] prtc = concat(ConstProtocol.STX, farmCode, harvCode);
        byte[] chk = new byte[]{HexUtil.checkSum(prtc)};
        byte[] ret = concat(prtc, chk, ConstProtocol.ETX);

        return ret;
    }

    public static byte[][] makeReadProtocols(int location, int length, int id, byte[] farmCode, byte[] harvCode){
        int start = location;
        int ceil = (int)Math.ceil((double)length / (double)ConstProtocol.READ_LIMIT);

        byte[][] bulk = new byte[ceil][];

        for(int e = 1; e <= ceil; e++){
            int jump = (ConstProtocol.READ_LIMIT * e) - 1;
            if(e == ceil) jump = length - 1;
            int newLen = (jump - (ConstProtocol.READ_LIMIT * (e - 1)) + 1);

            System.out.println("newlen :: " + newLen + " / start :: " + start);

            bulk[e - 1] = makeReadProtocol(start, newLen, id, farmCode, harvCode);

            start += newLen * 2;
        }

        System.out.println(bulk.length + " Protocol has been generated");

        return bulk;
    }

    public static byte[][] makeWriteProtocols(int location, int length, int id, byte[] farmCode, byte[] harvCode, byte[] data){
        int start = location;
        int ceil = (int)Math.ceil((double)length / (double)ConstProtocol.READ_LIMIT);

        byte[][] bulk = new byte[ceil][];

        for(int e = 1; e <= ceil; e++){
            int jump = (ConstProtocol.READ_LIMIT * e) - 1;
            if(e == ceil) jump = length - 1;
            int newLen = (jump - (ConstProtocol.READ_LIMIT * (e - 1)) + 1);

            byte[] cropData = Arrays.copyOfRange(data, (start - location), (start - location + (newLen * 2)));

            bulk[e - 1] = makeWriteProtocol(start, newLen, id, farmCode, harvCode, cropData);

            start += newLen * 2;
        }

        System.out.println(bulk.length + " Protocol has been generated");

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

    public static int getLength(byte[] array) throws NumberFormatException{
        int value = Integer.parseInt(HexUtil.getNumericStringFromAscii(array));
        return value;
    }

    public static byte[] leftShift(byte[] array, int offset){
        System.out.println(offset + "/" + array.length);
        return Arrays.copyOfRange(array, offset, array.length);
    }

    public static void main(String... args){
        System.out.println(new byte[]{83, 84, 48, 51, 48, 57, 49, 51, 53, 51, 48, 49, 2, -41, 3, 61, 5, -102, 2, -40, 3, 56, 22, -83, 2, -41, 3, 61, 5, -102, 2, -40, 3, 56, 22, -83, 2, -42, 2, -61, 0, 2, 2, -42, 2, -59, 34, 55, 2, -42, 2, -61, 0, 3, 2, -42, 2, -58, 34, 54, 2, -42, 2, -61, 0, 3, 2, -42, 2, -58, 34, 54, 2, -41, 3, 61, 5, -102, 2, -40, 3, 56, 22, -83, 2, -39, 4, 95, 16, 91, 2, -39, 4, 94, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 64, 0, 0, 0, 99, 4, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, -39, 4, 95, 27, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -52, -52, 0, 0, 0, 0, 1, 48, 1, -6, 2, -60, 3, -123, 0, -56, 0, 1, 0, 0, 0, 0, 26, 116, -1, -60, 0, 47, 3, -118, 3, 33, 0, 0, 7, -31, 2, -44, 5, -33, -1, -113, 0, 113, 13, 10}.length);
    }

    public static byte[] take(byte[] array){
        if(ByteSerial.startsWith(array, ConstProtocol.STX)){
            try{
                int len = getLength(Arrays.copyOfRange(array, 2, LENGTH_LEN_RANGE));
                if(array.length < len + LENGTH_LEN_RANGE) {
                    return null;
                }

                System.out.println(len + " ::: LEN");

                byte[] narr = SohaProtocolUtil.concat(ConstProtocol.STX, Arrays.copyOfRange(array, LENGTH_LEN_RANGE, LENGTH_LEN_RANGE + len));

                System.out.println(narr.length + " :: ");

                return narr;
            }catch (NumberFormatException e){
                return null;
            }
        }

        return null;
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

    public static int getErrorCount(RealtimePOJO realtimePOJO){
        int args[] = getErrorArray(realtimePOJO);
        int sum = 0;
        for(int i = 0; i < args.length; i++) sum += args[i];

        System.out.println("[REALTIME ERROR COUNT] :: " + sum);

        return sum;
    }

    public static int[] getErrorArrayWithDB(String farmCode, String harvCode){
        String sql = "SELECT \n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=0 ORDER BY regDate DESC LIMIT 1) AS err0,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=1 ORDER BY regDate DESC LIMIT 1) AS err1,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=2 ORDER BY regDate DESC LIMIT 1) AS err2,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=3 ORDER BY regDate DESC LIMIT 1) AS err3,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=4 ORDER BY regDate DESC LIMIT 1) AS err4,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=5 ORDER BY regDate DESC LIMIT 1) AS err5,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=6 ORDER BY regDate DESC LIMIT 1) AS err6,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=7 ORDER BY regDate DESC LIMIT 1) AS err7,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=8 ORDER BY regDate DESC LIMIT 1) AS err8,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=9 ORDER BY regDate DESC LIMIT 1) AS err9,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=10 ORDER BY regDate DESC LIMIT 1) AS err10,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=11 ORDER BY regDate DESC LIMIT 1) AS err11,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=12 ORDER BY regDate DESC LIMIT 1) AS err12,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=13 ORDER BY regDate DESC LIMIT 1) AS err13,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=14 ORDER BY regDate DESC LIMIT 1) AS err14,\n" +
                "(SELECT CASE WHEN flag='Y' THEN 1 ELSE 0 END AS flagNum FROM tblError WHERE farmCode='" + farmCode + "' AND dongCode='" + harvCode + "' AND `errCode`=15 ORDER BY regDate DESC LIMIT 1) AS err15\n" +
                ";\n";
        List<String> arr = DBManager.getInstance().getStrings(sql, "err0", "err1", "err2", "err3", "err4", "err5", "err6", "err7", "err8", "err9", "err10", "err11", "err12", "err13", "err14", "err15");
        int[] errArr = new int[16];

        for(int i = 0; i < arr.size(); i++){
            if(arr.get(i) != null && arr.get(i).equals("1")) errArr[i] = 1;
            else errArr[i] = 0;
        }

        return errArr;
    }

    public static int[] getErrorArray(RealtimePOJO realtimePOJO){
        int[] array = new int[]{
                realtimePOJO.getErrdata_internal_co2(),
                realtimePOJO.getErrdata_internal_temp(),
                realtimePOJO.getErrdata_internal_humid(),
                realtimePOJO.getErrdata_internal_ilum(),
                realtimePOJO.getErrdata_vent_relay(),
                realtimePOJO.getErrdata_raisetemp_relay(),
                realtimePOJO.getErrdata_raisecool_relay(),
                realtimePOJO.getErrdata_humidify_relay(),
                realtimePOJO.getErrdata_dehumidify_relay(),
                realtimePOJO.getErrdata_ilum_output(),
                realtimePOJO.getErrdata_crop_data(),
                realtimePOJO.getErrdata_device_connection(),
                realtimePOJO.getErrdata_network1(),
                realtimePOJO.getErrdata_network2(),
                realtimePOJO.getErrdata_network3(),
                realtimePOJO.getErrdata_network4()
        };

        return array;
    }

    public static String getErrorSQL(String farm, String harv, int errCode, String flag){

        if(flag.length() > 1) return "SELECT -1";

        String sql = "insert into `sohatechfarmdb`.`tblError`\n" +
                "            (\n" +
                "             `farmCode`,\n" +
                "             `dongCode`,\n" +
                "             `errCode`,\n" +
                "             `flag`,\n" +
                "             `regDate`)\n" +
                "values (\n" +
                "        '" + farm + "',\n" +
                "        '" + harv + "',\n" +
                "        '" + errCode + "',\n" +
                "        '" + flag + "',\n" +
                "        NOW());";

        return sql;
    }

    public static String getErrorMessage(int errorArray[], String farmName, String harvName){

        String errorMsg = "";

        if(errorArray.length <= 0) return null;

        for(int i = 0; i < errorArray.length; i++){
            if(errorArray[i] != ConstProtocol.FALSE) errorMsg += ConstProtocol.ERROR_MSG[i] + "\n";
        }

        errorMsg = errorMsg.trim();

        String finalMsg = "[" + farmName + " - " + harvName + "]\n" + errorMsg;

        return finalMsg;
    }

}
