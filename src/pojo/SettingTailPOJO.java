package pojo;

/**
 * Created by a on 2017-06-09.
 */
public class SettingTailPOJO {
    private int order;
    private int setting_value_co_2;
    private int setting_value_temp;
    private int setting_value_humid;
    private int setting_value_illum;
    private String start_time;
    private String end_time;

    public SettingTailPOJO(int order, int setting_value_co_2, int setting_value_temp, int setting_value_humid, int setting_value_illum, String start_time, String end_time) {
        this.order = order;
        this.setting_value_co_2 = setting_value_co_2;
        this.setting_value_temp = setting_value_temp;
        this.setting_value_humid = setting_value_humid;
        this.setting_value_illum = setting_value_illum;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    @Deprecated
    public SettingTailPOJO(){}

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getSetting_value_co_2() {
        return setting_value_co_2;
    }

    public void setSetting_value_co_2(int setting_value_co_2) {
        this.setting_value_co_2 = setting_value_co_2;
    }

    public int getSetting_value_temp() {
        return setting_value_temp;
    }

    public void setSetting_value_temp(int setting_value_temp) {
        this.setting_value_temp = setting_value_temp;
    }

    public int getSetting_value_humid() {
        return setting_value_humid;
    }

    public void setSetting_value_humid(int setting_value_humid) {
        this.setting_value_humid = setting_value_humid;
    }

    public int getSetting_value_illum() {
        return setting_value_illum;
    }

    public void setSetting_value_illum(int setting_value_illum) {
        this.setting_value_illum = setting_value_illum;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }
}
