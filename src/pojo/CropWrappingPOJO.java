package pojo;

import constants.ConstProtocol;
import models.ByteSerial;
import models.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 함의진
 * @version CropDaySubPOJO Set을 랩핑하는 클래스
 */
public class CropWrappingPOJO extends BasePOJO{
    List<CropSubPOJO> cropSubPOJOs;

    public CropWrappingPOJO(List<ByteSerial> recv, int order){
        cropSubPOJOs = new ArrayList<>();

        int range = getOrder(order).getHead();

        if(order == -1) {
            for (int e = 0; e < recv.size(); e++) {
                CropSubPOJO cropSubPOJO = new CropSubPOJO(byteSerial, range, e + 1);
                cropSubPOJOs.add(cropSubPOJO);
            }
        }else{
            CropSubPOJO cropSubPOJO = new CropSubPOJO(byteSerial, range, order);
            cropSubPOJOs.add(cropSubPOJO);
        }

    }

    public Pair<Integer> getOrder(int order){
        switch(order){
            case 1: return ConstProtocol.RANGE_DAYAGE_01;
            case 2: return ConstProtocol.RANGE_DAYAGE_02;
            case 3: return ConstProtocol.RANGE_DAYAGE_03;
            case 4: return ConstProtocol.RANGE_DAYAGE_04;
            case 5: return ConstProtocol.RANGE_DAYAGE_05;
            case 6: return ConstProtocol.RANGE_DAYAGE_06;
            default: return ConstProtocol.RANGE_DAYAGE;
        }
    }
}
