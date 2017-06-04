package pojo;

import models.ByteSerial;

import java.io.Serializable;

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
        int sum = getSumWith2Bytes(offset);
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
        int sum = getSumWith2BytesABS(offset);
        String strSum = Integer.toString(sum);
        int len = strSum.length() - (square + 1);
        if(strSum.length() <= len || len < 0) return 0;
        char ascii = strSum.charAt(len);
        int retVal = ascii - '0';

        return retVal;
    }

    protected int getSumWith2BytesABS(int offset){
        int absolute = offset;
        int lhs = byteSerial.getProcessed()[absolute] << 8;
        int rhs = byteSerial.getProcessed()[absolute + 1];
        int total = lhs + rhs;

        return total;
    }

    protected int getSingleByteABS(int offset){
        int absolute = offset;
        return byteSerial.getProcessed()[absolute];
    }

    protected char getHangleFrom2ByteABS(int offset){
        int a = getSingleByteABS(offset) << 8;
        int b = getSingleByteABS(offset + 1);
        char c = (char)(a + b);
        return c;
    }

    protected char getHangleFrom2Byte(int offset){
        int a = getSingleByte(offset) << 8;
        int b = getSingleByte(offset + 1);
        char c = (char)(a + b);
        return c;
    }

    protected int getSumByArbitraryRangeBytesABS(int offset, int length){
        int total = 0;
        for(int i = offset; i < offset + length; i++) total += getSumWith2BytesABS(i);
        return total;
    }

    protected int getSumByArbitraryRangeBytes(int offset, int length){
        int total = 0;
        for(int i = offset; i < offset + length; i++) total += getSumWith2Bytes(i);
        return total;
    }

    /**
     * Calculating Date or Time by byte index offset and formatting as String with delimiter
     * @param offset Byte array index
     * @param delimiter String delimiter
     * @return Formatted String
     */
    protected String getMDorHMWith2Bytes(int offset, String delimiter){
        int total = getSumWith2Bytes(offset);
        int header = total >> 8;
        int footer = total - (header << 8);

        return String.format("%02d" + delimiter + "%02d", header, footer);
    }

    protected String getMDorHMWith2BytesABS(int offset, String delimiter){
        int total = getSumWith2BytesABS(offset);
        int header = total >> 8;
        int footer = total - (header << 8);

        return String.format("%02d" + delimiter + "%02d", header, footer);
    }

    protected int getLhsFromDual(int offset){
        int total = getSumWith2Bytes(offset);
        int header = total >> 8;
        int footer = total - (header << 8);

        return header;
    }

    protected int getRhsFromDual(int offset){
        int total = getSumWith2Bytes(offset);
        int header = total >> 8;
        int footer = total - (header << 8);

        return footer;
    }

    protected int getLhsFromDualABS(int offset){
        int total = getSumWith2BytesABS(offset);
        int header = total >> 8;
        int footer = total - (header << 8);

        return header;
    }

    protected int getRhsFromDualABS(int offset){
        int total = getSumWith2BytesABS(offset);
        int header = total >> 8;
        int footer = total - (header << 8);

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
    protected int getSumWith2Bytes(int offset){
        int absolute = offset + ARRAY_START_RANGE;
        int lhs = byteSerial.getProcessed()[absolute] << 8;
        int rhs = byteSerial.getProcessed()[absolute + 1];
        int total = lhs + rhs;

        return total;
    }

    protected int getSingleByte(int offset){
        int absolute = offset + ARRAY_START_RANGE;
        return byteSerial.getProcessed()[absolute];
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

    protected int getBooleanValueFrom2Byte(int offset, int bitIndex){
        assert bitIndex < 16;

        if(bitIndex >= 8){
            return getBooleanValueFromByte(offset + 1, bitIndex - 8);
        }else{
            return getBooleanValueFromByte(offset, bitIndex);
        }
    }

    protected int toDecimalFromBinaryValue(int offset, int bitBeginIndex, int length){
        String total = "";
        for(int i = bitBeginIndex; i < bitBeginIndex + length; i++){
            total += getBooleanValueFrom2Byte(offset, i);
        }

        return Integer.parseInt(total, 2);
    }

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
            return getBooleanValueFromByteABS(offset + 1, bitIndex - 8);
        }else{
            return getBooleanValueFromByteABS(offset, bitIndex);
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
