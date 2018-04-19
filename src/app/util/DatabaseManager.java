package app.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库管理类
 */
public class DatabaseManager {

    public final static String MYSQL_JDBC_DRIVER_NAME = "com.mysql.jdbc.Driver";
    public final String SQLSERVER_JDBC_DRIVER_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public final String MYSQL_JDBC_URL_HEAD = "jdbc:mysql://";
    public final String SQLSERVER_JDBC_URL_HEAD = "jdbc:sqlserver://";
    public final static String DATABASE_NAME = "myconsole";
    public final static Driver DEFAULT_DRIVER = Driver.MYSQL;
    private Connection conn;
    private Driver driver = null;

    public void setDriver(Driver driver) throws ClassNotFoundException {
        this.driver = driver;
        switch (driver) {
            case MYSQL:
                Class.forName(MYSQL_JDBC_DRIVER_NAME);
                break;
            case SQLSERVER:
                Class.forName(SQLSERVER_JDBC_DRIVER_NAME);
                break;
        }
    }

    private String getURL(String server) throws ClassNotFoundException {
        if(driver == null)
            setDriver(DEFAULT_DRIVER);
        switch (driver) {
            case MYSQL:
                return MYSQL_JDBC_URL_HEAD + server;
            case SQLSERVER:
                return SQLSERVER_JDBC_URL_HEAD + server;
        }
        return "";
    }

    private String getURL(String server, String dbName) throws ClassNotFoundException {
        if(driver == null)
            setDriver(DEFAULT_DRIVER);
        switch (driver) {
            case MYSQL:
                return "jdbc:mysql://" + server + "/" + dbName;
            case SQLSERVER:
                return "jdbc:sqlserver://" + server + ";DatabaseName=" + dbName;
        }
        return "";
    }

    public void connect(String server, String username, String pass) throws Exception {

        conn = DriverManager.getConnection(getURL(server, DATABASE_NAME), username, pass);

        if (!conn.isClosed()) {
            System.out.println("[Database Connect]:  已成功连接至我的工作台数据库！");
        }
    }

    public boolean tryConnect(String server, String username, String pass) {
        try {

            conn = DriverManager.getConnection(getURL(server), username, pass);

            if (!conn.isClosed()) {
                System.out.println("[Database Connect]: 已成功连接至数据库！");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("[Database Connect]: 无法连接至数据库！");
        return false;
    }

    public void install(String server, String username, String pass) throws Exception {
        conn = DriverManager.getConnection(getURL(server), username, pass);

        if (!conn.isClosed()) {
            System.out.println("[Database Install]: 已成功连接至数据库！");
            String[] installSQL = readSQL("install.sql");
            for (String sql : installSQL) {
                sql = sql.trim();
                System.out.println("[Execute Install SQL]:\n" + sql);
                Statement s = conn.createStatement();
                s.execute(sql);
            }
            System.out.println("[Database Install]: 已成功安装数据库！");
        }
    }

    private String[] readSQL(String fileName) throws IOException {
        DataInputStream input = new DataInputStream(this.getClass().getResourceAsStream(fileName));
        byte[] b = new byte[1024];
        int l = 0;
        StringBuilder sb = new StringBuilder();
        while ((l = input.read(b, 0, 1024)) > 0) {
            sb.append(new String(b, 0, l));
        }
        input.close();
        return sb.toString().replaceAll("%DATABASE_NAME%", DATABASE_NAME).split("------");
    }

    public void disconnect() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("[Database Connect]: 数据库连接已断开！");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void execSQL(String sql) throws SQLException {
        System.out.println("[Execute SQL]: " + sql);
        Statement state = conn.createStatement();
        state.execute(sql);
        state.close();
    }

    /**
     * 注意！没有自动关闭Statement
     *
     * @param sql
     * @return
     * @throws SQLException
     */
    public ResultSet execSQLWithResult(String sql) throws SQLException {
        System.out.println("[Execute SQL]: " + sql);
        Statement state = conn.createStatement();
        state.execute(sql);
        ResultSet rs = state.getResultSet();
        return rs;
    }

    enum Driver {

        MYSQL, SQLSERVER;
    }
}
