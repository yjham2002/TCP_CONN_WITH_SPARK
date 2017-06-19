package server.whois;


public interface ISMSSyncSVC {

    int sendSMS(String toPhone,String fromPhone,String msg);
    int sendSMS(String toPhone,String msg);

}
