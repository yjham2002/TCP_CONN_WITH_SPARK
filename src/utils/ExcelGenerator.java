package utils;

import databases.DBManager;
import models.DataMap;
import models.RestProcessor;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelGenerator {

    private static CellStyle createBorderedStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        return style;
    }

    private static CellStyle createStyleWithColor(Workbook wb, boolean bold, short indexedColor){
        CellStyle style = createBorderedStyle(wb);
        Font headerFont = wb.createFont();
        if(bold) headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerFont.setColor(indexedColor);
        style.setFont(headerFont);

        return style;
    }

    private static Cell cell(Workbook wb, Row row, CellStyle style, int idx){
        Cell cell = row.createCell(idx);
        cell.setCellStyle(style);
        return cell;
    }

    private static String dongListKey(String farm, String dong){
        return farm + "-" + dong;
    }

    public static String genAndGetName(String farm, String harv, String start, String end) throws IOException{

        final String startDate = start;
        final String endDate = end;
        final String farmCode = farm;
        final String harvCode = harv;
        final String timestamp = Long.toString(Calendar.getInstance().getTimeInMillis());
        final String column[] = new String[]{"No", "운전상태", "작물이름", "운전모드", "재배일자", "CO2", "온도", "습도", "조도", "제어방식 설정", "", "", "", "릴레이 설정", "", "", "", "", "", "", "수신시간"};
        final String columnOfControl[] = new String[]{"", "", "", "", "", "", "", "", "", "CO2", "온도", "습도", "조도", "CO2", "난방", "냉방", "가습", "제습", "조명", "알람", ""};

        long startTime = Calendar.getInstance().getTimeInMillis();

        final String cacheSql = "SELECT * FROM dong_list;";
        List<DataMap> dongList = DBManager.getInstance().getList(cacheSql);

        final Map<String, String[]> cacheMap = new HashMap<>();
        for(DataMap map : dongList){
            final String key = dongListKey(map.getString("farm_code"), map.getString("dong_code"));
            final String pName1 = map.getString("plants_name1");
            final String pName2 = map.getString("plants_name2");
            final String pName3 = map.getString("plants_name31");
            final String pName4 = map.getString("plants_name4");
            final String pName5 = map.getString("plants_name5");
            final String pName6 = map.getString("plants_name6");
            if(key.length() == 7) cacheMap.put(key, new String[]{pName1, pName2, pName3, pName4, pName5, pName6});
        }

        final String sql = "" +
                "SELECT " +
                "  A.mcnctrl_mv510_pause, " +
                "  A.mcnctrl_mv510_order_main1, " +
                "  S.crop_data_num_and_ctrl_aggr, " +
                "  A.run_status_mode,  " +
                "  S.crop_data_num_and_ctrl_aggr, " +
                "  S.alert_alarm_time_select_lamp_unit,  " +
                "  A.co2_sr, " +
                "  A.temp_sr, " +
                "  A.humid_sr, " +
                "  A.illum_sr, " +
                "  A.controlstat_co2_type,   " +
                "  A.controlstat_temp_type, " +
                "  A.controlstat_humidity_type,  " +
                "  A.controlstat_ilum_type,  " +
                "  A.relay_output_co2, " +
                "  A.relay_output_heater, " +
                "  A.relay_output_freezer, " +
                "  A.relay_output_humidity, " +
                "  A.relay_output_dehumidity, " +
                "  A.relay_output_ilum, " +
                "  A.mcnctrl_mv510_stat_alarm, " +
                "  A.redisTime, " +
                "  A.growth_progress_dt, " +
                "  A.growth_progress_total, " +
                "  A.errdata_internal_co2 AS err1, " +
                "  A.errdata_internal_temp AS err2, " +
                "  A.errdata_internal_humid AS err3, " +
                "  A.errdata_internal_ilum AS err4, " +
                "  A.errdata_vent_relay AS err5, " +
                "  A.errdata_raisetemp_relay AS err6, " +
                "  A.errdata_raisecool_relay AS err7, " +
                "  A.errdata_humidify_relay AS err8, " +
                "  A.errdata_dehumidify_relay AS err9, " +
                "  A.errdata_ilum_output AS err10, " +
                "  A.errdata_crop_data AS err11, " +
                "  A.errdata_device_connection AS err12, " +
                "  A.errdata_network1 AS err13, " +
                "  A.errdata_network2 AS err14, " +
                "  A.errdata_network3 AS err15, " +
                "  A.errdata_network4 AS err16 " +
                "FROM tblRealTimeData A " +
                "JOIN tblSettingData S ON A.farmCode = S.farmCode AND A.dongCode = S.dongCode " +
                "WHERE A.farmCode='" + farmCode + "' AND A.dongCode = '" + harvCode + "' AND redisTime >= '" + startDate + "' AND redisTime <= '" + endDate + "' " +
                "ORDER BY A.redisTime " +
                "DESC LIMIT 0, 30000;";

        List<DataMap> list = DBManager.getInstance().getList(sql);

        Log.e("Time Passed - Data : " + (Calendar.getInstance().getTimeInMillis() - startTime));

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sheet");
        HSSFRow rowhead = sheet.createRow((short)0);

        CellStyle cellStyle = createBorderedStyle(workbook);
        Font font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cellStyle.setFont(font);
        cellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle border = createBorderedStyle(workbook);
        CellStyle blueFont = createStyleWithColor(workbook, false, IndexedColors.BLUE.getIndex());
        CellStyle redFont = createStyleWithColor(workbook, false, IndexedColors.RED.getIndex());

        for(int e = 0; e < 21; e++) {
            Cell cell = rowhead.createCell(e);
            cell.setCellValue(column[e]);
            CellUtil.setAlignment(cell, workbook, CellStyle.ALIGN_CENTER);
            cell.setCellStyle(cellStyle);
        }

        HSSFRow rowSub = sheet.createRow((short)1);
        for(int e = 0; e < 21; e++) {
            Cell cell = rowSub.createCell(e);
            cell.setCellValue(columnOfControl[e]);
            cell.setCellStyle(cellStyle);
        }

        for(int e = 0; e < 21; e++){
            if(e >= 9 && e <= 19){}
            else sheet.addMergedRegion(new CellRangeAddress(0, 1, e, e));
        }

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 9, 12));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 13, 19));

        final String fileName = "./excel/DataLogger_" + farmCode + "_" + harvCode + "_" + timestamp + ".xls";

        for(int i = 0; i < list.size(); i++) {
            HSSFRow row = sheet.createRow((short) (i + 2));
            try {
                DataMap data = list.get(i);

                final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final Date toParse = new Date();
                final int growth_progress_dt = data.getInt("growth_progress_dt");
                final int growth_progress_total = data.getInt("growth_progress_total");
                toParse.setTime(Long.parseLong(data.getString("redisTime")));
                final String ctrl_aggr_padd = String.format("%04d", data.getInt("crop_data_num_and_ctrl_aggr"));
                final String[] split = ctrl_aggr_padd.trim().split("");
                final String pNum = split[split.length - 1];
                final String recvTime = fmt.format(toParse);
                final String plantName = cacheMap.get(dongListKey(farmCode, harvCode))[Integer.parseInt(pNum) - 1];
                final int run_status_mode = data.getInt("run_status_mode");

                final String runningState = statusString(run_status_mode, ctrl_aggr_padd);

                final String cropDays = cropDaysString(growth_progress_dt, growth_progress_total, runningState);

                String alarmState = "정지";

                alarmDetect:
                for(int q = 1; q <= 16; q++){
                    if(data.getInt("err" + q) == 1){
                        alarmState = "작동";
                        break alarmDetect;
                    }
                }

                final String cell01 = (i + 1) + "";
                final String cell02 = forCell02(data.getString("mcnctrl_mv510_pause"), data.getString("mcnctrl_mv510_order_main1"));
                final String cell03 = plantName;
                final String cell04 = runningState;
                final String cell05 = cropDays;
                final String cell06 = data.getString("co2_sr");
                final String cell07 = String.format("%.1f", Double.parseDouble(data.getString("temp_sr")) / 10d);
                final String cell08 = String.format("%.1f", Double.parseDouble(data.getString("humid_sr")) / 10d);
                final String cell09 = data.getString("illum_sr");
                final String cell10 = controlString(data.getInt("controlstat_co2_type"));
                final String cell11 = controlString(data.getInt("controlstat_temp_type"));
                final String cell12 = controlString(data.getInt("controlstat_humidity_type"));
                final String cell13 = controlString(data.getInt("controlstat_ilum_type"));
                final String cell14 = relayString(data.getInt("relay_output_co2"));
                final String cell15 = relayString(data.getInt("relay_output_heater"));
                final String cell16 = relayString(data.getInt("relay_output_freezer"));
                final String cell17 = relayString(data.getInt("relay_output_humidity"));
                final String cell18 = relayString(data.getInt("relay_output_dehumidity"));
                final String cell19 = relayString(data.getInt("relay_output_ilum"));
                final String cell20 = alarmState;
                final String cell21 = recvTime;

                final String[] cells = new String[]{cell01, cell02, cell03, cell04, cell05, cell06, cell07, cell08, cell09, cell10, cell11, cell12, cell13, cell14, cell15, cell16, cell17, cell18, cell19, cell20, cell21};

                for (int k = 0; k < 21; k++) {
                    if(cells[k].equals("작동")){
                        cell(workbook, row, blueFont, k).setCellValue(String.format("      %s      ", cells[k]));
                    }else if(cells[k].equals("정지")){
                        cell(workbook, row, redFont, k).setCellValue(String.format("      %s      ", cells[k]));
                    }else{
                        cell(workbook, row, border, k).setCellValue(String.format("      %s      ", cells[k]));
                    }
                }
            }catch (Exception e){
                for (int k = 0; k < 21; k++) {
                    cell(workbook, row, border, k).setCellValue(String.format(" %s ", "데이터 오류"));
                }
            }
        }

        for(int k = 0; k < 21; k++) sheet.autoSizeColumn(k);

        FileOutputStream fileOut = new FileOutputStream(fileName);
        workbook.write(fileOut);
        fileOut.close();

        return fileName;
    }

    private static String forCell02(String pauseFlag, String main){
        if(pauseFlag.trim().equals("1")) return "일시정지";
        else{
            if(main.trim().equals("1")) return "정지";
            else return "운전중";
        }
    }

    private static String cropDaysString(int now, int total, String runningState){
        final boolean isDailyCtrl = runningState.trim().equals("일령제어");
        if(isDailyCtrl){
            return String.format("%d/%d", now, total);
        }else{
            return String.format("%d", now);
        }
    }

    private static String statusString(int runState, String aggr){
        String retVal = "데이터 오류";
        if(runState == 0) retVal = "일령제어";
        else if(runState == 1) retVal = "단일제어";
        else if(runState == 2) retVal = "가스제어";
        if(aggr.startsWith("1")) retVal = "단일+3단";
        else if(aggr.startsWith("2")) retVal = "가스+3단";

        return retVal;
    }

    private static String controlString(int value){
        switch (value){
            case 0: return "자동";
            case 1: return "타이머";
            case 2: return "자+타";
            case 3: return "고정";
            default: return "오류";
        }
    }

    private static String relayString(int value){
        if(value == 1) return "작동";
        else if(value == 0) return "정지";
        else return "오류";
    }

    public static void main(String... args) throws Exception{
        ExcelGenerator.genAndGetName("1357", "01", "1502264966262", "1508371717051");
    }

}
