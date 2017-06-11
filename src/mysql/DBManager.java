package mysql;

import pojo.RealtimePOJO;
import redis.RedisManager;

import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author 함의진
 */
public class DBManager extends DBConstManager {

    public static DBManager instance;

    public static DBManager getInstance(){
        if(instance == null) instance = new DBManager();
        return instance;
    }

    private DBManager(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection( getConnectionInfo() , USERNAME, PASSWORD);
            st = connection.createStatement();
        } catch (SQLException se1) {
            se1.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (st != null) st.close();
            } catch (SQLException se2) {}
            try {
                if (connection != null) connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public boolean migrateFromRedis(){
        try {
            String pattern = RedisManager.getYYMMDDwithPostfix("-");

            List<RealtimePOJO> list = RedisManager.getInstance().getList(pattern, RealtimePOJO.class);

            Collections.sort(list, (o1, o2) -> {
                long t1 = Long.parseLong(o1.getRedisTime());
                long t2 = Long.parseLong(o2.getRedisTime());
                if(t1 == t2) return 0;
                else if(t1 > t2) return 1;
                else return -1;
            });

            long maxTime = getNumber("SELECT MAX(redisTime) AS num FROM tblRealTimeData", "num");

            int count = 0;
            for (RealtimePOJO pojo : list) {
                if(Long.parseLong(pojo.getRedisTime()) > maxTime) {
                    count++;
                    execute(pojo.getInsertSQL());
                }
            }
            System.out.println("[Migrating DB Data from REDIS to MySQL] " + count++ + "/" + list.size() + " has newly inserted.");
        }catch(Exception e){
            return false;
        }finally {
            long delCount = getNumber("SELECT COUNT(*) AS num FROM tblRealTimeData WHERE (DATE_SUB(NOW(), INTERVAL 2 MONTH) > regDate)", "num");
            execute("DELETE FROM tblRealTimeData WHERE (DATE_SUB(NOW(), INTERVAL 2 MONTH) > regDate)");
            System.out.println("[Expiration Check] " + delCount + " items which are expired have deleted from MySQL DB");
        }

        return true;
    }

//
//    public String getDirectResponse(String msg){
//        try {
//            connection = DriverManager.getConnection( getConnectionInfo() , USERNAME, PASSWORD);
//            st = connection.createStatement();
//            String sql = "SELECT static FROM tblStaticSentence WHERE serialWord=\'" + msg + "\';";
//            ResultSet rs = st.executeQuery(sql);
//
//            String res = "";
//
//            while(rs.next()){
//                res = rs.getString("static");
//            }
//
//            rs.close();
//            st.close();
//
//            connection.close();
//
//            if(res == null) res = "";
//
//            return res;
//        }catch(SQLException e){
//            e.printStackTrace();
//
//            return "";
//        }
//    }
//
//    public List<NumberUnit> getNumberDictionary(){
//        List<NumberUnit> retVal = new ArrayList<>();
//        try{
//            connection = DriverManager.getConnection( getConnectionInfo() , USERNAME, PASSWORD);
//            st = connection.createStatement();
//            String sql = "SELECT `desc`, `value`, `tag` FROM tblNumber GROUP BY `desc` ORDER BY `desc`;";
//            ResultSet rs = st.executeQuery(sql);
//
//            while(rs.next()){
//                NumberUnit time = new NumberUnit(rs.getString("desc"), rs.getString("tag"), rs.getInt("value"));
//                retVal.add(time);
//            }
//
//            rs.close();
//            st.close();
//
//        }catch(SQLException e){
//            e.printStackTrace();
//        }
//        return retVal;
//    }

}