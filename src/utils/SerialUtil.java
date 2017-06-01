package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Redis Serialization/Deserialization Utility class
 * @author 김태영 대표님
 * Improved By 함의진
 */
public class SerialUtil {

    public static byte[] toByteArray(Object obj) {

        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        }
        catch (IOException ex) {
            //TODO: Handle the exception
            ex.printStackTrace();
        }
        finally {
            try {
                if(oos != null)
                    oos.close();
                if(oos != null)
                    bos.close();
            } catch (Exception e) {

                // TODO: handle exception

                e.printStackTrace();
            }
        }
        return bytes;
   }

    public static Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        }
        catch (IOException ex) {
            //TODO: Handle the exception
            ex.printStackTrace();
        }
        catch (ClassNotFoundException ex) {
            //TODO: Handle the exception
            ex.printStackTrace();
        }
        return obj;
   }

}
