package redis;

import configs.ServerConfig;
import org.apache.commons.lang3.SerializationUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.RealtimePOJO;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import utils.SerialUtil;

import java.io.Serializable;
import java.util.*;

/**
 * @author 함의진
 * @version 1.0
 * 레디스 CRUD 연산을 위한 기저 랩핑 클래스로 직접 생성해서 사용하지 않아야 함
 * 이를 상속하는 RedisManager의 싱글턴 패턴 인스턴스를 이용해야 함
 */
public class RedisWrapper extends ServerConfig{

    /**
     * SLF4J 로거 인스턴스
     */
    private Logger log;

    /**
     * < Comment >
     * 멀티 스레딩 환경이므로, 불가피하게 정적 멤버 변수로 구성
     * 절대로 변경해서는 안 됨
     * Redis는 Thread Safe하게 개발되지 않음
     */
    private static JedisPoolConfig jedisPoolConfig;
    private static JedisPool jedisPool;
    private static Jedis jedis;

    /**
     * 기본 내부 생성자로 직접 호출해서는 안 됨
     */
    protected RedisWrapper(){
        log = LoggerFactory.getLogger(this.getClass());
        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setNumTestsPerEvictionRun(10);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(60000);

        jedisPool = new JedisPool(jedisPoolConfig, REDIS_HOST, REDIS_PORT, REDIS_TIMEOUT, REDIS_PASSWORD);
        jedis = jedisPool.getResource();
    }

    /**
     * 레디스 데이터 삽입을 위한 메소드로 콜백을 포함한다
     * @param key 레디스 유니크키
     * @param object 삽입될 오브젝트
     * @param postProcess 삽입 이후 실행할 콜백 인터페이스(선택)
     * @return 정상 삽입 여부
     */
    public boolean put(String key, Object object, ICallback postProcess){

        try {
            jedis.connect();
            log.info("[JEDIS] PUT OPERATION INVOKED WITH [KEY:" + key + "] => [" + object + "]");

            ObjectMapper mapper = new ObjectMapper();

            String json = mapper.writeValueAsString(object);

            jedis.set(key.getBytes(), json.getBytes());

            if (postProcess != null) {
                postProcess.postExecuted();
            }

        }catch (Exception e){
            e.printStackTrace();
            if(jedis != null) jedis.close();
        }

        return true;
    }

    /**
     * 레디스 내 데이터 삽입을 위한 메소드로 콜백이 없는 단축 메소드
     * @param key 레디스 유니크키
     * @param object 삽입된 오브젝트
     * @return 정상 삽입 여부
     */
    public boolean put(String key, Object object){
        return put(key, object, null);
    }

    /**
     * 레디스에 삽입된 자바 클래스를 입력된 키로 검출하여 오브젝트로 반환한다.
     * @param key 레디스 유니크키
     * @param type 오브젝트 캐스팅 데이터 타입
     * @return 자바 오브젝트
     */
    public Object get(String key, Class type){

        Object retVal = null;

        try {
            jedis.connect();
            log.info("[JEDIS] GET OPERATION INVOKED WITH [KEY:" + key + "]");

            ObjectMapper mapper = new ObjectMapper();

            String object = jedis.get(key);
            if(object == null) return retVal;

            retVal = mapper.readValue(object, type);

        }catch (Exception e){
            e.printStackTrace();
            if(jedis != null) jedis.close();
        }

        return retVal;
    }

    /**
     * 레디스 정규표현을 Verbatim하게 전환하기 위한 캐스터
     * @param pattern 키 패턴
     * @return 이스케이프된 키 패턴
     */
    private String replaceRedisRegex(String pattern){
        return pattern.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]").replaceAll("\\-", "\\\\-");
    }

    /**
     * 패턴키로 해당 패턴을 가진 모든 키를 이터레이터로 이용하여 모든 밸류페어를 리스트로 반환한다.
     * @param pattern 키 패턴
     * @param type 리스트 오브젝트의 데이터타입
     * @param <T> 묵시적 캐스트를 위한 시그니쳐 삽입(무의미)
     * @return 오브젝트 리스트
     */
    public <T>List<T> getList(String pattern, Class type){
        List<T> retVal = new ArrayList<T>();

        pattern = replaceRedisRegex(pattern);

        try {
            jedis.connect();
            log.info("[JEDIS] GET_LIST OPERATION INVOKED WITH [Pattern:" + pattern + "]");

            ObjectMapper mapper = new ObjectMapper();

            Set<String> object = jedis.keys("*" + pattern + "*");
            if(object == null || object.size() == 0) return retVal;

            Iterator<String> iterator = object.iterator();

            while(iterator.hasNext()) {
                String key = iterator.next();
                String json = jedis.get(key);
                T unit = (T)mapper.readValue(json, type);
                retVal.add(unit);
            }

        }catch (Exception e){
            e.printStackTrace();
            if(jedis != null) jedis.close();
        }

        return retVal;
    }

}