package utils;

public class Log {
    public static final String ERR = "[WARN] ";
    public static final String INFO = "[INFO] ";

    public static void e(String msg){
        System.err.println(ERR + msg);
    }

    public static void i(String msg){
        System.out.println(INFO + msg);
    }

    public static void e(Object msg){
        System.err.println(ERR + msg.toString());
    }

    public static void i(Object msg){
        System.out.println(INFO + msg.toString());
    }
}
