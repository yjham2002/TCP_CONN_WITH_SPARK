package pojo;

import models.ByteSerial;

/**
 * Created by 전세호 on 2017-06-02.
 * 각 센서의 타이머 설정을 저장하는 클래스
 */
public class TimerSubPOJO extends BasePOJO{

    private int sensor_number;

    private int timer_setting_co2_on;
    private int timer_setting_co2_off;
    private int timer_setting_temp_on;
    private int timer_setting_temp_off;
    private int timer_setting_humidity_on;
    private int timer_setting_humidity_off;
    private int timer_setting_ilum_on;
    private int timer_setting_ilum_off;

    private TimerSubPOJO(){}

    /**
     * offset부터 각 on/off 값 맵핑
     * @param offset
     */
    public TimerSubPOJO(ByteSerial byteSerial, int offset, int sr_no){
        this.byteSerial = byteSerial;
        this.sensor_number = sr_no;

        this.timer_setting_co2_on = getLhsFromDualABS(offset);
        this.timer_setting_co2_off = getRhsFromDualABS(offset);
        this.timer_setting_temp_on = getLhsFromDualABS(offset+2);
        this.timer_setting_temp_off = getRhsFromDualABS(offset+2);
        this.timer_setting_humidity_on = getLhsFromDualABS(offset+4);
        this.timer_setting_humidity_off = getRhsFromDualABS(offset+4);
        this.timer_setting_ilum_on = getLhsFromDualABS(offset+6);
        this.timer_setting_ilum_off = getRhsFromDualABS(offset+6);
    }

    public int getTimer_setting_co2_on() {
        return timer_setting_co2_on;
    }

    public void setTimer_setting_co2_on(int timer_setting_co2_on) {
        this.timer_setting_co2_on = timer_setting_co2_on;
    }

    public int getTimer_setting_co2_off() {
        return timer_setting_co2_off;
    }

    public void setTimer_setting_co2_off(int timer_setting_co2_off) {
        this.timer_setting_co2_off = timer_setting_co2_off;
    }

    public int getTimer_setting_temp_on() {
        return timer_setting_temp_on;
    }

    public void setTimer_setting_temp_on(int timer_setting_temp_on) {
        this.timer_setting_temp_on = timer_setting_temp_on;
    }

    public int getTimer_setting_temp_off() {
        return timer_setting_temp_off;
    }

    public void setTimer_setting_temp_off(int timer_setting_temp_off) {
        this.timer_setting_temp_off = timer_setting_temp_off;
    }

    public int getTimer_setting_humidity_on() {
        return timer_setting_humidity_on;
    }

    public void setTimer_setting_humidity_on(int timer_setting_humidity_on) {
        this.timer_setting_humidity_on = timer_setting_humidity_on;
    }

    public int getTimer_setting_humidity_off() {
        return timer_setting_humidity_off;
    }

    public void setTimer_setting_humidity_off(int timer_setting_humidity_off) {
        this.timer_setting_humidity_off = timer_setting_humidity_off;
    }

    public int getSensor_number() {
        return sensor_number;
    }

    public void setSensor_number(int sensor_number) {
        this.sensor_number = sensor_number;
    }

    public int getTimer_setting_ilum_on() {
        return timer_setting_ilum_on;
    }

    public void setTimer_setting_ilum_on(int timer_setting_ilum_on) {
        this.timer_setting_ilum_on = timer_setting_ilum_on;
    }

    public int getTimer_setting_ilum_off() {
        return timer_setting_ilum_off;
    }

    public void setTimer_setting_ilum_off(int timer_setting_ilum_off) {
        this.timer_setting_ilum_off = timer_setting_ilum_off;
    }

}
