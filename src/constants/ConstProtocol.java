package constants;

import models.Pair;

/**
 * @author 함의진
 * @version 1.0
 * @apiNote 소하테크 프로토콜 정의 컨스턴트
 */
public class ConstProtocol {

    public static final byte[] STX = new byte[]{83, 84}; // Start Of Text
    public static final byte[] ETX = new byte[]{13, 10}; // End Of Text
    public static final byte[] INITIAL_PROTOCOL_START = new byte[]{83, 79, 72, 65, 85, 78, 73, 70, 65, 82, 77};

    public static final int RANGE_READ_START = 14;

    public static final byte[] FUNCTION_READ = new byte[]{0x03};
    public static final byte[] FUNCTION_WRITE = new byte[]{0x10};

    public static final int INIT_TERM_MIN10 = 0;
    public static final int INIT_TERM_MIN = 0;
    public static final int INIT_TERM_SEC10 = 0;
    public static final int INIT_TERM_SEC = 5;

    public static final int SOCKET_TIMEOUT_LIMIT = 3;

    public static final int READ_LIMIT = 255;

    public static final Pair<Integer> TIMER_RANGE = new Pair<>(448, 194);

    /**
     * API Parameters
     */
    public static final String MODE_READ_TIMER = "read_timer";
    public static final String MODE_READ_DAYAGE = "read_dayage";

}
