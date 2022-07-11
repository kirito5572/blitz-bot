package bot.objects;

import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

import static bot.App.openFileData;

public class SQLConnector {
    private static final Logger logger = LoggerFactory.getLogger(SQLConnector.class);
    private static Connection connection;
    private final String url;
    private final String user;
    private final String password;

    public final int STRING = 0;
    public final int INT = 1;
    public final int BOOLEAN = 2;
    private final String driverName = "com.mysql.cj.jdbc.Driver";

    //TODO SQL쿼리 Stack식으로 변경하기

    public SQLConnector() {
        url = "jdbc:mysql://" + openFileData("endPoint") + "/blitz_bot?serverTimezone=UTC";
        user = "blitzbot";
        password = openFileData("SQLPassword");
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void reConnection() {
        try {
            Class.forName(driverName);
            if (!connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ResultSet Select_Query(@Language("SQL") String Query, int[] dataType,  String[] data) {
        try {
            PreparedStatement statement = connection.prepareStatement(Query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Query(statement, Query, dataType, data);
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            reConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(Query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                Query(statement, Query, dataType, data);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }

    private void Query(PreparedStatement statement, @Language("SQL") String Query, int[] dataType, String[] data) throws SQLException {
        if(connection.isClosed()) {
            reConnection();
        }
        for(int i = 0; i < dataType.length; i++) {
            if(dataType[i] == STRING) {
                statement.setString(i + 1, data[i]);
            } else if(dataType[i] == INT) {
                statement.setInt(i + 1, Integer.parseInt(data[i]));
            } else if(dataType[i] == BOOLEAN) {
                statement.setBoolean(i + 1, Boolean.parseBoolean(data[i]));
            }
        }
    }

    public int Insert_Query(@Language("SQL") String Query, int[] dataType,  String[] data) {
        try {
            PreparedStatement statement = connection.prepareStatement(Query);
            Query(statement, Query, dataType, data);
            statement.execute();
            return 0;
        } catch (SQLException e) {
            reConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(Query);
                Query(statement, Query, dataType, data);
                statement.execute();
                return 0;
            } catch (SQLException e1) {
                e1.printStackTrace();
                reConnection();
            }
            return 1;
        }
    }

    public void filterRefresh() {

    }
}
