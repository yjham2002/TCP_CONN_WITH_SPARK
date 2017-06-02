package pojo;

/**
 * Created by a on 2017-06-02.
 */
public class CropDayDetailPOJO {

    private int order;
    private int co2_setting;
    private int temp_setting;
    private int humid_setting;
    private int illum_setting;
    private String start_time;
    private int co2_ctrl;
    private int co2_timer_on_unit;
    private int co2_timer_off_unit;
    private int temp_ctrl;
    private int temp_timer_on_unit;
    private int temp_timer_off_unit;
    private int humid_ctrl;
    private int humid_timer_on_unit;
    private int humid_timer_off_unit;
    private int illum_ctrl;
    private int illum_timer_on_unit;
    private int illum_timer_off_unit;

    public CropDayDetailPOJO(
            int order, int co2_setting, int temp_setting, int humid_setting, int illum_setting, String start_time,
            int co2_ctrl, int co2_timer_on_unit, int co2_timer_off_unit, int temp_ctrl, int temp_timer_on_unit,
            int temp_timer_off_unit, int humid_ctrl, int humid_timer_on_unit, int humid_timer_off_unit, int illum_ctrl,
            int illum_timer_on_unit, int illum_timer_off_unit) {
        this.order = order;
        this.co2_setting = co2_setting;
        this.temp_setting = temp_setting;
        this.humid_setting = humid_setting;
        this.illum_setting = illum_setting;
        this.start_time = start_time;
        this.co2_ctrl = co2_ctrl;
        this.co2_timer_on_unit = co2_timer_on_unit;
        this.co2_timer_off_unit = co2_timer_off_unit;
        this.temp_ctrl = temp_ctrl;
        this.temp_timer_on_unit = temp_timer_on_unit;
        this.temp_timer_off_unit = temp_timer_off_unit;
        this.humid_ctrl = humid_ctrl;
        this.humid_timer_on_unit = humid_timer_on_unit;
        this.humid_timer_off_unit = humid_timer_off_unit;
        this.illum_ctrl = illum_ctrl;
        this.illum_timer_on_unit = illum_timer_on_unit;
        this.illum_timer_off_unit = illum_timer_off_unit;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getCo2_setting() {
        return co2_setting;
    }

    public void setCo2_setting(int co2_setting) {
        this.co2_setting = co2_setting;
    }

    public int getTemp_setting() {
        return temp_setting;
    }

    public void setTemp_setting(int temp_setting) {
        this.temp_setting = temp_setting;
    }

    public int getHumid_setting() {
        return humid_setting;
    }

    public void setHumid_setting(int humid_setting) {
        this.humid_setting = humid_setting;
    }

    public int getIllum_setting() {
        return illum_setting;
    }

    public void setIllum_setting(int illum_setting) {
        this.illum_setting = illum_setting;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public int getCo2_ctrl() {
        return co2_ctrl;
    }

    public void setCo2_ctrl(int co2_ctrl) {
        this.co2_ctrl = co2_ctrl;
    }

    public int getCo2_timer_on_unit() {
        return co2_timer_on_unit;
    }

    public void setCo2_timer_on_unit(int co2_timer_on_unit) {
        this.co2_timer_on_unit = co2_timer_on_unit;
    }

    public int getCo2_timer_off_unit() {
        return co2_timer_off_unit;
    }

    public void setCo2_timer_off_unit(int co2_timer_off_unit) {
        this.co2_timer_off_unit = co2_timer_off_unit;
    }

    public int getTemp_ctrl() {
        return temp_ctrl;
    }

    public void setTemp_ctrl(int temp_ctrl) {
        this.temp_ctrl = temp_ctrl;
    }

    public int getTemp_timer_on_unit() {
        return temp_timer_on_unit;
    }

    public void setTemp_timer_on_unit(int temp_timer_on_unit) {
        this.temp_timer_on_unit = temp_timer_on_unit;
    }

    public int getTemp_timer_off_unit() {
        return temp_timer_off_unit;
    }

    public void setTemp_timer_off_unit(int temp_timer_off_unit) {
        this.temp_timer_off_unit = temp_timer_off_unit;
    }

    public int getHumid_ctrl() {
        return humid_ctrl;
    }

    public void setHumid_ctrl(int humid_ctrl) {
        this.humid_ctrl = humid_ctrl;
    }

    public int getHumid_timer_on_unit() {
        return humid_timer_on_unit;
    }

    public void setHumid_timer_on_unit(int humid_timer_on_unit) {
        this.humid_timer_on_unit = humid_timer_on_unit;
    }

    public int getHumid_timer_off_unit() {
        return humid_timer_off_unit;
    }

    public void setHumid_timer_off_unit(int humid_timer_off_unit) {
        this.humid_timer_off_unit = humid_timer_off_unit;
    }

    public int getIllum_ctrl() {
        return illum_ctrl;
    }

    public void setIllum_ctrl(int illum_ctrl) {
        this.illum_ctrl = illum_ctrl;
    }

    public int getIllum_timer_on_unit() {
        return illum_timer_on_unit;
    }

    public void setIllum_timer_on_unit(int illum_timer_on_unit) {
        this.illum_timer_on_unit = illum_timer_on_unit;
    }

    public int getIllum_timer_off_unit() {
        return illum_timer_off_unit;
    }

    public void setIllum_timer_off_unit(int illum_timer_off_unit) {
        this.illum_timer_off_unit = illum_timer_off_unit;
    }
}

