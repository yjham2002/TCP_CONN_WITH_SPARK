package server.whois;

import configs.ServerConfig;
import constants.ConstProtocol;
import mysql.DBManager;
import utils.HexUtil;
import utils.SohaProtocolUtil;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.logging.Logger;

public class SMSService extends ServerConfig implements ISMSSyncSVC {

    private Logger logger;
    private String id = ""	;
    private String pw = "" ;
    private String defaultFromPhone = "" ;

    public SMSService(String id, String pw, String defaultFromPhone) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.id = id ;
        this.pw = pw ;
        this.defaultFromPhone = defaultFromPhone ;
    }

    public SMSService(){
        this.logger = Logger.getLogger(this.getClass().getName());
        this.id = SMS_ID;
        this.pw = SMS_PW;
        this.defaultFromPhone = SMS_DEFAULT_PHONE;
    }

    @Override
    public int sendSMS(String toPhone, String fromPhone, String msg) {

        whoisSMS sms = new whoisSMS() ;

        try {
            msg = new String(msg.getBytes("MS949"),"ISO-8859-1") ;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sms.login(this.id, this.pw) ;
//        sms.setUtf8();
        sms.setParams(toPhone, fromPhone, msg, "0") ;
        sms.emmaSend() ;


        int rcode = sms.getRetCode() ;
        String rmsg = sms.getRetMessage() ;
        int lastPoint = sms.getLastPoint() ;

        /**
         * Send Message
         * success : Code=>0, CodeMsg=>Success!!, LastPoint=>9999
         * fail 1  : Code=>100, CodeMsg=>Not Registered ID
         * fail 2  : Code=>200, CodeMsg=>Not Enough Point
         * fail 3  : Code=>300, CodeMsg=>Login Fail
         * fail 4  : Code=>400, CodeMsg=>No Valid Number
         * fail 5  : Code=>500, CodeMsg=>No Valid Message
         * fail 6  : Code=>600, CodeMsg=>Auth Fail
         * fail 7  : Code=>700, CodeMsg=>Invalid Recall Number
         * fail 8  : Coude=>800, CodeMsg=>Send Number Auth Fail
         */

        if( rcode > 0 ) {
            logger.info("returnCode =" + rcode + ":::" + rmsg + " [Point] : " + lastPoint + " [toPhone] : " + toPhone); ;
        }

        return rcode ;
    }

    @Override
    public int sendSMS(String toPhone, String msg) {
        System.out.println("Sending SMS :: " + toPhone + " :: msg ::" + msg);
        return sendSMS(toPhone,this.defaultFromPhone, msg) ;
    }

}
