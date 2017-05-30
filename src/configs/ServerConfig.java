package configs;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 함의진
 * 서버 인스턴스의 속성을 정의하는 슈퍼클래스로 임시 설계 상태
 */
public class ServerConfig {

    /**
     * 서버 소켓 포트
     */
    public static final int SOCKET_PORT = 8000;

    /**
     * 디버깅 모드 설정을 위한 정적 변수
     */
    public static final boolean DEBUG_MODE = true;

    /**
     * 디버깅 출력 스트림
     */
    public static final PrintStream stream = System.out;

    /**
     * 시간 출력을 위한 메소드
     * @return 현재 날짜와 시간을 로컬 타임으로 반환
     */
    public static String getTime(){
        SimpleDateFormat fmt = new SimpleDateFormat("[yyyy-MM-d hh:mm:ss]");
        return fmt.format(new Date());
    }

}
