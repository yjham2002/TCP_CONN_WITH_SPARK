package agent;

import constants.ConstProtocol;
import mysql.Cache;
import mysql.DBManager;
import pojo.RealtimePOJO;
import pojo.WrappedPOJO;
import server.whois.SMSService;
import utils.SohaProtocolUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 경보내역 감시를 위한 에이전트 싱글턴 클래스
 * @author 함의진
 * @version 1.0.0
 */
public class AlertAgent {

    private static BlockingQueue<WrappedPOJO> blockingQueue = new LinkedBlockingQueue<>();

    private static AlertAgent ourInstance = new AlertAgent();

    public static AlertAgent getInstance() {
        if(ourInstance == null) ourInstance = new AlertAgent();
        if(blockingQueue == null) blockingQueue = new LinkedBlockingQueue<>();
        return ourInstance;
    }

    public static BlockingQueue<WrappedPOJO> getBlockingQueue() {
        if(ourInstance == null) ourInstance = new AlertAgent();
        if(blockingQueue == null) blockingQueue = new LinkedBlockingQueue<>();
        return blockingQueue;
    }

    private AlertAgent() {
        Thread process = new Thread(() -> {
            System.out.println("[AlertAgent] Stand-By");

            final SMSService smsService = new SMSService();

            while(true){
                try {
                    WrappedPOJO wrappedPOJO = blockingQueue.take();
                    RealtimePOJO realtimePOJO = wrappedPOJO.getRealtimePOJO();

                    String farmString = wrappedPOJO.getFarmString();
                    String harvString = wrappedPOJO.getHarvString();

                    String farmName = Cache.getInstance().farmNames.get(farmString);
                    String harvName = Cache.getInstance().harvNames.get(Cache.getHarvKey(farmString, harvString));

                    if(SohaProtocolUtil.getErrorCount(realtimePOJO) > 0){

//                        prevErrorData = SohaProtocolUtil.getErrorArrayWithDB(farmString, harvString);

                        String errStartTime[] = SohaProtocolUtil.getStartTimes(realtimePOJO);
                        int errArray[] = SohaProtocolUtil.getErrorArray(realtimePOJO);
                        int errSMSarray[] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

                        System.out.println("[AlertAgent] Error Detected - Processing " + Arrays.toString(errArray));

                        boolean haveToSend = false;

                        String mapKey = farmString + "_" + harvString;
                        int[] thisPrev = SohaProtocolUtil.getErrorDataArrayBySQL(farmString, harvString);

                        try {
                            if (thisPrev != null) {
                                for (int err = 0; err < errArray.length; err++) {
                                    if (errArray[err] != thisPrev[err]) {
                                        String sql;
                                        if (errArray[err] == ConstProtocol.TRUE) {
                                            haveToSend = true;
                                            errSMSarray[err] = ConstProtocol.TRUE;
                                            sql = SohaProtocolUtil.getErrorSQL(farmString, harvString, err, "Y", errStartTime[err]);
                                        } else {
                                            sql = SohaProtocolUtil.getErrorSQL(farmString, harvString, err, "N", errStartTime[err]);
                                        }
                                        DBManager.getInstance().execute(sql);
                                    }
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                            System.out.println("========================================================");
                            System.out.println("Reinstanciating DBManager singleton instance. :: " + e.getMessage());
                            System.out.println("========================================================");
                        }

                        if(haveToSend){
                            try {
                                String sql = "SELECT * FROM sohatechfarmdb.farm_list WHERE farm_code = '" + farmString + "' LIMIT 1";
                                List<String> sms_arr = DBManager.getInstance().getStrings(sql, ConstProtocol.SMS_COLS);

                                int sendCnt = 0;

                                for(int ee = 0; ee < sms_arr.size(); ee++) {
                                    if (sms_arr.get(ee).equals("0") || Integer.parseInt(sms_arr.get(ee)) == 0) {
                                        errSMSarray[ee] = 0;
                                        sendCnt++;
                                    }
                                }

                                haveToSend = (sendCnt == 16);

                            }catch(Exception e){
                                System.out.println("SMS Array Error");
                                e.printStackTrace();
                            }
                        }

                        if(haveToSend) {

                            String msg = SohaProtocolUtil.getErrorMessage(errSMSarray, farmName, harvName);
                            List<String> phones = DBManager.getInstance().getStrings("SELECT farm_code, a_tel, b_tel, c_tel, d_tel FROM user_list WHERE farm_code='"+farmString+"' OR user_auth='A'", "a_tel", "b_tel", "c_tel", "d_tel");
                            for(String tel : phones) smsService.sendSMS(tel, msg);
                        }

                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
    }

}
