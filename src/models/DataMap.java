package models;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

@SuppressWarnings("serial")
public class DataMap extends HashMap<String, Object>
{

    public DataMap()
    {
        super();
    }

    public DataMap(HashMap<String, Object> data)
    {
        super();

        if(data != null)
        {
            for (Map.Entry<String, Object> entry : data.entrySet())
            {

                Object value = entry.getValue();

                if(value.getClass() == java.util.HashMap.class)
                {
                    this.put(entry.getKey(), new ObjectMapper().convertValue(value, DataMap.class));
                }
                else if(value.getClass() == java.util.ArrayList.class)
                {
                    try
                    {
                        this.put(entry.getKey(), new ObjectMapper().convertValue(value, new TypeReference<List<DataMap>>()
                        {
                        }));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        this.put(entry.getKey(), entry.getValue());
                    }

                }
                else
                {
                    this.put(entry.getKey(), entry.getValue());
                }

            }
        }

    }

    @Override
    public Object put(String key, Object value)
    {
        return super.put(key, value);
    }

    @Override
    public Object get(Object key)
    {

        if(key.getClass() == String.class)
            return super.get(key.toString());

        return super.get(key);
    }

    /**
     * 데이터 맵으로 변경해서 전달
     *
     * @param key
     * @return
     */
    public DataMap getDataMap(Object key)
    {
        DataMap retVal = null;
        Object value = get(key.toString());

        if(value == null)
            return null;

        retVal = (DataMap) value;

        return retVal;
    }

    /**
     * map으로 return
     * @param key
     * @return
     */
    public Map<String, Object> getHashMap(Object key)
    {
        Map<String, Object> retVal = null;
        Object value = get(key.toString());

        if(value == null)
            return null;

        retVal = (Map<String, Object>) value;

        return retVal;
    }
    /**
     * 데이터 맵 리스트로 변경
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<DataMap> getDataMapList(Object key)
    {
        List<DataMap> retVal = null;
        Object value = get(key.toString());

        if(value == null)
            return null;

        retVal = (List<DataMap>) value;

        return retVal;
    }

    /**
     * 배열일 경우 배열의 첫번째 요소를 리턴한다.
     *
     * @param key
     * @return
     */
    public String getString(Object key)
    {

        String string = "";

        Object obj = get(key.toString());

        if(obj == null)
        {
            string = "";
        }
        else if(obj.getClass() == String[].class)
        {
            String[] array = (String[]) obj;
            string = array[0];
        }
        else if(obj.getClass() == String.class)
        {
            string = (String) obj;
        }
        else
        {
            string = obj.toString();
        }

        return string;
    }

    /**
     * 스트링 배열 필요
     *
     * @param key
     * @return
     */
    public String[] getStringToArr(Object key, String regex)
    {

        String[] stringArr;

        Object obj = get(key.toString());
        if(obj instanceof String)
        {
            stringArr = ((String) obj).split(regex);
        }
        else
        {
            stringArr = null;
        }

        return stringArr;
    }

    /**
     * 구분자로 된 스트링을 int 배열로 변환
     *
     * @param key
     * @param regex
     * @return
     */
    public int[] getStringToIntArr(Object key, String regex)
    {

        int[] retArr;

        Object obj = get(key.toString());

        if(obj.getClass() == String.class)
        {
            if(obj.equals(""))
                retArr = null;
            else
            {
                String[] stringArr = ((String) obj).split(regex);
                retArr = new int[stringArr.length];
                for (int i = 0; i < stringArr.length; i++)
                {
                    retArr[i] = Integer.parseInt(stringArr[i]);
                }
            }
        }
        else
        {
            retArr = null;
        }

        return retArr;
    }

    /**
     * 스트링 배열 필요
     *
     * @param key
     * @return
     */
    public String[] getStringArr(Object key)
    {

        String[] stringArr;
        String[] nullData = new String[0];

        Object obj = get(key.toString());

        if(obj == null)
        {
            return nullData;
        }
        else if(obj.getClass().isArray())
        {
            stringArr = (String[]) obj;
        }
        else if(obj.getClass() == String.class)
        {
            stringArr = new String[] { (String) obj };
        }
        else
        {
            stringArr = null;
        }

        return stringArr;
    }

    private String convert(double doubleObj, String pattern) throws Exception
    {

        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(doubleObj).toString();
    }

    public String getInteger(Object key) throws Exception
    {
        double dval = this.getDouble(key);

        return this.convert(dval, "#");
    }

    /**
     * 기본값을 세팅하면 값이 존재하지 않을 때 기본값을 리턴한다.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(Object key, String defaultValue)
    {

        String string = "";

        Object obj = get(key.toString());
        if(obj == null)
            string = "";
        else if(obj.getClass() == String[].class)
        {
            String[] array = (String[]) obj;
            string = array[0];
        }
        else if(obj.getClass() == String.class)
        {
            string = (String) obj;
        }

        if("".equals(string))
            return defaultValue;
        else
            return getString(key.toString());

    }

    /**
     * 값을 int 형식으로 리턴한다.
     *
     * @param key
     * @return
     */
    public int getInt(Object key)
    {

        int number = 0;

        String str = this.getNumberValueToString(key);

        number = Integer.parseInt(str);

        return number;

    }

    public int getInt(Object key, int dvalue)
    {

        int number = 0;

        String str = this.getNumberValueToString(key);

        if(str.equals("0"))
            number = dvalue;
        else
            number = Integer.parseInt(str);

        return number;

    }

    public int[] getIntArr(Object key)
    {
        Object value = this.get(key);
        int[] retVal = null;
        int[] nullData = new int[0];

        // if(value != null)
        // {
        // Log.i("getIntArr - valArr class : " + value.getClass());
        // }
        // else
        // {
        // Log.i("getIntArr - valArr class null : " + value);
        // }

        if(value == null)
        {
            return nullData;
        }
        else if(value.getClass().isArray())
        {
            Object[] valArr = (Object[]) value;
            retVal = new int[valArr.length];

            for (int i = 0; i < valArr.length; i++)
            {

                // Log.i("getIntArr - valArr[" + i + "] : " + valArr[i]);
                if(this.checkNumberFormat(valArr[i]))
                {
                    retVal[i] = Integer.parseInt(String.valueOf(valArr[i]));
                }
                else
                {
                    retVal[i] = 0;
                }
            }
        }
        else if(value.getClass() == String.class)
        {
            retVal = new int[1];
            retVal[0] = Integer.parseInt(String.valueOf(value));
        }
        else if(value.getClass() == Integer.class)
        {
            retVal = new int[1];
            retVal[0] = Integer.parseInt(String.valueOf(value));
        }

        return retVal;
    }

    private String getNumberValueToString(Object key)
    {

        Object obj = this.get(key.toString());

        if(obj == null)
        {
            return "0";
        }
        else if(obj.getClass() == String[].class)
        {

            String[] array = (String[]) obj;
            if(this.checkNumberFormat(array[0]))
            {
                return String.valueOf(array[0]);
            }
        }
        else
        {
            if(this.checkNumberFormat(obj))
                return String.valueOf(obj);
        }

        return "0";

    }

    private boolean checkNumberFormat(Object value)
    {

        try
        {
            if(value == null)
            {
                return false;
            }
            else if(value.getClass() == String.class)
            {
                Double.parseDouble((String) value);
                return true;
            }
            else if(value.getClass() == Integer.class || value.getClass() == Long.class || value.getClass() == Double.class || value.getClass() == Float.class || value.getClass() == BigDecimal.class)
            {
                return true;
            }
            else
            {
                return false;
            }

        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * 값을 long 형식으로 리턴한다.
     *
     * @param key
     * @return
     */
    public long getLong(Object key)
    {

        long number = 0;

        String str = this.getNumberValueToString(key);

        double dnumber = 0L;

        if("".equals(str))
            dnumber = 0L;
        else
            dnumber = Double.parseDouble(str); // 이거

        // number = Long.parseLong(str) ;

        number = (long) dnumber; // 이거

        return number;

    }

    public double getDouble(Object key)
    {
        double number = 0;

        String str = getString(key);

        if("".equals(str))
            number = 0;
        else
            number = Double.parseDouble(str);

        return number;

    }

    public double getDouble(Object key, int seq)
    {
        return Double.parseDouble(String.format("%#." + Integer.toString(seq) + "f", (this.getDouble(key) * 100)));
    }

    public Date getDate(Object key) throws ParseException
    {
        Date date = new Date();
        String str = "";

        Object obj = get(key.toString());

        if(obj.getClass() == String[].class)
        {

            String[] array = (String[]) obj;

            if("".equals(array[0]))
            {
            }
            else
                date = parseKoreanString(array[0]);
        }
        else if(obj.getClass() == String.class)
        {
            str = (String) obj;
            date = parseKoreanString(str);
        }

        return date;
    }

    public Date getFullDate(Object key) throws ParseException
    {
        Date date = new Date();
        String str = "";

        Object obj = get(key.toString());

        if(obj.getClass() == String[].class)
        {

            String[] array = (String[]) obj;

            if("".equals(array[0]))
            {
            }
            else
                date = parseKoreanFullString(array[0]);
        }
        else if(obj.getClass() == String.class)
        {
            str = (String) obj;
            date = parseKoreanFullString(str);
        }

        return date;
    }

    /**
     * 원하는 형식의 날짜로 가져오기
     *
     * @param key
     * @param fmt
     *            "yyyy-MM-dd HH:mm:ss"
     * @return
     * @throws ParseException
     */
    public String getFmDate(String key, String fmt) throws ParseException
    {

        if(get(key.toString()).equals(""))
            return "";
        else if(get(key.toString()).toString().length() == 10)
        {
            return get(key.toString()).toString();
        }

        DateFormat df = new SimpleDateFormat(fmt);
        return df.format(getFullDate(key));
    }

    private static Date parseKoreanFullString(String str) throws ParseException
    {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = df.parse(str);

        return date;
    }

    private static Date parseKoreanString(String str) throws ParseException
    {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = df.parse(str);

        return date;
    }

    public String getStringSafeArray(Object key, int seq)
    {
        String temp = "";

        Object obj = get(key.toString());

        if(obj.getClass() == String[].class)
        {
            String[] array = (String[]) obj;
            if(seq < array.length)
            {
                temp = array[seq];
            }
        }
        else if(obj.getClass() == String.class)
            temp = (String) obj;
        else
            temp = obj.toString();

        return temp;

    }

    public String getStringFromArray(Object key, String identifier)
    {

        String temp = "";

        Object obj = get(key.toString());

        if(obj.getClass() == String[].class)
        {

            temp = StringUtils.join((Object[]) obj, identifier);

        }
        else if(obj.getClass() == String.class)
            temp = (String) obj;
        else
            temp = obj.toString();

        return temp;

    }

    public String getJoined(Object key, String str)
    {
        return getStringFromArray(key, str);
    }

    public String getStringReplaceKey(String key, String replaceKey)
    {
        String virName = "";

        virName = this.getString(key, "");

        if(virName.equals(""))
            virName = this.getString(replaceKey, "");

        return virName;
    }

    public String getFmDateUpper(String key, String fmt) throws ParseException
    {

        return getFmDate(((String) key), fmt);
    }

    public String getStringUpper(Object key)
    {
        return getString(((String) key));
    }

    public String getStringUpper(Object key, String defaultValue)
    {
        return getString(((String) key), defaultValue);
    }

    public boolean isSetData(Object key)
    {
        if(super.containsKey(key))
        {
            Object value = super.get(key);

            if(value == null)
                return false;
            else if(value.getClass() == String.class)
            {
                if(!value.equals(""))
                    return true;
                else
                    return false;
            }
            else if(value.getClass().isArray())
            {

                if(((Object[]) value).length > 0)
                    return true;
                else
                    return false;
            }
            else
            {
                return true;
            }
        }

        return false;
    }

}
