package utils;

import java.util.Arrays;
import java.util.Calendar;

/**
 * @author 함의진
 * 16진수 및 아스키 코드 관련 유틸리티 메소드를 정의하는 클래스
 */
public class HexUtil {

    /**
     * 아스키로부터 16진수 값을 반환
     * @param asciiValue
     * @return
     */
    public static String asciiToHex(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

    /**
     * 16진수 값으로부터 아스키를 반환
     * @param hexValue
     * @return
     */
    public static String hexToAscii(String hexValue) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    /**
     * 바이트 어레이로부터 체크섬을 계산하기 위한 메소드
     * 비고 : 체크섬을 구하기 위한 범위만을 파라미터로 전송해야 함
     * @param bytes 체크섬을 구하기 위한 범위만을 추출한 바이트 어레이
     * @return 체크섬 바이트
     */
    public static byte checkSum(byte[] bytes){
        int checkSum = 0;
        for(byte b : bytes) {
//            if(b < 0) checkSum += b & 0xff;
//            else checkSum += b;
            checkSum += b & 0xff;
        }

        return (byte)checkSum;
    }

    /**
     * 모드버스 프로토콜에서 종결로부터 3번째의 자리까지를 추출하여 체크섬을 계산하는 메소드
     * checkSum 메소드와 구분하여 사용하여야 함
     * @param bytes 모드버스 프로토콜 전체 바이트 어레이
     * @return 체크섬 바이트
     */
    public static byte checkSumByFull(byte[] bytes){
        return checkSum(Arrays.copyOf(bytes, bytes.length - 3));
    }

    /**
     * 수신된 바이트 버퍼에 명시된 체크섬과 계산하여 도출한 체크섬이 동일한지 체크하기 위한 무결성 확인 메소드
     * @param bytes 바이트 버퍼(모드버스 혹은 캐리지리턴 종결 기반 프로토콜 바이트 어레이)
     * @return 체크섬 무결성 여부
     */
    public static boolean isCheckSumSound(byte[] bytes){
        if(bytes.length - 3 < 0) return false;
        int chk = bytes[bytes.length - 3];
        if(chk < 0) chk = chk & 0xff;
        return checkSumByFull(bytes) == (byte)chk;
    }

    //byte 배열을 string 배열로 교체후 Decimal값으로 전환
    public static String[] byteToStringDecimalArray(byte [] ValueData) {
        String[] HexStrArray = null;
        String[] DecimalArray = null;
        try{
            String Hexstr = hexbyteToStr(ValueData);
            Hexstr.trim();
            int start =0 ;
            int end =4;
            HexStrArray = new String [ValueData.length/2];
            for (int count =0 ; count < Hexstr.length() / 4; count ++) {
                HexStrArray[count] = Hexstr.substring(start,end);
                start +=4;
                end +=4;
            }
            DecimalArray = new String [HexStrArray.length];
            for (int count =0; count < HexStrArray.length; count++) {
                DecimalArray[count] = getHextoDec(HexStrArray[count]);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return DecimalArray;
    }

    public static int getNumericValue(byte[] array){
        String str = "";
        for(int i = 0; i < array.length; i++) {
            str += (array[i] - '0') + "";
        }

        return Integer.parseInt(str);
    }

    public static long timestamp(){
        return Calendar.getInstance().getTimeInMillis();
    }

    public static String getNumericStringFromAscii(byte[] array){
        String str = "";
        for(int i = 0; i < array.length; i++) {
            str += (array[i] - '0') + "";
        }

        return str;
    }

    //16진수를 10진수로 변환
    public static String getHextoDec(String hex)
    {
        long value = Long.parseLong(hex,16);
        return String.valueOf(value);
    }

    //byte배열을 String배열로 변환
    public static String hexbyteToStr(byte[] data) {
        StringBuffer sb = new StringBuffer();
        String HexaID;
        for(int x = 0; x < data.length ; x++) {
            HexaID = "0" + Integer.toHexString(0xff & data[x]);
            sb.append(HexaID.substring(HexaID.length()-2));
        }
        //System.out.println("변형 태스트 : "+sb);
        return sb.toString();
    }

    public static int getNegativeValueConditionally(int value, int threshold){
        if(value > threshold) return value - (65536);
        return value;
    }

}
