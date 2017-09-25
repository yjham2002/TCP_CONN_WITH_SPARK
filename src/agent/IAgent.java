package agent;

/**
 * @author Euijin.Ham
 * @version 1.0.0
 * @description 에이전트 클래스 설계 강제를 위한 인터페이스
 */
public interface IAgent {
    /**
     * A Method for calling an agent to start explicitly
     */
    void start(int poolSize);
}
