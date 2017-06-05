package mysql;

import java.sql.*;

/**
 * @author 함의진
 */
public class DBManager extends DBConstManager {

    private Connection connection = null;
    private Statement st = null;
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

    public int getMachineNumber(String farm, String harv){
        try {
            connection = DriverManager.getConnection( getConnectionInfo() , USERNAME, PASSWORD);
            st = connection.createStatement();
            String sql = "SELECT machine_no \n" +
                    "FROM dong_list \n" +
                    "WHERE delete_flag='N' \n" +
                    "AND farm_code='"+ farm +"' \n" +
                    "AND dong_code='" + harv + "'";
            ResultSet rs = st.executeQuery(sql);

            int res = 0;

            while(rs.next()){
                res = rs.getInt("machine_no");
            }

            rs.close();
            st.close();

            connection.close();

            return res;
        }catch(SQLException e){
            e.printStackTrace();
            return 0;
        }
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