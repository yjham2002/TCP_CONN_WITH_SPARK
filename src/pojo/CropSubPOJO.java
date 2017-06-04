package pojo;

import models.ByteSerial;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 함의진
 * @version 1.0
 * 작물 정보 시리얼을 추상화하기 위한 클래스
 * - 삶 속에서 매일 먹는 곡식과 여러 작물들이 원망스러워서 육식주의자가 되도록 하는 인생 메소드를 제공하는 클래스
 * - 정말 많은 정보들이 작물에 사용되는 것을 알게되어 우울감이 드는 클래스
 * - 먹기 위해 사는지 살기 위해 먹는지 고민을 제공하며 답은 안주는 인포메이션 하이딩 클래스
 */
public class CropSubPOJO extends BasePOJO{

    private static final int DAY_TERM = 36;

    private String name;
    private int order;
    private List<CropDaySubPOJO> cropDaySubPOJOs;
    private int startIndex;

    private static final int NAME_RANGE = 20;

    /**
     * 작물 데이터 범위를 한정하여 작물 정보를 추출하는 클래스 생성자
     * AddressPOJO와 CropWrappingPOJO에 대해 한정적 Aggregation 관계를 가짐
     * @param byteSerial
     */
    public CropSubPOJO(ByteSerial byteSerial, int startIndex, int order){
        this.order = order;
        this.byteSerial = byteSerial;
        this.startIndex = startIndex;

        name = "";
        for(int e = 0; e <= 18; e += 2){
            name += getHangleFrom2ByteABS(startIndex + e);
        }

        init();
    }

    public void init(){
        cropDaySubPOJOs = new ArrayList<>();

        int term = 0;
        for(int e = 1; e <= 50; e++){
            int start = NAME_RANGE + startIndex + term;
            CropDaySubPOJO cropDaySubPOJO = new CropDaySubPOJO(this.byteSerial, start, e);
            cropDaySubPOJOs.add(cropDaySubPOJO);
            term += DAY_TERM;
        }

    }

}
