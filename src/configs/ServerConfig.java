package configs;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 함의진
 * 서버 인스턴스의 속성을 정의하는 슈퍼클래스로 임시 설계 상태
 */
public class ServerConfig {

    public static final int SOCKET_PORT = 8000;
    public static final boolean DEBUG_MODE = true;

    public static final PrintStream stream = System.out;

    public static String getTime(){
        SimpleDateFormat fmt = new SimpleDateFormat("[yyyy-MM-d hh:mm:ss]");
        return fmt.format(new Date());
    }

}
