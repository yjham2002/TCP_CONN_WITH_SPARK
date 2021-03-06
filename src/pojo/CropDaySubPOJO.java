package pojo;

import models.ByteSerial;
import org.codehaus.jackson.annotate.JsonIgnore;
import utils.SohaProtocolUtil;

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
    private static final int DETAIL_LENGTH = 12;

    private int day = 0;
    private List<CropDayDetailPOJO> cropDayDetailPOJOs;

    public CropDaySubPOJO(ByteSerial byteSerial, int offset, int day){
        this.day = day;
        this.byteSerial = byteSerial;
        cropDayDetailPOJOs = new ArrayList<>();
        for(int i = 1; i <= DETAIL_LIMIT; i++){
            int rev = DETAIL_LENGTH * (i - 1);
            cropDayDetailPOJOs.add(new CropDayDetailPOJO(
                    i,
                    getSumWith2BytesABS(offset + rev, SUM_MODE_P),
                    getSumWith2BytesABS(offset + 2 + rev, SUM_MODE_TEMP),
                    getSumWith2BytesABS(offset + 4 + rev, SUM_MODE_HUMID),
                    getSumWith2BytesABS(offset + 6 + rev, SUM_MODE_P),
                    getMDorHMWith2BytesABS(offset + 8 + rev, ":"),
                    toDecimalFromBinaryValueABS(offset + 10 + rev, 0, 2),
                    getBooleanValueFrom2ByteABS(offset + 10 + rev, 2),
                    getBooleanValueFrom2ByteABS(offset + 10 + rev, 3),
                    toDecimalFromBinaryValueABS(offset + 10 + rev, 4, 2),
                    getBooleanValueFrom2ByteABS(offset + 10 + rev, 6),
                    getBooleanValueFrom2ByteABS(offset + 10 + rev, 7),
                    toDecimalFromBinaryValueABS(offset + 10 + rev, 8, 2),
                    getBooleanValueFrom2ByteABS(offset + 10 + rev, 10),
                    getBooleanValueFrom2ByteABS(offset + 10 + rev, 11),
                    toDecimalFromBinaryValueABS(offset + 10 + rev, 12, 2),
                    getBooleanValueFrom2ByteABS(offset + 10 + rev, 14),
                    getBooleanValueFrom2ByteABS(offset + 10 + rev, 15)
            ));
        }
    }

    /**
     * 일령 상세 정보 리스트 중 하나의 날짜에 대한 바이트를 반환하는 메소드
     * @return 하나의 날짜에 대한 바이트
     */
    @JsonIgnore
    public byte[] getUnitBytes(int order){
        if(order < 0 || order > 2) return null;

        CropDayDetailPOJO detail = cropDayDetailPOJOs.get(order);

        int bitAggr1 =
                getBitAggregation(
                        detail.getIllum_timer_off_unit(),
                        detail.getIllum_timer_on_unit(),
                        getBitLhsFromDual(detail.getIllum_ctrl()),
                        getBitRhsFromDual(detail.getIllum_ctrl()),



                        detail.getHumid_timer_off_unit(),
                        detail.getHumid_timer_on_unit(),
                        getBitLhsFromDual(detail.getHumid_ctrl()),
                        getBitRhsFromDual(detail.getHumid_ctrl())

                );
        int bitAggr2 =
                getBitAggregation(
                        detail.getTemp_timer_off_unit(),
                        detail.getTemp_timer_on_unit(),
                        getBitLhsFromDual(detail.getTemp_ctrl()),
                        getBitRhsFromDual(detail.getTemp_ctrl()),

                        detail.getCo2_timer_off_unit(),
                        detail.getCo2_timer_on_unit(),
                        getBitLhsFromDual(detail.getCo2_ctrl()),
                        getBitRhsFromDual(detail.getCo2_ctrl())

                );

        byte[] retVal = SohaProtocolUtil.concat(
                SohaProtocolUtil.getHexLocation(detail.getCo2_setting()),
                SohaProtocolUtil.getHexLocation(detail.getTemp_setting()),
                SohaProtocolUtil.getHexLocation(detail.getHumid_setting()),
                SohaProtocolUtil.getHexLocation(detail.getIllum_setting()),
                getValuePairFromString(detail.getStart_time()),
                new byte[]{(byte)bitAggr1, (byte)bitAggr2}
        );

        return retVal;
    }

    private CropDaySubPOJO(){}

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
