package redis;

import models.ByteSerial;
import org.apache.commons.lang3.SerializationUtils;
import pojo.RealtimePOJO;

import java.util.Arrays;

/**
 * Created by a on 2017-05-31.
 */
public class RedisTestMain {
    public static void main(String... args){

        RealtimePOJO a = new RealtimePOJO(new ByteSerial(new byte[]{83, 84, 48, 48, 55, 56, 48, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, -108, 0, -10, 1, -124, 0, 0, 0, 0, -116, 0, 0, 13, 6, 0, -123, -127, 69, 64, -121, 1, 71, -64, 70, -128, -122, 65, -126, 1, 66, -64, 67, -128, -125, 65, 65, 0, -127, -63, -128, -127, 64, 64, 0, 0, 0, 28, 0, 56, 0, 84, 0, 112, 0, -116, 0, -88, 0, -60, 0, -32, 0, -4, -109, 117, 63, -120, 122, -31, 63, -108, 71, -82, 63, -95, 20, 123, 63, -82, 112, -92, 63, -67, -52, -51, 63, -52, 112, -92, 63, -35, 92, 41, 63, -17, 71, -82, 64, 1, -123, 31, 64, 11, 102, 102, 64, 22, -113, 92, 64, 34, 0, 0, 2, -108, 0, -10, 1, -124, 0, 0, 64, 74, -103, -102, 64, 89, 61, 113, 64, 106, -123, 31, 64, 123, 10, 61, 64, -121, -93, -41, 64, -112, 51, 51, 0, 0, 0, 0, 0, 0, 0, 0, 9, 55, 102, 102, 64, -66, 0, 0, 0, 0, 3, -14, 3, -14, 0, -56, 0, 0, 0, 0, 0, 0, 69, -45, 0, 0, 0, 39, 6, -82, 2, 19, 0, 0, 7, -32, 4, -79, 5, -108, 0, 0, 0, 0, 0, 0, -11, 13, 10}));
//        byte[] ser = SerializationUtils.serialize(a);
//        RealtimePOJO b = (RealtimePOJO)SerializationUtils.deserialize(ser);
//
//        System.out.println(Arrays.toString(b.getByteSerial().getProcessed()));
//        RedisManager.getInstance().put("test", new RealtimePOJO(new ByteSerial(new byte[]{83, 84, 48, 48, 55, 56, 48, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, -108, 0, -10, 1, -124, 0, 0, 0, 0, -116, 0, 0, 13, 6, 0, -123, -127, 69, 64, -121, 1, 71, -64, 70, -128, -122, 65, -126, 1, 66, -64, 67, -128, -125, 65, 65, 0, -127, -63, -128, -127, 64, 64, 0, 0, 0, 28, 0, 56, 0, 84, 0, 112, 0, -116, 0, -88, 0, -60, 0, -32, 0, -4, -109, 117, 63, -120, 122, -31, 63, -108, 71, -82, 63, -95, 20, 123, 63, -82, 112, -92, 63, -67, -52, -51, 63, -52, 112, -92, 63, -35, 92, 41, 63, -17, 71, -82, 64, 1, -123, 31, 64, 11, 102, 102, 64, 22, -113, 92, 64, 34, 0, 0, 2, -108, 0, -10, 1, -124, 0, 0, 64, 74, -103, -102, 64, 89, 61, 113, 64, 106, -123, 31, 64, 123, 10, 61, 64, -121, -93, -41, 64, -112, 51, 51, 0, 0, 0, 0, 0, 0, 0, 0, 9, 55, 102, 102, 64, -66, 0, 0, 0, 0, 3, -14, 3, -14, 0, -56, 0, 0, 0, 0, 0, 0, 69, -45, 0, 0, 0, 39, 6, -82, 2, 19, 0, 0, 7, -32, 4, -79, 5, -108, 0, 0, 0, 0, 0, 0, -11, 13, 10})));

//        RealtimePOJO b = RedisManager.getInstance().get("test");

        try {
//            new Thread(()->{
//                new Thread(()->{
//                    Thread ss = new Thread(()->{
//                        RedisManager.getInstance().put("hello4", a);
//                    });
//                    Thread dd = new Thread(()->{
//                        RedisManager.getInstance().put("hello5", a);
//                    });
//                    ss.start();
//                    dd.start();
//                }).start();
//            }).start();

            RealtimePOJO obj1 = (RealtimePOJO) RedisManager.getInstance().get("hello", RealtimePOJO.class);
            RealtimePOJO obj2 = (RealtimePOJO) RedisManager.getInstance().get("hello2", RealtimePOJO.class);
            RealtimePOJO obj3 = (RealtimePOJO) RedisManager.getInstance().get("hello3", RealtimePOJO.class);
            RealtimePOJO obj4 = (RealtimePOJO) RedisManager.getInstance().get("hello4", RealtimePOJO.class);
            RealtimePOJO obj5 = (RealtimePOJO) RedisManager.getInstance().get("hello5", RealtimePOJO.class);

            System.out.println(Arrays.toString(obj1.getByteSerial().getProcessed()));
            System.out.println(Arrays.toString(obj2.getByteSerial().getProcessed()));
            System.out.println(Arrays.toString(obj3.getByteSerial().getProcessed()));
            System.out.println(Arrays.toString(obj4.getByteSerial().getProcessed()));
            System.out.println(Arrays.toString(obj5.getByteSerial().getProcessed()));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
