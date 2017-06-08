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

    public static final int RANGE_READ_START = 15;

    public static final byte[] FUNCTION_READ = new byte[]{0x03};
    public static final byte[] FUNCTION_WRITE = new byte[]{0x10};

    public static final int INIT_TERM_MIN10 = 0;
    public static final int INIT_TERM_MIN = 0;
    public static final int INIT_TERM_SEC10 = 3;
    public static final int INIT_TERM_SEC = 0;

    public static final int SOCKET_TIMEOUT_LIMIT = 60000;
    public static final int SOCKET_TIMEOUT_COUNT = 3;

    public static final int READ_LIMIT = 255;

    public static final Pair<Integer> RANGE_SETTING = new Pair<>(300, 136);
    public static final Pair<Integer> RANGE_TIMER = new Pair<>(448, 93);
    public static final Pair<Integer> RANGE_DAYAGE = new Pair<>(650, 12740);
    public static final Pair<Integer> RANGE_DAYAGE_01 = new Pair<>(650, 1820);
    public static final Pair<Integer> RANGE_DAYAGE_02 = new Pair<>(2470, 1820);
    public static final Pair<Integer> RANGE_DAYAGE_03 = new Pair<>(4290, 1820);
    public static final Pair<Integer> RANGE_DAYAGE_04 = new Pair<>(6110, 1820);
    public static final Pair<Integer> RANGE_DAYAGE_05 = new Pair<>(7930, 1820);
    public static final Pair<Integer> RANGE_DAYAGE_06 = new Pair<>(9750, 1820);

}
