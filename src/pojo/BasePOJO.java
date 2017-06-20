package pojo;

import constants.ConstProtocol;
import models.ByteSerial;
import utils.HexUtil;
import utils.SohaProtocolUtil;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author 함의진
 * POJO 객체 랩핑 클래스
 * - 이를 상속하는 모든 클래스는 POJO(Plain Old Java Object) 형식을 가지며, 별도의 인터페이스 상속을 해서는 안된다.
 */
public class BasePOJO implements Serializable{

    protected ByteSerial byteSerial;

    protected static final int ARRAY_START_RANGE = 8;
    protected static final int ARRAY_END_LENGTH = 3;

    protected Class classType;


    public ByteSerial getByteSerial() {
        return byteSerial;
    }

    public void setByteSerial(ByteSerial byteSerial) {
        this.byteSerial = byteSerial;
    }

    /**
     * 바이트 합으로부터 해당 자리수의 10진수 값을 반환한다
     * @param offset 메모리 번지수
     * @param square 10의 승수
     * @return 바이트 합의 10의 승수번째 자리수
     */
    protected int getUnitNumberFrom2Byte(int offset, int square){
        int sum = getSumWith2Bytes(offset, SUM_MODE_P);
        String strSum = Integer.toString(sum);
        int len = strSum.length() - (square + 1);
        if(strSum.length() <= len || len < 0) return 0;
        char ascii = strSum.charAt(len);
        int retVal = ascii - '0';

        return retVal;
    }

    /**
     * 바이트 합으로부터 해당 자리수의 10진수 값을 반환한다
     * @param offset 메모리 번지수
     * @param square 10의 승수
     * @return 바이트 합의 10의 승수번째 자리수
     */
    protected int getUnitNumberFrom2ByteABS(int offset, int square){
        int sum = getSumWith2BytesABS(offset, SUM_MODE_P);
        String strSum = Integer.toString(sum);
        int len = strSum.length() - (square + 1);
        if(strSum.length() <= len || len < 0) return 0;
        char ascii = strSum.charAt(len);
        int retVal = ascii - '0';

        return retVal;
    }

    protected int getSumWith2BytesABS(int offset, int mode){
        try {
            int absolute = offset;
            int tempLeft = byteSerial.getProcessed()[absolute];
            int tempRight = byteSerial.getProcessed()[absolute + 1];

            if (tempLeft < 0) tempLeft = tempLeft & 0xff;
            if (tempRight < 0) tempRight = tempRight & 0xff;
            int lhs = tempLeft << 8;
            int rhs = tempRight;

            if (lhs < 0) lhs = lhs & 0xff;
            if (rhs < 0) rhs = rhs & 0xff;

            int total = lhs + rhs;

            if(mode == SUM_MODE_TEMP && total > ConstProtocol.NEGATIVE_THRESHOLD_TEMP){
                total = total - ConstProtocol.NEGATIVE_OFFSET;
            }else if(mode == SUM_MODE_HUMID && total > ConstProtocol.NEGATIVE_THRESHOLD_HUMID){
                total = total - ConstProtocol.NEGATIVE_OFFSET;
            }else if(mode == SUM_MODE_REV && total > ConstProtocol.NEGATIVE_THRESHOLD_REVISION){
                total = total - ConstProtocol.NEGATIVE_OFFSET;
            }

            return total;
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("The size of array was " + byteSerial.getProcessed().length + " and tried to refer an offset[" + (offset + 1) + "]");
            e.printStackTrace();
            return 0;
        }
    }

    protected int getSingleByteABS(int offset){
        int absolute = offset;
        int value = byteSerial.getProcessed()[absolute];
        if(value < 0) value = value & 0xff;
        return value;
    }

    protected char getHangleFrom2ByteABS(int offset){
        int a = getSingleByteABS(offset) << 8;
        int b = getSingleByteABS(offset + 1);

        if(a < 0) a = a & 0xff;
        if(b < 0) b = b & 0xff;

        char c = (char)(a + b);
        return c;
    }

    protected byte[] getAggregation(int lhs, int rhs){
        int total = (lhs * 100) + rhs;
        byte[] ret = SohaProtocolUtil.getHexLocation(total);

        return ret;
    }

    protected char getHangleFrom2Byte(int offset){
        return getHangleFrom2ByteABS(offset);
    }

    protected int getSumByArbitraryRangeBytesABS(int offset, int length){
        int total = 0;
        for(int i = offset; i < offset + length; i++) total += getSumWith2BytesABS(i, SUM_MODE_P);
        return total;
    }

    protected int getSumByArbitraryRangeBytes(int offset, int length){
        int total = 0;
        for(int i = offset; i < offset + length; i++) total += getSumWith2Bytes(i, SUM_MODE_P);
        return total;
    }

    /**
     * Calculating Date or Time by byte index offset and formatting as String with delimiter
     * @param offset Byte array index
     * @param delimiter String delimiter
     * @return Formatted String
     */
    protected String getMDorHMWith2Bytes(int offset, String delimiter){
        int total = getSumWith2Bytes(offset, SUM_MODE_P);
        int header = total / 100;
        int footer = total - (header * 100);

        return String.format("%02d" + delimiter + "%02d", header, footer);
    }

    protected String getMDorHMWith2BytesABS(int offset, String delimiter){
        int total = getSumWith2BytesABS(offset, SUM_MODE_P);
        int header = total / 100;
        int footer = total - (header * 100);

        return String.format("%02d" + delimiter + "%02d", header, footer);
    }

    protected int getLhsFromDual(int offset){
        int total = getSumWith2Bytes(offset, SUM_MODE_P);
        int header = total / 100;
        int footer = total - (header * 100);

        return header;
    }

    protected int getRhsFromDual(int offset){
        int total = getSumWith2Bytes(offset, SUM_MODE_P);
        int header = total / 100;
        int footer = total - (header * 100);

        return footer;
    }

    public static int getBitAggregation(int... bits){
        int total = 0;
        for(int e = 0; e < bits.length; e++) total += bits[e] << (bits.length - e - 1);
        return total;
    }

    // TODO getBitAggr - 일반화

    public static byte[] getValuePairFromString(String str){

        String[] arr = null;
        int idx = -1;

        for(int e = 0; e < str.toCharArray().length; e++){
            char c = str.toCharArray()[e];
            if(!Character.isDigit(c)) idx = e;
        }

        if(idx == -1) return new byte[]{0, 0};

        String head = str.substring(0, idx);
        String tail = str.substring(idx + 1, str.length());

        int ihead, itail;

        try{
            ihead = Integer.parseInt(head);
            itail = Integer.parseInt(tail);
            if(ihead < 0) ihead = ihead & 0xff;
            if(itail < 0) itail = itail & 0xff;
        }catch (NumberFormatException e){
            ihead = 0;
            itail = 0;
        }

        int total = (ihead * 100) + itail;

        return SohaProtocolUtil.getHexLocation(total);
    }

    protected int getLhsFromDualABS(int offset){
        int total = getSumWith2BytesABS(offset, SUM_MODE_P);
        int header = total / 100;
        int footer = total - (header * 100);

        return header;
    }

    protected int getRhsFromDualABS(int offset){
        int total = getSumWith2BytesABS(offset, SUM_MODE_P);
        int header = total / 100;
        int footer = total - (header * 100);

        return footer;
    }

    /**
     * Calculating the numerical summation in range of offset
     * @param offset
     * @return Numerical summation
     *
     * < Comment >
     * '절대위치'를 이곳에서 계산하기에 다른 곳의 offset을 조정해서는 절대 안 됨
     * 프로토콜 변경이 있을 경우, BasePOJO의 상수를 수정해야 함
     */
    protected int getSumWith2Bytes(int offset, int mode){
        int absolute = offset + ARRAY_START_RANGE;
        int tempLeft = byteSerial.getProcessed()[absolute];
        int tempRight = byteSerial.getProcessed()[absolute + 1];
        if(tempLeft < 0) tempLeft = tempLeft & 0xff;
        if(tempRight < 0) tempRight = tempRight & 0xff;
        int lhs = tempLeft << 8;
        int rhs = tempRight;

        if(lhs < 0) lhs = lhs & 0xff;
        if(rhs < 0) rhs = rhs & 0xff;

        int total = lhs + rhs;

        if(mode == SUM_MODE_TEMP && total > ConstProtocol.NEGATIVE_THRESHOLD_TEMP){
            total = total - ConstProtocol.NEGATIVE_OFFSET;
        }else if(mode == SUM_MODE_HUMID && total > ConstProtocol.NEGATIVE_THRESHOLD_HUMID){
            total = total - ConstProtocol.NEGATIVE_OFFSET;
        }else if(mode == SUM_MODE_REV && total > ConstProtocol.NEGATIVE_THRESHOLD_REVISION){
            total = total - ConstProtocol.NEGATIVE_OFFSET;
        }

        return total;
    }

    public static final int SUM_MODE_P = 0;
    public static final int SUM_MODE_TEMP = 1;
    public static final int SUM_MODE_HUMID = 2;
    public static final int SUM_MODE_REV = 3;

    protected int getSingleByte(int offset){
        return getSingleByteABS(ARRAY_START_RANGE + offset);
    }

    protected int getBooleanValueFromByte(int offset, int bitIndex){
        assert bitIndex < 8;

        final String format = "00000000";
        int value = getSingleByte(offset);

        String bin = Integer.toBinaryString(value);
        String fmt = "";
        String retVal = "";
        if(bin.length() < 8){
            int leak = 8 - bin.length();
            fmt = format.substring(0, leak);
        }

        retVal = fmt + bin;

        int newIndex = retVal.length() - (bitIndex + 1);

        if(retVal.charAt(newIndex) == '1') return 1;
        else return 0;
    }

    public static int getBooleanValue(byte[] array, int offset, int index){
        assert index < 8;

        final String format = "00000000";

        int absolute = offset;
        int value = array[absolute];
        if(value < 0) value = value & 0xff;
        String bin = Integer.toBinaryString(value);
        String fmt = "";
        String retVal = "";
        if(bin.length() < 8){
            int leak = 8 - bin.length();
            fmt = format.substring(0, leak);
        }

        retVal = fmt + bin;

        int newIndex = retVal.length() - (index + 1);

        if(retVal.charAt(newIndex) == '1') return 1;
        else return 0;
    }

    protected int getBooleanValueFrom2Byte(int offset, int bitIndex){
        assert bitIndex < 16;

        if(bitIndex >= 8){
            return getBooleanValueFromByte(offset, bitIndex - 8);
        }else{
            return getBooleanValueFromByte(offset + 1, bitIndex);
        }
    }

    public static void main(String... args){
        byte[] arr = new byte[]{83, 84, 48, 48, 55, 56, 48, 49, 2, 107, 6, 93, 0, 7, 0, 0, 0, 0, -111, -19, 2, 107, 6, 93, 0, 7, 0, 0, 0, 0, -111, -19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -111, -12, 2, 107, 6, 93, 0, 7, 0, 0, 0, 0, -111, -19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -111, -12, 2, 107, 6, 93, 0, 7, 0, 0, 0, 0, -111, -19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -111, -12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 44, -102, 1, -65, 1, 28, 0, 9, 0, 43, 0, 0, 0, 99, 6, 5, 40, -10, 64, 60, 61, 113, 64, 74, -103, -102, 64, 89, 61, 113, 64, 106, -123, 31, 64, 123, 10, 61, 64, -121, -93, -41, 64, -112, 51, 51, 64, -101, 20, 123, 64, -90, -21, -123, 64, -79, 102, 102, 64, -66, 51, 51, 64, -53, -103, -102, 64, -39, 81, -20, 64, -24, 0, 0, 64, -8, 81, -20, 65, 4, 30, -72, 65, 13, 102, 102, 65, 22, 0, 0, 65, 32, -103, -102, 65, 41, -52, -51, 65, 52, -103, -102, 65, 65, -52, -51, 65, 76, -103, -102, 65, 89, 0, 0, 44, -102, 1, -65, 1, 28, 0, 9, 65, -126, 102, 102, 65, -118, 102, 102, 65, -110, 51, 51, 65, -101, -52, -51, 65, -92, 102, 102, 65, -82, 0, 0, 4, -80, 0, -6, 1, -62, 0, 9, -35, -35, -103, -102, 65, 25, 4, 87, 8, -82, 13, 5, 4, -57, 0, -56, 0, 5, 0, 0, 0, 0, 9, -63, -1, -60, 0, 44, 6, 100, 2, 107, 0, 0, 7, -31, 2, 107, 6, 93, -1, 15, 0, 0, 0, 0, 86, 13, 10};
        BasePOJO basePOJO = new BasePOJO();
        basePOJO.byteSerial = new ByteSerial(arr);

        System.out.println(basePOJO.getSumWith2Bytes(294, SUM_MODE_P));
        for(int i = 0; i < 8; i++)
            System.out.print(basePOJO.getBooleanValueFromByte(294, i) + " ");

    }

    protected int toDecimalFromBinaryValue(int offset, int bitBeginIndex, int length){
        String total = "";
        for(int i = bitBeginIndex; i < bitBeginIndex + length; i++){
            total += getBooleanValueFrom2Byte(offset, i);
        }

        return Integer.parseInt(total, 2);
    }

    protected int getBitLhsFromDual(int value){
        String bin = Integer.toBinaryString(value);
        if(bin.length() > 2) return 0;
        if(bin.length() < 2) return 0;
        return Integer.parseInt(bin.substring(0, 1));
    }

    protected int getBitRhsFromDual(int value){
        String bin = Integer.toBinaryString(value);
        if(bin.length() > 2) return 0;
        if(bin.length() < 2) return value;
        return Integer.parseInt(bin.substring(1, 2));
    }

    @Deprecated
    protected int getBooleanValueFromByteABS(int offset, int bitIndex){
        assert bitIndex < 8;

        final String format = "00000000";
        int value = getSingleByteABS(offset);
        String bin = Integer.toBinaryString(value);
        String fmt = "";
        String retVal = "";
        if(bin.length() < 8){
            int leak = 8 - bin.length();
            fmt = format.substring(0, leak);
        }

        retVal = fmt + bin;

        int newIndex = retVal.length() - (bitIndex + 1);

        if(retVal.charAt(newIndex) == '1') return 1;
        else return 0;
    }

    protected int getBooleanValueFrom2ByteABS(int offset, int bitIndex){
        assert bitIndex < 16;

        if(bitIndex >= 8){
            return getBooleanValueFromByteABS(offset, bitIndex - 8);
        }else{
            return getBooleanValueFromByteABS(offset + 1, bitIndex);
        }
    }

    protected int toDecimalFromBinaryValueABS(int offset, int bitBeginIndex, int length){
        String total = "";
        for(int i = bitBeginIndex; i < bitBeginIndex + length; i++){
            total += getBooleanValueFrom2ByteABS(offset, i);
        }

        return Integer.parseInt(total, 2);
    }
}
