package databases;

import java.sql.*;
import java.util.Properties;
import java.util.Vector;

public class DBConnectionPool{

    public static final String CONNECTOR = "jdbc";
    public static final String DBMS = "mysql";
    public static final String HOST = "localhost";
    public static final String PORT = "3306";
    public static final String DBNAME = "sohatechfarmdb";
    public static final String USERNAME = "sohatechfarmdb";
    public static final String PASSWORD = "1!sohatechfarmdb";

    //드라이버 Class
    private String _driver = "com.mysql.jdbc.Driver";
    private String _url = getConnectionInfo();

    //계정
    private String _user = USERNAME;
    private String _password = PASSWORD;

    private boolean _traceOn = false;
    private boolean initialized = false;
    private int _openConnections = 10;
    private static DBConnectionPool instance = null;

    private Vector connections = new Vector(_openConnections);

    public DBConnectionPool() {
    }

    public static String getConnectionInfo(){
        return CONNECTOR + ":" + DBMS + "://" + HOST + ":" + PORT + "/" + DBNAME + "?useUnicode=yes&amp;characterEncoding=UTF-8&amp;autoReconnect=" + true;
    }

    public static DBConnectionPool getInstance() {
        if (instance == null) {
            synchronized (DBConnectionPool.class) {
                if (instance == null) {
                    instance = new DBConnectionPool();
                }
            }
        }
        return instance;

    }

    public void setOpenConnectionCount(int count) {
        _openConnections = count;
    }

    public void setEnableTrace(boolean enable) {
        _traceOn = enable;
    }

    public Vector getConnectionList() {
        return connections;
    }

    public synchronized void setInitOpenConnections(int count) throws SQLException {
        Connection c = null;
        ConnectionObject co = null;
        for (int i = 0; i < count; i++) {
            c = createConnection();
            co = new ConnectionObject(c, false);
            connections.addElement(co);
            trace("ConnectionPoolManager: Adding new DB connection to pool (" + connections.size() + ")");
        }
    }

    public int getConnectionCount() {
        return connections.size();
    }

    public synchronized Connection getConnection() throws Exception {
        if (!initialized) {
            Class c = Class.forName(_driver);
            DriverManager.registerDriver((Driver) c.newInstance());
            initialized = true;
        }

        Connection c = null;
        ConnectionObject co = null;
        boolean badConnection = false;
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (!co.inUse) {//false

                try {

                    badConnection = co.connection.isClosed();//true

                    if (!badConnection)//false

                        //에러가 발생했다면 true 설정

                        badConnection = (co.connection.getWarnings() != null);

                } catch (Exception e) {

                    badConnection = true;

                    e.printStackTrace();

                }

                // Connection is bad, remove from pool

                if (badConnection) {

                    //Vector에서 Connection 제거

                    connections.removeElementAt(i);

                    trace("ConnectionPoolManager: Remove disconnected DB connection #" + i);

                    continue;

                }

                c = co.connection;

                co.inUse = true;

                trace("ConnectionPoolManager: Using existing DB connection #" + (i + 1));

                break;

            }

        }
        if (c == null) {
            c = createConnection();
            co = new ConnectionObject(c, true);
            connections.addElement(co);
            trace("ConnectionPoolManager: Creating new DB connection #" + connections.size());
        }
        return c;
    }

    public synchronized void freeConnection(Connection c) {
        if (c == null) return;
        ConnectionObject co = null;
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (c == co.connection) {
                co.inUse = false;
                break;
            }

        }

        //Connection의 수가 10개를 넘었으면 제거합니다.

        for (int i = 0; i < connections.size(); i++) {

            co = (ConnectionObject) connections.elementAt(i);
            if ((i + 1) > _openConnections && !co.inUse) removeConnection(co.connection);
        }
    }

    public void freeConnection(Connection c, PreparedStatement p, ResultSet r) {
        try {
            if (r != null) r.close();
            if (p != null) p.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeConnection(Connection c, Statement s, ResultSet r) {
        try {
            if (r != null) r.close();
            if (s != null) s.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeConnection(Connection c, PreparedStatement p) {
        try {
            if (p != null) p.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeConnection(Connection c, Statement s) {
        try {
            if (s != null) s.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void removeConnection(Connection c) {
        if (c == null) return;
        ConnectionObject co = null;
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (c == co.connection) {
                try {
                    c.close();
                    connections.removeElementAt(i);
                    trace("Removed " + c.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private Connection createConnection() throws SQLException {
        Connection con = null;
        try {
            if (_user == null) _user = "";
            if (_password == null) _password = "";
            Properties props = new Properties();
            props.put("user", _user);
            props.put("password", _password);
            con = DriverManager.getConnection(_url, props);
        } catch (Throwable t) {
            throw new SQLException(t.getMessage());
        }
        return con;
    }

    public void releaseFreeConnections() {
        trace("ConnectionPoolManager.releaseFreeConnections()");
        Connection c = null;
        ConnectionObject co = null;
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (!co.inUse) removeConnection(co.connection);
        }
    }

    public void finalize() {
        trace("ConnectionPoolManager.finalize()");
        Connection c = null;
        ConnectionObject co = null;
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            try {
                co.connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            co = null;
        }
        connections.removeAllElements();
    }

    private void trace(String s) {
        if (_traceOn) System.err.println(s);
    }
}

class ConnectionObject {
    public Connection connection = null;
    public boolean inUse = false; //Connection 의 사용 여부
    public ConnectionObject(Connection c, boolean useFlag) {
        connection = c;
        inUse = useFlag;
    }

}

