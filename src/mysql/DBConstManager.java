package mysql;

import constants.ConstProtocol;
import models.DataMap;

import java.sql.*;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by a on 2017-04-03.
 */
public class DBConstManager {

    protected Connection connection = null;
    protected Statement st = null;

    public static final String CONNECTOR = "jdbc";
    public static final String DBMS = "mysql";
    public static final String HOST = "1.201.142.86";
//    public static final String HOST = "localhost";
    public static final String PORT = "3306";
    public static final String DBNAME = "sohatechfarmdb";
    public static final String USERNAME = "sohatechfarmdb";
    public static final String PASSWORD = "1!sohatechfarmdb";
    public boolean autoConnect = true;

    public String getConnectionInfo(){
        return CONNECTOR + ":" + DBMS + "://" + HOST + ":" + PORT + "/" + DBNAME + "?useUnicode=yes&amp;characterEncoding=UTF-8&amp;autoReconnect=" + autoConnect;
    }

    protected boolean debug = false;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
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
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error Handled :: getLastInsertId");
            return 1;
        }
    }


    public List<DataMap> getList(String sql){
        if(debug){
            System.out.println("getList");
        }
        List<DataMap> list = new Vector<>();
        try{
            connection = DriverManager.getConnection( getConnectionInfo() , USERNAME, PASSWORD);
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();

            int cols = metaData.getColumnCount();

            while(rs.next()){
                DataMap dataMap = new DataMap();
                for(int i = 1; i <= cols; i++){
                    dataMap.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                list.add(dataMap);
            }

            rs.close();
            st.close();

        }catch(SQLException e){
            e.printStackTrace();
        }

        return list;
    }


    public boolean execute(String sql){
        if(debug){
            System.out.println("execute :: " + sql.replaceAll("\n", ""));
        }
        boolean retVal = false;

        try {
            connection = DriverManager.getConnection(getConnectionInfo(), USERNAME, PASSWORD);
            st = connection.createStatement();
            retVal = st.execute(sql);
            st.close();
            connection.close();

            return retVal;
        }catch (DataTruncation ee){
            ee.printStackTrace();
            return false;
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error Handled :: execute");
            return false;
        }
    }

    public long getNumber(String sql, String column){
        if(debug){
            System.out.println("getNumber");
        }
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
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error Handled :: getNumber");
            return 0;
        }
    }

    public List<String> getStrings(String sql, String... column){
        if(debug){
            System.out.println("getStrings :: " + sql.replaceAll("\n", ""));
        }
        List<String> phones = new Vector<>();

        try {
            connection = DriverManager.getConnection( getConnectionInfo() , USERNAME, PASSWORD);
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            String res = "";

            while(rs.next()){
                for(String col : column) {
                    if(rs.getString(col) == null){
                        phones.add(null);
                    }else {
                        if (!rs.getString(col).equals("--")) phones.add(rs.getString(col));
                    }
                }
            }

            rs.close();
            st.close();

            connection.close();

            return phones;
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error Handled :: getStrings");
            return phones;
        }
    }

    public String getString(String sql, String column){
        if(debug){
            System.out.println("getString");
        }
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
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error Handled :: getString");
            return null;
        }
    }

}
