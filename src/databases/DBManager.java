package databases;

import java.sql.SQLException;

/**
 * @author 함의진
 * @version 1.0.0
 * MySQL 데이터베이스 연결을 위한 싱글턴 클래스
 * Jul-21-2017
 */
public class DBManager extends DBConstManager {

    private static DBManager instance;

    public static DBManager getInstance(){
        if(instance == null) instance = new DBManager();
        return instance;
    }

    private DBManager(){
        DBConnectionPool db = DBConnectionPool.getInstance();
        try {
            connection = db.getConnection();
            st = connection.createStatement();
        } catch (SQLException se1) {
            se1.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (st != null) st.close();
            } catch (SQLException se2) {}
            db.freeConnection(connection);
        }
    }

}