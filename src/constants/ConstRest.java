package constants;

/**
 * Created by a on 2017-05-30.
 */
public class ConstRest {
//    public static final int REST_PORT = 8001;
    public static final int REST_PORT = 8011;

    public static final String REST_CACHE = "cache";
    public static final String REST_CONNECT_TEST = "conn";
    public static final String REST_READ_REQUEST = "read";
    public static final String REST_WRITE_REQUEST = "write";

    public static final String RESPONSE_NONE = "{}";
    public static final String RESPONSE_INVALID = "{\"err\":\"Invalid Parameter\"}";

    /**
     * API Parameter names
     */
    public static final String FARM_CODE = "s_farm_code";
    public static final String HARV_CODE = "s_dong_code";
    public static final String JSON_CODE = "json";

    /**
     * API Parameters
     */
    public static final String MODE_READ_REALTIME = "read_realtime";
    public static final String MODE_READ_TIMER = "read_timer";
    public static final String MODE_READ_DAYAGE = "read_dayage";
    public static final String MODE_READ_SETTING = "read_setting";
    public static final String MODE_WRITE_TIMER = "write_timer";
    public static final String MODE_WRITE_DAYAGE = "write_dayage";
    public static final String MODE_WRITE_DAYAGE_ONCE = "write_dayage_once";
    public static final String MODE_WRITE_SETTING = "write_setting";
    public static final String MODE_WRITE_REALTIME = "write_realtime";
}
