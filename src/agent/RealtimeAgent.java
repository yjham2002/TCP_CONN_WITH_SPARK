package agent;

import databases.DBManager;
import pojo.RealtimePOJO;
import server.engine.ServiceProvider;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RealtimeAgent implements IAgent {

    private static RealtimeAgent instance;
    private BlockingQueue<RealtimePOJO> offerList;

    public static RealtimeAgent getInstance(){
        if(instance == null) instance = new RealtimeAgent();
        return instance;
    }

    private RealtimeAgent(){
        offerList = new LinkedBlockingQueue<>();
    }

    @Override
    public void start(int poolSize){
        for(int e = 0; e < poolSize; e++){
            final int temp = e;
            new Thread(() -> {
                System.out.println("[RealtimeAgent] Stand-By - Thread[" + temp + "]");

                while(true){
                    try {
                        RealtimePOJO r = offerList.take();
                        String sql = r.getInsertSQL();
                        DBManager.getInstance().execute(sql);
                        System.out.println("[RealtimeAgent] POOL SIZE : " + poolSize + " / Queued : " + offerList.size());
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public BlockingQueue<RealtimePOJO> getOfferList() {
        return offerList;
    }
}
