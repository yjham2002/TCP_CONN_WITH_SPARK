package constants;

/**
 * @author 함의진
 * @version 1.0
 * @apiNote 소하테크 프로토콜 정의 컨스턴트
 */
public class ConstProtocol {

    public static final byte[] STX = new byte[]{83, 84}; // Start Of Text
    public static final byte[] ETX = new byte[]{13, 10}; // End Of Text

    /**
     * @deprecated
     */
    public static final byte[] INIT_CODE = new byte[]{83, 84, 83, 79, 72, 65, 85, 78, 73, 70, 65, 82, 77};
}
