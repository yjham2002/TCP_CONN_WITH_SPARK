package pojo;

import models.ByteSerial;

/**
 * @author 함의진
 * variable setting supported by 전세호
 * - 번지수가 하나라도 잘못되면 인생도 슬퍼진다는 것을 배울 수 있는 클래스
 * - 진정한 하드코딩이 무엇인지 추상화하는 클래스
 * - 내가 메모리인지 메모리가 나인지 구분할 수 없게 만드는 인포메이션 하이딩 클래스
 * - 일정 조정이 개발에 얼마나 중요한 부분인지 배울 수 있는 클래스
 * - 원망스러운 클래스
 * - 슈퍼클래스는 더 원망스러움
 * @version 1.0
 * 전체 데이터맵 파싱 및 캡슐화를 위한 클래스로 실시간 데이터 POJO를 상속하여
 * 실시간 데이터 이외의 범위(EEPROM 방식으로 저장되는 메모리 범위)에 대한 맵핑을 수행한다.
 */
public class SettingPOJO extends BasePOJO {

    private String farmCode;
    private String harvCode;

    private int device_id;

    private int crop_data_num_and_ctrl_aggr;
    private int sensor_quantity;
    private int sensor_selected_1;
    private int sensor_selected_2;
    private int sensor_selected_3;
    private int sensor_selected_4;
    private int singular_ctrl_setting_co2;
    private int singular_ctrl_setting_temp;
    private int singular_ctrl_setting_humid;
    private int singular_ctrl_setting_illum;
    private int relay_output_setting_co2;
    private int relay_output_setting_heat;
    private int relay_output_setting_cool;
    private int relay_output_setting_humidify;
    private int relay_output_setting_dehumidify;
    private int relay_output_setting_illum;
    private int relay_output_setting_alarm;
    private int relay_output_setting_reserve;
    private int dry_condition_setting_aggr;
    private int dry_condition_setting_ctrl;
    private int dry_condition_setting_humidity;
    private int alert_alarm_time_select_aggr;
    private int alert_alarm_time_select_auto;
    private int alert_alarm_time_select_timer;
    private int alert_alarm_time_select_lamp_unit;
    private int alert_alarm_time_select_timeset;
    private int cthi_ctrl_stat_aggr;
    private int cthi_ctrl_stat_co2_ctrl;
    private int cthi_ctrl_stat_co2_ontype;
    private int cthi_ctrl_stat_co2_offtype;
    private int cthi_ctrl_stat_temp_ctrl;
    private int cthi_ctrl_stat_temp_ontype;
    private int cthi_ctrl_stat_temp_offtype;
    private int cthi_ctrl_stat_humid_ctrl;
    private int cthi_ctrl_stat_humid_ontype;
    private int cthi_ctrl_stat_humid_offtype;
    private int cthi_ctrl_stat_illum_ctrl;
    private int cthi_ctrl_stat_illum_ontype;
    private int cthi_ctrl_stat_illum_offtype;
    private int calm_threshold_co2_low;
    private int calm_threshold_co2_high;
    private int calm_threshold_temp_low;
    private int calm_threshold_temp_high;
    private int calm_threshold_humid_low;
    private int calm_threshold_humid_high;
    private int calm_threshold_illum_low;
    private int calm_threshold_illum_high;
    private int setting_range_co2_min;
    private int setting_range_co2_max;
    private int setting_range_temp_min;
    private int setting_range_temp_max;
    private int setting_range_humid_min;
    private int setting_range_humid_max;
    private int setting_range_illum_min;
    private int setting_range_illum_max;
    private int sr_revision_co2_01;
    private int sr_revision_temp_01;
    private int sr_revision_humid_01;
    private int sr_revision_illum_01;
    private int sr_revision_co2_02;
    private int sr_revision_temp_02;
    private int sr_revision_humid_02;
    private int sr_revision_illum_02;
    private int sr_revision_co2_03;
    private int sr_revision_temp_03;
    private int sr_revision_humid_03;
    private int sr_revision_illum_03;
    private int sr_revision_co2_04;
    private int sr_revision_temp_04;
    private int sr_revision_humid_04;
    private int sr_revision_illum_04;
    private int setting_onoff_range_co2;
    private int setting_onoff_range_co2_revision;
    private int setting_onoff_range_temp;
    private int setting_onoff_range_temp_revision;
    private int setting_onoff_range_humid;
    private int setting_onoff_range_humid_revision;
    private int setting_onoff_range_illum;
    private int setting_onoff_range_illum_revision;


    /**
     * 시리얼 바이트로부터 맵핑 및 의미를 구체화하기 위한 생성자
     * @param byteSerial 시리얼 바이트
     */
    public SettingPOJO(ByteSerial byteSerial, int offset, String farmCode, String harvCode){
        this.byteSerial = byteSerial;
        this.farmCode = farmCode;
        this.harvCode = harvCode;
        init(offset);
    }

    public void init(int offset){
        this.device_id = getSumWith2BytesABS(offset);
        this.crop_data_num_and_ctrl_aggr = getSumWith2BytesABS(offset + 2);
        this.sensor_quantity = getSingleByteABS(offset + 4);
        this.sensor_selected_1 = getBooleanValueFromByteABS(offset + 5, 0);
        this.sensor_selected_2 = getBooleanValueFromByteABS(offset + 5, 1);
        this.sensor_selected_3 = getBooleanValueFromByteABS(offset + 5, 2);
        this.sensor_selected_4 = getBooleanValueFromByteABS(offset + 5, 3);
        this.singular_ctrl_setting_co2 = getSumWith2BytesABS(offset + 6);
        this.singular_ctrl_setting_temp = getSumWith2BytesABS(offset + 8);
        this.singular_ctrl_setting_humid = getSumWith2BytesABS(offset + 10);
        this.singular_ctrl_setting_illum = getSumWith2BytesABS(offset + 12);
        this.relay_output_setting_co2 = getLhsFromDualABS(offset + 14);
        this.relay_output_setting_heat = getRhsFromDualABS(offset + 14);
        this.relay_output_setting_cool = getLhsFromDualABS(offset + 16);
        this.relay_output_setting_humidify = getRhsFromDualABS(offset + 16);
        this.relay_output_setting_dehumidify = getLhsFromDualABS(offset + 18);
        this.relay_output_setting_illum = getRhsFromDualABS(offset + 18);
        this.relay_output_setting_alarm = getLhsFromDualABS(offset + 20);
        this.relay_output_setting_reserve = getRhsFromDualABS(offset + 20);
        this.dry_condition_setting_aggr = getSumWith2BytesABS(offset + 22);
        this.dry_condition_setting_ctrl = getLhsFromDualABS(offset + 22);
        this.dry_condition_setting_humidity = getRhsFromDualABS(offset + 22);
        this.alert_alarm_time_select_aggr = getSingleByteABS(offset + 24);
        this.alert_alarm_time_select_auto = getBooleanValueFromByteABS(offset + 24, 0);
        this.alert_alarm_time_select_timer = getBooleanValueFromByteABS(offset + 24, 1);
        this.alert_alarm_time_select_lamp_unit = toDecimalFromBinaryValueABS(offset + 24, 2, 2);
        this.alert_alarm_time_select_timeset = getBooleanValueFromByteABS(offset + 24, 4);
        this.cthi_ctrl_stat_aggr = getSumWith2BytesABS(offset + 26);
        this.cthi_ctrl_stat_co2_ctrl = toDecimalFromBinaryValueABS(offset + 26, 0, 2);
        this.cthi_ctrl_stat_co2_ontype = getBooleanValueFrom2ByteABS(offset + 26, 2);
        this.cthi_ctrl_stat_co2_offtype = getBooleanValueFrom2ByteABS(offset + 26, 3);
        this.cthi_ctrl_stat_temp_ctrl = toDecimalFromBinaryValueABS(offset + 26, 4, 2);
        this.cthi_ctrl_stat_temp_ontype = getBooleanValueFrom2ByteABS(offset + 26, 6);
        this.cthi_ctrl_stat_temp_offtype = getBooleanValueFrom2ByteABS(offset + 26, 7);
        this.cthi_ctrl_stat_humid_ctrl = toDecimalFromBinaryValueABS(offset + 26, 8, 2);
        this.cthi_ctrl_stat_humid_ontype = getBooleanValueFrom2ByteABS(offset + 26, 10);
        this.cthi_ctrl_stat_humid_offtype = getBooleanValueFrom2ByteABS(offset + 26, 11);
        this.cthi_ctrl_stat_illum_ctrl = toDecimalFromBinaryValueABS(offset + 26, 12, 2);
        this.cthi_ctrl_stat_illum_ontype = getBooleanValueFrom2ByteABS(offset + 26, 14);
        this.cthi_ctrl_stat_illum_offtype = getBooleanValueFrom2ByteABS(offset + 26, 15);
        this.calm_threshold_co2_low = getSumWith2BytesABS(offset + 28);
        this.calm_threshold_co2_high = getSumWith2BytesABS(offset + 30);
        this.calm_threshold_temp_low = getSumWith2BytesABS(offset + 32);
        this.calm_threshold_temp_high = getSumWith2BytesABS(offset + 34);
        this.calm_threshold_humid_low = getSumWith2BytesABS(offset + 36);
        this.calm_threshold_humid_high = getSumWith2BytesABS(offset + 38);
        this.calm_threshold_illum_low = getSumWith2BytesABS(offset + 40);
        this.calm_threshold_illum_high = getSumWith2BytesABS(offset + 42);
        this.setting_range_co2_min = getSumWith2BytesABS(offset + 44);
        this.setting_range_co2_max = getSumWith2BytesABS(offset + 46);
        this.setting_range_temp_min = getSumWith2BytesABS(offset + 48);
        this.setting_range_temp_max = getSumWith2BytesABS(offset + 50);
        this.setting_range_humid_min = getSumWith2BytesABS(offset + 52);
        this.setting_range_humid_max = getSumWith2BytesABS(offset + 54);
        this.setting_range_illum_min = getSumWith2BytesABS(offset + 56);
        this.setting_range_illum_max = getSumWith2BytesABS(offset + 58);
        this.sr_revision_co2_01 = getSumWith2BytesABS(offset + 60);
        this.sr_revision_temp_01 = getSumWith2BytesABS(offset + 62);
        this.sr_revision_humid_01 = getSumWith2BytesABS(offset + 64);
        this.sr_revision_illum_01 = getSumWith2BytesABS(offset + 66);
        this.sr_revision_co2_02 = getSumWith2BytesABS(offset + 68);
        this.sr_revision_temp_02 = getSumWith2BytesABS(offset + 70);
        this.sr_revision_humid_02 = getSumWith2BytesABS(offset + 72);
        this.sr_revision_illum_02 = getSumWith2BytesABS(offset + 74);
        this.sr_revision_co2_03 = getSumWith2BytesABS(offset + 76);
        this.sr_revision_temp_03 = getSumWith2BytesABS(offset + 78);
        this.sr_revision_humid_03 = getSumWith2BytesABS(offset + 80);
        this.sr_revision_illum_03 = getSumWith2BytesABS(offset + 82);
        this.sr_revision_co2_04 = getSumWith2BytesABS(offset + 84);
        this.sr_revision_temp_04 = getSumWith2BytesABS(offset + 86);
        this.sr_revision_humid_04 = getSumWith2BytesABS(offset + 88);
        this.sr_revision_illum_04 = getSumWith2BytesABS(offset + 90);
        this.setting_onoff_range_co2 = getSumWith2BytesABS(offset + 92);
        this.setting_onoff_range_co2_revision = getSumWith2BytesABS(offset + 94);
        this.setting_onoff_range_temp = getSumWith2BytesABS(offset + 96);
        this.setting_onoff_range_temp_revision = getSumWith2BytesABS(offset + 98);
        this.setting_onoff_range_humid = getSumWith2BytesABS(offset + 100);
        this.setting_onoff_range_humid_revision = getSumWith2BytesABS(offset + 102);
        this.setting_onoff_range_illum = getSumWith2BytesABS(offset + 104);
        this.setting_onoff_range_illum_revision = getSumWith2BytesABS(offset + 106);
    }

    public String getFarmCode() {
        return farmCode;
    }

    public void setFarmCode(String farmCode) {
        this.farmCode = farmCode;
    }

    public String getHarvCode() {
        return harvCode;
    }

    public void setHarvCode(String harvCode) {
        this.harvCode = harvCode;
    }

    public int getDevice_id() {
        return device_id;
    }

    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }

    public int getCrop_data_num_and_ctrl_aggr() {
        return crop_data_num_and_ctrl_aggr;
    }

    public void setCrop_data_num_and_ctrl_aggr(int crop_data_num_and_ctrl_aggr) {
        this.crop_data_num_and_ctrl_aggr = crop_data_num_and_ctrl_aggr;
    }

    public int getSensor_quantity() {
        return sensor_quantity;
    }

    public void setSensor_quantity(int sensor_quantity) {
        this.sensor_quantity = sensor_quantity;
    }

    public int getSensor_selected_1() {
        return sensor_selected_1;
    }

    public void setSensor_selected_1(int sensor_selected_1) {
        this.sensor_selected_1 = sensor_selected_1;
    }

    public int getSensor_selected_2() {
        return sensor_selected_2;
    }

    public void setSensor_selected_2(int sensor_selected_2) {
        this.sensor_selected_2 = sensor_selected_2;
    }

    public int getSensor_selected_3() {
        return sensor_selected_3;
    }

    public void setSensor_selected_3(int sensor_selected_3) {
        this.sensor_selected_3 = sensor_selected_3;
    }

    public int getSensor_selected_4() {
        return sensor_selected_4;
    }

    public void setSensor_selected_4(int sensor_selected_4) {
        this.sensor_selected_4 = sensor_selected_4;
    }

    public int getSingular_ctrl_setting_co2() {
        return singular_ctrl_setting_co2;
    }

    public void setSingular_ctrl_setting_co2(int singular_ctrl_setting_co2) {
        this.singular_ctrl_setting_co2 = singular_ctrl_setting_co2;
    }

    public int getSingular_ctrl_setting_temp() {
        return singular_ctrl_setting_temp;
    }

    public void setSingular_ctrl_setting_temp(int singular_ctrl_setting_temp) {
        this.singular_ctrl_setting_temp = singular_ctrl_setting_temp;
    }

    public int getSingular_ctrl_setting_humid() {
        return singular_ctrl_setting_humid;
    }

    public void setSingular_ctrl_setting_humid(int singular_ctrl_setting_humid) {
        this.singular_ctrl_setting_humid = singular_ctrl_setting_humid;
    }

    public int getSingular_ctrl_setting_illum() {
        return singular_ctrl_setting_illum;
    }

    public void setSingular_ctrl_setting_illum(int singular_ctrl_setting_illum) {
        this.singular_ctrl_setting_illum = singular_ctrl_setting_illum;
    }

    public int getRelay_output_setting_co2() {
        return relay_output_setting_co2;
    }

    public void setRelay_output_setting_co2(int relay_output_setting_co2) {
        this.relay_output_setting_co2 = relay_output_setting_co2;
    }

    public int getRelay_output_setting_heat() {
        return relay_output_setting_heat;
    }

    public void setRelay_output_setting_heat(int relay_output_setting_heat) {
        this.relay_output_setting_heat = relay_output_setting_heat;
    }

    public int getRelay_output_setting_cool() {
        return relay_output_setting_cool;
    }

    public void setRelay_output_setting_cool(int relay_output_setting_cool) {
        this.relay_output_setting_cool = relay_output_setting_cool;
    }

    public int getRelay_output_setting_humidify() {
        return relay_output_setting_humidify;
    }

    public void setRelay_output_setting_humidify(int relay_output_setting_humidify) {
        this.relay_output_setting_humidify = relay_output_setting_humidify;
    }

    public int getRelay_output_setting_dehumidify() {
        return relay_output_setting_dehumidify;
    }

    public void setRelay_output_setting_dehumidify(int relay_output_setting_dehumidify) {
        this.relay_output_setting_dehumidify = relay_output_setting_dehumidify;
    }

    public int getRelay_output_setting_illum() {
        return relay_output_setting_illum;
    }

    public void setRelay_output_setting_illum(int relay_output_setting_illum) {
        this.relay_output_setting_illum = relay_output_setting_illum;
    }

    public int getRelay_output_setting_alarm() {
        return relay_output_setting_alarm;
    }

    public void setRelay_output_setting_alarm(int relay_output_setting_alarm) {
        this.relay_output_setting_alarm = relay_output_setting_alarm;
    }

    public int getRelay_output_setting_reserve() {
        return relay_output_setting_reserve;
    }

    public void setRelay_output_setting_reserve(int relay_output_setting_reserve) {
        this.relay_output_setting_reserve = relay_output_setting_reserve;
    }

    public int getDry_condition_setting_aggr() {
        return dry_condition_setting_aggr;
    }

    public void setDry_condition_setting_aggr(int dry_condition_setting_aggr) {
        this.dry_condition_setting_aggr = dry_condition_setting_aggr;
    }

    public int getDry_condition_setting_ctrl() {
        return dry_condition_setting_ctrl;
    }

    public void setDry_condition_setting_ctrl(int dry_condition_setting_ctrl) {
        this.dry_condition_setting_ctrl = dry_condition_setting_ctrl;
    }

    public int getDry_condition_setting_humidity() {
        return dry_condition_setting_humidity;
    }

    public void setDry_condition_setting_humidity(int dry_condition_setting_humidity) {
        this.dry_condition_setting_humidity = dry_condition_setting_humidity;
    }

    public int getAlert_alarm_time_select_aggr() {
        return alert_alarm_time_select_aggr;
    }

    public void setAlert_alarm_time_select_aggr(int alert_alarm_time_select_aggr) {
        this.alert_alarm_time_select_aggr = alert_alarm_time_select_aggr;
    }

    public int getAlert_alarm_time_select_auto() {
        return alert_alarm_time_select_auto;
    }

    public void setAlert_alarm_time_select_auto(int alert_alarm_time_select_auto) {
        this.alert_alarm_time_select_auto = alert_alarm_time_select_auto;
    }

    public int getAlert_alarm_time_select_timer() {
        return alert_alarm_time_select_timer;
    }

    public void setAlert_alarm_time_select_timer(int alert_alarm_time_select_timer) {
        this.alert_alarm_time_select_timer = alert_alarm_time_select_timer;
    }

    public int getAlert_alarm_time_select_lamp_unit() {
        return alert_alarm_time_select_lamp_unit;
    }

    public void setAlert_alarm_time_select_lamp_unit(int alert_alarm_time_select_lamp_unit) {
        this.alert_alarm_time_select_lamp_unit = alert_alarm_time_select_lamp_unit;
    }

    public int getAlert_alarm_time_select_timeset() {
        return alert_alarm_time_select_timeset;
    }

    public void setAlert_alarm_time_select_timeset(int getAlert_alarm_time_select_timeset) {
        this.alert_alarm_time_select_timeset = getAlert_alarm_time_select_timeset;
    }

    public int getCthi_ctrl_stat_aggr() {
        return cthi_ctrl_stat_aggr;
    }

    public void setCthi_ctrl_stat_aggr(int cthi_ctrl_stat_aggr) {
        this.cthi_ctrl_stat_aggr = cthi_ctrl_stat_aggr;
    }

    public int getCthi_ctrl_stat_co2_ctrl() {
        return cthi_ctrl_stat_co2_ctrl;
    }

    public void setCthi_ctrl_stat_co2_ctrl(int cthi_ctrl_stat_co2_ctrl) {
        this.cthi_ctrl_stat_co2_ctrl = cthi_ctrl_stat_co2_ctrl;
    }

    public int getCthi_ctrl_stat_co2_ontype() {
        return cthi_ctrl_stat_co2_ontype;
    }

    public void setCthi_ctrl_stat_co2_ontype(int cthi_ctrl_stat_co2_ontype) {
        this.cthi_ctrl_stat_co2_ontype = cthi_ctrl_stat_co2_ontype;
    }

    public int getCthi_ctrl_stat_co2_offtype() {
        return cthi_ctrl_stat_co2_offtype;
    }

    public void setCthi_ctrl_stat_co2_offtype(int cthi_ctrl_stat_co2_offtype) {
        this.cthi_ctrl_stat_co2_offtype = cthi_ctrl_stat_co2_offtype;
    }

    public int getCthi_ctrl_stat_temp_ctrl() {
        return cthi_ctrl_stat_temp_ctrl;
    }

    public void setCthi_ctrl_stat_temp_ctrl(int cthi_ctrl_stat_temp_ctrl) {
        this.cthi_ctrl_stat_temp_ctrl = cthi_ctrl_stat_temp_ctrl;
    }

    public int getCthi_ctrl_stat_temp_ontype() {
        return cthi_ctrl_stat_temp_ontype;
    }

    public void setCthi_ctrl_stat_temp_ontype(int cthi_ctrl_stat_temp_ontype) {
        this.cthi_ctrl_stat_temp_ontype = cthi_ctrl_stat_temp_ontype;
    }

    public int getCthi_ctrl_stat_temp_offtype() {
        return cthi_ctrl_stat_temp_offtype;
    }

    public void setCthi_ctrl_stat_temp_offtype(int cthi_ctrl_stat_temp_offtype) {
        this.cthi_ctrl_stat_temp_offtype = cthi_ctrl_stat_temp_offtype;
    }

    public int getCthi_ctrl_stat_humid_ctrl() {
        return cthi_ctrl_stat_humid_ctrl;
    }

    public void setCthi_ctrl_stat_humid_ctrl(int cthi_ctrl_stat_humid_ctrl) {
        this.cthi_ctrl_stat_humid_ctrl = cthi_ctrl_stat_humid_ctrl;
    }

    public int getCthi_ctrl_stat_humid_ontype() {
        return cthi_ctrl_stat_humid_ontype;
    }

    public void setCthi_ctrl_stat_humid_ontype(int cthi_ctrl_stat_humid_ontype) {
        this.cthi_ctrl_stat_humid_ontype = cthi_ctrl_stat_humid_ontype;
    }

    public int getCthi_ctrl_stat_humid_offtype() {
        return cthi_ctrl_stat_humid_offtype;
    }

    public void setCthi_ctrl_stat_humid_offtype(int cthi_ctrl_stat_humid_offtype) {
        this.cthi_ctrl_stat_humid_offtype = cthi_ctrl_stat_humid_offtype;
    }

    public int getCthi_ctrl_stat_illum_ctrl() {
        return cthi_ctrl_stat_illum_ctrl;
    }

    public void setCthi_ctrl_stat_illum_ctrl(int cthi_ctrl_stat_illum_ctrl) {
        this.cthi_ctrl_stat_illum_ctrl = cthi_ctrl_stat_illum_ctrl;
    }

    public int getCthi_ctrl_stat_illum_ontype() {
        return cthi_ctrl_stat_illum_ontype;
    }

    public void setCthi_ctrl_stat_illum_ontype(int cthi_ctrl_stat_illum_ontype) {
        this.cthi_ctrl_stat_illum_ontype = cthi_ctrl_stat_illum_ontype;
    }

    public int getCthi_ctrl_stat_illum_offtype() {
        return cthi_ctrl_stat_illum_offtype;
    }

    public void setCthi_ctrl_stat_illum_offtype(int cthi_ctrl_stat_illum_offtype) {
        this.cthi_ctrl_stat_illum_offtype = cthi_ctrl_stat_illum_offtype;
    }

    public int getCalm_threshold_co2_low() {
        return calm_threshold_co2_low;
    }

    public void setCalm_threshold_co2_low(int calm_threshold_co2_low) {
        this.calm_threshold_co2_low = calm_threshold_co2_low;
    }

    public int getCalm_threshold_co2_high() {
        return calm_threshold_co2_high;
    }

    public void setCalm_threshold_co2_high(int calm_threshold_co2_high) {
        this.calm_threshold_co2_high = calm_threshold_co2_high;
    }

    public int getCalm_threshold_temp_low() {
        return calm_threshold_temp_low;
    }

    public void setCalm_threshold_temp_low(int calm_threshold_temp_low) {
        this.calm_threshold_temp_low = calm_threshold_temp_low;
    }

    public int getCalm_threshold_temp_high() {
        return calm_threshold_temp_high;
    }

    public void setCalm_threshold_temp_high(int calm_threshold_temp_high) {
        this.calm_threshold_temp_high = calm_threshold_temp_high;
    }

    public int getCalm_threshold_humid_low() {
        return calm_threshold_humid_low;
    }

    public void setCalm_threshold_humid_low(int calm_threshold_humid_low) {
        this.calm_threshold_humid_low = calm_threshold_humid_low;
    }

    public int getCalm_threshold_humid_high() {
        return calm_threshold_humid_high;
    }

    public void setCalm_threshold_humid_high(int calm_threshold_humid_high) {
        this.calm_threshold_humid_high = calm_threshold_humid_high;
    }

    public int getCalm_threshold_illum_low() {
        return calm_threshold_illum_low;
    }

    public void setCalm_threshold_illum_low(int calm_threshold_illum_low) {
        this.calm_threshold_illum_low = calm_threshold_illum_low;
    }

    public int getCalm_threshold_illum_high() {
        return calm_threshold_illum_high;
    }

    public void setCalm_threshold_illum_high(int calm_threshold_illum_high) {
        this.calm_threshold_illum_high = calm_threshold_illum_high;
    }

    public int getSetting_range_co2_min() {
        return setting_range_co2_min;
    }

    public void setSetting_range_co2_min(int setting_range_co2_min) {
        this.setting_range_co2_min = setting_range_co2_min;
    }

    public int getSetting_range_co2_max() {
        return setting_range_co2_max;
    }

    public void setSetting_range_co2_max(int setting_range_co2_max) {
        this.setting_range_co2_max = setting_range_co2_max;
    }

    public int getSetting_range_temp_min() {
        return setting_range_temp_min;
    }

    public void setSetting_range_temp_min(int setting_range_temp_min) {
        this.setting_range_temp_min = setting_range_temp_min;
    }

    public int getSetting_range_temp_max() {
        return setting_range_temp_max;
    }

    public void setSetting_range_temp_max(int setting_range_temp_max) {
        this.setting_range_temp_max = setting_range_temp_max;
    }

    public int getSetting_range_humid_min() {
        return setting_range_humid_min;
    }

    public void setSetting_range_humid_min(int setting_range_humid_min) {
        this.setting_range_humid_min = setting_range_humid_min;
    }

    public int getSetting_range_humid_max() {
        return setting_range_humid_max;
    }

    public void setSetting_range_humid_max(int setting_range_humid_max) {
        this.setting_range_humid_max = setting_range_humid_max;
    }

    public int getSetting_range_illum_min() {
        return setting_range_illum_min;
    }

    public void setSetting_range_illum_min(int setting_range_illum_min) {
        this.setting_range_illum_min = setting_range_illum_min;
    }

    public int getSetting_range_illum_max() {
        return setting_range_illum_max;
    }

    public void setSetting_range_illum_max(int setting_range_illum_max) {
        this.setting_range_illum_max = setting_range_illum_max;
    }

    public int getSr_revision_co2_01() {
        return sr_revision_co2_01;
    }

    public void setSr_revision_co2_01(int sr_revision_co2_01) {
        this.sr_revision_co2_01 = sr_revision_co2_01;
    }

    public int getSr_revision_temp_01() {
        return sr_revision_temp_01;
    }

    public void setSr_revision_temp_01(int sr_revision_temp_01) {
        this.sr_revision_temp_01 = sr_revision_temp_01;
    }

    public int getSr_revision_humid_01() {
        return sr_revision_humid_01;
    }

    public void setSr_revision_humid_01(int sr_revision_humid_01) {
        this.sr_revision_humid_01 = sr_revision_humid_01;
    }

    public int getSr_revision_illum_01() {
        return sr_revision_illum_01;
    }

    public void setSr_revision_illum_01(int sr_revision_illum_01) {
        this.sr_revision_illum_01 = sr_revision_illum_01;
    }

    public int getSr_revision_co2_02() {
        return sr_revision_co2_02;
    }

    public void setSr_revision_co2_02(int sr_revision_co2_02) {
        this.sr_revision_co2_02 = sr_revision_co2_02;
    }

    public int getSr_revision_temp_02() {
        return sr_revision_temp_02;
    }

    public void setSr_revision_temp_02(int sr_revision_temp_02) {
        this.sr_revision_temp_02 = sr_revision_temp_02;
    }

    public int getSr_revision_humid_02() {
        return sr_revision_humid_02;
    }

    public void setSr_revision_humid_02(int sr_revision_humid_02) {
        this.sr_revision_humid_02 = sr_revision_humid_02;
    }

    public int getSr_revision_illum_02() {
        return sr_revision_illum_02;
    }

    public void setSr_revision_illum_02(int sr_revision_illum_02) {
        this.sr_revision_illum_02 = sr_revision_illum_02;
    }

    public int getSr_revision_co2_03() {
        return sr_revision_co2_03;
    }

    public void setSr_revision_co2_03(int sr_revision_co2_03) {
        this.sr_revision_co2_03 = sr_revision_co2_03;
    }

    public int getSr_revision_temp_03() {
        return sr_revision_temp_03;
    }

    public void setSr_revision_temp_03(int sr_revision_temp_03) {
        this.sr_revision_temp_03 = sr_revision_temp_03;
    }

    public int getSr_revision_humid_03() {
        return sr_revision_humid_03;
    }

    public void setSr_revision_humid_03(int sr_revision_humid_03) {
        this.sr_revision_humid_03 = sr_revision_humid_03;
    }

    public int getSr_revision_illum_03() {
        return sr_revision_illum_03;
    }

    public void setSr_revision_illum_03(int sr_revision_illum_03) {
        this.sr_revision_illum_03 = sr_revision_illum_03;
    }

    public int getSr_revision_co2_04() {
        return sr_revision_co2_04;
    }

    public void setSr_revision_co2_04(int sr_revision_co2_04) {
        this.sr_revision_co2_04 = sr_revision_co2_04;
    }

    public int getSr_revision_temp_04() {
        return sr_revision_temp_04;
    }

    public void setSr_revision_temp_04(int sr_revision_temp_04) {
        this.sr_revision_temp_04 = sr_revision_temp_04;
    }

    public int getSr_revision_humid_04() {
        return sr_revision_humid_04;
    }

    public void setSr_revision_humid_04(int sr_revision_humid_04) {
        this.sr_revision_humid_04 = sr_revision_humid_04;
    }

    public int getSr_revision_illum_04() {
        return sr_revision_illum_04;
    }

    public void setSr_revision_illum_04(int sr_revision_illum_04) {
        this.sr_revision_illum_04 = sr_revision_illum_04;
    }

    public int getSetting_onoff_range_co2() {
        return setting_onoff_range_co2;
    }

    public void setSetting_onoff_range_co2(int setting_onoff_range_co2) {
        this.setting_onoff_range_co2 = setting_onoff_range_co2;
    }

    public int getSetting_onoff_range_co2_revision() {
        return setting_onoff_range_co2_revision;
    }

    public void setSetting_onoff_range_co2_revision(int setting_onoff_range_co2_revision) {
        this.setting_onoff_range_co2_revision = setting_onoff_range_co2_revision;
    }

    public int getSetting_onoff_range_temp() {
        return setting_onoff_range_temp;
    }

    public void setSetting_onoff_range_temp(int setting_onoff_range_temp) {
        this.setting_onoff_range_temp = setting_onoff_range_temp;
    }

    public int getSetting_onoff_range_temp_revision() {
        return setting_onoff_range_temp_revision;
    }

    public void setSetting_onoff_range_temp_revision(int setting_onoff_range_temp_revision) {
        this.setting_onoff_range_temp_revision = setting_onoff_range_temp_revision;
    }

    public int getSetting_onoff_range_humid() {
        return setting_onoff_range_humid;
    }

    public void setSetting_onoff_range_humid(int setting_onoff_range_humid) {
        this.setting_onoff_range_humid = setting_onoff_range_humid;
    }

    public int getSetting_onoff_range_humid_revision() {
        return setting_onoff_range_humid_revision;
    }

    public void setSetting_onoff_range_humid_revision(int setting_onoff_range_humid_revision) {
        this.setting_onoff_range_humid_revision = setting_onoff_range_humid_revision;
    }

    public int getSetting_onoff_range_illum() {
        return setting_onoff_range_illum;
    }

    public void setSetting_onoff_range_illum(int setting_onoff_range_illum) {
        this.setting_onoff_range_illum = setting_onoff_range_illum;
    }

    public int getSetting_onoff_range_illum_revision() {
        return setting_onoff_range_illum_revision;
    }

    public void setSetting_onoff_range_illum_revision(int setting_onoff_range_illum_revision) {
        this.setting_onoff_range_illum_revision = setting_onoff_range_illum_revision;
    }
}
