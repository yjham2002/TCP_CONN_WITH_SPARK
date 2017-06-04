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
@Deprecated
public class AddressPOJO extends RealtimePOJO {

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
    private int getAlert_alarm_time_select_timeset;
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


    /**
     * 시리얼 바이트로부터 맵핑 및 의미를 구체화하기 위한 생성자
     * @param byteSerial 시리얼 바이트
     */
    public AddressPOJO(ByteSerial byteSerial){
        super(byteSerial);
        init();
    }

    public void init(){

    }

}
