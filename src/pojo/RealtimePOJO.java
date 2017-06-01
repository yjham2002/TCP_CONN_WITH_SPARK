package pojo;

import models.ByteSerial;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;

import java.io.IOException;

/**
 * @author 함의진
 * 실시간 전송 데이터 캡슐화 클래스
 */
public class RealtimePOJO extends BasePOJO{

    /**
     * Convention
     * 날짜 = '-' 문자로 구분 ex) 2017-01-01
     * 시간 = ':' 문자로 구분 ex) 15:00, 17:50:30
     *
     * Memory Map Convention
     * 낙타표기법을 사용하지 않으며, 전체 소문자로 띄어쓰기는 '_' 문자로 처리하여 메모리맵과 최대한 유사하게 처리
     * 단, Month/Day = md, Hour/Minute = hm, Error = err, Sensor = sr
     * 난방 - heat, 냉방 - cool, 가습 - humidify, 제습 - dehumidify, 습도 - humidity, 알람 - alarm, 조명 - illum, 건조 - dry, 일령 - dayage
     * error status - errstat, error data - errdata, 온도 - temp, CO2 - co2, 환기 - vent
     * Bit 연산이 요구되는 정보의 경우, 이를 각각 저장함과 동시에 이를 하나의 정수형으로 가지는 변수를 별도로 설정한다 - Postfix : aggr
     */
    private ByteSerial byteSerial;

    /**
     * Relay Data 시작
     */
    private String co2_relay_on_start_md;
    private String co2_relay_on_start_hm;
    private int co2_relay_on_time;
    private String co2_relay_off_start_md;
    private String co2_relay_off_start_hm;
    private int co2_relay_off_time;
    private String heat_relay_on_start_md;
    private String heat_relay_on_start_hm;
    private int heat_relay_on_time;
    private String heat_relay_off_start_md;
    private String heat_relay_off_start_hm;
    private int heat_relay_off_time;
    private String cool_relay_on_start_md;
    private String cool_relay_on_start_hm;
    private int cool_relay_on_time;
    private String cool_relay_off_start_md;
    private String cool_relay_off_start_hm;
    private int cool_relay_off_time;
    private String humidify_relay_on_start_md;
    private String humidify_relay_on_start_hm;
    private int humidify_relay_on_time;
    private String humidify_relay_off_start_md;
    private String humidify_relay_off_start_hm;
    private int humidify_relay_off_time;
    private String dehumidify_relay_on_start_md;
    private String dehumidify_relay_on_start_hm;
    private int dehumidify_relay_on_time;
    private String dehumidify_relay_off_start_md;
    private String dehumidify_relay_off_start_hm;
    private int dehumidify_relay_off_time;
    private String illum_relay_on_start_md;
    private String illum_relay_on_start_hm;
    private int illum_relay_on_time;
    private String illum_relay_off_start_md;
    private String illum_relay_off_start_hm;
    private int illum_relay_off_time;
    private String alarm_relay_on_start_md;
    private String alarm_relay_on_start_hm;
    private int alarm_relay_on_time;
    private String alarm_relay_off_start_md;
    private String alarm_relay_off_start_hm;
    private int alarm_relay_off_time;
    /**
     * Relay Data 종결
     */

    /**
     * 센서 측정 데이터 시작
     */
    private int co2_sr;
    private int temp_sr;
    private int humid_sr;
    private int illum_sr;
    /**
     * 센서 측정 데이터 종결
     */

    /**
     * 릴레이 출력 데이터 시작
     */
    private int relay_output_aggr;
    private int relay_output_co2;
    private int relay_output_heater;
    private int relay_output_freezer;
    private int relay_output_humidity;
    private int relay_output_dehumidity;
    private int relay_output_ilum;
    private int relay_output_alarm;
    private int relay_output_reserve;
    /**
     * 릴레이 출력 데이터 종결
     */

    /**
     * 옵션 변화 데이터 시작
     */
    private int option_changed_aggr;
    private int option_changed_setting;
    private int option_changed_timer;
    private int option_changed_crop1;
    private int option_changed_crop2;
    private int option_changed_crop3;
    private int option_changed_crop4;
    private int option_changed_crop5;
    private int option_changed_crop6;
    /**
     * 옵션 변화 데이터 종결
     */

    /**
     * 재배 진행 일자 데이터 시작
     */
    private int growth_progress_dt;
    private int growth_progress_total;
    /**
     * 재배 진행 일자 데이터 종결
     */

    /**
     * 실행 상태 데이터 시작
     */
    private int run_status_aggr;
    private int run_status_current;
    private int run_status_mode;
    private int run_status_prevdata;
    private int run_status_dry_enabled;
    private int run_status_dayage_count;
    private int run_status_dayage_progress;
    /**
     * 실행 상태 데이터 종결
     */

    /**
     * 에러 상태 및 에러데이터 멤버 시작
     */
    private int errstat_err0_start_md;
    private int errstat_err0_start_time;
    private int errstat_err0_progress_time;
    private int errstat_err1_data;
    private int errstat_err2_data;
    private int errstat_err3_data;
    private int errstat_err4_data;
    private int errstat_err5_data;
    private int errstat_err6_data;
    private int errstat_err7_data;
    private int errstat_err8_data;
    private int errstat_err9_data;
    private int errstat_err10_data;
    private int errstat_err11_data;
    private int errstat_err12_data;
    private int errstat_err13_data;
    private int errstat_err14_data;
    private int errstat_err15_data;

    private int errdata_aggr;
    private int errdata_internal_co2;
    private int errdata_internal_temp;
    private int errdata_internal_humid;
    private int errdata_internal_ilum;
    private int errdata_vent_relay;
    private int errdata_raisetemp_relay;
    private int errdata_raisecool_relay;
    private int errdata_dehumidify_relay;
    private int errdata_crop_data;
    private int errdata_device_connection;
    private int errdata_network1;
    private int errdata_network2;
    private int errdata_network3;
    private int errdata_network4;
    /**
     * 에러 상태 및 에러데이터 멤버 종결
     */

    /**
     * 기타 멤버 시작
     */

    private int sr_set1_co2;
    private int sr_set1_temp;
    private int sr_set1_humidity;
    private int sr_set1_ilum;
    private int sr_set2_co2;
    private int sr_set2_temp;
    private int sr_set2_humidity;
    private int sr_set2_ilum;
    private int sr_set3_co2;
    private int sr_set3_temp;
    private int sr_set3_humidity;
    private int sr_set3_ilum;
    private int sr_set4_co2;
    private int sr__set4_temp;
    private int sr_set4_humidity;
    private int sr_set4_ilum;

    private int sr_val_co2;
    private int sr_val_temp;
    private int sr_val_humidity;
    private int sr_val_ilum;


    private int controlstat_aggr;
    private int controlstat_co2_type;
    private int controlstat_co2_ontype;
    private int controlstat_co2_offtype;
    private int controlstat_temp_type;
    private int controlstat_temp_ontype;
    private int controlstat_temp_offtype;
    private int controlstat_humidity_type;
    private int controlstat_humidity_ontype;
    private int controlstat_humidity_offtype;
    private int controlstat_ilum_type;
    private int controlstat_ilum_ontype;
    private int controlstat_ilum_offtype;

    private int co2_value;
    private int temp_value;
    private int humidity_value;
    private int ilum_value;

    private int vt515_version;

    private int lcdorder_run;
    private int lcdorder_mode;
    private int lcdorder_dayage_start;

    private int changetype_lcd_setting;
    private int changetype_lcd_timer;
    private int changetype_lcd_dayage1;
    private int changetype_lcd_dayage2;
    private int changetype_lcd_dayage3;
    private int changetype_lcd_dayage4;
    private int changetype_lcd_dayage5;
    private int changetype_lcd_dayage6;
    private int changetype_pc_setting;
    private int changetype_pc_timer;
    private int changetype_pc_dayage1;
    private int changetype_pc_dayage2;
    private int changetype_pc_dayage3;
    private int changetype_pc_dayage4;
    private int changetype_pc_dayage5;
    private int changetype_pc_dayage6;

    private int networkerr_510to515;

    private int dayage_low;
    private int dayage_high;

    private int real_sec;
    private int real_hm;
    private int real_md;

    private int dymamic_output_mode;
    private int dymamic_output_inc;
    private int dymamic_output_dec;
    private int dymamic_output_analog;
    private int dymamic_output_valid;

    private int start_year;
    private int start_md;
    private int start_hm;

    private int mcnctrl_mv510_aggr;
    private int mcnctrl_mv510_order_co2;
    private int mcnctrl_mv510_order_temp;
    private int mcnctrl_mv510_order_humidity;
    private int mcnctrl_mv510_order_ilum;
    private int mcnctrl_mv510_order_main1;
    private int mcnctrl_mv510_order_main2;
    private int mcnctrl_mv510_stat_fan;
    private int mcnctrl_mv510_stat_heater;
    private int mcnctrl_mv510_stat_freezer;
    private int mcnctrl_mv510_stat_humidifier;
    private int mcnctrl_mv510_stat_dehumidifier;
    private int mcnctrl_mv510_stat_ilum;
    private int mcnctrl_mv510_stat_alarm;
    private int mcnctrl_mv510_stat_reserve;

    private int mcnctrl_web_aggr;
    private int mcnctrl_web_order_co2;
    private int mcnctrl_web_order_temp;
    private int mcnctrl_web_order_humidity;
    private int mcnctrl_web_order_ilum;
    private int mcnctrl_web_order_main1;
    private int mcnctrl_web_order_main2;
    private int mcnctrl_web_stat_fan;
    private int mcnctrl_web_stat_heater;
    private int mcnctrl_web_stat_freezer;
    private int mcnctrl_web_stat_humidifier;
    private int mcnctrl_web_stat_dehumidifier;
    private int mcnctrl_web_stat_ilum;
    private int mcnctrl_web_stat_alarm;
    private int mcnctrl_web_stat_reserve;
    /**
     * 기타 멤버 종결
     */

    public RealtimePOJO(ByteSerial byteSerial){
        this.byteSerial = byteSerial;
        this.classType = this.getClass();

        ellaborate();
    }

    /**
     * Retrieving meaning of data from byte serial array
     * 바이트 어레이로부터 의미상의 인덱스를 데이터맵에 의거하여 배치함
     * 데이터 범위 내의 위치가 offset으로 사용되며, '절대위치'가 사용되어서는 안된다. (중요)
     */
    private void ellaborate(){
        /**
         * Relay Data init
         */
        this.co2_relay_on_start_md = getMDorHMWith2Bytes(0, "-");
        this.co2_relay_on_start_hm = getMDorHMWith2Bytes(2, ":");
        this.co2_relay_on_time = getSumWith2Bytes(4);
        this.co2_relay_off_start_md = getMDorHMWith2Bytes(6, "-");
        this.co2_relay_off_start_hm = getMDorHMWith2Bytes(8, ":");
        this.co2_relay_off_time = getSumWith2Bytes(10);
        this.heat_relay_on_start_md = getMDorHMWith2Bytes(12, "-");
        this.heat_relay_on_start_hm = getMDorHMWith2Bytes(14, ":");
        this.heat_relay_on_time = getSumWith2Bytes(16);
        this.heat_relay_off_start_md = getMDorHMWith2Bytes(18, "-");
        this.heat_relay_off_start_hm = getMDorHMWith2Bytes(20, ":");
        this.heat_relay_off_time = getSumWith2Bytes(22);
        this.cool_relay_on_start_md = getMDorHMWith2Bytes(24, "-");
        this.cool_relay_on_start_hm = getMDorHMWith2Bytes(26, ":");
        this.cool_relay_on_time = getSumWith2Bytes(28);
        this.cool_relay_off_start_md = getMDorHMWith2Bytes(30, "-");
        this.cool_relay_off_start_hm = getMDorHMWith2Bytes(32, ":");
        this.cool_relay_off_time = getSumWith2Bytes(34);
        this.humidify_relay_on_start_md = getMDorHMWith2Bytes(36, "-");
        this.humidify_relay_on_start_hm = getMDorHMWith2Bytes(38, ":");
        this.humidify_relay_on_time = getSumWith2Bytes(40);
        this.humidify_relay_off_start_md = getMDorHMWith2Bytes(42, "-");
        this.humidify_relay_off_start_hm = getMDorHMWith2Bytes(44, ":");
        this.humidify_relay_off_time = getSumWith2Bytes(46);
        this.dehumidify_relay_on_start_md = getMDorHMWith2Bytes(48, "-");
        this.dehumidify_relay_on_start_hm = getMDorHMWith2Bytes(50, ":");
        this.dehumidify_relay_on_time = getSumWith2Bytes(52);
        this.dehumidify_relay_off_start_md = getMDorHMWith2Bytes(54, "-");
        this.dehumidify_relay_off_start_hm = getMDorHMWith2Bytes(56, ":");
        this.dehumidify_relay_off_time = getSumWith2Bytes(58);
        this.illum_relay_on_start_md = getMDorHMWith2Bytes(60, "-");
        this.illum_relay_on_start_hm = getMDorHMWith2Bytes(62, ":");
        this.illum_relay_on_time = getSumWith2Bytes(64);
        this.illum_relay_off_start_md = getMDorHMWith2Bytes(66, "-");
        this.illum_relay_off_start_hm = getMDorHMWith2Bytes(68, ":");
        this.illum_relay_off_time = getSumWith2Bytes(70);
        this.alarm_relay_on_start_md = getMDorHMWith2Bytes(72, "-");
        this.alarm_relay_on_start_hm = getMDorHMWith2Bytes(74, ":");
        this.alarm_relay_on_time = getSumWith2Bytes(76);
        this.alarm_relay_off_start_md = getMDorHMWith2Bytes(78, "-");
        this.alarm_relay_off_start_hm = getMDorHMWith2Bytes(80, ":");
        this.alarm_relay_off_time = getSumWith2Bytes(82);

        /**
         * Sensor measured data
         */
        this.co2_sr = getSumWith2Bytes(100);
        this.temp_sr = getSumWith2Bytes(102);
        this.humid_sr = getSumWith2Bytes(104);
        this.illum_sr = getSumWith2Bytes(106);

//        /**
//         * 릴레이 출력 데이터 시작
//         */
//        this.relay_output_aggr = ;
//        this.relay_output_co2;
//        this.relay_output_heater;
//        this.relay_output_freezer;
//        this.relay_output_humidity;
//        this.relay_output_dehumidity;
//        this.relay_output_ilum;
//        this.relay_output_alarm;
//        this.relay_output_reserve;
//        /**
//         * 릴레이 출력 데이터 종결
//         */
//
//        /**
//         * 옵션 변화 데이터 시작
//         */
//        this.option_changed_aggr;
//        this.option_changed_setting;
//        this.option_changed_timer;
//        this.option_changed_crop1;
//        this.option_changed_crop2;
//        this.option_changed_crop3;
//        this.option_changed_crop4;
//        this.option_changed_crop5;
//        this.option_changed_crop6;
//        /**
//         * 옵션 변화 데이터 종결
//         */
//
//        /**
//         * 재배 진행 일자 데이터 시작
//         */
//        this.growth_progress_dt;
//        this.growth_progress_total;
//        /**
//         * 재배 진행 일자 데이터 종결
//         */
//
//        /**
//         * 실행 상태 데이터 시작
//         */
//        this.run_status_aggr;
//        this.run_status_current;
//        this.run_status_mode;
//        this.run_status_prevdata;
//        this.run_status_dry_enabled;
//        this.run_status_dayage_count;
//        this.run_status_dayage_progress;
//        /**
//         * 실행 상태 데이터 종결
//         */
//
//        /**
//         * 에러 상태 및 에러데이터 멤버 시작
//         */
//        this.errstat_err0_start_md;
//        this.errstat_err0_start_time;
//        this.errstat_err0_progress_time;
//        this.errstat_err1_data;
//        this.errstat_err2_data;
//        this.errstat_err3_data;
//        this.errstat_err4_data;
//        this.errstat_err5_data;
//        this.errstat_err6_data;
//        this.errstat_err7_data;
//        this.errstat_err8_data;
//        this.errstat_err9_data;
//        this.errstat_err10_data;
//        this.errstat_err11_data;
//        this.errstat_err12_data;
//        this.errstat_err13_data;
//        this.errstat_err14_data;
//        this.errstat_err15_data;
//
//        this.errdata_aggr;
//        this.errdata_internal_co2;
//        this.errdata_internal_temp;
//        this.errdata_internal_humid;
//        this.errdata_internal_ilum;
//        this.errdata_vent_relay;
//        this.errdata_raisetemp_relay;
//        this.errdata_raisecool_relay;
//        this.errdata_dehumidify_relay;
//        this.errdata_crop_data;
//        this.errdata_device_connection;
//        this.errdata_network1;
//        this.errdata_network2;
//        this.errdata_network3;
//        this.errdata_network4;
//        /**
//         * 에러 상태 및 에러데이터 멤버 종결
//         */
//
//        /**
//         * 기타 멤버 시작
//         */
//
//        this.sr_set1_co2;
//        this.sr_set1_temp;
//        this.sr_set1_humidity;
//        this.sr_set1_ilum;
//        this.sr_set2_co2;
//        this.sr_set2_temp;
//        this.sr_set2_humidity;
//        this.sr_set2_ilum;
//        this.sr_set3_co2;
//        this.sr_set3_temp;
//        this.sr_set3_humidity;
//        this.sr_set3_ilum;
//        this.sr_set4_co2;
//        this.sr__set4_temp;
//        this.sr_set4_humidity;
//        this.sr_set4_ilum;
//
//        this.sr_val_co2;
//        this.sr_val_temp;
//        this.sr_val_humidity;
//        this.sr_val_ilum;
//
//
//        this.controlstat_aggr;
//        this.controlstat_co2_type;
//        this.controlstat_co2_ontype;
//        this.controlstat_co2_offtype;
//        this.controlstat_temp_type;
//        this.controlstat_temp_ontype;
//        this.controlstat_temp_offtype;
//        this.controlstat_humidity_type;
//        this.controlstat_humidity_ontype;
//        this.controlstat_humidity_offtype;
//        this.controlstat_ilum_type;
//        this.controlstat_ilum_ontype;
//        this.controlstat_ilum_offtype;
//
//        this.co2_value;
//        this.temp_value;
//        this.humidity_value;
//        this.ilum_value;
//
//        this.vt515_version;
//
//        this.lcdorder_run;
//        this.lcdorder_mode;
//        this.lcdorder_dayage_start;
//
//        this.changetype_lcd_setting;
//        this.changetype_lcd_timer;
//        this.changetype_lcd_dayage1;
//        this.changetype_lcd_dayage2;
//        this.changetype_lcd_dayage3;
//        this.changetype_lcd_dayage4;
//        this.changetype_lcd_dayage5;
//        this.changetype_lcd_dayage6;
//        this.changetype_pc_setting;
//        this.changetype_pc_timer;
//        this.changetype_pc_dayage1;
//        this.changetype_pc_dayage2;
//        this.changetype_pc_dayage3;
//        this.changetype_pc_dayage4;
//        this.changetype_pc_dayage5;
//        this.changetype_pc_dayage6;
//
//        this.networkerr_510to515;
//
//        this.dayage_low;
//        this.dayage_high;
//
//        this.real_sec;
//        this.real_hm;
//        this.real_md;
//
//        this.dymamic_output_mode;
//        this.dymamic_output_inc;
//        this.dymamic_output_dec;
//        this.dymamic_output_analog;
//        this.dymamic_output_valid;
//
//        this.start_year;
//        this.start_md;
//        this.start_hm;
//
//        this.mcnctrl_mv510_aggr;
//        this.mcnctrl_mv510_order_co2;
//        this.mcnctrl_mv510_order_temp;
//        this.mcnctrl_mv510_order_humidity;
//        this.mcnctrl_mv510_order_ilum;
//        this.mcnctrl_mv510_order_main1;
//        this.mcnctrl_mv510_order_main2;
//        this.mcnctrl_mv510_stat_fan;
//        this.mcnctrl_mv510_stat_heater;
//        this.mcnctrl_mv510_stat_freezer;
//        this.mcnctrl_mv510_stat_humidifier;
//        this.mcnctrl_mv510_stat_dehumidifier;
//        this.mcnctrl_mv510_stat_ilum;
//        this.mcnctrl_mv510_stat_alarm;
//        this.mcnctrl_mv510_stat_reserve;
//
//        this.mcnctrl_web_aggr;
//        this.mcnctrl_web_order_co2;
//        this.mcnctrl_web_order_temp;
//        this.mcnctrl_web_order_humidity;
//        this.mcnctrl_web_order_ilum;
//        this.mcnctrl_web_order_main1;
//        this.mcnctrl_web_order_main2;
//        this.mcnctrl_web_stat_fan;
//        this.mcnctrl_web_stat_heater;
//        this.mcnctrl_web_stat_freezer;
//        this.mcnctrl_web_stat_humidifier;
//        this.mcnctrl_web_stat_dehumidifier;
//        this.mcnctrl_web_stat_ilum;
//        this.mcnctrl_web_stat_alarm;
//        this.mcnctrl_web_stat_reserve;
    }

    /**
     * Calculating Date or Time by byte index offset and formatting as String with delimiter
     * @param offset Byte array index
     * @param delimiter String delimiter
     * @return Formatted String
     */
    private String getMDorHMWith2Bytes(int offset, String delimiter){
        int total = getSumWith2Bytes(offset);
        int header = total >> 8;
        int footer = total - (header << 8);

        return String.format("%02d" + delimiter + "%02d", header, footer);
    }

    /**
     * Calculating the numerical summation in range of offset
     * @param offset
     * @return Numerical summation
     *
     * < Comment >
     * '절대위치'를 이곳에서 계산하기에 다른 곳의 offset을 조정해서는 절대 안 됨
     * 프로토콜 변경이 있을 경우, BasePOJO의 상수를 수정해야 함
     */
    private int getSumWith2Bytes(int offset){
        int absolute = offset + ARRAY_START_RANGE;
        int lhs = byteSerial.getProcessed()[absolute] << 8;
        int rhs = byteSerial.getProcessed()[absolute + 1];
        int total = lhs + rhs;

        return total;
    }

    private int getSingleByte(int offset){
        int absolute = offset + ARRAY_START_RANGE;
        return byteSerial.getProcessed()[absolute];
    }

    private int getBooleanValueFromByte(int offset, int bitIndex){
        int value = getSingleByte(offset);
        String binary = Integer.toBinaryString(value);

        return 0;
    }

    public static void main(String... args){
        final String format = "00000000";
        String bin = Integer.toBinaryString(32);
        String fmt = "";
        String retVal = "";
        if(bin.length() < 8){
            int leak = 8 - bin.length();
            fmt = format.substring(0, leak);
        }

        retVal = fmt + bin;

        System.out.println(bin);
    }

    public ByteSerial getByteSerial() {
        return byteSerial;
    }

    public void setByteSerial(ByteSerial byteSerial) {
        this.byteSerial = byteSerial;
    }

    public String getCo2_relay_on_start_md() {
        return co2_relay_on_start_md;
    }

    public void setCo2_relay_on_start_md(String co2_relay_on_start_md) {
        this.co2_relay_on_start_md = co2_relay_on_start_md;
    }

    public String getCo2_relay_on_start_hm() {
        return co2_relay_on_start_hm;
    }

    public void setCo2_relay_on_start_hm(String co2_relay_on_start_hm) {
        this.co2_relay_on_start_hm = co2_relay_on_start_hm;
    }

    public int getCo2_relay_on_time() {
        return co2_relay_on_time;
    }

    public void setCo2_relay_on_time(int co2_relay_on_time) {
        this.co2_relay_on_time = co2_relay_on_time;
    }

    public String getCo2_relay_off_start_md() {
        return co2_relay_off_start_md;
    }

    public void setCo2_relay_off_start_md(String co2_relay_off_start_md) {
        this.co2_relay_off_start_md = co2_relay_off_start_md;
    }

    public String getCo2_relay_off_start_hm() {
        return co2_relay_off_start_hm;
    }

    public void setCo2_relay_off_start_hm(String co2_relay_off_start_hm) {
        this.co2_relay_off_start_hm = co2_relay_off_start_hm;
    }

    public int getCo2_relay_off_time() {
        return co2_relay_off_time;
    }

    public void setCo2_relay_off_time(int co2_relay_off_time) {
        this.co2_relay_off_time = co2_relay_off_time;
    }

    public String getHeat_relay_on_start_md() {
        return heat_relay_on_start_md;
    }

    public void setHeat_relay_on_start_md(String heat_relay_on_start_md) {
        this.heat_relay_on_start_md = heat_relay_on_start_md;
    }

    public String getHeat_relay_on_start_hm() {
        return heat_relay_on_start_hm;
    }

    public void setHeat_relay_on_start_hm(String heat_relay_on_start_hm) {
        this.heat_relay_on_start_hm = heat_relay_on_start_hm;
    }

    public int getHeat_relay_on_time() {
        return heat_relay_on_time;
    }

    public void setHeat_relay_on_time(int heat_relay_on_time) {
        this.heat_relay_on_time = heat_relay_on_time;
    }

    public String getHeat_relay_off_start_md() {
        return heat_relay_off_start_md;
    }

    public void setHeat_relay_off_start_md(String heat_relay_off_start_md) {
        this.heat_relay_off_start_md = heat_relay_off_start_md;
    }

    public String getHeat_relay_off_start_hm() {
        return heat_relay_off_start_hm;
    }

    public void setHeat_relay_off_start_hm(String heat_relay_off_start_hm) {
        this.heat_relay_off_start_hm = heat_relay_off_start_hm;
    }

    public int getHeat_relay_off_time() {
        return heat_relay_off_time;
    }

    public void setHeat_relay_off_time(int heat_relay_off_time) {
        this.heat_relay_off_time = heat_relay_off_time;
    }

    public String getCool_relay_on_start_md() {
        return cool_relay_on_start_md;
    }

    public void setCool_relay_on_start_md(String cool_relay_on_start_md) {
        this.cool_relay_on_start_md = cool_relay_on_start_md;
    }

    public String getCool_relay_on_start_hm() {
        return cool_relay_on_start_hm;
    }

    public void setCool_relay_on_start_hm(String cool_relay_on_start_hm) {
        this.cool_relay_on_start_hm = cool_relay_on_start_hm;
    }

    public int getCool_relay_on_time() {
        return cool_relay_on_time;
    }

    public void setCool_relay_on_time(int cool_relay_on_time) {
        this.cool_relay_on_time = cool_relay_on_time;
    }

    public String getCool_relay_off_start_md() {
        return cool_relay_off_start_md;
    }

    public void setCool_relay_off_start_md(String cool_relay_off_start_md) {
        this.cool_relay_off_start_md = cool_relay_off_start_md;
    }

    public String getCool_relay_off_start_hm() {
        return cool_relay_off_start_hm;
    }

    public void setCool_relay_off_start_hm(String cool_relay_off_start_hm) {
        this.cool_relay_off_start_hm = cool_relay_off_start_hm;
    }

    public int getCool_relay_off_time() {
        return cool_relay_off_time;
    }

    public void setCool_relay_off_time(int cool_relay_off_time) {
        this.cool_relay_off_time = cool_relay_off_time;
    }

    public String getHumidify_relay_on_start_md() {
        return humidify_relay_on_start_md;
    }

    public void setHumidify_relay_on_start_md(String humidify_relay_on_start_md) {
        this.humidify_relay_on_start_md = humidify_relay_on_start_md;
    }

    public String getHumidify_relay_on_start_hm() {
        return humidify_relay_on_start_hm;
    }

    public void setHumidify_relay_on_start_hm(String humidify_relay_on_start_hm) {
        this.humidify_relay_on_start_hm = humidify_relay_on_start_hm;
    }

    public int getHumidify_relay_on_time() {
        return humidify_relay_on_time;
    }

    public void setHumidify_relay_on_time(int humidify_relay_on_time) {
        this.humidify_relay_on_time = humidify_relay_on_time;
    }

    public String getHumidify_relay_off_start_md() {
        return humidify_relay_off_start_md;
    }

    public void setHumidify_relay_off_start_md(String humidify_relay_off_start_md) {
        this.humidify_relay_off_start_md = humidify_relay_off_start_md;
    }

    public String getHumidify_relay_off_start_hm() {
        return humidify_relay_off_start_hm;
    }

    public void setHumidify_relay_off_start_hm(String humidify_relay_off_start_hm) {
        this.humidify_relay_off_start_hm = humidify_relay_off_start_hm;
    }

    public int getHumidify_relay_off_time() {
        return humidify_relay_off_time;
    }

    public void setHumidify_relay_off_time(int humidify_relay_off_time) {
        this.humidify_relay_off_time = humidify_relay_off_time;
    }

    public String getDehumidify_relay_on_start_md() {
        return dehumidify_relay_on_start_md;
    }

    public void setDehumidify_relay_on_start_md(String dehumidify_relay_on_start_md) {
        this.dehumidify_relay_on_start_md = dehumidify_relay_on_start_md;
    }

    public String getDehumidify_relay_on_start_hm() {
        return dehumidify_relay_on_start_hm;
    }

    public void setDehumidify_relay_on_start_hm(String dehumidify_relay_on_start_hm) {
        this.dehumidify_relay_on_start_hm = dehumidify_relay_on_start_hm;
    }

    public int getDehumidify_relay_on_time() {
        return dehumidify_relay_on_time;
    }

    public void setDehumidify_relay_on_time(int dehumidify_relay_on_time) {
        this.dehumidify_relay_on_time = dehumidify_relay_on_time;
    }

    public String getDehumidify_relay_off_start_md() {
        return dehumidify_relay_off_start_md;
    }

    public void setDehumidify_relay_off_start_md(String dehumidify_relay_off_start_md) {
        this.dehumidify_relay_off_start_md = dehumidify_relay_off_start_md;
    }

    public String getDehumidify_relay_off_start_hm() {
        return dehumidify_relay_off_start_hm;
    }

    public void setDehumidify_relay_off_start_hm(String dehumidify_relay_off_start_hm) {
        this.dehumidify_relay_off_start_hm = dehumidify_relay_off_start_hm;
    }

    public int getDehumidify_relay_off_time() {
        return dehumidify_relay_off_time;
    }

    public void setDehumidify_relay_off_time(int dehumidify_relay_off_time) {
        this.dehumidify_relay_off_time = dehumidify_relay_off_time;
    }

    public String getIllum_relay_on_start_md() {
        return illum_relay_on_start_md;
    }

    public void setIllum_relay_on_start_md(String illum_relay_on_start_md) {
        this.illum_relay_on_start_md = illum_relay_on_start_md;
    }

    public String getIllum_relay_on_start_hm() {
        return illum_relay_on_start_hm;
    }

    public void setIllum_relay_on_start_hm(String illum_relay_on_start_hm) {
        this.illum_relay_on_start_hm = illum_relay_on_start_hm;
    }

    public int getIllum_relay_on_time() {
        return illum_relay_on_time;
    }

    public void setIllum_relay_on_time(int illum_relay_on_time) {
        this.illum_relay_on_time = illum_relay_on_time;
    }

    public String getIllum_relay_off_start_md() {
        return illum_relay_off_start_md;
    }

    public void setIllum_relay_off_start_md(String illum_relay_off_start_md) {
        this.illum_relay_off_start_md = illum_relay_off_start_md;
    }

    public String getIllum_relay_off_start_hm() {
        return illum_relay_off_start_hm;
    }

    public void setIllum_relay_off_start_hm(String illum_relay_off_start_hm) {
        this.illum_relay_off_start_hm = illum_relay_off_start_hm;
    }

    public int getIllum_relay_off_time() {
        return illum_relay_off_time;
    }

    public void setIllum_relay_off_time(int illum_relay_off_time) {
        this.illum_relay_off_time = illum_relay_off_time;
    }

    public String getAlarm_relay_on_start_md() {
        return alarm_relay_on_start_md;
    }

    public void setAlarm_relay_on_start_md(String alarm_relay_on_start_md) {
        this.alarm_relay_on_start_md = alarm_relay_on_start_md;
    }

    public String getAlarm_relay_on_start_hm() {
        return alarm_relay_on_start_hm;
    }

    public void setAlarm_relay_on_start_hm(String alarm_relay_on_start_hm) {
        this.alarm_relay_on_start_hm = alarm_relay_on_start_hm;
    }

    public int getAlarm_relay_on_time() {
        return alarm_relay_on_time;
    }

    public void setAlarm_relay_on_time(int alarm_relay_on_time) {
        this.alarm_relay_on_time = alarm_relay_on_time;
    }

    public String getAlarm_relay_off_start_md() {
        return alarm_relay_off_start_md;
    }

    public void setAlarm_relay_off_start_md(String alarm_relay_off_start_md) {
        this.alarm_relay_off_start_md = alarm_relay_off_start_md;
    }

    public String getAlarm_relay_off_start_hm() {
        return alarm_relay_off_start_hm;
    }

    public void setAlarm_relay_off_start_hm(String alarm_relay_off_start_hm) {
        this.alarm_relay_off_start_hm = alarm_relay_off_start_hm;
    }

    public int getAlarm_relay_off_time() {
        return alarm_relay_off_time;
    }

    public void setAlarm_relay_off_time(int alarm_relay_off_time) {
        this.alarm_relay_off_time = alarm_relay_off_time;
    }

    public int getCo2_sr() {
        return co2_sr;
    }

    public void setCo2_sr(int co2_sr) {
        this.co2_sr = co2_sr;
    }

    public int getTemp_sr() {
        return temp_sr;
    }

    public void setTemp_sr(int temp_sr) {
        this.temp_sr = temp_sr;
    }

    public int getHumid_sr() {
        return humid_sr;
    }

    public void setHumid_sr(int humid_sr) {
        this.humid_sr = humid_sr;
    }

    public int getIllum_sr() {
        return illum_sr;
    }

    public void setIllum_sr(int illum_sr) {
        this.illum_sr = illum_sr;
    }

    public int getRelay_output_aggr() {
        return relay_output_aggr;
    }

    public void setRelay_output_aggr(int relay_output_aggr) {
        this.relay_output_aggr = relay_output_aggr;
    }

    public int getRelay_output_co2() {
        return relay_output_co2;
    }

    public void setRelay_output_co2(int relay_output_co2) {
        this.relay_output_co2 = relay_output_co2;
    }

    public int getRelay_output_heater() {
        return relay_output_heater;
    }

    public void setRelay_output_heater(int relay_output_heater) {
        this.relay_output_heater = relay_output_heater;
    }

    public int getRelay_output_freezer() {
        return relay_output_freezer;
    }

    public void setRelay_output_freezer(int relay_output_freezer) {
        this.relay_output_freezer = relay_output_freezer;
    }

    public int getRelay_output_humidity() {
        return relay_output_humidity;
    }

    public void setRelay_output_humidity(int relay_output_humidity) {
        this.relay_output_humidity = relay_output_humidity;
    }

    public int getRelay_output_dehumidity() {
        return relay_output_dehumidity;
    }

    public void setRelay_output_dehumidity(int relay_output_dehumidity) {
        this.relay_output_dehumidity = relay_output_dehumidity;
    }

    public int getRelay_output_ilum() {
        return relay_output_ilum;
    }

    public void setRelay_output_ilum(int relay_output_ilum) {
        this.relay_output_ilum = relay_output_ilum;
    }

    public int getRelay_output_alarm() {
        return relay_output_alarm;
    }

    public void setRelay_output_alarm(int relay_output_alarm) {
        this.relay_output_alarm = relay_output_alarm;
    }

    public int getRelay_output_reserve() {
        return relay_output_reserve;
    }

    public void setRelay_output_reserve(int relay_output_reserve) {
        this.relay_output_reserve = relay_output_reserve;
    }

    public int getOption_changed_aggr() {
        return option_changed_aggr;
    }

    public void setOption_changed_aggr(int option_changed_aggr) {
        this.option_changed_aggr = option_changed_aggr;
    }

    public int getOption_changed_setting() {
        return option_changed_setting;
    }

    public void setOption_changed_setting(int option_changed_setting) {
        this.option_changed_setting = option_changed_setting;
    }

    public int getOption_changed_timer() {
        return option_changed_timer;
    }

    public void setOption_changed_timer(int option_changed_timer) {
        this.option_changed_timer = option_changed_timer;
    }

    public int getOption_changed_crop1() {
        return option_changed_crop1;
    }

    public void setOption_changed_crop1(int option_changed_crop1) {
        this.option_changed_crop1 = option_changed_crop1;
    }

    public int getOption_changed_crop2() {
        return option_changed_crop2;
    }

    public void setOption_changed_crop2(int option_changed_crop2) {
        this.option_changed_crop2 = option_changed_crop2;
    }

    public int getOption_changed_crop3() {
        return option_changed_crop3;
    }

    public void setOption_changed_crop3(int option_changed_crop3) {
        this.option_changed_crop3 = option_changed_crop3;
    }

    public int getOption_changed_crop4() {
        return option_changed_crop4;
    }

    public void setOption_changed_crop4(int option_changed_crop4) {
        this.option_changed_crop4 = option_changed_crop4;
    }

    public int getOption_changed_crop5() {
        return option_changed_crop5;
    }

    public void setOption_changed_crop5(int option_changed_crop5) {
        this.option_changed_crop5 = option_changed_crop5;
    }

    public int getOption_changed_crop6() {
        return option_changed_crop6;
    }

    public void setOption_changed_crop6(int option_changed_crop6) {
        this.option_changed_crop6 = option_changed_crop6;
    }

    public int getGrowth_progress_dt() {
        return growth_progress_dt;
    }

    public void setGrowth_progress_dt(int growth_progress_dt) {
        this.growth_progress_dt = growth_progress_dt;
    }

    public int getGrowth_progress_total() {
        return growth_progress_total;
    }

    public void setGrowth_progress_total(int growth_progress_total) {
        this.growth_progress_total = growth_progress_total;
    }

    public int getRun_status_aggr() {
        return run_status_aggr;
    }

    public void setRun_status_aggr(int run_status_aggr) {
        this.run_status_aggr = run_status_aggr;
    }

    public int getRun_status_current() {
        return run_status_current;
    }

    public void setRun_status_current(int run_status_current) {
        this.run_status_current = run_status_current;
    }

    public int getRun_status_mode() {
        return run_status_mode;
    }

    public void setRun_status_mode(int run_status_mode) {
        this.run_status_mode = run_status_mode;
    }

    public int getRun_status_prevdata() {
        return run_status_prevdata;
    }

    public void setRun_status_prevdata(int run_status_prevdata) {
        this.run_status_prevdata = run_status_prevdata;
    }

    public int getRun_status_dry_enabled() {
        return run_status_dry_enabled;
    }

    public void setRun_status_dry_enabled(int run_status_dry_enabled) {
        this.run_status_dry_enabled = run_status_dry_enabled;
    }

    public int getRun_status_dayage_count() {
        return run_status_dayage_count;
    }

    public void setRun_status_dayage_count(int run_status_dayage_count) {
        this.run_status_dayage_count = run_status_dayage_count;
    }

    public int getRun_status_dayage_progress() {
        return run_status_dayage_progress;
    }

    public void setRun_status_dayage_progress(int run_status_dayage_progress) {
        this.run_status_dayage_progress = run_status_dayage_progress;
    }

    public int getErrstat_err0_start_md() {
        return errstat_err0_start_md;
    }

    public void setErrstat_err0_start_md(int errstat_err0_start_md) {
        this.errstat_err0_start_md = errstat_err0_start_md;
    }

    public int getErrstat_err0_start_time() {
        return errstat_err0_start_time;
    }

    public void setErrstat_err0_start_time(int errstat_err0_start_time) {
        this.errstat_err0_start_time = errstat_err0_start_time;
    }

    public int getErrstat_err0_progress_time() {
        return errstat_err0_progress_time;
    }

    public void setErrstat_err0_progress_time(int errstat_err0_progress_time) {
        this.errstat_err0_progress_time = errstat_err0_progress_time;
    }

    public int getErrstat_err1_data() {
        return errstat_err1_data;
    }

    public void setErrstat_err1_data(int errstat_err1_data) {
        this.errstat_err1_data = errstat_err1_data;
    }

    public int getErrstat_err2_data() {
        return errstat_err2_data;
    }

    public void setErrstat_err2_data(int errstat_err2_data) {
        this.errstat_err2_data = errstat_err2_data;
    }

    public int getErrstat_err3_data() {
        return errstat_err3_data;
    }

    public void setErrstat_err3_data(int errstat_err3_data) {
        this.errstat_err3_data = errstat_err3_data;
    }

    public int getErrstat_err4_data() {
        return errstat_err4_data;
    }

    public void setErrstat_err4_data(int errstat_err4_data) {
        this.errstat_err4_data = errstat_err4_data;
    }

    public int getErrstat_err5_data() {
        return errstat_err5_data;
    }

    public void setErrstat_err5_data(int errstat_err5_data) {
        this.errstat_err5_data = errstat_err5_data;
    }

    public int getErrstat_err6_data() {
        return errstat_err6_data;
    }

    public void setErrstat_err6_data(int errstat_err6_data) {
        this.errstat_err6_data = errstat_err6_data;
    }

    public int getErrstat_err7_data() {
        return errstat_err7_data;
    }

    public void setErrstat_err7_data(int errstat_err7_data) {
        this.errstat_err7_data = errstat_err7_data;
    }

    public int getErrstat_err8_data() {
        return errstat_err8_data;
    }

    public void setErrstat_err8_data(int errstat_err8_data) {
        this.errstat_err8_data = errstat_err8_data;
    }

    public int getErrstat_err9_data() {
        return errstat_err9_data;
    }

    public void setErrstat_err9_data(int errstat_err9_data) {
        this.errstat_err9_data = errstat_err9_data;
    }

    public int getErrstat_err10_data() {
        return errstat_err10_data;
    }

    public void setErrstat_err10_data(int errstat_err10_data) {
        this.errstat_err10_data = errstat_err10_data;
    }

    public int getErrstat_err11_data() {
        return errstat_err11_data;
    }

    public void setErrstat_err11_data(int errstat_err11_data) {
        this.errstat_err11_data = errstat_err11_data;
    }

    public int getErrstat_err12_data() {
        return errstat_err12_data;
    }

    public void setErrstat_err12_data(int errstat_err12_data) {
        this.errstat_err12_data = errstat_err12_data;
    }

    public int getErrstat_err13_data() {
        return errstat_err13_data;
    }

    public void setErrstat_err13_data(int errstat_err13_data) {
        this.errstat_err13_data = errstat_err13_data;
    }

    public int getErrstat_err14_data() {
        return errstat_err14_data;
    }

    public void setErrstat_err14_data(int errstat_err14_data) {
        this.errstat_err14_data = errstat_err14_data;
    }

    public int getErrstat_err15_data() {
        return errstat_err15_data;
    }

    public void setErrstat_err15_data(int errstat_err15_data) {
        this.errstat_err15_data = errstat_err15_data;
    }

    public int getErrdata_aggr() {
        return errdata_aggr;
    }

    public void setErrdata_aggr(int errdata_aggr) {
        this.errdata_aggr = errdata_aggr;
    }

    public int getErrdata_internal_co2() {
        return errdata_internal_co2;
    }

    public void setErrdata_internal_co2(int errdata_internal_co2) {
        this.errdata_internal_co2 = errdata_internal_co2;
    }

    public int getErrdata_internal_temp() {
        return errdata_internal_temp;
    }

    public void setErrdata_internal_temp(int errdata_internal_temp) {
        this.errdata_internal_temp = errdata_internal_temp;
    }

    public int getErrdata_internal_humid() {
        return errdata_internal_humid;
    }

    public void setErrdata_internal_humid(int errdata_internal_humid) {
        this.errdata_internal_humid = errdata_internal_humid;
    }

    public int getErrdata_internal_ilum() {
        return errdata_internal_ilum;
    }

    public void setErrdata_internal_ilum(int errdata_internal_ilum) {
        this.errdata_internal_ilum = errdata_internal_ilum;
    }

    public int getErrdata_vent_relay() {
        return errdata_vent_relay;
    }

    public void setErrdata_vent_relay(int errdata_vent_relay) {
        this.errdata_vent_relay = errdata_vent_relay;
    }

    public int getErrdata_raisetemp_relay() {
        return errdata_raisetemp_relay;
    }

    public void setErrdata_raisetemp_relay(int errdata_raisetemp_relay) {
        this.errdata_raisetemp_relay = errdata_raisetemp_relay;
    }

    public int getErrdata_raisecool_relay() {
        return errdata_raisecool_relay;
    }

    public void setErrdata_raisecool_relay(int errdata_raisecool_relay) {
        this.errdata_raisecool_relay = errdata_raisecool_relay;
    }

    public int getErrdata_dehumidify_relay() {
        return errdata_dehumidify_relay;
    }

    public void setErrdata_dehumidify_relay(int errdata_dehumidify_relay) {
        this.errdata_dehumidify_relay = errdata_dehumidify_relay;
    }

    public int getErrdata_crop_data() {
        return errdata_crop_data;
    }

    public void setErrdata_crop_data(int errdata_crop_data) {
        this.errdata_crop_data = errdata_crop_data;
    }

    public int getErrdata_device_connection() {
        return errdata_device_connection;
    }

    public void setErrdata_device_connection(int errdata_device_connection) {
        this.errdata_device_connection = errdata_device_connection;
    }

    public int getErrdata_network1() {
        return errdata_network1;
    }

    public void setErrdata_network1(int errdata_network1) {
        this.errdata_network1 = errdata_network1;
    }

    public int getErrdata_network2() {
        return errdata_network2;
    }

    public void setErrdata_network2(int errdata_network2) {
        this.errdata_network2 = errdata_network2;
    }

    public int getErrdata_network3() {
        return errdata_network3;
    }

    public void setErrdata_network3(int errdata_network3) {
        this.errdata_network3 = errdata_network3;
    }

    public int getErrdata_network4() {
        return errdata_network4;
    }

    public void setErrdata_network4(int errdata_network4) {
        this.errdata_network4 = errdata_network4;
    }

    public int getSr_set1_co2() {
        return sr_set1_co2;
    }

    public void setSr_set1_co2(int sr_set1_co2) {
        this.sr_set1_co2 = sr_set1_co2;
    }

    public int getSr_set1_temp() {
        return sr_set1_temp;
    }

    public void setSr_set1_temp(int sr_set1_temp) {
        this.sr_set1_temp = sr_set1_temp;
    }

    public int getSr_set1_humidity() {
        return sr_set1_humidity;
    }

    public void setSr_set1_humidity(int sr_set1_humidity) {
        this.sr_set1_humidity = sr_set1_humidity;
    }

    public int getSr_set1_ilum() {
        return sr_set1_ilum;
    }

    public void setSr_set1_ilum(int sr_set1_ilum) {
        this.sr_set1_ilum = sr_set1_ilum;
    }

    public int getSr_set2_co2() {
        return sr_set2_co2;
    }

    public void setSr_set2_co2(int sr_set2_co2) {
        this.sr_set2_co2 = sr_set2_co2;
    }

    public int getSr_set2_temp() {
        return sr_set2_temp;
    }

    public void setSr_set2_temp(int sr_set2_temp) {
        this.sr_set2_temp = sr_set2_temp;
    }

    public int getSr_set2_humidity() {
        return sr_set2_humidity;
    }

    public void setSr_set2_humidity(int sr_set2_humidity) {
        this.sr_set2_humidity = sr_set2_humidity;
    }

    public int getSr_set2_ilum() {
        return sr_set2_ilum;
    }

    public void setSr_set2_ilum(int sr_set2_ilum) {
        this.sr_set2_ilum = sr_set2_ilum;
    }

    public int getSr_set3_co2() {
        return sr_set3_co2;
    }

    public void setSr_set3_co2(int sr_set3_co2) {
        this.sr_set3_co2 = sr_set3_co2;
    }

    public int getSr_set3_temp() {
        return sr_set3_temp;
    }

    public void setSr_set3_temp(int sr_set3_temp) {
        this.sr_set3_temp = sr_set3_temp;
    }

    public int getSr_set3_humidity() {
        return sr_set3_humidity;
    }

    public void setSr_set3_humidity(int sr_set3_humidity) {
        this.sr_set3_humidity = sr_set3_humidity;
    }

    public int getSr_set3_ilum() {
        return sr_set3_ilum;
    }

    public void setSr_set3_ilum(int sr_set3_ilum) {
        this.sr_set3_ilum = sr_set3_ilum;
    }

    public int getSr_set4_co2() {
        return sr_set4_co2;
    }

    public void setSr_set4_co2(int sr_set4_co2) {
        this.sr_set4_co2 = sr_set4_co2;
    }

    public int getSr__set4_temp() {
        return sr__set4_temp;
    }

    public void setSr__set4_temp(int sr__set4_temp) {
        this.sr__set4_temp = sr__set4_temp;
    }

    public int getSr_set4_humidity() {
        return sr_set4_humidity;
    }

    public void setSr_set4_humidity(int sr_set4_humidity) {
        this.sr_set4_humidity = sr_set4_humidity;
    }

    public int getSr_set4_ilum() {
        return sr_set4_ilum;
    }

    public void setSr_set4_ilum(int sr_set4_ilum) {
        this.sr_set4_ilum = sr_set4_ilum;
    }

    public int getSr_val_co2() {
        return sr_val_co2;
    }

    public void setSr_val_co2(int sr_val_co2) {
        this.sr_val_co2 = sr_val_co2;
    }

    public int getSr_val_temp() {
        return sr_val_temp;
    }

    public void setSr_val_temp(int sr_val_temp) {
        this.sr_val_temp = sr_val_temp;
    }

    public int getSr_val_humidity() {
        return sr_val_humidity;
    }

    public void setSr_val_humidity(int sr_val_humidity) {
        this.sr_val_humidity = sr_val_humidity;
    }

    public int getSr_val_ilum() {
        return sr_val_ilum;
    }

    public void setSr_val_ilum(int sr_val_ilum) {
        this.sr_val_ilum = sr_val_ilum;
    }

    public int getControlstat_aggr() {
        return controlstat_aggr;
    }

    public void setControlstat_aggr(int controlstat_aggr) {
        this.controlstat_aggr = controlstat_aggr;
    }

    public int getControlstat_co2_type() {
        return controlstat_co2_type;
    }

    public void setControlstat_co2_type(int controlstat_co2_type) {
        this.controlstat_co2_type = controlstat_co2_type;
    }

    public int getControlstat_co2_ontype() {
        return controlstat_co2_ontype;
    }

    public void setControlstat_co2_ontype(int controlstat_co2_ontype) {
        this.controlstat_co2_ontype = controlstat_co2_ontype;
    }

    public int getControlstat_co2_offtype() {
        return controlstat_co2_offtype;
    }

    public void setControlstat_co2_offtype(int controlstat_co2_offtype) {
        this.controlstat_co2_offtype = controlstat_co2_offtype;
    }

    public int getControlstat_temp_type() {
        return controlstat_temp_type;
    }

    public void setControlstat_temp_type(int controlstat_temp_type) {
        this.controlstat_temp_type = controlstat_temp_type;
    }

    public int getControlstat_temp_ontype() {
        return controlstat_temp_ontype;
    }

    public void setControlstat_temp_ontype(int controlstat_temp_ontype) {
        this.controlstat_temp_ontype = controlstat_temp_ontype;
    }

    public int getControlstat_temp_offtype() {
        return controlstat_temp_offtype;
    }

    public void setControlstat_temp_offtype(int controlstat_temp_offtype) {
        this.controlstat_temp_offtype = controlstat_temp_offtype;
    }

    public int getControlstat_humidity_type() {
        return controlstat_humidity_type;
    }

    public void setControlstat_humidity_type(int controlstat_humidity_type) {
        this.controlstat_humidity_type = controlstat_humidity_type;
    }

    public int getControlstat_humidity_ontype() {
        return controlstat_humidity_ontype;
    }

    public void setControlstat_humidity_ontype(int controlstat_humidity_ontype) {
        this.controlstat_humidity_ontype = controlstat_humidity_ontype;
    }

    public int getControlstat_humidity_offtype() {
        return controlstat_humidity_offtype;
    }

    public void setControlstat_humidity_offtype(int controlstat_humidity_offtype) {
        this.controlstat_humidity_offtype = controlstat_humidity_offtype;
    }

    public int getControlstat_ilum_type() {
        return controlstat_ilum_type;
    }

    public void setControlstat_ilum_type(int controlstat_ilum_type) {
        this.controlstat_ilum_type = controlstat_ilum_type;
    }

    public int getControlstat_ilum_ontype() {
        return controlstat_ilum_ontype;
    }

    public void setControlstat_ilum_ontype(int controlstat_ilum_ontype) {
        this.controlstat_ilum_ontype = controlstat_ilum_ontype;
    }

    public int getControlstat_ilum_offtype() {
        return controlstat_ilum_offtype;
    }

    public void setControlstat_ilum_offtype(int controlstat_ilum_offtype) {
        this.controlstat_ilum_offtype = controlstat_ilum_offtype;
    }

    public int getCo2_value() {
        return co2_value;
    }

    public void setCo2_value(int co2_value) {
        this.co2_value = co2_value;
    }

    public int getTemp_value() {
        return temp_value;
    }

    public void setTemp_value(int temp_value) {
        this.temp_value = temp_value;
    }

    public int getHumidity_value() {
        return humidity_value;
    }

    public void setHumidity_value(int humidity_value) {
        this.humidity_value = humidity_value;
    }

    public int getIlum_value() {
        return ilum_value;
    }

    public void setIlum_value(int ilum_value) {
        this.ilum_value = ilum_value;
    }

    public int getVt515_version() {
        return vt515_version;
    }

    public void setVt515_version(int vt515_version) {
        this.vt515_version = vt515_version;
    }

    public int getLcdorder_run() {
        return lcdorder_run;
    }

    public void setLcdorder_run(int lcdorder_run) {
        this.lcdorder_run = lcdorder_run;
    }

    public int getLcdorder_mode() {
        return lcdorder_mode;
    }

    public void setLcdorder_mode(int lcdorder_mode) {
        this.lcdorder_mode = lcdorder_mode;
    }

    public int getLcdorder_dayage_start() {
        return lcdorder_dayage_start;
    }

    public void setLcdorder_dayage_start(int lcdorder_dayage_start) {
        this.lcdorder_dayage_start = lcdorder_dayage_start;
    }

    public int getChangetype_lcd_setting() {
        return changetype_lcd_setting;
    }

    public void setChangetype_lcd_setting(int changetype_lcd_setting) {
        this.changetype_lcd_setting = changetype_lcd_setting;
    }

    public int getChangetype_lcd_timer() {
        return changetype_lcd_timer;
    }

    public void setChangetype_lcd_timer(int changetype_lcd_timer) {
        this.changetype_lcd_timer = changetype_lcd_timer;
    }

    public int getChangetype_lcd_dayage1() {
        return changetype_lcd_dayage1;
    }

    public void setChangetype_lcd_dayage1(int changetype_lcd_dayage1) {
        this.changetype_lcd_dayage1 = changetype_lcd_dayage1;
    }

    public int getChangetype_lcd_dayage2() {
        return changetype_lcd_dayage2;
    }

    public void setChangetype_lcd_dayage2(int changetype_lcd_dayage2) {
        this.changetype_lcd_dayage2 = changetype_lcd_dayage2;
    }

    public int getChangetype_lcd_dayage3() {
        return changetype_lcd_dayage3;
    }

    public void setChangetype_lcd_dayage3(int changetype_lcd_dayage3) {
        this.changetype_lcd_dayage3 = changetype_lcd_dayage3;
    }

    public int getChangetype_lcd_dayage4() {
        return changetype_lcd_dayage4;
    }

    public void setChangetype_lcd_dayage4(int changetype_lcd_dayage4) {
        this.changetype_lcd_dayage4 = changetype_lcd_dayage4;
    }

    public int getChangetype_lcd_dayage5() {
        return changetype_lcd_dayage5;
    }

    public void setChangetype_lcd_dayage5(int changetype_lcd_dayage5) {
        this.changetype_lcd_dayage5 = changetype_lcd_dayage5;
    }

    public int getChangetype_lcd_dayage6() {
        return changetype_lcd_dayage6;
    }

    public void setChangetype_lcd_dayage6(int changetype_lcd_dayage6) {
        this.changetype_lcd_dayage6 = changetype_lcd_dayage6;
    }

    public int getChangetype_pc_setting() {
        return changetype_pc_setting;
    }

    public void setChangetype_pc_setting(int changetype_pc_setting) {
        this.changetype_pc_setting = changetype_pc_setting;
    }

    public int getChangetype_pc_timer() {
        return changetype_pc_timer;
    }

    public void setChangetype_pc_timer(int changetype_pc_timer) {
        this.changetype_pc_timer = changetype_pc_timer;
    }

    public int getChangetype_pc_dayage1() {
        return changetype_pc_dayage1;
    }

    public void setChangetype_pc_dayage1(int changetype_pc_dayage1) {
        this.changetype_pc_dayage1 = changetype_pc_dayage1;
    }

    public int getChangetype_pc_dayage2() {
        return changetype_pc_dayage2;
    }

    public void setChangetype_pc_dayage2(int changetype_pc_dayage2) {
        this.changetype_pc_dayage2 = changetype_pc_dayage2;
    }

    public int getChangetype_pc_dayage3() {
        return changetype_pc_dayage3;
    }

    public void setChangetype_pc_dayage3(int changetype_pc_dayage3) {
        this.changetype_pc_dayage3 = changetype_pc_dayage3;
    }

    public int getChangetype_pc_dayage4() {
        return changetype_pc_dayage4;
    }

    public void setChangetype_pc_dayage4(int changetype_pc_dayage4) {
        this.changetype_pc_dayage4 = changetype_pc_dayage4;
    }

    public int getChangetype_pc_dayage5() {
        return changetype_pc_dayage5;
    }

    public void setChangetype_pc_dayage5(int changetype_pc_dayage5) {
        this.changetype_pc_dayage5 = changetype_pc_dayage5;
    }

    public int getChangetype_pc_dayage6() {
        return changetype_pc_dayage6;
    }

    public void setChangetype_pc_dayage6(int changetype_pc_dayage6) {
        this.changetype_pc_dayage6 = changetype_pc_dayage6;
    }

    public int getNetworkerr_510to515() {
        return networkerr_510to515;
    }

    public void setNetworkerr_510to515(int networkerr_510to515) {
        this.networkerr_510to515 = networkerr_510to515;
    }

    public int getDayage_low() {
        return dayage_low;
    }

    public void setDayage_low(int dayage_low) {
        this.dayage_low = dayage_low;
    }

    public int getDayage_high() {
        return dayage_high;
    }

    public void setDayage_high(int dayage_high) {
        this.dayage_high = dayage_high;
    }

    public int getReal_sec() {
        return real_sec;
    }

    public void setReal_sec(int real_sec) {
        this.real_sec = real_sec;
    }

    public int getReal_hm() {
        return real_hm;
    }

    public void setReal_hm(int real_hm) {
        this.real_hm = real_hm;
    }

    public int getReal_md() {
        return real_md;
    }

    public void setReal_md(int real_md) {
        this.real_md = real_md;
    }

    public int getDymamic_output_mode() {
        return dymamic_output_mode;
    }

    public void setDymamic_output_mode(int dymamic_output_mode) {
        this.dymamic_output_mode = dymamic_output_mode;
    }

    public int getDymamic_output_inc() {
        return dymamic_output_inc;
    }

    public void setDymamic_output_inc(int dymamic_output_inc) {
        this.dymamic_output_inc = dymamic_output_inc;
    }

    public int getDymamic_output_dec() {
        return dymamic_output_dec;
    }

    public void setDymamic_output_dec(int dymamic_output_dec) {
        this.dymamic_output_dec = dymamic_output_dec;
    }

    public int getDymamic_output_analog() {
        return dymamic_output_analog;
    }

    public void setDymamic_output_analog(int dymamic_output_analog) {
        this.dymamic_output_analog = dymamic_output_analog;
    }

    public int getDymamic_output_valid() {
        return dymamic_output_valid;
    }

    public void setDymamic_output_valid(int dymamic_output_valid) {
        this.dymamic_output_valid = dymamic_output_valid;
    }

    public int getStart_year() {
        return start_year;
    }

    public void setStart_year(int start_year) {
        this.start_year = start_year;
    }

    public int getStart_md() {
        return start_md;
    }

    public void setStart_md(int start_md) {
        this.start_md = start_md;
    }

    public int getStart_hm() {
        return start_hm;
    }

    public void setStart_hm(int start_hm) {
        this.start_hm = start_hm;
    }

    public int getMcnctrl_mv510_aggr() {
        return mcnctrl_mv510_aggr;
    }

    public void setMcnctrl_mv510_aggr(int mcnctrl_mv510_aggr) {
        this.mcnctrl_mv510_aggr = mcnctrl_mv510_aggr;
    }

    public int getMcnctrl_mv510_order_co2() {
        return mcnctrl_mv510_order_co2;
    }

    public void setMcnctrl_mv510_order_co2(int mcnctrl_mv510_order_co2) {
        this.mcnctrl_mv510_order_co2 = mcnctrl_mv510_order_co2;
    }

    public int getMcnctrl_mv510_order_temp() {
        return mcnctrl_mv510_order_temp;
    }

    public void setMcnctrl_mv510_order_temp(int mcnctrl_mv510_order_temp) {
        this.mcnctrl_mv510_order_temp = mcnctrl_mv510_order_temp;
    }

    public int getMcnctrl_mv510_order_humidity() {
        return mcnctrl_mv510_order_humidity;
    }

    public void setMcnctrl_mv510_order_humidity(int mcnctrl_mv510_order_humidity) {
        this.mcnctrl_mv510_order_humidity = mcnctrl_mv510_order_humidity;
    }

    public int getMcnctrl_mv510_order_ilum() {
        return mcnctrl_mv510_order_ilum;
    }

    public void setMcnctrl_mv510_order_ilum(int mcnctrl_mv510_order_ilum) {
        this.mcnctrl_mv510_order_ilum = mcnctrl_mv510_order_ilum;
    }

    public int getMcnctrl_mv510_order_main1() {
        return mcnctrl_mv510_order_main1;
    }

    public void setMcnctrl_mv510_order_main1(int mcnctrl_mv510_order_main1) {
        this.mcnctrl_mv510_order_main1 = mcnctrl_mv510_order_main1;
    }

    public int getMcnctrl_mv510_order_main2() {
        return mcnctrl_mv510_order_main2;
    }

    public void setMcnctrl_mv510_order_main2(int mcnctrl_mv510_order_main2) {
        this.mcnctrl_mv510_order_main2 = mcnctrl_mv510_order_main2;
    }

    public int getMcnctrl_mv510_stat_fan() {
        return mcnctrl_mv510_stat_fan;
    }

    public void setMcnctrl_mv510_stat_fan(int mcnctrl_mv510_stat_fan) {
        this.mcnctrl_mv510_stat_fan = mcnctrl_mv510_stat_fan;
    }

    public int getMcnctrl_mv510_stat_heater() {
        return mcnctrl_mv510_stat_heater;
    }

    public void setMcnctrl_mv510_stat_heater(int mcnctrl_mv510_stat_heater) {
        this.mcnctrl_mv510_stat_heater = mcnctrl_mv510_stat_heater;
    }

    public int getMcnctrl_mv510_stat_freezer() {
        return mcnctrl_mv510_stat_freezer;
    }

    public void setMcnctrl_mv510_stat_freezer(int mcnctrl_mv510_stat_freezer) {
        this.mcnctrl_mv510_stat_freezer = mcnctrl_mv510_stat_freezer;
    }

    public int getMcnctrl_mv510_stat_humidifier() {
        return mcnctrl_mv510_stat_humidifier;
    }

    public void setMcnctrl_mv510_stat_humidifier(int mcnctrl_mv510_stat_humidifier) {
        this.mcnctrl_mv510_stat_humidifier = mcnctrl_mv510_stat_humidifier;
    }

    public int getMcnctrl_mv510_stat_dehumidifier() {
        return mcnctrl_mv510_stat_dehumidifier;
    }

    public void setMcnctrl_mv510_stat_dehumidifier(int mcnctrl_mv510_stat_dehumidifier) {
        this.mcnctrl_mv510_stat_dehumidifier = mcnctrl_mv510_stat_dehumidifier;
    }

    public int getMcnctrl_mv510_stat_ilum() {
        return mcnctrl_mv510_stat_ilum;
    }

    public void setMcnctrl_mv510_stat_ilum(int mcnctrl_mv510_stat_ilum) {
        this.mcnctrl_mv510_stat_ilum = mcnctrl_mv510_stat_ilum;
    }

    public int getMcnctrl_mv510_stat_alarm() {
        return mcnctrl_mv510_stat_alarm;
    }

    public void setMcnctrl_mv510_stat_alarm(int mcnctrl_mv510_stat_alarm) {
        this.mcnctrl_mv510_stat_alarm = mcnctrl_mv510_stat_alarm;
    }

    public int getMcnctrl_mv510_stat_reserve() {
        return mcnctrl_mv510_stat_reserve;
    }

    public void setMcnctrl_mv510_stat_reserve(int mcnctrl_mv510_stat_reserve) {
        this.mcnctrl_mv510_stat_reserve = mcnctrl_mv510_stat_reserve;
    }

    public int getMcnctrl_web_aggr() {
        return mcnctrl_web_aggr;
    }

    public void setMcnctrl_web_aggr(int mcnctrl_web_aggr) {
        this.mcnctrl_web_aggr = mcnctrl_web_aggr;
    }

    public int getMcnctrl_web_order_co2() {
        return mcnctrl_web_order_co2;
    }

    public void setMcnctrl_web_order_co2(int mcnctrl_web_order_co2) {
        this.mcnctrl_web_order_co2 = mcnctrl_web_order_co2;
    }

    public int getMcnctrl_web_order_temp() {
        return mcnctrl_web_order_temp;
    }

    public void setMcnctrl_web_order_temp(int mcnctrl_web_order_temp) {
        this.mcnctrl_web_order_temp = mcnctrl_web_order_temp;
    }

    public int getMcnctrl_web_order_humidity() {
        return mcnctrl_web_order_humidity;
    }

    public void setMcnctrl_web_order_humidity(int mcnctrl_web_order_humidity) {
        this.mcnctrl_web_order_humidity = mcnctrl_web_order_humidity;
    }

    public int getMcnctrl_web_order_ilum() {
        return mcnctrl_web_order_ilum;
    }

    public void setMcnctrl_web_order_ilum(int mcnctrl_web_order_ilum) {
        this.mcnctrl_web_order_ilum = mcnctrl_web_order_ilum;
    }

    public int getMcnctrl_web_order_main1() {
        return mcnctrl_web_order_main1;
    }

    public void setMcnctrl_web_order_main1(int mcnctrl_web_order_main1) {
        this.mcnctrl_web_order_main1 = mcnctrl_web_order_main1;
    }

    public int getMcnctrl_web_order_main2() {
        return mcnctrl_web_order_main2;
    }

    public void setMcnctrl_web_order_main2(int mcnctrl_web_order_main2) {
        this.mcnctrl_web_order_main2 = mcnctrl_web_order_main2;
    }

    public int getMcnctrl_web_stat_fan() {
        return mcnctrl_web_stat_fan;
    }

    public void setMcnctrl_web_stat_fan(int mcnctrl_web_stat_fan) {
        this.mcnctrl_web_stat_fan = mcnctrl_web_stat_fan;
    }

    public int getMcnctrl_web_stat_heater() {
        return mcnctrl_web_stat_heater;
    }

    public void setMcnctrl_web_stat_heater(int mcnctrl_web_stat_heater) {
        this.mcnctrl_web_stat_heater = mcnctrl_web_stat_heater;
    }

    public int getMcnctrl_web_stat_freezer() {
        return mcnctrl_web_stat_freezer;
    }

    public void setMcnctrl_web_stat_freezer(int mcnctrl_web_stat_freezer) {
        this.mcnctrl_web_stat_freezer = mcnctrl_web_stat_freezer;
    }

    public int getMcnctrl_web_stat_humidifier() {
        return mcnctrl_web_stat_humidifier;
    }

    public void setMcnctrl_web_stat_humidifier(int mcnctrl_web_stat_humidifier) {
        this.mcnctrl_web_stat_humidifier = mcnctrl_web_stat_humidifier;
    }

    public int getMcnctrl_web_stat_dehumidifier() {
        return mcnctrl_web_stat_dehumidifier;
    }

    public void setMcnctrl_web_stat_dehumidifier(int mcnctrl_web_stat_dehumidifier) {
        this.mcnctrl_web_stat_dehumidifier = mcnctrl_web_stat_dehumidifier;
    }

    public int getMcnctrl_web_stat_ilum() {
        return mcnctrl_web_stat_ilum;
    }

    public void setMcnctrl_web_stat_ilum(int mcnctrl_web_stat_ilum) {
        this.mcnctrl_web_stat_ilum = mcnctrl_web_stat_ilum;
    }

    public int getMcnctrl_web_stat_alarm() {
        return mcnctrl_web_stat_alarm;
    }

    public void setMcnctrl_web_stat_alarm(int mcnctrl_web_stat_alarm) {
        this.mcnctrl_web_stat_alarm = mcnctrl_web_stat_alarm;
    }

    public int getMcnctrl_web_stat_reserve() {
        return mcnctrl_web_stat_reserve;
    }

    public void setMcnctrl_web_stat_reserve(int mcnctrl_web_stat_reserve) {
        this.mcnctrl_web_stat_reserve = mcnctrl_web_stat_reserve;
    }

    @Override
    public String toString(){
        return "Realtime Data : " + byteSerial.getProcessed();
    }

    public String toJson() throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(this);

        return json;
    }

    @Deprecated
    private RealtimePOJO(){}

    public static RealtimePOJO parse(String json) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        RealtimePOJO realtimePOJO = mapper.readValue(json, RealtimePOJO.class);

        return realtimePOJO;
    }

}
