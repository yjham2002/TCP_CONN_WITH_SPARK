package redis;

/**
 * Created by a on 2017-05-31.
 */
public class RedisManager extends RedisWrapper{

    private static RedisManager instance;

    private RedisManager(){
        super();
    }

    public static RedisManager getInstance(){
        if(instance == null) instance = new RedisManager();
        return instance;
    }

}
