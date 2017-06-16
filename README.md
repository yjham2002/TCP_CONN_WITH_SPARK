
# TCP/IP 서버

- 경량 REST 프레임워크가 적용된 TCP/IP 통신을 위한 소켓 서버

## Class Description

#### Service Provider
- 가변 스레딩 처리를 위한 운영 스레드로 싱글턴 패턴 및 데코레이터 패턴 기반으로 작성
1. 싱크로나이즈된 해시맵으로 클라이언트들을 농장코드 기반의 유니크키로 관리하며, 본 해시맵의 레퍼런스 포인터는 개발 편의를 위해 각 스레드마다 레퍼런스 포인터로 단일 포함 관계를 이룬다.
2. 서버 소켓으로부터 새로운 IP의 요청이 있을 때마다 셀렉터와 채널을 증가시키며, 각 스레드별로 바이트 어레이 관리를 위한 스택을 크게 사용하고 연속적인 처리에 대응해야 하므로, Selector를 이용하여 Non-Blocking 소켓 채널로 관리한다.

- 서버 시동 예시
```java
ServiceProvider serviceProvider = ServiceProvider.getInstance(ServerConfig.SOCKET_PORT).start();
```

#### ProtocolResponder
- 가변 스레딩 처리를 위한 단위로 ServiceProvider로부터 Aggregation 관계를 이룬다.
- Selector를 이용하여 요청을 분할 처리하며, Non-Blocking 방식이기에 소켓에 대한 별도의 설정은 적용되지 않는다.

#### ByteSerial
- 충분한 사이즈로 입력스트림을 수용하므로, 이를 트림하고 손상 여부를 생성과 함께 판단하는 프로토콜 패킷 추상화 클래스
- 두 가지의 생성자를 가지며, 바이트 어레이 하나만을 파라미터로 갖는 시그니쳐의 생성자는 수신 프로토콜을 추상화하며, 바이트 어레이와 함께 정수형 타입을 수용하는 생성자의 경우, 발신용 프로토콜 구성을 위한 역할을 추상화한다.
1. Original 멤버 변수는 처음 입력된 바이트 어레이를 가지며, Processed는 트림된 바이트 어레이를 가짐
2. Loss의 Getter를 통해 손실 여부를 파악할 수 있음
3. 발신용 생성자의 타입은 클래스 내부에 정적으로 정의되어 있음

- 정적 선언 타입 종류
```java
public static final int TYPE_NONE = 0; // 미결정 타입 혹은 손상된 타입 (수신)
public static final int TYPE_INIT = 10; // 전원 인가 시 접속되는 프로토콜 (수신)
public static final int TYPE_SET = 20; // 데이터 업로드 주기 설정 프로토콜 타입 (발신)
public static final int TYPE_WRITE = 30; // 클라이언트로의 쓰기 요청 (발신)
public static final int TYPE_READ = 40; // 클라이언트로의 읽기 요청 (발신)
public static final int TYPE_WRITE_SUCC = 50; // 클라이언트로부터 수신되는 쓰기 성공 프로토콜 (수신)
public static final int TYPE_READ_SUCC = 60; // 클라이언트로부터 수신되는 쓰기 성공 프로토콜 (수신)
public static final int TYPE_ALERT = 70; // 클라이언트에게 경고를 전송하기 위한 프로토콜 (발신)
```

### Pair
- 제네릭 데이터 타입으로 두 가지 값을 가질 수 있는 밸류 페어 클래스

### ConstProtocol
- 프로토콜 관련 상수를 정의하며, 플래그 비트 값을 관리하는 정적 메소드를 포함한다.

### BasePOJO and Derived Classes (Plain Old Java Object)
- 특정 목적을 가진 데이터들을 캡슐화하는 클래스로, 바이트 어레이 계산 및 인코딩/디코딩을 수행하며, DB 및 REDIS I/O의 단위체로서 이용된다.

### RedisWrapper
- REDIS를 키 기반으로 쿼리잉하고 리스트 및 객체 등으로 삽입/추출할 수 있도록 구성된 유틸리티 클래스로 이를 상속하여 싱글턴 패턴으로 이용해야 한다.

```java
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
        init();
    }

    public void init(){
        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(400);
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
            log.info("Put Request is not sound - Skipping");
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

            Set<String> object = null;

            try {
                object = jedis.keys("*" + pattern + "*");
            }catch(Exception e){
                System.out.println("Key Execution Error :: Skipping");
                init();
                return retVal;
            }
            if(object == null || object.size() == 0) return retVal;

            Iterator<String> iterator = object.iterator();

            while(iterator.hasNext()) {
                String key = iterator.next();
                try {
                    String json = jedis.get(key);
                    T unit = (T) mapper.readValue(json, type);
                    retVal.add(unit);
                }catch(Exception e){
                    System.out.println("Parsing Error :: Skipping");
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            if(jedis != null) jedis.close();
        }

        return retVal;
    }

}

```