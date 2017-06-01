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

/**
 * @author 함의진
 * @version 1.0
 * 레디스 CRUD 연산을 위한 기저 랩핑 클래스로 직접 생성해서 사용하지 않아야 함
 * 이를 상속하는 RedisManager의 싱글턴 패턴 인스턴스를 이용해야 함
 */
public class RedisWrapper extends ServerConfig{

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

    public boolean put(String key, Object object){
        return put(key, object, null);
    }

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

}
