package pojo;

import models.ByteSerial;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 함의진
 * 일령 데이터를 추상화 하기 위한 클래스
 * CropDayDetailPOJO는 순서가 있는 센서들의 값 집합이며, 이를 Aggregation 관계로 가짐
 *
 * [Comment]
 * - 생성자가 저렇게 길어질수도 있구나라는 말이 저절로 나오는 클래스
 * - 뎁스가 이렇게 깊어지다가 지옥까지 갈 수도 있겠다는 것을 느낄 수 있는 클래스
 * - 앞이 보이지 않는 인포메이션 하이딩 클래스
 * - 도무지 감이 잡히지 않는 인캡셜레이션 클래스
 * - 내가 객체인지 객체가 나인지 알 수 없는 클래스
 */
public class CropDaySubPOJO extends BasePOJO{

    private static final int DETAIL_LIMIT = 3;

    private int day = 0;
    private List<CropDayDetailPOJO> cropDayDetailPOJOs;

    public CropDaySubPOJO(ByteSerial byteSerial, int offset, int day){
        this.day = day;
        this.byteSerial = byteSerial;
        cropDayDetailPOJOs = new ArrayList<>();
        for(int i = 1; i <= DETAIL_LIMIT; i++){
            cropDayDetailPOJOs.add(new CropDayDetailPOJO(
                    i,
                    getSumWith2Bytes(offset),
                    getSumWith2Bytes(offset + 2),
                    getSumWith2Bytes(offset + 4),
                    getSumWith2Bytes(offset + 6), // 676
                    getMDorHMWith2Bytes(offset + 8, ":"),
                    toDecimalFromBinaryValue(offset + 10, 0, 2),
                    getBooleanValueFrom2Byte(offset + 10, 2),
                    getBooleanValueFrom2Byte(offset + 10, 3),
                    toDecimalFromBinaryValue(offset + 10, 4, 2),
                    getBooleanValueFrom2Byte(offset + 10, 6),
                    getBooleanValueFrom2Byte(offset + 10, 7),
                    toDecimalFromBinaryValue(offset + 10, 8, 2),
                    getBooleanValueFrom2Byte(offset + 10, 10),
                    getBooleanValueFrom2Byte(offset + 10, 11),
                    toDecimalFromBinaryValue(offset + 10, 12, 2),
                    getBooleanValueFrom2Byte(offset + 10, 14),
                    getBooleanValueFrom2Byte(offset + 10, 15)
            ));
        }
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public List<CropDayDetailPOJO> getCropDayDetailPOJOs() {
        return cropDayDetailPOJOs;
    }

    public void setCropDayDetailPOJOs(List<CropDayDetailPOJO> cropDayDetailPOJOs) {
        this.cropDayDetailPOJOs = cropDayDetailPOJOs;
    }
}
