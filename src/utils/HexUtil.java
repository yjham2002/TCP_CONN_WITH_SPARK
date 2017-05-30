package utils;

import java.util.Arrays;

/**
 * Created by a on 2017-05-29.
 */
public class HexUtil {

    public static String asciiToHex(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

    public static String hexToAscii(String hexValue) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static byte checkSum(byte[] bytes){
        int checkSum = 0;
        for(byte b : bytes) checkSum += b;

        return (byte)checkSum;
    }

    public static byte checkSumByFull(byte[] bytes){
        return checkSum(Arrays.copyOf(bytes, bytes.length - 3));
    }

    public static boolean isCheckSumSound(byte[] bytes){
        if(bytes.length - 3 < 0) return false;
        return checkSumByFull(bytes) == bytes[bytes.length - 3];
    }

}
