package BOT.Objects;

import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.sql.*;

public class SQLConnector {
    private static final Logger logger = LoggerFactory.getLogger(SQLConnector.class);
    private static Connection connection;
    private String url, user, password;

    public final int STRING = 0;
    public final int INT = 1;

    public SQLConnector() {
        StringBuilder SQLPassword = new StringBuilder();
        try {
            File file = new File("C:\\DiscordServerBotSecrets\\blitz_bot\\SQLPassword.txt");
            FileReader fileReader = new FileReader(file);
            int singalCh;
            while((singalCh = fileReader.read()) != -1) {
                SQLPassword.append((char) singalCh);
            }
        } catch (Exception e) {

            StackTraceElement[] eStackTrace = e.getStackTrace();
            StringBuilder a = new StringBuilder();
            for (StackTraceElement stackTraceElement : eStackTrace) {
                a.append(stackTraceElement).append("\n");
            }
            logger.warn(a.toString());
        }
        StringBuilder endPoint = new StringBuilder();
        try {
            File file = new File("C:\\DiscordServerBotSecrets\\blitz_bot\\endPoint.txt");
            FileReader fileReader = new FileReader(file);
            int singalCh;
            while((singalCh = fileReader.read()) != -1) {
                endPoint.append((char) singalCh);
            }
        } catch (Exception e) {

            StackTraceElement[] eStackTrace = e.getStackTrace();
            StringBuilder a = new StringBuilder();
            for (StackTraceElement stackTraceElement : eStackTrace) {
                a.append(stackTraceElement).append("\n");
            }
            logger.warn(a.toString());
        }
        String driverName = "com.mysql.cj.jdbc.Driver";
        url = "jdbc:mysql://" + endPoint.toString() + "/blitz_bot?serverTimezone=UTC";
        user = "blitzbot";
        password = SQLPassword.toString();
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reConnection() {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet Select_Query(@Language("SQL") String Query, int[] dataType,  String[] data) {
        try {
            if(connection.isClosed()) {
                reConnection();
            }
            PreparedStatement statement = connection.prepareStatement(Query);
            for(int i = 0; i < dataType.length; i++) {
                if(dataType[i] == STRING) {
                    statement.setString(i + 1, data[i]);
                } else if(dataType[i] == INT) {
                    statement.setInt(i + 1, Integer.parseInt(data[i]));
                }
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            reConnection();
            try {
                if (connection.isClosed()) {
                    reConnection();
                }
                PreparedStatement statement = connection.prepareStatement(Query);
                for (int i = 0; i < dataType.length; i++) {
                    if (dataType[i] == STRING) {
                        statement.setString(i + 1, data[i]);
                    } else if (dataType[i] == INT) {
                        statement.setInt(i + 1, Integer.parseInt(data[i]));
                    }
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }

    public int Insert_Query(@Language("SQL") String Query, int[] dataType,  String[] data) {
        try {
            PreparedStatement statement = connection.prepareStatement(Query);
            for(int i = 0; i < dataType.length; i++) {
                if(dataType[i] == STRING) {
                    statement.setString(i + 1, data[i]);
                } else if(dataType[i] == INT) {
                    statement.setInt(i + 1, Integer.parseInt(data[i]));
                }
            }
            statement.execute();
            return 0;
        } catch (SQLException e) {
            reConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(Query);
                for(int i = 0; i < dataType.length; i++) {
                    if(dataType[i] == STRING) {
                        statement.setString(i + 1, data[i]);
                    } else if(dataType[i] == INT) {
                        statement.setInt(i + 1, Integer.parseInt(data[i]));
                    }
                }
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
