package databases;

import databases.exception.NothingToTakeException;
import models.DataMap;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

/**
 * Created by a on 2017-04-03.
 */
public class DBConstManager {

    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    protected boolean debug = false;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    // TODO PrimaryKey Constraint Violation
    public int getLastInsertId(String table){
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            BasicDataSource bds = DBConnectionPool.getInstance().getBds();
            connection = bds.getConnection();

            String sql = "SELECT LAST_INSERT_ID() AS number FROM " + table + " LIMIT 1";
            st = connection.prepareStatement(sql);

            rs = st.executeQuery();

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
        }finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public List<DataMap> getList(String sql){
        if(debug){
            System.out.println("getList");
        }
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        List<DataMap> list = new Vector<>();
        try{
            BasicDataSource bds = DBConnectionPool.getInstance().getBds();
            connection = bds.getConnection();

            st = connection.prepareStatement(sql);
            rs = st.executeQuery();
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
        }finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public DataMap getRow(String sql) throws NothingToTakeException {
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        DataMap dataMap = new DataMap();
        DBConnectionPool db = DBConnectionPool.getInstance();
        try {
            BasicDataSource bds = DBConnectionPool.getInstance().getBds();
            connection = bds.getConnection();
            st = connection.prepareStatement(sql);

            rs = st.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            int cols = metaData.getColumnCount();

            rs.next();
            for(int i = 1; i <= cols; i++){
                Object obj = rs.getObject(i);
                if(obj instanceof java.sql.Timestamp){
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
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean execute(String sql){
        if(debug){
            System.out.println("execute :: " + sql.replaceAll("\n", ""));
        }
        boolean retVal = false;

        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            BasicDataSource bds = DBConnectionPool.getInstance().getBds();
            connection = bds.getConnection();

            st = connection.prepareStatement(sql);
            retVal = st.execute();
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
        }finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public long getNumber(String sql, String column) throws NothingToTakeException{
        if(debug){
            System.out.println("getNumber");
        }

        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            BasicDataSource bds = DBConnectionPool.getInstance().getBds();
            connection = bds.getConnection();

            st = connection.prepareStatement(sql);
            rs = st.executeQuery();

            long res = 0;

            while(rs.next()){
                res = rs.getLong(column);
            }

            rs.close();
            st.close();

            connection.close();

            return res;
        }catch (SQLException e){
            throw new NothingToTakeException();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error Handled :: getNumber");
            return 0;
        }finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getStrings(String sql, String... column){
        if(debug){
            System.out.println("getStrings :: " + sql.replaceAll("\n", ""));
        }
        List<String> phones = new Vector<>();

        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            BasicDataSource bds = DBConnectionPool.getInstance().getBds();
            connection = bds.getConnection();

            st = connection.prepareStatement(sql);
            rs = st.executeQuery();

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
        }finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String getString(String sql, String column){
        if(debug){
            System.out.println("getString");
        }

        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            BasicDataSource bds = DBConnectionPool.getInstance().getBds();
            connection = bds.getConnection();

            st = connection.prepareStatement(sql);
            rs = st.executeQuery();

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
        }finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
