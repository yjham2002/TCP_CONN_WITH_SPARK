package mysql;

import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by a on 2017-04-03.
 */
public class DBConstManager {

    protected Connection connection = null;
    protected Statement st = null;

    public static final String CONNECTOR = "jdbc";
    public static final String DBMS = "mysql";
    public static final String HOST = "182.161.118.74";
    public static final String PORT = "3306";
    public static final String DBNAME = "sohatechfarmdb";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "$#@!richware7";
    public boolean autoConnect = true;

    public String getConnectionInfo(){
        return CONNECTOR + ":" + DBMS + "://" + HOST + ":" + PORT + "/" + DBNAME + "?useUnicode=yes&amp;characterEncoding=UTF-8&amp;autoReconnect=" + autoConnect;
    }

    public boolean setAutoConnect(boolean value){
        this.autoConnect = value;
        return this.autoConnect;
    }

    // TODO PrimaryKey Constraint Violation
    public int getLastInsertId(String table){
        try {
            connection = DriverManager.getConnection( getConnectionInfo() , USERNAME, PASSWORD);
            st = connection.createStatement();
            String sql = "SELECT LAST_INSERT_ID() AS number FROM " + table + " LIMIT 1";
            ResultSet rs = st.executeQuery(sql);

            int res = 1;

            while(rs.next()){
                res = rs.getInt("number");
            }

            if(res == 0) res = 1;

            rs.close();
            st.close();

            connection.close();

            return res;
        }catch(SQLException e){
            e.printStackTrace();
            return 1;
        }
    }

    public boolean execute(String sql){
        boolean retVal = false;

        try {
            connection = DriverManager.getConnection( getConnectionInfo() , USERNAME, PASSWORD);
            st = connection.createStatement();
            retVal = st.execute(sql);
            st.close();
            connection.close();

            return  retVal;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public long getNumber(String sql, String column){
        try {
            connection = DriverManager.getConnection( getConnectionInfo() , USERNAME, PASSWORD);
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            long res = 0;

            while(rs.next()){
                res = rs.getLong(column);
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

    public String getString(String sql, String column){
        try {
            connection = DriverManager.getConnection( getConnectionInfo() , USERNAME, PASSWORD);
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            String res = "";

            while(rs.next()){
                res = rs.getString(column);
            }

            rs.close();
            st.close();

            connection.close();

            return res;
        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }

}
