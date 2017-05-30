package models;

import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by a on 2017-05-30.
 */
public class RestProcessor {
    /**
     * req 데이터 파싱
     *
     * @param request
     * @return
     */
    public static DataMap makeProcessData(HttpServletRequest request){
        DataMap params = new DataMap();
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()){
            String name = (String) paramNames.nextElement();
            String[] value = request.getParameterValues(name);
            // String value = request.getParameter(name);

            if(value.length > 1)
                params.put(name, value);
            else
                params.put(name, value[0]);
        }

        return params;

    }

    /**
     * 결과 셋 만들기
     *
     * @param returnCode
     * @param returnMessage
     * @param entity
     * @param addData
     * @return
     */
    public static String makeResultJson(int returnCode, String returnMessage, Object entity, Object addData) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        RetJsonBean resultData = new RetJsonBean();
        resultData.setReturnCode(returnCode);
        resultData.setReturnMessage(returnMessage);
        resultData.setEntity(entity);
        resultData.setAddData(addData);

        return objectMapper.writeValueAsString(resultData);

    }

    /**
     * 결과 셋 만들기
     *
     * @param returnCode
     * @param returnMessage
     * @param entity
     * @return
     */
    public static String makeResultJson(int returnCode, String returnMessage, Object entity) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        RetJsonBean resultData = new RetJsonBean();
        resultData.setReturnCode(returnCode);
        resultData.setReturnMessage(returnMessage);
        resultData.setEntity(entity);

        return objectMapper.writeValueAsString(resultData);

    }

    /**
     * 결과 셋 만들기
     *
     * @param returnCode
     * @param returnMessage
     * @return
     */
    public static String makeResultJson(int returnCode, String returnMessage) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        RetJsonBean resultData = new RetJsonBean();
        resultData.setReturnCode(returnCode);
        resultData.setReturnMessage(returnMessage);

        return objectMapper.writeValueAsString(resultData);

    }
}
