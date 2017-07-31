package pojo;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import constants.ConstProtocol;
import models.ByteSerial;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import utils.DebugUtil;
import utils.HexUtil;
import utils.Modbus;
import utils.SohaProtocolUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 전세호
 * @version 1.1
 * Improved By 함의진 - 슈퍼 클래스 명시 호출 제거 및 상대 오프셋
 *
 * 각 센서의 타이머 설정을 저장하기 위한 클래스
 */
public class TimerPOJO extends BasePOJO{

    private int offset;
    private String farmCode;
    private String dongCode;
    private int machine_no;

    public int getMachine_no() {
        return machine_no;
    }

    public void setMachine_no(int machine_no) {
        this.machine_no = machine_no;
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

    private List<TimerSubPOJO> timerSubPOJOList;

    /**
     timer 제어방식 선택
     */
    private int timer_ctrl_aggr;
    private int timer_ctrl_co2_type;
    private int timer_ctrl_co2_on;
    private int timer_ctrl_co2_off;
    private int timer_ctrl_temp_type;
    private int timer_ctrl_temp_on;
    private int timer_ctrl_temp_off;
    private int timer_ctrl_humidity_type;
    private int timer_ctrl_humidity_on;
    private int timer_ctrl_humidity_off;
    private int timer_ctrl_ilum_type;
    private int timer_ctrl_ilum_on;
    private int timer_ctrl_ilum_off;

    /**
     * 시리얼 바이트로부터 맵핑 및 의미를 구체화하기 위한 생성자
     * @param byteSerial 시리얼 바이트
     * @param offset
     * TODO super error
     */
    public TimerPOJO(ByteSerial byteSerial, int offset, String farmCode, String harvCode){
        this.farmCode = farmCode;
        this.dongCode = harvCode;
        this.byteSerial = byteSerial;
        this.offset = offset;
        init();
    }

    //TODO 인덱싱
    private void init(){
        timerSubPOJOList = new ArrayList<>();

        this.timer_ctrl_aggr = getSumWith2BytesABS(offset, SUM_MODE_P);
        this.timer_ctrl_co2_type = getBooleanValueFrom2ByteABS(offset, 0);
        this.timer_ctrl_co2_on = getBooleanValueFrom2ByteABS(offset, 2);
        this.timer_ctrl_co2_off = getBooleanValueFrom2ByteABS(offset, 3);
        this.timer_ctrl_temp_type = getBooleanValueFrom2ByteABS(offset, 4);
        this.timer_ctrl_temp_on = getBooleanValueFrom2ByteABS(offset, 6);
        this.timer_ctrl_temp_off = getBooleanValueFrom2ByteABS(offset, 7);
        this.timer_ctrl_humidity_type = getBooleanValueFrom2ByteABS(offset, 8);
        this.timer_ctrl_humidity_on = getBooleanValueFrom2ByteABS(offset, 10);
        this.timer_ctrl_humidity_off = getBooleanValueFrom2ByteABS(offset, 11);
        this.timer_ctrl_ilum_type = getBooleanValueFrom2ByteABS(offset, 12);
        this.timer_ctrl_ilum_on = getBooleanValueFrom2ByteABS(offset, 14);
        this.timer_ctrl_ilum_off = getBooleanValueFrom2ByteABS(offset, 15);

        for(int i = 1; i <= 24; i++){
            int newOff = offset + 2 + ( (i - 1) * 8 );
            TimerSubPOJO timerSubPOJO = new TimerSubPOJO(byteSerial, newOff, i);
            timerSubPOJO.setByteSerial(null);
            timerSubPOJOList.add(timerSubPOJO);
        }

        this.byteSerial = null;
    }

    @JsonIgnore
    public byte[] getBytes(){

        if(this.farmCode == null)
            System.out.println("aaaaaaaaaaaaaaaaaaaa");

        if(this.dongCode == null)
            System.out.println("bbbbbbbbbbbbbbbbbbbb");


        byte[] check = SohaProtocolUtil.concat(ConstProtocol.STX, this.farmCode.getBytes(), this.dongCode.getBytes());

        int bitAggr_timerctrl_1 =
                getBitAggregation(
                        timer_ctrl_ilum_off,
                        timer_ctrl_ilum_on,
                        0,
                        timer_ctrl_ilum_type,
                        timer_ctrl_humidity_off,
                        timer_ctrl_humidity_on,
                        0,
                        timer_ctrl_humidity_type
                );

        int bitAggr_timerctrl_2 =
                getBitAggregation(
                        timer_ctrl_temp_off,
                        timer_ctrl_temp_on,
                        0,
                        timer_ctrl_temp_type,
                        timer_ctrl_co2_off,
                        timer_ctrl_co2_on,
                        0,
                        timer_ctrl_co2_type
                );

        byte[] modbusData = SohaProtocolUtil.concat(new byte[]{Byte.parseByte(this.dongCode), 3, (byte)(ConstProtocol.RANGE_TIMER.getTail() * 2)}, new byte[]{(byte)bitAggr_timerctrl_1, (byte)bitAggr_timerctrl_2});

        for(int i=0; i<this.timerSubPOJOList.size(); i++){
            TimerSubPOJO tmpSub = timerSubPOJOList.get(i);
            modbusData = SohaProtocolUtil.concat(modbusData, getAggregation(tmpSub.getTimer_setting_co2_on(), tmpSub.getTimer_setting_co2_off()),
                    getAggregation(tmpSub.getTimer_setting_temp_on(), tmpSub.getTimer_setting_temp_off()),
                    getAggregation(tmpSub.getTimer_setting_humidity_on(), tmpSub.getTimer_setting_humidity_off()),
                    getAggregation(tmpSub.getTimer_setting_ilum_on(), tmpSub.getTimer_setting_ilum_off()));
        }

        Modbus modbus = new Modbus();
        byte[] crc = modbus.fn_makeCRC16(modbusData);
        byte[] fin = SohaProtocolUtil.concat(check, modbusData, crc);
        byte chk = HexUtil.checkSum(fin);
        byte[] retVal = SohaProtocolUtil.concat(fin, new byte[]{chk}, ConstProtocol.ETX);

        return retVal;
    }

    public List<TimerSubPOJO> getTimerSubPOJOList() {
        return timerSubPOJOList;
    }

    public void setTimerSubPOJOList(List<TimerSubPOJO> timerSubPOJOList) {
        this.timerSubPOJOList = timerSubPOJOList;
    }

    public int getTimer_ctrl_aggr() {
        return timer_ctrl_aggr;
    }

    public void setTimer_ctrl_aggr(int timer_ctrl_aggr) {
        this.timer_ctrl_aggr = timer_ctrl_aggr;
    }

    public int getTimer_ctrl_co2_type() {
        return timer_ctrl_co2_type;
    }

    public void setTimer_ctrl_co2_type(int timer_ctrl_co2_type) {
        this.timer_ctrl_co2_type = timer_ctrl_co2_type;
    }

    public int getTimer_ctrl_co2_on() {
        return timer_ctrl_co2_on;
    }

    public void setTimer_ctrl_co2_on(int timer_ctrl_co2_on) {
        this.timer_ctrl_co2_on = timer_ctrl_co2_on;
    }

    public int getTimer_ctrl_co2_off() {
        return timer_ctrl_co2_off;
    }

    public void setTimer_ctrl_co2_off(int timer_ctrl_co2_off) {
        this.timer_ctrl_co2_off = timer_ctrl_co2_off;
    }

    public int getTimer_ctrl_temp_type() {
        return timer_ctrl_temp_type;
    }

    public void setTimer_ctrl_temp_type(int timer_ctrl_temp_type) {
        this.timer_ctrl_temp_type = timer_ctrl_temp_type;
    }

    public int getTimer_ctrl_temp_on() {
        return timer_ctrl_temp_on;
    }

    public void setTimer_ctrl_temp_on(int timer_ctrl_temp_on) {
        this.timer_ctrl_temp_on = timer_ctrl_temp_on;
    }

    public int getTimer_ctrl_temp_off() {
        return timer_ctrl_temp_off;
    }

    public void setTimer_ctrl_temp_off(int timer_ctrl_temp_off) {
        this.timer_ctrl_temp_off = timer_ctrl_temp_off;
    }

    public int getTimer_ctrl_humidity_type() {
        return timer_ctrl_humidity_type;
    }

    public void setTimer_ctrl_humidity_type(int timer_ctrl_humidity_type) {
        this.timer_ctrl_humidity_type = timer_ctrl_humidity_type;
    }

    public int getTimer_ctrl_humidity_on() {
        return timer_ctrl_humidity_on;
    }

    public void setTimer_ctrl_humidity_on(int timer_ctrl_humidity_on) {
        this.timer_ctrl_humidity_on = timer_ctrl_humidity_on;
    }

    public int getTimer_ctrl_humidity_off() {
        return timer_ctrl_humidity_off;
    }

    public void setTimer_ctrl_humidity_off(int timer_ctrl_humidity_off) {
        this.timer_ctrl_humidity_off = timer_ctrl_humidity_off;
    }

    public int getTimer_ctrl_ilum_type() {
        return timer_ctrl_ilum_type;
    }

    public void setTimer_ctrl_ilum_type(int timer_ctrl_ilum_type) {
        this.timer_ctrl_ilum_type = timer_ctrl_ilum_type;
    }

    public int getTimer_ctrl_ilum_on() {
        return timer_ctrl_ilum_on;
    }

    public void setTimer_ctrl_ilum_on(int timer_ctrl_ilum_on) {
        this.timer_ctrl_ilum_on = timer_ctrl_ilum_on;
    }

    public int getTimer_ctrl_ilum_off() {
        return timer_ctrl_ilum_off;
    }

    public void setTimer_ctrl_ilum_off(int timer_ctrl_ilum_off) {
        this.timer_ctrl_ilum_off = timer_ctrl_ilum_off;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    private TimerPOJO(){}

    @JsonIgnore
    public String getInsertSQL() throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(this);
        String sql = "INSERT INTO `sohatechfarmdb`.`tblTimerData`\n" +
                "            (`farmCode`,\n" +
                "             `dongCode`,\n" +
                "             `rawJson`,\n" +
                "             `regDate`)\n" +
                "VALUES ('" + farmCode + "',\n" +
                "        '" + dongCode + "',\n" +
                "        '" + json + "',\n" +
                "        NOW())\n" +
                "        ON DUPLICATE KEY UPDATE\n" +
                "        `rawJson` = '" + json + "',\n" +
                "        `regDate` = NOW();";
        return sql;
    }

}
