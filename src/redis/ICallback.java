package redis;

import java.io.Serializable;

/**
 * @author 함의진
 * 레디스 포스트 프로세스 콜백 인터페이스
 */
public interface ICallback extends Serializable {

    /**
     * 후처리 프로세스 콜백 인터페이스 메소드
     */
    public void postExecuted();
}
