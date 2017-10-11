package server.whois;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import utils.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

public class whoisSMS {
    private String mId;
    private String mPass;
    private String mTo;
    private String mFrom;
    private String mMessage;
    private String mDate;
    private String mYear;
    private String mMonth;
    private String mSmsType;
    private int mRetCode;
    private String mRetMessage;
    private Hashtable mStatistic;
    private int mLastPoint;
    private static String server_url = "http://www.whoisweb.net/emma/API/EmmaSend_JSP.php";

    public whoisSMS() {
    }

    public void setId(String arg) {
        this.mId = arg;
    }

    public void setPass(String arg) {
        this.mPass = arg;
    }

    public void setTo(String arg) {
        this.mTo = arg;
    }

    public void setFrom(String arg) {
        this.mFrom = arg;
    }

    public void setMessage(String arg) {
        this.mMessage = arg;
    }

    public void setDate(String arg) {
        this.mDate = arg;
    }

    public void setYear(Integer arg) {
        this.mYear = arg.toString();
    }

    public void setMonth(Integer arg) {
        this.mMonth = arg.toString();
    }

    public void setSmsType(String arg) {
        this.mSmsType = arg.toString();
    }

    public String getDate() {
        return this.mYear + "-" + this.mMonth;
    }

    public void login(String pId, String pPass) {
        this.setId(pId);
        this.setPass(pPass);
    }

    public void setParams(String pId, String pPass, String pTo, String pFrom, String pMessage, String pDate) {
        this.setId(pId);
        this.setPass(pPass);
        this.setTo(pTo);
        this.setFrom(pFrom);
        this.setMessage(pMessage);
        this.setDate(pDate);
    }

    public void setParams(String pTo, String pFrom, String pMessage, String pDate) {
        this.setTo(pTo);
        this.setFrom(pFrom);
        this.setMessage(pMessage);
        this.setDate(pDate);
    }

    public void setParams(String pTo, String pFrom, String pMessage, String pDate, String pSmsType) {
        this.setTo(pTo);
        this.setFrom(pFrom);
        this.setMessage(pMessage);
        this.setDate(pDate);
        this.setSmsType(pSmsType);
    }

    public void setParams(String pId, String pPass, Integer pYear, Integer pMonth) {
        this.setId(pId);
        this.setPass(pPass);
        this.setYear(pYear);
        this.setMonth(pMonth);
    }

    public void setParams(Integer pYear, Integer pMonth) {
        this.setYear(pYear);
        this.setMonth(pMonth);
    }

    public void setUtf8() {
        server_url = "http://www.whoisweb.net/emma/API/EmmaSend_JSP_All.php";
    }

    public int getRetCode() {
        return this.mRetCode;
    }

    public String getRetMessage() {
        return this.mRetMessage;
    }

    public int getLastPoint() {
        return this.mLastPoint;
    }

    public Hashtable getStatistic() {
        return this.mStatistic;
    }

    public void emmaSend() {
        try {
            XmlRpcClient exception = new XmlRpcClient(server_url);
            Vector params = new Vector();
            params.addElement(this.mId.toString());
            params.addElement(this.mPass.toString());
            params.addElement(this.mTo.toString());
            params.addElement(this.mFrom.toString());
            params.addElement(this.mMessage.toString());
            params.addElement(this.mDate.toString());
            if(this.mSmsType == null) {
                this.mSmsType = new String();
                this.mSmsType = "";
            }

            params.addElement(this.mSmsType.toString());
            Hashtable result = (Hashtable)exception.execute("EmmaSend", params);
            if(result.get("Code") instanceof String) {
                String retCode = (String)result.get("Code");
                this.mRetCode = (new Integer(retCode)).intValue();
            } else {
                this.mRetCode = ((Integer)result.get("Code")).intValue();
            }

            if(this.mRetCode == 0) {
                this.mLastPoint = ((Integer)result.get("LastPoint")).intValue();
            }

            this.mRetMessage = result.get("CodeMsg").toString();
        } catch (XmlRpcException var5) {
            System.err.println("JavaClient: XML-RPC Fault #" + Integer.toString(var5.code) + ": " + var5.toString());
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    public void emmaPoint() {
        try {
            XmlRpcClient exception = new XmlRpcClient(server_url);
            Vector params = new Vector();
            params.addElement(this.mId.toString());
            params.addElement(this.mPass.toString());
            Hashtable result = (Hashtable)exception.execute("EmmaPoint", params);
            if(result.get("Code") instanceof String) {
                String retCode = (String)result.get("Code");
                this.mRetCode = (new Integer(retCode)).intValue();
            } else {
                this.mRetCode = ((Integer)result.get("Code")).intValue();
            }

            if(this.mRetCode == 0) {
                this.mLastPoint = ((Integer)result.get("Point")).intValue();
            }

            this.mRetMessage = result.get("CodeMsg").toString();
        } catch (XmlRpcException var5) {
            System.err.println("JavaClient: XML-RPC Fault #" + Integer.toString(var5.code) + ": " + var5.toString());
        } catch (Exception var6) {
            System.err.println("JavaClient: " + var6.toString());
            var6.printStackTrace();
        }

    }

    public void emmaStatistic() {
        try {
            XmlRpcClient exception = new XmlRpcClient(server_url);
            Vector params = new Vector();
            params.addElement(this.mId.toString());
            params.addElement(this.mPass.toString());
            params.addElement(this.getDate());
            Hashtable result = (Hashtable)exception.execute("EmmaStatistic", params);
            this.mRetCode = ((Integer)result.get("Code")).intValue();
            if(this.mRetCode == 0) {
                this.mLastPoint = ((Integer)result.get("Point")).intValue();
                this.mStatistic = (Hashtable)result.get("Statistics");
            }

            this.mRetMessage = result.get("CodeMsg").toString();
        } catch (XmlRpcException var4) {
            System.err.println("JavaClient: XML-RPC Fault #" + Integer.toString(var4.code) + ": " + var4.toString());
        } catch (Exception var5) {
            System.err.println("JavaClient: " + var5.toString());
        }

    }

    public static void main(String[] args) {
        whoisSMS whois_sms = new whoisSMS();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        Date cNow = new Date();
        String toDayTime = sdf.format(cNow);
        Log.i(toDayTime);
        whois_sms.login("smstest", "smstest");
        whois_sms.setParams("01065134259", "15884259", "SMS JSPAPI TEST", toDayTime);
        whois_sms.setUtf8();
        whois_sms.emmaSend();
        Log.i("getRetCode : " + whois_sms.getRetCode());
        Log.i("getRetMessage : " + whois_sms.getRetMessage());
        Log.i("getLastPoint : " + whois_sms.getLastPoint());
        whois_sms.emmaPoint();
        Log.i("getRetCode : " + whois_sms.getRetCode());
        Log.i("getRetMessage : " + whois_sms.getRetMessage());
        Log.i("getLastPoint : " + whois_sms.getLastPoint());
    }
}
