package pojo;

import models.ByteSerial;

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
    public TimerPOJO(ByteSerial byteSerial, int offset){
        this.byteSerial = byteSerial;
        this.offset = offset;
        init();
    }

    //TODO 인덱싱
    private void init(){
        timerSubPOJOList = new ArrayList<>();

        this.timer_ctrl_aggr = getSumWith2Bytes(offset);
        this.timer_ctrl_co2_type = toDecimalFromBinaryValue(offset, 0, 2);
        this.timer_ctrl_co2_on = getBooleanValueFrom2Byte(offset, 2);
        this.timer_ctrl_co2_off = getBooleanValueFrom2Byte(offset, 3);
        this.timer_ctrl_temp_type = toDecimalFromBinaryValue(offset, 4, 2);
        this.timer_ctrl_temp_on = getBooleanValueFrom2Byte(offset, 6);
        this.timer_ctrl_temp_off = getBooleanValueFrom2Byte(offset, 7);
        this.timer_ctrl_humidity_type = toDecimalFromBinaryValue(offset, 8, 2);
        this.timer_ctrl_humidity_on = getBooleanValueFrom2Byte(offset, 10);
        this.timer_ctrl_humidity_off = getBooleanValueFrom2Byte(offset, 11);
        this.timer_ctrl_ilum_type = toDecimalFromBinaryValue(offset, 12, 2);
        this.timer_ctrl_ilum_on = getBooleanValueFrom2Byte(offset, 14);
        this.timer_ctrl_ilum_off = getBooleanValueFrom2Byte(offset, 15);

        for(int i = 1; i <= 24; i++){
            TimerSubPOJO timerSubPOJO = new TimerSubPOJO(byteSerial, offset + 2 + ( (i - 1) * 8 ), i);
            timerSubPOJO.setByteSerial(null);
            timerSubPOJOList.add(timerSubPOJO);
        }

        this.byteSerial = null;
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


}