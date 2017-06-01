package configs;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author 함의진
 * 서버 인스턴스의 속성을 정의하는 슈퍼클래스로 임시 설계 상태
 */
public class ServerConfig {

    /**
     * REDIS CONFIGURATION
     * RUNNING WITH REDIS 3.2.9 STABLE VERSION
     */
    protected static final String REDIS_HOST = "localhost";
    protected static final int REDIS_PORT = 6379;
    protected static final int REDIS_TIMEOUT = 10000;
    protected static final String REDIS_PASSWORD = "richware";


    /**
     * 서버 소켓 포트
     */
    public static final int SOCKET_PORT = 8000;

    /**
     * 간단한 배치 작업을 위한 간격 시간
     */
    protected static final int BATCH_TIME = 600 * 1000;

    /**
     * 디버깅 모드 설정을 위한 정적 변수
     */
    protected static final boolean DEBUG_MODE = true;

    /**
     * 디버깅 출력 스트림
     */
    protected static final PrintStream stream = System.out;

    /**
     * 시간 출력을 위한 메소드
     * @return 현재 날짜와 시간을 로컬 타임으로 반환
     */
    public static String getTime(){
        SimpleDateFormat fmt = new SimpleDateFormat("[yyyy-MM-d hh:mm:ss]");
        return fmt.format(new Date());
    }

    public static String getMillis(){
        return Long.toString(Calendar.getInstance().getTimeInMillis());
    }

}
