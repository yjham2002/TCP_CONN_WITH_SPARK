package pojo;

import constants.ConstProtocol;
import models.ByteSerial;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import utils.HexUtil;
import utils.Modbus;
import utils.SohaProtocolUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private String dongCode;

    private int machine_no;

    private int crop_data_num_and_ctrl_aggr;

    private int sensor_quantity_and_selection_aggr;
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

    private int reserve_setting_year;
    private String reserve_setting_md;
    private String reserve_setting_hm;
    private int reserve_setting_cropno;

    private int dynamic_output_type;
    private int dynamic_output_value;

    private int backup_year;
    private String backup_md;
    private String backup_hm;

    private int backup_read_year;
    private String backup_read_md;
    private String backup_read_hm;

    private int growth_start_year;
    private String growth_start_md;
    private String growth_start_hm;

    private int change_date_time;

    private int alert_alarm_aggr;
    private int alert_alarm_internal_co2;
    private int alert_alarm_internal_temp;
    private int alert_alarm_internal_humidity;
    private int alert_alarm_internal_ilum;
    private int alert_alarm_vent_relay;
    private int alert_alarm_heat_relay;
    private int alert_alarm_cool_relay;
    private int alert_alarm_humidify_relay;
    private int alert_alarm_dehumidify_relay;
    private int alert_alarm_ilum_relay;
    private int alert_alarm_rs485;
    private int alert_alarm_vt515;
    private int alert_alarm_vt250_1;
    private int alert_alarm_vt250_2;
    private int alert_alarm_vt250_3;
    private int alert_alarm_vt250_4;

    private List<SettingTailPOJO> settingTails;

    /**
     * 시리얼 바이트로부터 맵핑 및 의미를 구체화하기 위한 생성자
     * @param byteSerial 시리얼 바이트
     */
    public SettingPOJO(ByteSerial byteSerial, int offset, String farmCode, String harvCode){
        this.byteSerial = byteSerial;
        this.farmCode = farmCode;
        this.dongCode = harvCode;

        init(offset);
    }

    private SettingPOJO(){}

    public void initTails(ByteSerial byteSerial, int offset){
        settingTails = new ArrayList<>();

        ByteSerial temp = this.byteSerial;

        setByteSerial(byteSerial);

        int order = 1;
        for(int cursor = offset; cursor <= offset + 24; cursor += 12){
            SettingTailPOJO tail = new SettingTailPOJO(
                    order++,
                    getSumWith2BytesABS(cursor, SUM_MODE_P),
                    getSumWith2BytesABS(cursor + 2, SUM_MODE_TEMP),
                    getSumWith2BytesABS(cursor + 4, SUM_MODE_HUMID),
                    getSumWith2BytesABS(cursor + 6, SUM_MODE_P),
                    getMDorHMWith2BytesABS(cursor + 8, ":"),
                    getMDorHMWith2BytesABS(cursor + 10, ":")
            );

            settingTails.add(tail);
        }

        setByteSerial(temp);
    }

    public static void main(String... args){
        byte[] arr = new byte[]{83, 84, 48, 48, 55, 56, 48, 49, 1, 3, -114, 0, 1, 35, -15, 1, 1, 4, 100, 4, -40, 0, -126, 0, 9, 0, -55, 1, -109, 3, -119, 0, 7, 0, 0, 0, 3, -52, -52, -2, 12, 1, -12, -1, -30, 0, 30, -1, -100, 0, 100, 0, 0, 0, 0, 1, 44, -61, 80, -1, -99, 2, 88, 0, 0, 3, -25, 0, 0, 0, 79, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 0, 0, 0, 5, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 39, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, -1, -1, 56, 117, -82, 13, 10};
        SettingPOJO sp = new SettingPOJO(new ByteSerial(arr), ConstProtocol.RANGE_READ_START, "0078", "01");

        byte[] gen = sp.getBytes();

        System.out.println(Arrays.toString(gen));
        System.out.println(Arrays.toString(arr));

        for(int i = 0; i < arr.length; i++) if(gen[i] != arr[i]) System.out.println(":::: " + i);
    }

    @JsonIgnore
    public byte[] getTailBytes(){
        byte[] check = SohaProtocolUtil.concat(ConstProtocol.STX, this.farmCode.getBytes(), this.dongCode.getBytes());

        byte[] modbusData = SohaProtocolUtil.concat(new byte[]{Byte.parseByte(this.dongCode), 3, (byte)(ConstProtocol.RANGE_SETTING_TAILS.getTail() * 2)});

        for(int i = 0; i < settingTails.size(); i++){
            SettingTailPOJO setTail = settingTails.get(i);
            modbusData = SohaProtocolUtil.concat(
                    modbusData,
                    SohaProtocolUtil.getHexLocation(setTail.getSetting_value_co_2()), SohaProtocolUtil.getHexLocation(setTail.getSetting_value_temp()),
                    SohaProtocolUtil.getHexLocation(setTail.getSetting_value_humid()), SohaProtocolUtil.getHexLocation(setTail.getSetting_value_illum()),
                    getValuePairFromString(setTail.getStart_time()), getValuePairFromString(setTail.getEnd_time()));
        }

        Modbus modbus = new Modbus();

        byte[] crc = modbus.fn_makeCRC16(modbusData);

        byte[] fin = SohaProtocolUtil.concat(check, modbusData, crc);

        byte chk = HexUtil.checkSum(fin);
        byte[] retVal = SohaProtocolUtil.concat(fin, new byte[]{chk}, ConstProtocol.ETX);

        return retVal;
    }

    @JsonIgnore
    public byte[] getBytes(){
        byte[] check = SohaProtocolUtil.concat(ConstProtocol.STX, this.farmCode.getBytes(), this.dongCode.getBytes());

        int bitAggr_h =
                getBitAggregation(
                        alert_alarm_vt250_4,
                        alert_alarm_vt250_3,
                        alert_alarm_vt250_2,
                        alert_alarm_vt250_1,
                        alert_alarm_vt515,
                        alert_alarm_rs485,
                        alert_alarm_ilum_relay,
                        alert_alarm_dehumidify_relay
                );

        int bitAggr_t =
                getBitAggregation(
                        alert_alarm_humidify_relay,
                        alert_alarm_cool_relay,
                        alert_alarm_heat_relay,
                        alert_alarm_vent_relay,
                        alert_alarm_internal_ilum,
                        alert_alarm_internal_humidity,
                        alert_alarm_internal_temp,
                        alert_alarm_internal_co2
                );

        int bitAggr_sr =
                getBitAggregation(
                        0,
                        0,
                        0,
                        0,
                        sensor_selected_4,
                        sensor_selected_3,
                        sensor_selected_2,
                        sensor_selected_1
                );

        int bitAggr_ctrl_1 =
                getBitAggregation(
                        cthi_ctrl_stat_illum_offtype,
                        cthi_ctrl_stat_illum_ontype,
                        getBitRhsFromDual(cthi_ctrl_stat_illum_ctrl),
                        getBitLhsFromDual(cthi_ctrl_stat_illum_ctrl),
                        cthi_ctrl_stat_humid_offtype,
                        cthi_ctrl_stat_humid_ontype,
                        getBitRhsFromDual(cthi_ctrl_stat_humid_ctrl),
                        getBitLhsFromDual(cthi_ctrl_stat_humid_ctrl)

                );
        int bitAggr_ctrl_2 =
                getBitAggregation(
                        cthi_ctrl_stat_temp_offtype,
                        cthi_ctrl_stat_temp_ontype,
                        getBitRhsFromDual(cthi_ctrl_stat_temp_ctrl),
                        getBitLhsFromDual(cthi_ctrl_stat_temp_ctrl),
                        cthi_ctrl_stat_co2_offtype,
                        cthi_ctrl_stat_co2_ontype,
                        getBitRhsFromDual(cthi_ctrl_stat_co2_ctrl),
                        getBitLhsFromDual(cthi_ctrl_stat_co2_ctrl)
                );

        int bitAggr_alt =
                getBitAggregation(
                        this.alert_alarm_time_select_timeset,
                        getBitRhsFromDual(this.alert_alarm_time_select_lamp_unit),
                        getBitLhsFromDual(this.alert_alarm_time_select_lamp_unit),
                        this.alert_alarm_time_select_timer,
                        this.alert_alarm_time_select_auto
                );

        byte[] modbusData =
                SohaProtocolUtil.concat(
                        new byte[]{Byte.parseByte(this.dongCode), 3, (byte)(ConstProtocol.RANGE_SETTING.getTail() * 2)}, SohaProtocolUtil.getHexLocation(this.machine_no),

                        SohaProtocolUtil.getHexLocation(bitAggr_alt),
                        new byte[]{(byte)this.sensor_quantity, (byte)bitAggr_sr},

                        SohaProtocolUtil.getHexLocation(this.singular_ctrl_setting_co2), SohaProtocolUtil.getHexLocation(this.singular_ctrl_setting_temp),
                        SohaProtocolUtil.getHexLocation(this.singular_ctrl_setting_humid),
                        SohaProtocolUtil.getHexLocation(this.singular_ctrl_setting_illum),
                        getAggregation(this.relay_output_setting_heat, this.relay_output_setting_co2),
                        getAggregation(this.relay_output_setting_humidify, this.relay_output_setting_cool),
                        getAggregation(this.relay_output_setting_illum, this.relay_output_setting_dehumidify),
                        getAggregation(this.relay_output_setting_reserve, this.relay_output_setting_alarm),

                        getAggregation(this.dry_condition_setting_ctrl, this.dry_condition_setting_humidity),
                        SohaProtocolUtil.getHexLocation(this.alert_alarm_time_select_aggr),
                        new byte[]{(byte)bitAggr_ctrl_1, (byte)bitAggr_ctrl_2},

                        SohaProtocolUtil.getHexLocation(this.calm_threshold_co2_low), SohaProtocolUtil.getHexLocation(this.calm_threshold_co2_high),
                        SohaProtocolUtil.getHexLocation(this.calm_threshold_temp_low), SohaProtocolUtil.getHexLocation(this.calm_threshold_temp_high),
                        SohaProtocolUtil.getHexLocation(this.calm_threshold_humid_low), SohaProtocolUtil.getHexLocation(this.calm_threshold_humid_high),
                        SohaProtocolUtil.getHexLocation(this.calm_threshold_illum_low), SohaProtocolUtil.getHexLocation(this.calm_threshold_illum_high),

                        SohaProtocolUtil.getHexLocation(this.setting_range_co2_min),
                        SohaProtocolUtil.getHexLocation(this.setting_range_co2_max), SohaProtocolUtil.getHexLocation(this.setting_range_temp_min),
                        SohaProtocolUtil.getHexLocation(this.setting_range_temp_max), SohaProtocolUtil.getHexLocation(this.setting_range_humid_min),
                        SohaProtocolUtil.getHexLocation(this.setting_range_humid_max), SohaProtocolUtil.getHexLocation(this.setting_range_illum_min),
                        SohaProtocolUtil.getHexLocation(this.setting_range_illum_max), SohaProtocolUtil.getHexLocation(this.sr_revision_co2_01),
                        SohaProtocolUtil.getHexLocation(this.sr_revision_temp_01), SohaProtocolUtil.getHexLocation(this.sr_revision_humid_01),
                        SohaProtocolUtil.getHexLocation(this.sr_revision_illum_01), SohaProtocolUtil.getHexLocation(this.sr_revision_co2_02),
                        SohaProtocolUtil.getHexLocation(this.sr_revision_temp_02), SohaProtocolUtil.getHexLocation(this.sr_revision_humid_02),
                        SohaProtocolUtil.getHexLocation(this.sr_revision_illum_02), SohaProtocolUtil.getHexLocation(this.sr_revision_co2_03),
                        SohaProtocolUtil.getHexLocation(this.sr_revision_temp_03), SohaProtocolUtil.getHexLocation(this.sr_revision_humid_03),
                        SohaProtocolUtil.getHexLocation(this.sr_revision_illum_03), SohaProtocolUtil.getHexLocation(this.sr_revision_co2_04),

                        SohaProtocolUtil.getHexLocation(this.sr_revision_temp_04), SohaProtocolUtil.getHexLocation(this.sr_revision_humid_04),
                        SohaProtocolUtil.getHexLocation(this.sr_revision_illum_04), SohaProtocolUtil.getHexLocation(this.setting_onoff_range_co2),
                        SohaProtocolUtil.getHexLocation(this.setting_onoff_range_co2_revision), SohaProtocolUtil.getHexLocation(this.setting_onoff_range_temp),
                        SohaProtocolUtil.getHexLocation(this.setting_onoff_range_temp_revision), SohaProtocolUtil.getHexLocation(this.setting_onoff_range_humid),
                        SohaProtocolUtil.getHexLocation(this.setting_onoff_range_humid_revision), SohaProtocolUtil.getHexLocation(this.setting_onoff_range_illum),
                        SohaProtocolUtil.getHexLocation(this.setting_onoff_range_illum_revision),

                        SohaProtocolUtil.getHexLocation(this.reserve_setting_year), getValuePairFromString(this.reserve_setting_md), getValuePairFromString(this.reserve_setting_hm),
                        SohaProtocolUtil.getHexLocation(this.reserve_setting_cropno),
                        SohaProtocolUtil.getHexLocation(this.dynamic_output_type),
                        SohaProtocolUtil.getHexLocation(this.dynamic_output_value),


                        SohaProtocolUtil.getHexLocation(this.backup_year), getValuePairFromString(this.backup_md), getValuePairFromString(this.backup_hm),
                        SohaProtocolUtil.getHexLocation(this.backup_read_year), getValuePairFromString(this.backup_read_md), getValuePairFromString(this.backup_read_hm),

                        SohaProtocolUtil.getHexLocation(this.growth_start_year),

                        getValuePairFromString(this.growth_start_md),
                        getValuePairFromString(this.growth_start_hm),
                        SohaProtocolUtil.getHexLocation(this.change_date_time),

                        new byte[]{(byte)bitAggr_h, (byte)bitAggr_t}
        );

        Modbus modbus = new Modbus();

        byte[] crc = modbus.fn_makeCRC16(modbusData);

        byte[] fin = SohaProtocolUtil.concat(check, modbusData, crc);

        byte chk = HexUtil.checkSum(fin);
        byte[] retVal = SohaProtocolUtil.concat(fin, new byte[]{chk}, ConstProtocol.ETX);

        return retVal;
    }

    public List<SettingTailPOJO> getSettingTails() {
        return settingTails;
    }

    public void setSettingTails(List<SettingTailPOJO> settingTails) {
        this.settingTails = settingTails;
    }

    public void init(int offset){
        this.machine_no = getSumWith2BytesABS(offset, SUM_MODE_P);
        this.crop_data_num_and_ctrl_aggr = getSumWith2BytesABS(offset + 2, SUM_MODE_P);
        this.sensor_quantity_and_selection_aggr = getSumWith2BytesABS(offset + 4, SUM_MODE_P);
        this.sensor_quantity = getSingleByteABS(offset + 4);
        this.sensor_selected_1 = getBooleanValueFrom2ByteABS(offset + 5, 8);
        this.sensor_selected_2 = getBooleanValueFrom2ByteABS(offset + 5, 9);
        this.sensor_selected_3 = getBooleanValueFrom2ByteABS(offset + 5, 10);
        this.sensor_selected_4 = getBooleanValueFrom2ByteABS(offset + 5, 11);
        this.singular_ctrl_setting_co2 = getSumWith2BytesABS(offset + 6, SUM_MODE_P);
        this.singular_ctrl_setting_temp = getSumWith2BytesABS(offset + 8, SUM_MODE_TEMP);
        this.singular_ctrl_setting_humid = getSumWith2BytesABS(offset + 10, SUM_MODE_HUMID);
        this.singular_ctrl_setting_illum = getSumWith2BytesABS(offset + 12, SUM_MODE_P);
        this.relay_output_setting_co2 = getRhsFromDualABS(offset + 14);
        this.relay_output_setting_heat = getLhsFromDualABS(offset + 14);
        this.relay_output_setting_cool = getRhsFromDualABS(offset + 16);
        this.relay_output_setting_humidify = getLhsFromDualABS(offset + 16);
        this.relay_output_setting_dehumidify = getRhsFromDualABS(offset + 18);
        this.relay_output_setting_illum = getLhsFromDualABS(offset + 18);
        this.relay_output_setting_alarm = getRhsFromDualABS(offset + 20);
        this.relay_output_setting_reserve = getLhsFromDualABS(offset + 20);
        this.dry_condition_setting_aggr = getSumWith2BytesABS(offset + 22, SUM_MODE_P);
        this.dry_condition_setting_ctrl = getLhsFromDualABS(offset + 22);
        this.dry_condition_setting_humidity = getRhsFromDualABS(offset + 22);
        this.alert_alarm_time_select_aggr = getSumWith2BytesABS(offset + 24, SUM_MODE_P);
        this.alert_alarm_time_select_auto = getBooleanValueFrom2ByteABS(offset + 24, 0);
        this.alert_alarm_time_select_timer = getBooleanValueFrom2ByteABS(offset + 24, 1);
        this.alert_alarm_time_select_lamp_unit = toDecimalFromBinaryValueABS(offset + 24, 2, 2);
        this.alert_alarm_time_select_timeset = getBooleanValueFrom2ByteABS(offset + 24, 4);
        this.cthi_ctrl_stat_aggr = getSumWith2BytesABS(offset + 26, SUM_MODE_P);
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
        this.calm_threshold_co2_low = getSumWith2BytesABS(offset + 28, SUM_MODE_REV);
        this.calm_threshold_co2_high = getSumWith2BytesABS(offset + 30, SUM_MODE_REV);
        this.calm_threshold_temp_low = getSumWith2BytesABS(offset + 32, SUM_MODE_TEMP);
        this.calm_threshold_temp_high = getSumWith2BytesABS(offset + 34, SUM_MODE_TEMP);
        this.calm_threshold_humid_low = getSumWith2BytesABS(offset + 36, SUM_MODE_HUMID);
        this.calm_threshold_humid_high = getSumWith2BytesABS(offset + 38, SUM_MODE_HUMID);
        this.calm_threshold_illum_low = getSumWith2BytesABS(offset + 40, SUM_MODE_REV);
        this.calm_threshold_illum_high = getSumWith2BytesABS(offset + 42, SUM_MODE_REV);
        this.setting_range_co2_min = getSumWith2BytesABS(offset + 44, SUM_MODE_P);
        this.setting_range_co2_max = getSumWith2BytesABS(offset + 46, SUM_MODE_P);
        this.setting_range_temp_min = getSumWith2BytesABS(offset + 48, SUM_MODE_TEMP);
        this.setting_range_temp_max = getSumWith2BytesABS(offset + 50, SUM_MODE_TEMP);
        this.setting_range_humid_min = getSumWith2BytesABS(offset + 52, SUM_MODE_HUMID);
        this.setting_range_humid_max = getSumWith2BytesABS(offset + 54, SUM_MODE_HUMID);
        this.setting_range_illum_min = getSumWith2BytesABS(offset + 56, SUM_MODE_P);
        this.setting_range_illum_max = getSumWith2BytesABS(offset + 58, SUM_MODE_P);
        this.sr_revision_co2_01 = getSumWith2BytesABS(offset + 60, SUM_MODE_REV);
        this.sr_revision_temp_01 = getSumWith2BytesABS(offset + 62, SUM_MODE_TEMP);
        this.sr_revision_humid_01 = getSumWith2BytesABS(offset + 64, SUM_MODE_HUMID);
        this.sr_revision_illum_01 = getSumWith2BytesABS(offset + 66, SUM_MODE_REV);
        this.sr_revision_co2_02 = getSumWith2BytesABS(offset + 68, SUM_MODE_REV);
        this.sr_revision_temp_02 = getSumWith2BytesABS(offset + 70, SUM_MODE_TEMP);
        this.sr_revision_humid_02 = getSumWith2BytesABS(offset + 72, SUM_MODE_HUMID);
        this.sr_revision_illum_02 = getSumWith2BytesABS(offset + 74, SUM_MODE_REV);
        this.sr_revision_co2_03 = getSumWith2BytesABS(offset + 76, SUM_MODE_REV);
        this.sr_revision_temp_03 = getSumWith2BytesABS(offset + 78, SUM_MODE_TEMP);
        this.sr_revision_humid_03 = getSumWith2BytesABS(offset + 80, SUM_MODE_HUMID);
        this.sr_revision_illum_03 = getSumWith2BytesABS(offset + 82, SUM_MODE_REV);
        this.sr_revision_co2_04 = getSumWith2BytesABS(offset + 84, SUM_MODE_REV);
        this.sr_revision_temp_04 = getSumWith2BytesABS(offset + 86, SUM_MODE_TEMP);
        this.sr_revision_humid_04 = getSumWith2BytesABS(offset + 88, SUM_MODE_HUMID);
        this.sr_revision_illum_04 = getSumWith2BytesABS(offset + 90, SUM_MODE_REV);
        this.setting_onoff_range_co2 = getSumWith2BytesABS(offset + 92, SUM_MODE_P);
        this.setting_onoff_range_co2_revision = getSumWith2BytesABS(offset + 94, SUM_MODE_P);
        this.setting_onoff_range_temp = getSumWith2BytesABS(offset + 96, SUM_MODE_TEMP);
        this.setting_onoff_range_temp_revision = getSumWith2BytesABS(offset + 98, SUM_MODE_TEMP);
        this.setting_onoff_range_humid = getSumWith2BytesABS(offset + 100, SUM_MODE_HUMID);
        this.setting_onoff_range_humid_revision = getSumWith2BytesABS(offset + 102, SUM_MODE_HUMID);
        this.setting_onoff_range_illum = getSumWith2BytesABS(offset + 104, SUM_MODE_P);
        this.setting_onoff_range_illum_revision = getSumWith2BytesABS(offset + 106, SUM_MODE_P);
        this.reserve_setting_year = getSumWith2BytesABS(offset + 108, SUM_MODE_P);
        this.reserve_setting_md = getMDorHMWith2BytesABS(offset + 110, "-");
        this.reserve_setting_hm = getMDorHMWith2BytesABS(offset + 112, ":");
        this.reserve_setting_cropno = getSumWith2BytesABS(offset + 114, SUM_MODE_P);
        this.dynamic_output_type = getSumWith2BytesABS(offset + 116, SUM_MODE_P);
        this.dynamic_output_value = getSumWith2BytesABS(offset + 118, SUM_MODE_P);

        this.backup_year = getSumWith2BytesABS(offset + 120, SUM_MODE_P);
        this.backup_md = getMDorHMWith2BytesABS(offset + 122, "-");
        this.backup_hm = getMDorHMWith2BytesABS(offset + 124, ":");

        this.backup_read_year = getSumWith2BytesABS(offset + 126, SUM_MODE_P);
        this.backup_read_md = getMDorHMWith2BytesABS(offset + 128, "-");
        this.backup_read_hm = getMDorHMWith2BytesABS(offset + 130, ":");

        this.growth_start_year = getSumWith2BytesABS(offset + 132, SUM_MODE_P);
        this.growth_start_md = getMDorHMWith2BytesABS(offset + 134, "-");
        this.growth_start_hm = getMDorHMWith2BytesABS(offset + 136, ":");
        this.change_date_time = getSumWith2BytesABS(offset + 138, SUM_MODE_P);

        this.alert_alarm_aggr = getSumWith2BytesABS(offset + 140, SUM_MODE_P);
        this.alert_alarm_internal_co2 = getBooleanValueFrom2ByteABS(offset + 140, 0);
        this.alert_alarm_internal_temp = getBooleanValueFrom2ByteABS(offset + 140, 1);
        this.alert_alarm_internal_humidity = getBooleanValueFrom2ByteABS(offset + 140, 2);
        this.alert_alarm_internal_ilum = getBooleanValueFrom2ByteABS(offset + 140, 3);
        this.alert_alarm_vent_relay = getBooleanValueFrom2ByteABS(offset + 140, 4);
        this.alert_alarm_heat_relay = getBooleanValueFrom2ByteABS(offset + 140, 5);
        this.alert_alarm_cool_relay = getBooleanValueFrom2ByteABS(offset + 140, 6);
        this.alert_alarm_humidify_relay = getBooleanValueFrom2ByteABS(offset + 140, 7);
        this.alert_alarm_dehumidify_relay = getBooleanValueFrom2ByteABS(offset + 140, 8);
        this.alert_alarm_ilum_relay = getBooleanValueFrom2ByteABS(offset + 140, 9);
        this.alert_alarm_rs485 = getBooleanValueFrom2ByteABS(offset + 140, 10);
        this.alert_alarm_vt515 = getBooleanValueFrom2ByteABS(offset + 140, 11);
        this.alert_alarm_vt250_1 = getBooleanValueFrom2ByteABS(offset + 140, 12);
        this.alert_alarm_vt250_2 = getBooleanValueFrom2ByteABS(offset + 140, 13);
        this.alert_alarm_vt250_3 = getBooleanValueFrom2ByteABS(offset + 140, 14);
        this.alert_alarm_vt250_4 = getBooleanValueFrom2ByteABS(offset + 140, 15);
    }

    public int getBackup_year() {
        return backup_year;
    }

    public void setBackup_year(int backup_year) {
        this.backup_year = backup_year;
    }

    public String getBackup_md() {
        return backup_md;
    }

    public void setBackup_md(String backup_md) {
        this.backup_md = backup_md;
    }

    public String getBackup_hm() {
        return backup_hm;
    }

    public void setBackup_hm(String backup_hm) {
        this.backup_hm = backup_hm;
    }

    public int getBackup_read_year() {
        return backup_read_year;
    }

    public void setBackup_read_year(int backup_read_year) {
        this.backup_read_year = backup_read_year;
    }

    public String getBackup_read_md() {
        return backup_read_md;
    }

    public void setBackup_read_md(String backup_read_md) {
        this.backup_read_md = backup_read_md;
    }

    public String getBackup_read_hm() {
        return backup_read_hm;
    }

    public void setBackup_read_hm(String backup_read_hm) {
        this.backup_read_hm = backup_read_hm;
    }

    public int getSensor_quantity_and_selection_aggr() {
        return sensor_quantity_and_selection_aggr;
    }

    public void setSensor_quantity_and_selection_aggr(int sensor_quantity_and_selection_aggr) {
        this.sensor_quantity_and_selection_aggr = sensor_quantity_and_selection_aggr;
    }

    public int getReserve_setting_year() {
        return reserve_setting_year;
    }

    public void setReserve_setting_year(int reserve_setting_year) {
        this.reserve_setting_year = reserve_setting_year;
    }

    public String getReserve_setting_md() {
        return reserve_setting_md;
    }

    public void setReserve_setting_md(String reserve_setting_md) {
        this.reserve_setting_md = reserve_setting_md;
    }

    public String getReserve_setting_hm() {
        return reserve_setting_hm;
    }

    public void setReserve_setting_hm(String reserve_setting_hm) {
        this.reserve_setting_hm = reserve_setting_hm;
    }

    public int getReserve_setting_cropno() {
        return reserve_setting_cropno;
    }

    public void setReserve_setting_cropno(int reserve_setting_cropno) {
        this.reserve_setting_cropno = reserve_setting_cropno;
    }

    public int getDynamic_output_type() {
        return dynamic_output_type;
    }

    public void setDynamic_output_type(int dynamic_output_type) {
        this.dynamic_output_type = dynamic_output_type;
    }

    public int getDynamic_output_value() {
        return dynamic_output_value;
    }

    public void setDynamic_output_value(int dynamic_output_value) {
        this.dynamic_output_value = dynamic_output_value;
    }

    public int getGrowth_start_year() {
        return growth_start_year;
    }

    public void setGrowth_start_year(int growth_start_year) {
        this.growth_start_year = growth_start_year;
    }

    public String getGrowth_start_md() {
        return growth_start_md;
    }

    public void setGrowth_start_md(String growth_start_md) {
        this.growth_start_md = growth_start_md;
    }

    public String getGrowth_start_hm() {
        return growth_start_hm;
    }

    public void setGrowth_start_hm(String growth_start_hm) {
        this.growth_start_hm = growth_start_hm;
    }

    public int getChange_date_time() {
        return change_date_time;
    }

    public void setChange_date_time(int change_date_time) {
        this.change_date_time = change_date_time;
    }

    public int getAlert_alarm_aggr() {
        return alert_alarm_aggr;
    }

    public void setAlert_alarm_aggr(int alert_alarm_aggr) {
        this.alert_alarm_aggr = alert_alarm_aggr;
    }

    public int getAlert_alarm_internal_co2() {
        return alert_alarm_internal_co2;
    }

    public void setAlert_alarm_internal_co2(int alert_alarm_internal_co2) {
        this.alert_alarm_internal_co2 = alert_alarm_internal_co2;
    }

    public int getAlert_alarm_internal_temp() {
        return alert_alarm_internal_temp;
    }

    public void setAlert_alarm_internal_temp(int alert_alarm_internal_temp) {
        this.alert_alarm_internal_temp = alert_alarm_internal_temp;
    }

    public int getAlert_alarm_internal_humidity() {
        return alert_alarm_internal_humidity;
    }

    public void setAlert_alarm_internal_humidity(int alert_alarm_internal_humidity) {
        this.alert_alarm_internal_humidity = alert_alarm_internal_humidity;
    }

    public int getAlert_alarm_internal_ilum() {
        return alert_alarm_internal_ilum;
    }

    public void setAlert_alarm_internal_ilum(int alert_alarm_internal_ilum) {
        this.alert_alarm_internal_ilum = alert_alarm_internal_ilum;
    }

    public int getAlert_alarm_vent_relay() {
        return alert_alarm_vent_relay;
    }

    public void setAlert_alarm_vent_relay(int alert_alarm_vent_relay) {
        this.alert_alarm_vent_relay = alert_alarm_vent_relay;
    }

    public int getAlert_alarm_heat_relay() {
        return alert_alarm_heat_relay;
    }

    public void setAlert_alarm_heat_relay(int alert_alarm_heat_relay) {
        this.alert_alarm_heat_relay = alert_alarm_heat_relay;
    }

    public int getAlert_alarm_cool_relay() {
        return alert_alarm_cool_relay;
    }

    public void setAlert_alarm_cool_relay(int alert_alarm_cool_relay) {
        this.alert_alarm_cool_relay = alert_alarm_cool_relay;
    }

    public int getAlert_alarm_humidify_relay() {
        return alert_alarm_humidify_relay;
    }

    public void setAlert_alarm_humidify_relay(int alert_alarm_humidify_relay) {
        this.alert_alarm_humidify_relay = alert_alarm_humidify_relay;
    }

    public int getAlert_alarm_dehumidify_relay() {
        return alert_alarm_dehumidify_relay;
    }

    public void setAlert_alarm_dehumidify_relay(int alert_alarm_dehumidify_relay) {
        this.alert_alarm_dehumidify_relay = alert_alarm_dehumidify_relay;
    }

    public int getAlert_alarm_ilum_relay() {
        return alert_alarm_ilum_relay;
    }

    public void setAlert_alarm_ilum_relay(int alert_alarm_ilum_relay) {
        this.alert_alarm_ilum_relay = alert_alarm_ilum_relay;
    }

    public int getAlert_alarm_rs485() {
        return alert_alarm_rs485;
    }

    public void setAlert_alarm_rs485(int alert_alarm_rs485) {
        this.alert_alarm_rs485 = alert_alarm_rs485;
    }

    public int getAlert_alarm_vt515() {
        return alert_alarm_vt515;
    }

    public void setAlert_alarm_vt515(int alert_alarm_vt515) {
        this.alert_alarm_vt515 = alert_alarm_vt515;
    }

    public int getAlert_alarm_vt250_1() {
        return alert_alarm_vt250_1;
    }

    public void setAlert_alarm_vt250_1(int alert_alarm_vt250_1) {
        this.alert_alarm_vt250_1 = alert_alarm_vt250_1;
    }

    public int getAlert_alarm_vt250_2() {
        return alert_alarm_vt250_2;
    }

    public void setAlert_alarm_vt250_2(int alert_alarm_vt250_2) {
        this.alert_alarm_vt250_2 = alert_alarm_vt250_2;
    }

    public int getAlert_alarm_vt250_3() {
        return alert_alarm_vt250_3;
    }

    public void setAlert_alarm_vt250_3(int alert_alarm_vt250_3) {
        this.alert_alarm_vt250_3 = alert_alarm_vt250_3;
    }

    public int getAlert_alarm_vt250_4() {
        return alert_alarm_vt250_4;
    }

    public void setAlert_alarm_vt250_4(int alert_alarm_vt250_4) {
        this.alert_alarm_vt250_4 = alert_alarm_vt250_4;
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

    public int getMachine_no() {
        return machine_no;
    }

    public void setMachine_no(int machine_no) {
        this.machine_no = machine_no;
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

    /**
     * SQL 문법으로 해당 인스턴스의 삽입 구문을 추출
     * @return MySQL 쿼리
     */
    @JsonIgnore
    public String getInsertSQL(){
        if(settingTails == null || settingTails.size() < 3) return "SELECT -1";

        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(this);
        }catch (IOException e){
            json = "";
        }

        String sql = "insert into `sohatechfarmdb`.`tblSettingData`\n" +
                "            (" +
                "             `farmCode`,\n" +
                "             `dongCode`,\n" +
                "             `crop_data_num_and_ctrl_aggr`,\n" +
                "             `sensor_quantity`,\n" +
                "             `sensor_selected_1`,\n" +
                "             `sensor_selected_2`,\n" +
                "             `sensor_selected_3`,\n" +
                "             `sensor_selected_4`,\n" +
                "             `singular_ctrl_setting_co2`,\n" +
                "             `singular_ctrl_setting_temp`,\n" +
                "             `singular_ctrl_setting_humid`,\n" +
                "             `singular_ctrl_setting_illum`,\n" +
                "             `relay_output_setting_co2`,\n" +
                "             `relay_output_setting_heat`,\n" +
                "             `relay_output_setting_cool`,\n" +
                "             `relay_output_setting_humidify`,\n" +
                "             `relay_output_setting_dehumidify`,\n" +
                "             `relay_output_setting_illum`,\n" +
                "             `relay_output_setting_alarm`,\n" +
                "             `relay_output_setting_reserve`,\n" +
                "             `dry_condition_setting_aggr`,\n" +
                "             `dry_condition_setting_ctrl`,\n" +
                "             `dry_condition_setting_humidity`,\n" +
                "             `alert_alarm_time_select_aggr`,\n" +
                "             `alert_alarm_time_select_auto`,\n" +
                "             `alert_alarm_time_select_timer`,\n" +
                "             `alert_alarm_time_select_lamp_unit`,\n" +
                "             `alert_alarm_time_select_timeset`,\n" +
                "             `cthi_ctrl_stat_aggr`,\n" +
                "             `cthi_ctrl_stat_co2_ctrl`,\n" +
                "             `cthi_ctrl_stat_co2_ontype`,\n" +
                "             `cthi_ctrl_stat_co2_offtype`,\n" +
                "             `cthi_ctrl_stat_temp_ctrl`,\n" +
                "             `cthi_ctrl_stat_temp_ontype`,\n" +
                "             `cthi_ctrl_stat_temp_offtype`,\n" +
                "             `cthi_ctrl_stat_humid_ctrl`,\n" +
                "             `cthi_ctrl_stat_humid_ontype`,\n" +
                "             `cthi_ctrl_stat_humid_offtype`,\n" +
                "             `cthi_ctrl_stat_illum_ctrl`,\n" +
                "             `cthi_ctrl_stat_illum_ontype`,\n" +
                "             `cthi_ctrl_stat_illum_offtype`,\n" +
                "             `calm_threshold_co2_low`,\n" +
                "             `calm_threshold_co2_high`,\n" +
                "             `calm_threshold_temp_low`,\n" +
                "             `calm_threshold_temp_high`,\n" +
                "             `calm_threshold_humid_low`,\n" +
                "             `calm_threshold_humid_high`,\n" +
                "             `calm_threshold_illum_low`,\n" +
                "             `calm_threshold_illum_high`,\n" +
                "             `setting_range_co2_min`,\n" +
                "             `setting_range_co2_max`,\n" +
                "             `setting_range_temp_min`,\n" +
                "             `setting_range_temp_max`,\n" +
                "             `setting_range_humid_min`,\n" +
                "             `setting_range_humid_max`,\n" +
                "             `setting_range_illum_min`,\n" +
                "             `setting_range_illum_max`,\n" +
                "             `sr_revision_co2_01`,\n" +
                "             `sr_revision_temp_01`,\n" +
                "             `sr_revision_humid_01`,\n" +
                "             `sr_revision_illum_01`,\n" +
                "             `sr_revision_co2_02`,\n" +
                "             `sr_revision_temp_02`,\n" +
                "             `sr_revision_humid_02`,\n" +
                "             `sr_revision_illum_02`,\n" +
                "             `sr_revision_co2_03`,\n" +
                "             `sr_revision_temp_03`,\n" +
                "             `sr_revision_humid_03`,\n" +
                "             `sr_revision_illum_03`,\n" +
                "             `sr_revision_co2_04`,\n" +
                "             `sr_revision_temp_04`,\n" +
                "             `sr_revision_humid_04`,\n" +
                "             `sr_revision_illum_04`,\n" +
                "             `setting_onoff_range_co2`,\n" +
                "             `setting_onoff_range_co2_revision`,\n" +
                "             `setting_onoff_range_temp`,\n" +
                "             `setting_onoff_range_temp_revision`,\n" +
                "             `setting_onoff_range_humid`,\n" +
                "             `setting_onoff_range_humid_revision`,\n" +
                "             `setting_onoff_range_illum`,\n" +
                "             `setting_onoff_range_illum_revision`,\n" +
                "             `reserve_setting_year`,\n" +
                "             `reserve_setting_md`,\n" +
                "             `reserve_setting_hm`,\n" +
                "             `reserve_setting_cropno`,\n" +
                "             `dynamic_output_type`,\n" +
                "             `dynamic_output_value`,\n" +
                "             `backup_year`,\n" +
                "             `backup_md`,\n" +
                "             `backup_hm`,\n" +
                "             `backup_read_year`,\n" +
                "             `backup_read_md`,\n" +
                "             `backup_read_hm`,\n" +
                "             `growth_start_year`,\n" +
                "             `growth_start_md`,\n" +
                "             `growth_start_hm`,\n" +
                "             `change_date_time`,\n" +
                "             `alert_alarm_aggr`,\n" +
                "             `alert_alarm_internal_co2`,\n" +
                "             `alert_alarm_internal_temp`,\n" +
                "             `alert_alarm_internal_humidity`,\n" +
                "             `alert_alarm_internal_ilum`,\n" +
                "             `alert_alarm_vent_relay`,\n" +
                "             `alert_alarm_heat_relay`,\n" +
                "             `alert_alarm_cool_relay`,\n" +
                "             `alert_alarm_humidify_relay`,\n" +
                "             `alert_alarm_dehumidify_relay`,\n" +
                "             `alert_alarm_ilum_relay`,\n" +
                "             `alert_alarm_rs485`,\n" +
                "             `alert_alarm_vt515`,\n" +
                "             `alert_alarm_vt250_1`,\n" +
                "             `alert_alarm_vt250_2`,\n" +
                "             `alert_alarm_vt250_3`,\n" +
                "             `alert_alarm_vt250_4`,\n" +
                "             `start_time_1`,\n" +
                "             `end_time_1`,\n" +
                "             `setting_value_co_2_1`,\n" +
                "             `setting_value_temp_1`,\n" +
                "             `setting_value_humid_1`,\n" +
                "             `setting_value_illum_1`,\n" +
                "             `start_time_2`,\n" +
                "             `end_time_2`,\n" +
                "             `setting_value_co_2_2`,\n" +
                "             `setting_value_temp_2`,\n" +
                "             `setting_value_humid_2`,\n" +
                "             `setting_value_illum_2`,\n" +
                "             `start_time_3`,\n" +
                "             `end_time_3`,\n" +
                "             `setting_value_co_2_3`,\n" +
                "             `setting_value_temp_3`,\n" +
                "             `setting_value_humid_3`,\n" +
                "             `setting_value_illum_3`,\n" +
                "             `rawJson`,\n" +
                "             `regDate`)\n" +
                "values (" +
                "'" +farmCode+"',\n" +
                "'" +dongCode+"',\n" +
                "'" +crop_data_num_and_ctrl_aggr+"',\n" +
                "'" +sensor_quantity+"',\n" +
                "'" +sensor_selected_1+"',\n" +
                "'" +sensor_selected_2+"',\n" +
                "'" +sensor_selected_3+"',\n" +
                "'" +sensor_selected_4+"',\n" +
                "'" +singular_ctrl_setting_co2+"',\n" +
                "'" +singular_ctrl_setting_temp+"',\n" +
                "'" +singular_ctrl_setting_humid+"',\n" +
                "'" +singular_ctrl_setting_illum+"',\n" +
                "'" +relay_output_setting_co2+"',\n" +
                "'" +relay_output_setting_heat+"',\n" +
                "'" +relay_output_setting_cool+"',\n" +
                "'" +relay_output_setting_humidify+"',\n" +
                "'" +relay_output_setting_dehumidify+"',\n" +
                "'" +relay_output_setting_illum+"',\n" +
                "'" +relay_output_setting_alarm+"',\n" +
                "'" +relay_output_setting_reserve+"',\n" +
                "'" +dry_condition_setting_aggr+"',\n" +
                "'" +dry_condition_setting_ctrl+"',\n" +
                "'" +dry_condition_setting_humidity+"',\n" +
                "'" +alert_alarm_time_select_aggr+"',\n" +
                "'" +alert_alarm_time_select_auto+"',\n" +
                "'" +alert_alarm_time_select_timer+"',\n" +
                "'" +alert_alarm_time_select_lamp_unit+"',\n" +
                "'" +alert_alarm_time_select_timeset+"',\n" +
                "'" +cthi_ctrl_stat_aggr+"',\n" +
                "'" +cthi_ctrl_stat_co2_ctrl+"',\n" +
                "'" +cthi_ctrl_stat_co2_ontype+"',\n" +
                "'" +cthi_ctrl_stat_co2_offtype+"',\n" +
                "'" +cthi_ctrl_stat_temp_ctrl+"',\n" +
                "'" +cthi_ctrl_stat_temp_ontype+"',\n" +
                "'" +cthi_ctrl_stat_temp_offtype+"',\n" +
                "'" +cthi_ctrl_stat_humid_ctrl+"',\n" +
                "'" +cthi_ctrl_stat_humid_ontype+"',\n" +
                "'" +cthi_ctrl_stat_humid_offtype+"',\n" +
                "'" +cthi_ctrl_stat_illum_ctrl+"',\n" +
                "'" +cthi_ctrl_stat_illum_ontype+"',\n" +
                "'" +cthi_ctrl_stat_illum_offtype+"',\n" +
                "'" +calm_threshold_co2_low+"',\n" +
                "'" +calm_threshold_co2_high+"',\n" +
                "'" +calm_threshold_temp_low+"',\n" +
                "'" +calm_threshold_temp_high+"',\n" +
                "'" +calm_threshold_humid_low+"',\n" +
                "'" +calm_threshold_humid_high+"',\n" +
                "'" +calm_threshold_illum_low+"',\n" +
                "'" +calm_threshold_illum_high+"',\n" +
                "'" +setting_range_co2_min+"',\n" +
                "'" +setting_range_co2_max+"',\n" +
                "'" +setting_range_temp_min+"',\n" +
                "'" +setting_range_temp_max+"',\n" +
                "'" +setting_range_humid_min+"',\n" +
                "'" +setting_range_humid_max+"',\n" +
                "'" +setting_range_illum_min+"',\n" +
                "'" +setting_range_illum_max+"',\n" +
                "'" +sr_revision_co2_01+"',\n" +
                "'" +sr_revision_temp_01+"',\n" +
                "'" +sr_revision_humid_01+"',\n" +
                "'" +sr_revision_illum_01+"',\n" +
                "'" +sr_revision_co2_02+"',\n" +
                "'" +sr_revision_temp_02+"',\n" +
                "'" +sr_revision_humid_02+"',\n" +
                "'" +sr_revision_illum_02+"',\n" +
                "'" +sr_revision_co2_03+"',\n" +
                "'" +sr_revision_temp_03+"',\n" +
                "'" +sr_revision_humid_03+"',\n" +
                "'" +sr_revision_illum_03+"',\n" +
                "'" +sr_revision_co2_04+"',\n" +
                "'" +sr_revision_temp_04+"',\n" +
                "'" +sr_revision_humid_04+"',\n" +
                "'" +sr_revision_illum_04+"',\n" +
                "'" +setting_onoff_range_co2+"',\n" +
                "'" +setting_onoff_range_co2_revision+"',\n" +
                "'" +setting_onoff_range_temp+"',\n" +
                "'" +setting_onoff_range_temp_revision+"',\n" +
                "'" +setting_onoff_range_humid+"',\n" +
                "'" +setting_onoff_range_humid_revision+"',\n" +
                "'" +setting_onoff_range_illum+"',\n" +
                "'" +setting_onoff_range_illum_revision+"',\n" +
                "'" +reserve_setting_year+"',\n" +
                "'" +reserve_setting_md+"',\n" +
                "'" +reserve_setting_hm+"',\n" +
                "'" +reserve_setting_cropno+"',\n" +
                "'" +dynamic_output_type+"',\n" +
                "'" +dynamic_output_value+"',\n" +
                "'" +backup_year+"',\n" +
                "'" +backup_md+"',\n" +
                "'" +backup_hm+"',\n" +
                "'" +backup_read_year+"',\n" +
                "'" +backup_read_md+"',\n" +
                "'" +backup_read_hm+"',\n" +
                "'" +growth_start_year+"',\n" +
                "'" +growth_start_md+"',\n" +
                "'" +growth_start_hm+"',\n" +
                "'" +change_date_time+"',\n" +
                "'" +alert_alarm_aggr+"',\n" +
                "'" +alert_alarm_internal_co2+"',\n" +
                "'" +alert_alarm_internal_temp+"',\n" +
                "'" +alert_alarm_internal_humidity+"',\n" +
                "'" +alert_alarm_internal_ilum+"',\n" +
                "'" +alert_alarm_vent_relay+"',\n" +
                "'" +alert_alarm_heat_relay+"',\n" +
                "'" +alert_alarm_cool_relay+"',\n" +
                "'" +alert_alarm_humidify_relay+"',\n" +
                "'" +alert_alarm_dehumidify_relay+"',\n" +
                "'" +alert_alarm_ilum_relay+"',\n" +
                "'" +alert_alarm_rs485+"',\n" +
                "'" +alert_alarm_vt515+"',\n" +
                "'" +alert_alarm_vt250_1+"',\n" +
                "'" +alert_alarm_vt250_2+"',\n" +
                "'" +alert_alarm_vt250_3+"',\n" +
                "'" +alert_alarm_vt250_4+"',\n" +
                "'" +settingTails.get(0).getStart_time()+"',\n" +
                "'" +settingTails.get(0).getEnd_time()+"',\n" +
                "'" +settingTails.get(0).getSetting_value_co_2()+"',\n" +
                "'" +settingTails.get(0).getSetting_value_temp()+"',\n" +
                "'" +settingTails.get(0).getSetting_value_humid()+"',\n" +
                "'" +settingTails.get(0).getSetting_value_illum()+"',\n" +
                "'" +settingTails.get(1).getStart_time()+"',\n" +
                "'" +settingTails.get(1).getEnd_time()+"',\n" +
                "'" +settingTails.get(1).getSetting_value_co_2()+"',\n" +
                "'" +settingTails.get(1).getSetting_value_temp()+"',\n" +
                "'" +settingTails.get(1).getSetting_value_humid()+"',\n" +
                "'" +settingTails.get(1).getSetting_value_illum()+"',\n" +
                "'" +settingTails.get(2).getStart_time()+"',\n" +
                "'" +settingTails.get(2).getEnd_time()+"',\n" +
                "'" +settingTails.get(2).getSetting_value_co_2()+"',\n" +
                "'" +settingTails.get(2).getSetting_value_temp()+"',\n" +
                "'" +settingTails.get(2).getSetting_value_humid()+"',\n" +
                "'" +settingTails.get(2).getSetting_value_illum()+"',\n" +
                "'" +json+"',\n" +
                "        NOW()) " +
                "ON DUPLICATE KEY UPDATE " +
                "  `crop_data_num_and_ctrl_aggr` = '" + crop_data_num_and_ctrl_aggr+ "',\n" +
                "  `sensor_quantity` = '" + sensor_quantity+ "',\n" +
                "  `sensor_selected_1` = '" + sensor_selected_1+ "',\n" +
                "  `sensor_selected_2` = '" + sensor_selected_2+ "',\n" +
                "  `sensor_selected_3` = '" + sensor_selected_3+ "',\n" +
                "  `sensor_selected_4` = '" + sensor_selected_4+ "',\n" +
                "  `singular_ctrl_setting_co2` = '" + singular_ctrl_setting_co2+ "',\n" +
                "  `singular_ctrl_setting_temp` = '" + singular_ctrl_setting_temp+ "',\n" +
                "  `singular_ctrl_setting_humid` = '" + singular_ctrl_setting_humid+ "',\n" +
                "  `singular_ctrl_setting_illum` = '" + singular_ctrl_setting_illum+ "',\n" +
                "  `relay_output_setting_co2` = '" + relay_output_setting_co2+ "',\n" +
                "  `relay_output_setting_heat` = '" + relay_output_setting_heat+ "',\n" +
                "  `relay_output_setting_cool` = '" + relay_output_setting_cool+ "',\n" +
                "  `relay_output_setting_humidify` = '" + relay_output_setting_humidify+ "',\n" +
                "  `relay_output_setting_dehumidify` = '" + relay_output_setting_dehumidify+ "',\n" +
                "  `relay_output_setting_illum` = '" + relay_output_setting_illum+ "',\n" +
                "  `relay_output_setting_alarm` = '" + relay_output_setting_alarm+ "',\n" +
                "  `relay_output_setting_reserve` = '" + relay_output_setting_reserve+ "',\n" +
                "  `dry_condition_setting_aggr` = '" + dry_condition_setting_aggr+ "',\n" +
                "  `dry_condition_setting_ctrl` = '" + dry_condition_setting_ctrl+ "',\n" +
                "  `dry_condition_setting_humidity` = '" + dry_condition_setting_humidity+ "',\n" +
                "  `alert_alarm_time_select_aggr` = '" + alert_alarm_time_select_aggr+ "',\n" +
                "  `alert_alarm_time_select_auto` = '" + alert_alarm_time_select_auto+ "',\n" +
                "  `alert_alarm_time_select_timer` = '" + alert_alarm_time_select_timer+ "',\n" +
                "  `alert_alarm_time_select_lamp_unit` = '" + alert_alarm_time_select_lamp_unit+ "',\n" +
                "  `alert_alarm_time_select_timeset` = '" + alert_alarm_time_select_timeset+ "',\n" +
                "  `cthi_ctrl_stat_aggr` = '" + cthi_ctrl_stat_aggr+ "',\n" +
                "  `cthi_ctrl_stat_co2_ctrl` = '" + cthi_ctrl_stat_co2_ctrl+ "',\n" +
                "  `cthi_ctrl_stat_co2_ontype` = '" + cthi_ctrl_stat_co2_ontype+ "',\n" +
                "  `cthi_ctrl_stat_co2_offtype` = '" + cthi_ctrl_stat_co2_offtype+ "',\n" +
                "  `cthi_ctrl_stat_temp_ctrl` = '" + cthi_ctrl_stat_temp_ctrl+ "',\n" +
                "  `cthi_ctrl_stat_temp_ontype` = '" + cthi_ctrl_stat_temp_ontype+ "',\n" +
                "  `cthi_ctrl_stat_temp_offtype` = '" + cthi_ctrl_stat_temp_offtype+ "',\n" +
                "  `cthi_ctrl_stat_humid_ctrl` = '" + cthi_ctrl_stat_humid_ctrl+ "',\n" +
                "  `cthi_ctrl_stat_humid_ontype` = '" + cthi_ctrl_stat_humid_ontype+ "',\n" +
                "  `cthi_ctrl_stat_humid_offtype` = '" + cthi_ctrl_stat_humid_offtype+ "',\n" +
                "  `cthi_ctrl_stat_illum_ctrl` = '" + cthi_ctrl_stat_illum_ctrl+ "',\n" +
                "  `cthi_ctrl_stat_illum_ontype` = '" + cthi_ctrl_stat_illum_ontype+ "',\n" +
                "  `cthi_ctrl_stat_illum_offtype` = '" + cthi_ctrl_stat_illum_offtype+ "',\n" +
                "  `calm_threshold_co2_low` = '" + calm_threshold_co2_low+ "',\n" +
                "  `calm_threshold_co2_high` = '" + calm_threshold_co2_high+ "',\n" +
                "  `calm_threshold_temp_low` = '" + calm_threshold_temp_low+ "',\n" +
                "  `calm_threshold_temp_high` = '" + calm_threshold_temp_high+ "',\n" +
                "  `calm_threshold_humid_low` = '" + calm_threshold_humid_low+ "',\n" +
                "  `calm_threshold_humid_high` = '" + calm_threshold_humid_high+ "',\n" +
                "  `calm_threshold_illum_low` = '" + calm_threshold_illum_low+ "',\n" +
                "  `calm_threshold_illum_high` = '" + calm_threshold_illum_high+ "',\n" +
                "  `setting_range_co2_min` = '" + setting_range_co2_min+ "',\n" +
                "  `setting_range_co2_max` = '" + setting_range_co2_max+ "',\n" +
                "  `setting_range_temp_min` = '" + setting_range_temp_min+ "',\n" +
                "  `setting_range_temp_max` = '" + setting_range_temp_max+ "',\n" +
                "  `setting_range_humid_min` = '" + setting_range_humid_min+ "',\n" +
                "  `setting_range_humid_max` = '" + setting_range_humid_max+ "',\n" +
                "  `setting_range_illum_min` = '" + setting_range_illum_min+ "',\n" +
                "  `setting_range_illum_max` = '" + setting_range_illum_max+ "',\n" +
                "  `sr_revision_co2_01` = '" + sr_revision_co2_01+ "',\n" +
                "  `sr_revision_temp_01` = '" + sr_revision_temp_01+ "',\n" +
                "  `sr_revision_humid_01` = '" + sr_revision_humid_01+ "',\n" +
                "  `sr_revision_illum_01` = '" + sr_revision_illum_01+ "',\n" +
                "  `sr_revision_co2_02` = '" + sr_revision_co2_02+ "',\n" +
                "  `sr_revision_temp_02` = '" + sr_revision_temp_02+ "',\n" +
                "  `sr_revision_humid_02` = '" + sr_revision_humid_02+ "',\n" +
                "  `sr_revision_illum_02` = '" + sr_revision_illum_02+ "',\n" +
                "  `sr_revision_co2_03` = '" + sr_revision_co2_03+ "',\n" +
                "  `sr_revision_temp_03` = '" + sr_revision_temp_03+ "',\n" +
                "  `sr_revision_humid_03` = '" + sr_revision_humid_03+ "',\n" +
                "  `sr_revision_illum_03` = '" + sr_revision_illum_03+ "',\n" +
                "  `sr_revision_co2_04` = '" + sr_revision_co2_04+ "',\n" +
                "  `sr_revision_temp_04` = '" + sr_revision_temp_04+ "',\n" +
                "  `sr_revision_humid_04` = '" + sr_revision_humid_04+ "',\n" +
                "  `sr_revision_illum_04` = '" + sr_revision_illum_04+ "',\n" +
                "  `setting_onoff_range_co2` = '" + setting_onoff_range_co2+ "',\n" +
                "  `setting_onoff_range_co2_revision` = '" + setting_onoff_range_co2_revision+ "',\n" +
                "  `setting_onoff_range_temp` = '" + setting_onoff_range_temp+ "',\n" +
                "  `setting_onoff_range_temp_revision` = '" + setting_onoff_range_temp_revision+ "',\n" +
                "  `setting_onoff_range_humid` = '" + setting_onoff_range_humid+ "',\n" +
                "  `setting_onoff_range_humid_revision` = '" + setting_onoff_range_humid_revision+ "',\n" +
                "  `setting_onoff_range_illum` = '" + setting_onoff_range_illum+ "',\n" +
                "  `setting_onoff_range_illum_revision` = '" + setting_onoff_range_illum_revision+ "',\n" +
                "  `reserve_setting_year` = '" + reserve_setting_year+ "',\n" +
                "  `reserve_setting_md` = '" + reserve_setting_md+ "',\n" +
                "  `reserve_setting_hm` = '" + reserve_setting_hm+ "',\n" +
                "  `reserve_setting_cropno` = '" + reserve_setting_cropno+ "',\n" +
                "  `dynamic_output_type` = '" + dynamic_output_type+ "',\n" +
                "  `dynamic_output_value` = '" + dynamic_output_value+ "',\n" +
                "  `backup_year` = '" + backup_year+ "',\n" +
                "  `backup_md` = '" + backup_md+ "',\n" +
                "  `backup_hm` = '" + backup_hm+ "',\n" +
                "  `backup_read_year` = '" + backup_read_year+ "',\n" +
                "  `backup_read_md` = '" + backup_read_md+ "',\n" +
                "  `backup_read_hm` = '" + backup_read_hm+ "',\n" +
                "  `growth_start_year` = '" + growth_start_year+ "',\n" +
                "  `growth_start_md` = '" + growth_start_md+ "',\n" +
                "  `growth_start_hm` = '" + growth_start_hm+ "',\n" +
                "  `change_date_time` = '" + change_date_time+ "',\n" +
                "  `alert_alarm_aggr` = '" + alert_alarm_aggr+ "',\n" +
                "  `alert_alarm_internal_co2` = '" + alert_alarm_internal_co2+ "',\n" +
                "  `alert_alarm_internal_temp` = '" + alert_alarm_internal_temp+ "',\n" +
                "  `alert_alarm_internal_humidity` = '" + alert_alarm_internal_humidity+ "',\n" +
                "  `alert_alarm_internal_ilum` = '" + alert_alarm_internal_ilum+ "',\n" +
                "  `alert_alarm_vent_relay` = '" + alert_alarm_vent_relay+ "',\n" +
                "  `alert_alarm_heat_relay` = '" + alert_alarm_heat_relay+ "',\n" +
                "  `alert_alarm_cool_relay` = '" + alert_alarm_cool_relay+ "',\n" +
                "  `alert_alarm_humidify_relay` = '" + alert_alarm_humidify_relay+ "',\n" +
                "  `alert_alarm_dehumidify_relay` = '" + alert_alarm_dehumidify_relay+ "',\n" +
                "  `alert_alarm_ilum_relay` = '" + alert_alarm_ilum_relay+ "',\n" +
                "  `alert_alarm_rs485` = '" + alert_alarm_rs485+ "',\n" +
                "  `alert_alarm_vt515` = '" + alert_alarm_vt515+ "',\n" +
                "  `alert_alarm_vt250_1` = '" + alert_alarm_vt250_1+ "',\n" +
                "  `alert_alarm_vt250_2` = '" + alert_alarm_vt250_2+ "',\n" +
                "  `alert_alarm_vt250_3` = '" + alert_alarm_vt250_3+ "',\n" +
                "  `alert_alarm_vt250_4` = '" + alert_alarm_vt250_4+ "',\n" +
                "  `start_time_1` = '" + settingTails.get(0).getStart_time() + "',\n" +
                "  `end_time_1` = '" + settingTails.get(0).getEnd_time() + "',\n" +
                "  `setting_value_co_2_1` = '" + settingTails.get(0).getSetting_value_co_2() + "',\n" +
                "  `setting_value_temp_1` = '" + settingTails.get(0).getSetting_value_temp() + "',\n" +
                "  `setting_value_humid_1` = '" + settingTails.get(0).getSetting_value_humid() + "',\n" +
                "  `setting_value_illum_1` = '" + settingTails.get(0).getSetting_value_illum() + "',\n" +
                "  `start_time_2` = '" + settingTails.get(1).getStart_time() + "',\n" +
                "  `end_time_2` = '" + settingTails.get(1).getEnd_time() + "',\n" +
                "  `setting_value_co_2_2` = '" + settingTails.get(1).getSetting_value_co_2() + "',\n" +
                "  `setting_value_temp_2` = '" + settingTails.get(1).getSetting_value_temp() + "',\n" +
                "  `setting_value_humid_2` = '" + settingTails.get(1).getSetting_value_humid() + "',\n" +
                "  `setting_value_illum_2` = '" + settingTails.get(1).getSetting_value_illum() + "',\n" +
                "  `start_time_3` = '" + settingTails.get(2).getStart_time() + "',\n" +
                "  `end_time_3` = '" + settingTails.get(2).getEnd_time() + "',\n" +
                "  `setting_value_co_2_3` = '" + settingTails.get(2).getSetting_value_co_2() + "',\n" +
                "  `setting_value_temp_3` = '" + settingTails.get(2).getSetting_value_temp() + "',\n" +
                "  `setting_value_humid_3` = '" + settingTails.get(2).getSetting_value_humid() + "',\n" +
                "  `setting_value_illum_3` = '" + settingTails.get(2).getSetting_value_illum() + "',\n" +
                "  `rawJson` = '" + json + "',\n" +
                "  `regDate` = NOW();";

        return sql;
    }



    }
