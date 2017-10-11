package server.response;

import org.codehaus.jackson.map.ObjectMapper;
import utils.Log;

import java.io.IOException;

/**
 * Created by a on 2017-06-07.
 */
public class Response {
    private int returnCode;
    private String returnMessage;
    private Object data;

    public Response(){}

    public Response(int code, String message, Object object){
        this();
        this.returnCode = code;
        this.returnMessage = message;
        this.data = object;

        Log.i("Response Generated [" + this.returnCode + "/" + this.returnMessage + "/ Data Attached :" + (this.data != null) + "]");
    }

    public static String response(int code, String message, Object object) throws IOException{
        Response response = new Response(code, message, object);
        String res = new ObjectMapper().writeValueAsString(response);

        return res;
    }

    public static String response(int code, String message) throws IOException{
        return response(code, message, null);
    }

    public static String response(int code) throws IOException{
        return response(code, null);
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
