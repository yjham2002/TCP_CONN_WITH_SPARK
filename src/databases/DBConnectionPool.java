package databases;


import org.apache.commons.dbcp2.BasicDataSource;


public class DBConnectionPool {


    public static final String CONNECTOR = "jdbc";
    public static final String DBMS = "mysql";
    public static final String HOST = "localhost";
    public static final String PORT = "3306";
    public static final String DBNAME = "sohatechfarmdb";
    public static final String USERNAME = "sohatechfarmdb";
    public static final String PASSWORD = "1!sohatechfarmdb";

    private static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    private static final int CONN_POOL_SIZE = 10;

    private BasicDataSource bds = new BasicDataSource();

    public String getConnectionInfo(){
        return CONNECTOR + ":" + DBMS + "://" + HOST + ":" + PORT + "/" + DBNAME + "?useUnicode=yes&characterEncoding=UTF-8&autoReconnect=true";
    }

    private DBConnectionPool() {
        //Set database driver name
        bds.setDriverClassName(DRIVER_CLASS_NAME);
        //Set database url
        bds.setUrl(getConnectionInfo());
        //Set database user
        bds.setUsername(USERNAME);
        //Set database password
        bds.setPassword(PASSWORD);
        //Set the connection pool size
        bds.setInitialSize(CONN_POOL_SIZE);
    }

    private static class DataSourceHolder {
        private static final DBConnectionPool INSTANCE = new DBConnectionPool();
    }

    public static DBConnectionPool getInstance() {
        return DataSourceHolder.INSTANCE;
    }

    public BasicDataSource getBds() {
        return bds;
    }

    public void setBds(BasicDataSource bds) {
        this.bds = bds;
    }
}
