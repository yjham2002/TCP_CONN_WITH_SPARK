package utils;

import java.util.Calendar;

/**
 * @author EuiJin.Ham
 * @version 1.2.0
 * @description A Class for logging various type of strings with short syntax.
 * @apiNote The error stream and the standard stream are none-thread-safe.
 */
public class Log {
    public static final String ERR = "[WARN]";
    public static final String INFO = "[INFO]";

    public static void e(String msg){
        System.err.println(logPrefix(ERR) + msg);
    }

    public static void i(String msg){
        System.out.println(logPrefix(INFO) + msg);
    }

    public static void e(Object msg){
        System.err.println(logPrefix(ERR) + msg.toString());
    }

    public static void i(Object msg){
        System.out.println(logPrefix(INFO) + msg.toString());
    }

    public static void e(String tag, Object msg){
        System.err.println(logPrefix(ERR) + "[" + tag + "] " + msg.toString());
    }

    public static void i(String tag, Object msg){
        System.err.println(logPrefix(INFO) + "[" + tag + "] " + msg.toString());
    }

    private static String logPrefix(String prefix){
        final String str = String.format("%s %s - [%s] ", prefix, Calendar.getInstance().getTime().toString(), Thread.currentThread().getName());
        return str;
    }
}

