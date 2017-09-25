package databases;

import java.sql.*;

/**
 * @author 함의진
 */
public class DBManager extends DBConstManager {

    public static DBManager instance;

    public static void setInstance(DBManager instance) {
        DBManager.instance = instance;
    }

    public static DBManager getInstance(){
        if(instance == null) instance = new DBManager();
        return instance;
    }

    private DBManager(){ }

}