package redis;

/**
 * @author 함의진
 * @version 1.0
 * 레디스 랩퍼로부터 이를 싱글턴으로 구현한 실제 연결 객체 클래스
 */
public class RedisManager extends RedisWrapper{

    /**
     * 싱글턴 정적 인스턴스
     */
    private static RedisManager instance;

    /**
     * 슈퍼클래스 생성자를 호출하는 내부 생성자
     */
    private RedisManager(){
        super();
    }

    /**
     * 싱글턴 인스턴스 할당자
     * @return
     */
    public static RedisManager getInstance(){
        if(instance == null) instance = new RedisManager();
        return instance;
    }

    public static long getMillisFromRedisKey(String pureKey){
        String raw = pureKey.split("\\-")[1];
        long millis = Long.parseLong(raw);

        return millis;
    }

}
