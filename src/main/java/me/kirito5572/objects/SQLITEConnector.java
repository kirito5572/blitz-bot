package me.kirito5572.objects;

import me.kirito5572.App;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.*;


public class SQLITEConnector {
    /** @noinspection FieldCanBeLocal*/
    private final Logger logger = LoggerFactory.getLogger(SQLITEConnector.class);
    private final MySQLConnector mySqlConnector;
    private static Connection sqliteConnection;
    private static Connection wargamingConnection;
    private static @NotNull String dbUrl = "";

    public final int STRING = 0;

    public SQLITEConnector(MySQLConnector mySQLConnector) throws ClassNotFoundException, SQLException, URISyntaxException {
        this.mySqlConnector = mySQLConnector;
        Class.forName("org.sqlite.JDBC");
        String FilePath = new File(getClass().getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getAbsolutePath();
        try {
            FilePath = FilePath.substring(0, FilePath.lastIndexOf("blitz_bot"));
            if(App.OS == App.WINDOWS) {
                dbUrl = FilePath;
            } else if(App.OS == App.UNIX) {
                dbUrl = FilePath;
            } else if(App.OS == App.MAC) {
                dbUrl = FilePath;
            }
        } catch (StringIndexOutOfBoundsException e) {
            dbUrl = "C:\\Users\\CKIRUser\\IdeaProjects\\blitz-bot\\build\\libs\\";
        }
        logger.info(dbUrl);
        sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + dbUrl + "sqlite.db");
        wargamingConnection = DriverManager.getConnection("jdbc:sqlite:" + dbUrl + "wargaming.db");
    }

    /**
     * check SQL Connection is closed
     * @return {@code true} if this {@code Connection} object
     *      is closed; {@code false} if it is still open
     * @throws SQLException if a database access error occurs
     */
    public boolean isConnectionClosedSqlite() throws SQLException {
        return sqliteConnection.isClosed();
    }

    public boolean isConnectionClosedWargaming() throws SQLException {
        return wargamingConnection.isClosed();
    }

    /**
     * reconnecting with sql server
     * @throws SQLException - if a database access error occurs
     */
    public void reConnectionSqlite() throws SQLException {
        if (!sqliteConnection.isClosed()) {
            sqliteConnection.close();
        }
        sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + dbUrl + "sqlite.db");
    }

    public void reConnectionWargaming() throws SQLException {
        if (!wargamingConnection.isClosed()) {
            wargamingConnection.close();
        }
        wargamingConnection = DriverManager.getConnection("jdbc:sqlite:" + dbUrl + "wargaming.db");
    }

    /**
     * select query to sql server
     * @param Query sql query
     * @param dataType the data types that input
     * @param data the data that input
     * @throws SQLException if query execution fail or database access error occurs
     * @return {@link java.sql.ResultSet}
     */
    public ResultSet Select_Query_Sqlite(@Language("SQLite") String Query, int @NotNull [] dataType, String[] data) throws SQLException {
        PreparedStatement statement = sqliteConnection.prepareStatement(Query);
        mySqlConnector.Query(statement, dataType, data);
        return statement.executeQuery();
    }

    public ResultSet Select_Query_Wargaming(@Language("SQLite") String Query, int @NotNull [] dataType, String[] data) throws SQLException {
        PreparedStatement statement = wargamingConnection.prepareStatement(Query);
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
    public boolean Insert_Query_Sqlite(@Language("SQLite") String Query, int @NotNull [] dataType, String[] data) throws SQLException {
        PreparedStatement statement = sqliteConnection.prepareStatement(Query);
        mySqlConnector.Query(statement, dataType, data);
        return statement.execute();
    }

    public boolean Insert_Query_Wargaming(@Language("SQLite") String Query, int @NotNull [] dataType, String[] data) throws SQLException {
        PreparedStatement statement = wargamingConnection.prepareStatement(Query);
        mySqlConnector.Query(statement, dataType, data);
        return statement.execute();
    }
}
