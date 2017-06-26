package constants;

import models.Pair;

import java.util.Arrays;

/**
 * @author 함의진
 * @version 1.0
 * @apiNote 소하테크 프로토콜 정의 컨스턴트
 */
public class ConstProtocol {

    public static final byte[] STX = new byte[]{83, 84}; // Start Of Text
    public static final byte[] ETX = new byte[]{13, 10}; // End Of Text
    public static final byte[] INITIAL_PROTOCOL_START = new byte[]{83, 79, 72, 65, 85, 78, 73, 70, 65, 82, 77};

    public static final int RANGE_READ_START = 11;
    public static final int RANGE_READ_END = 5;

    public static final byte[] FUNCTION_READ = new byte[]{0x03};
    public static final byte[] FUNCTION_WRITE = new byte[]{0x10};

    public static final int INIT_TERM_MIN10 = 0;
    public static final int INIT_TERM_MIN = 0;
    public static final int INIT_TERM_SEC10 = 3;
    public static final int INIT_TERM_SEC = 0;

    public static final int SOCKET_TIMEOUT_LIMIT = 60000;
    public static final int SOCKET_TIMEOUT_COUNT = 3;

    public static final int READ_LIMIT = 125;

    public static final int LENGTH_REALTIME = 311;

    public static final int LENGTH_INIT = 20;

    public static final int RETRY = 3;

    /**
     * 메모리 참조 범위를 기술하기 위한 페어 상수
     * Head : 메모리 주소(Byte)
     * Tail : 메모리 참조 길이(Word)
     */
    public static final Pair<Integer> RANGE_REALTIME_READABLE = new Pair<>(100, 100);
    public static final Pair<Integer> RANGE_REALTIME_READABLE_TAILS = new Pair<>(60000, 41);
    public static final Pair<Integer> RANGE_REALTIME_WRITABLE = new Pair<>(296, 2);
    public static final Pair<Integer> RANGE_SETTING_TAILS = new Pair<>(11534, 18);
    public static final Pair<Integer> RANGE_SETTING = new Pair<>(300, 71);
    public static final Pair<Integer> RANGE_TIMER = new Pair<>(448, 97);
    public static final Pair<Integer> RANGE_DAYAGE = new Pair<>(650, 6370);
    public static final Pair<Integer> RANGE_DAYAGE_01 = new Pair<>(650, 910);
    public static final Pair<Integer> RANGE_DAYAGE_02 = new Pair<>(2470, 910);
    public static final Pair<Integer> RANGE_DAYAGE_03 = new Pair<>(4290, 910);
    public static final Pair<Integer> RANGE_DAYAGE_04 = new Pair<>(6110, 910);
    public static final Pair<Integer> RANGE_DAYAGE_05 = new Pair<>(7930, 910);
    public static final Pair<Integer> RANGE_DAYAGE_06 = new Pair<>(9750, 910);

    public static final Pair<Integer> RANGE_FLAG_BIT = new Pair<>(110, 1);

    public static final int FLAG_INIT = 0;

    public static final int FLAG_SETTING = 1;
    public static final int FLAG_TIMER = 2;
    public static final int FLAG_DAILY_1 = 4;
    public static final int FLAG_DAILY_2 = 8;
    public static final int FLAG_DAILY_3 = 16;
    public static final int FLAG_DAILY_4 = 32;
    public static final int FLAG_DAILY_5 = 64;
    public static final int FLAG_DAILY_6 = 128;

    public static final int NEGATIVE_OFFSET = 65536;
    public static final int NEGATIVE_THRESHOLD_TEMP = 32767;
    public static final int NEGATIVE_THRESHOLD_HUMID = 32767;
    public static final int NEGATIVE_THRESHOLD_REVISION = 32767;

    public static byte makeFlagSet(int... flags){
        int total = 0;
        for(int e = 0; e < flags.length; e++) total += flags[e];
        return (byte)total;
    }

    public static final int LENGTH_DAILY_AGE = 1820;

    public static final int TRUE = 1;
    public static final int FALSE = 0;

    public static final String[] ERROR_MSG = {
            "실내 CO2값 이상",
            "실내 온도 이상",
            "실내 습도 이상",
            "실내 조도 이상",
            "환기 릴레이 이상",
            "가온 릴레이 이상",
            "가냉 릴레이 이상",
            "가습 릴레이 이상",
            "제습 릴레이 이상",
            "조도 출력 이상",
            "작물 데이터 이상",
            "장치간 통신 이상",
            "VT250 #1 통신 이상",
            "VT250 #2 통신 이상",
            "VT250 #3 통신 이상",
            "VT250 #4 통신 이상"
    };

    public static final String SQL_FARMNAME_FORMAT = "SELECT farm_name AS farmName FROM farm_list WHERE farm_code = '%s' LIMIT 1";
    public static final String SQL_DONGNAME_FORMAT = "SELECT dong_name AS dongName FROM dong_list WHERE farm_code = '%s' AND dong_code = '%s' LIMIT 1";
    public static final String SQL_FARM_TEL = "SELECT farm_tel AS tel FROM farm_list WHERE farm_code = '%s' LIMIT 1";

    public static final String SQL_COL_FARMNAME = "farmName";
    public static final String SQL_COL_DONGNAME = "dongName";
    public static final String SQL_COL_FARM_TEL = "tel";

}
