package databases;

import databases.exception.NothingToTakeException;
import models.DataMap;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

/**
 * @author 함의진
 * @version 1.0.0
 * MySQL 데이터베이스 연결 컨스턴트를 정의하고 연결 작업을 수행하기 위한 클래스
 * Jul-21-2017
 */
public class DBConstManager{

    protected Connection connection = null;
    protected Statement st = null;

    public static boolean autoConnect = true;

    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public boolean setAutoConnect(boolean value){
        this.autoConnect = value;
        return this.autoConnect;
    }

    // TODO PrimaryKey Constraint Violation
    public int getLastInsertId(String table){
        DBConnectionPool db = DBConnectionPool.getInstance();
        try {
            connection = db.getConnection();

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

            return res;
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error Handled :: getLastInsertId");
            return 1;
        }finally {
            db.freeConnection(connection);
        }
    }

    public List<DataMap> getList(String sql){
        List<DataMap> list = new Vector<>();
        DBConnectionPool db = DBConnectionPool.getInstance();
        try{
            connection = db.getConnection();
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

        }catch(Exception e){
            System.out.println("Error Handled :: getList");
        }finally {
            db.freeConnection(connection);
        }

        return list;
    }

    public DataMap getRow(String sql) throws NothingToTakeException {
        DataMap dataMap = new DataMap();
        DBConnectionPool db = DBConnectionPool.getInstance();
        try {
            connection = db.getConnection();
            st = connection.createStatement();

            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();

            int cols = metaData.getColumnCount();

            rs.next();
            for(int i = 1; i <= cols; i++){
                Object obj = rs.getObject(i);
                if(obj instanceof Timestamp){
                    obj = new SimpleDateFormat(STANDARD_DATE_FORMAT).format(obj).toString();
                }
                dataMap.put(metaData.getColumnLabel(i), obj);
            }

            rs.close();
            st.close();

            return dataMap;
        }catch (SQLException e){
            throw new NothingToTakeException();
        }catch(Exception e){
            System.out.println("Error Handled :: getRow");
            return dataMap;
        }finally {
            db.freeConnection(connection);
        }
    }

    public boolean execute(String sql){
        boolean retVal = false;
        DBConnectionPool db = DBConnectionPool.getInstance();
        try {
            connection = db.getConnection();
            st = connection.createStatement();
            retVal = st.execute(sql);
            return  retVal;
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error Handled :: execute");
            return false;
        }finally {
            db.freeConnection(connection, st);
        }
    }

    public long getNumber(String sql, String column) throws NothingToTakeException {
        DBConnectionPool db = DBConnectionPool.getInstance();
        try {
            connection = db.getConnection();
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            long res = 0;

            int isNone = 0;

            while(rs.next()){
                isNone++;
                res = rs.getLong(column);
            }

            if(isNone == 0) throw new NothingToTakeException();

            rs.close();
            st.close();

            return res;
        }catch(Exception e){
            System.out.println("Error Handled :: getNumber");
            return 0;
        }finally {
            db.freeConnection(connection);
        }
    }

    public List<String> getStrings(String sql, String... column){
        List<String> returnList = new Vector<>();
        DBConnectionPool db = DBConnectionPool.getInstance();
        try {
            connection = db.getConnection();
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            String res = "";

            while(rs.next()){
                for(String col : column) returnList.add(rs.getString(col));
            }

            rs.close();
            st.close();

            return returnList;
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error Handled :: getStrings");
            return returnList;
        }finally {
            db.freeConnection(connection);
        }
    }

    public String getString(String sql, String column){
        DBConnectionPool db = DBConnectionPool.getInstance();
        try {
            connection = db.getConnection();
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            String res = "";

            while(rs.next()){
                res = rs.getString(column);
            }

            rs.close();
            st.close();

            return res;
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error Handled :: getString");
            return null;
        }finally {
            db.freeConnection(connection);
        }
    }

}
