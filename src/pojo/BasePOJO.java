package pojo;

import java.io.Serializable;

/**
 * @author 함의진
 * POJO 객체 랩핑 클래스
 * - 이를 상속하는 모든 클래스는 POJO(Plain Old Java Object) 형식을 가지며, 별도의 인터페이스 상속을 해서는 안된다.
 */
public class BasePOJO implements Serializable{

    protected static final int ARRAY_START_RANGE = 8;
    protected static final int ARRAY_END_LENGTH = 3;

    protected Class classType;
}
