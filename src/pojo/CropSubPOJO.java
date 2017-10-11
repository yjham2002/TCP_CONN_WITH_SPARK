package pojo;

import constants.ConstProtocol;
import models.ByteSerial;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import utils.DebugUtil;
import utils.HexUtil;
import utils.Log;
import utils.SohaProtocolUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

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

    private String farmCode;
    private String dongCode;
    private String name;
    private int order;
    private List<CropDaySubPOJO> cropDaySubPOJOs;

    private static final int NAME_RANGE = 20;


    /**
     * 작물 데이터 범위를 한정하여 작물 정보를 추출하는 클래스 생성자
     * AddressPOJO와 CropWrappingPOJO에 대해 한정적 Aggregation 관계를 가짐
     * @param recvs
     * @param order
     */
    public CropSubPOJO(List<ByteSerial> recvs, int order, String farm, String harv){

        this.farmCode = farm;
        this.dongCode = harv;

        // TODO START POINT
        /**
         * 설정 쓰기 파싱
         * 타이머 쓰기 파싱
         * 플래그 비트 설정
         * 일령 리스트 쓰기 파싱
         * 경보 문자 쓰기 파싱
         */

        byte[] pure = ByteSerial.getPureDataConcat(recvs);

        Log.i(Arrays.toString(pure));

        ByteSerial bs = new ByteSerial(pure, ByteSerial.TYPE_FORCE);
        this.byteSerial = bs;
        this.order = order;

        name = "";
        for(int e = 0; e <= 18; e += 2){
            name += getHangleFrom2ByteABS(e);
        }

        init();
    }

    @JsonIgnore
    public byte[] getPureBytes(){
        byte[] nameSet = this.name.getBytes();

        byte[] set = new byte[]{};

        for(int k = 0; k < nameSet.length; k++){
            int temp = nameSet[k];
            if(temp < 0) temp = nameSet[k] & 0xff;
            set = SohaProtocolUtil.concat(set, SohaProtocolUtil.getHexLocation(temp));
        }

        for(int e = 0; e < cropDaySubPOJOs.size(); e++){
            CropDaySubPOJO crop = cropDaySubPOJOs.get(e);
            for(int u = 0; u < crop.getCropDayDetailPOJOs().size(); u++) {
                CropDayDetailPOJO detail = crop.getCropDayDetailPOJOs().get(u);
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
                set = SohaProtocolUtil.concat(set,
                        SohaProtocolUtil.getHexLocation(detail.getCo2_setting()),
                        SohaProtocolUtil.getHexLocation(detail.getTemp_setting()),
                        SohaProtocolUtil.getHexLocation(detail.getHumid_setting()),
                        SohaProtocolUtil.getHexLocation(detail.getIllum_setting()),
                        getValuePairFromString(detail.getStart_time()),
                        new byte[]{(byte)bitAggr1, (byte)bitAggr2}
                );
            }
        }

        return set;
    }

    private CropSubPOJO(){}

    public void init(){
        cropDaySubPOJOs = new ArrayList<>();

        int term = 0;
        for(int e = 1; e <= 50; e++){
            int start = NAME_RANGE + term;
            CropDaySubPOJO cropDaySubPOJO = new CropDaySubPOJO(this.byteSerial, start, e);
            cropDaySubPOJO.setByteSerial(null);
            cropDaySubPOJOs.add(cropDaySubPOJO);
            term += DAY_TERM;
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<CropDaySubPOJO> getCropDaySubPOJOs() {
        return cropDaySubPOJOs;
    }

    public void setCropDaySubPOJOs(List<CropDaySubPOJO> cropDaySubPOJOs) {
        this.cropDaySubPOJOs = cropDaySubPOJOs;
    }

    private String toJson() throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(this);

        return json;
    }

    public String getFarmCode() {
        return farmCode;
    }

    public void setFarmCode(String farmCode) {
        this.farmCode = farmCode;
    }

    public String getDongCode() {
        return dongCode;
    }

    public void setDongCode(String dongCode) {
        this.dongCode = dongCode;
    }

    @JsonIgnore
    public String getInsertSQL() throws IOException{
        try {
            String sql = "INSERT INTO `sohatechfarmdb`.`tblDaily`\n" +
                    "            (`farmCode`,\n" +
                    "             `dongCode`,\n" +
                    "             `order`,\n" +
                    "             `rawJson`,\n" +
                    "             `regDate`)\n" +
                    "VALUES ('" + farmCode + "',\n" +
                    "        '" + dongCode + "',\n" +
                    "        '" + order + "',\n" +
                    "        '" + this.toJson() + "',\n" +
                    "        NOW())" +
                    "ON DUPLICATE KEY UPDATE " +
                    "`rawJson` = '" + this.toJson() + "',\n" +
                    "  `regDate` = NOW()";
            return sql;
        }catch(IOException e){
            Log.i("WARN ::: [CropSubPOJO :: Parse Error]");
            throw new IOException();
        }
    }

}
