package server.whois;

import configs.ServerConfig;

import java.io.UnsupportedEncodingException;

public class WhoisSmsSVC extends ServerConfig implements ISMSSyncSVC {

    private String id = ""	;
    private String pw = "" ;
    private String defaultFromPhone = "" ;

    private WhoisSmsSVC(String id, String pw, String defaultFromPhone)
    {
        this.id = id ;
        this.pw = pw ;
        this.defaultFromPhone = defaultFromPhone ;
    }

    public WhoisSmsSVC(){
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

        sms.login(id, pw) ;
        // sms.setUtf8();
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
         */

        if( rcode > 0 ) {
            System.out.println("[WhoisSmsSVC] rcode =" + rcode + ":::" + rmsg + " [Point] : " + lastPoint + " [toPhone] : " + toPhone) ;
        }

        return rcode ;
    }

    @Override
    public int sendSMS(String toPhone, String msg) {
        return sendSMS(toPhone,this.defaultFromPhone,msg) ;
    }

    public static void main(String... args){
        WhoisSmsSVC whoisSmsSVC = new WhoisSmsSVC();
        whoisSmsSVC.sendSMS("010-2918-9484", "test");
    }

}
