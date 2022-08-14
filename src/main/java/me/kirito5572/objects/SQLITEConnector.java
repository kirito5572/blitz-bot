package me.kirito5572.objects;

import me.kirito5572.App;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.*;


public class SQLITEConnector {
    private final MySQLConnector mySqlConnector;
    private static final Logger logger = LoggerFactory.getLogger(SQLITEConnector.class);
    private static Connection connection;
    private static String dbUrl = "";

    public final int STRING = 0;
    public final int INT = 1;
    public final int BOOLEAN = 2;
    public final int LONG = 3;

    public SQLITEConnector(MySQLConnector mySQLConnector) throws ClassNotFoundException, SQLException, URISyntaxException {
        this.mySqlConnector = mySQLConnector;
        Class.forName("org.sqlite.JDBC");
        String FilePath = new File(getClass().getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getAbsolutePath();
        FilePath = FilePath.substring(0, FilePath.lastIndexOf("blitz_bot"));
        if(App.OS == App.WINDOWS) {
            dbUrl = FilePath + "sqlite.db";
        } else if(App.OS == App.UNIX) {
            dbUrl = FilePath + "sqlite.db";
        } else if(App.OS == App.MAC) {
            dbUrl = FilePath + "sqlite.db";
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbUrl);
    }

    /**
     * check SQL Connection is closed
     * @return {@code true} if this {@code Connection} object
     *      is closed; {@code false} if it is still open
     * @throws SQLException if a database access error occurs
     */
    public boolean isConnectionClosed() throws SQLException {
        return connection.isClosed();
    }

    /**
     * reconnecting with sql server
     * @throws SQLException - if a database access error occurs
     */
    public void reConnection() throws SQLException {
        if (!connection.isClosed()) {
            connection.close();
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbUrl);
    }

    /**
     * select query to sql server
     * @param Query sql query
     * @param dataType the data types that input
     * @param data the data that input
     * @throws SQLException if query execution fail or database access error occurs
     * @return {@link java.sql.ResultSet}
     */
    public ResultSet Select_Query(@Language("SQLite") String Query, int[] dataType, String[] data) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(Query);
        mySqlConnector.Query(statement, dataType, data);
        return statement.executeQuery();
    }

    /**
     * insert query to sql server
     * @param Query sql query
     * @param dataType the data types that input
     * @param data the data that input
     * @return true = success, false = failed
     * @throws SQLException if query execution fail or database access error occurs
     */
    public boolean Insert_Query(@Language("SQLite") String Query, int[] dataType, String[] data) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(Query);
        mySqlConnector.Query(statement, dataType, data);
        return statement.execute();
    }
}
