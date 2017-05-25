package configs;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by a on 2017-05-25.
 */
public class ServerConfig {

    public static final int SOCEKET_PORT = 8000;
    public static final boolean DEBUG_MODE = true;

    public static final PrintStream stream = System.out;

    public static String getTime(){
        SimpleDateFormat fmt = new SimpleDateFormat("[yyyy-MM-d hh:mm:ss]");
        return fmt.format(new Date());
    }

}
