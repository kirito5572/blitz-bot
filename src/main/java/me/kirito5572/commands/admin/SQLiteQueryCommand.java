package me.kirito5572.commands.admin;

import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.SQLITEConnector;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class SQLiteQueryCommand implements ICommand {

    private final SQLITEConnector sqliteConnector;
    private final Logger logger = LoggerFactory.getLogger(SQLiteQueryCommand.class);

    public SQLiteQueryCommand(SQLITEConnector sqliteConnector) {
        this.sqliteConnector = sqliteConnector;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        if(!Objects.requireNonNull(event.getMember()).getId().equals("284508374924787713")) {
            return;
        }
        if (args.isEmpty()) {
            event.getChannel().sendMessage("Missing arguments").queue();

            return;
        }
        StringBuilder builder = new StringBuilder();
        for(String a : args) {
            builder.append(a).append(" ");
        }
        @Language("SQLite") String SQLQuery = builder.toString();
        if(!SQLQuery.contains(";")) {
            builder.append(";");
            SQLQuery = builder.toString();
        }
        builder.setLength(0);
        switch(args.get(0)) {
            case "INSERT":
            case "DELETE":
            case "UPDATE":
            case "insert":
            case "delete":
            case "update":
                insertDeleteUpdateQuery(SQLQuery, event);
                break;
            case "SELECT":
            case "select":
                try {
                    ResultSet resultSet = sqliteConnector.Select_Query(SQLQuery, new int[0], new String[0]);
                    if(resultSet == null) {
                        event.getChannel().sendMessage("NO DATA in TABLE").queue();
                        return;
                    }
                    @Language("SQLite") String countQuery = SQLQuery.replaceFirst("\\*", "").replaceFirst("SELECT", "SELECT COUNT(*) ");
                    System.out.println(countQuery);
                    ResultSet countSet = sqliteConnector.Select_Query(countQuery, new int[0], new String[0]);
                    countSet.next();
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    int columnCount = resultSetMetaData.getColumnCount();
                    int rowCount = countSet.getInt(1) + 1;
                    String[][] returnData = new String[rowCount][columnCount];
                    int i;
                    for(i = 1; i <= columnCount; i++) {
                        returnData[0][i - 1] = resultSetMetaData.getColumnName(i);
                    }
                    i = 1;
                    while(resultSet.next()) {
                        for(int j = 1; j < columnCount; j++) {
                            returnData[i][j - 1] = resultSet.getString(j);
                        }
                        i++;
                    }
                    StringBuilder allBuilder = new StringBuilder();
                    StringBuilder insideBuilder = new StringBuilder();
                    for(String[] data : returnData) {
                        for(String s : data) {
                            insideBuilder.append(s).append(" | ");
                        }
                        insideBuilder.append("\n");
                        String insideString = insideBuilder.toString();
                        if(insideString.length() + allBuilder.length() >= 1980) {
                            allBuilder.insert(0, "```sql\n");
                            allBuilder.append("\n```");
                            event.getChannel().sendMessage(allBuilder.toString()).queue();
                            allBuilder.setLength(0);
                        }
                        allBuilder.append(insideBuilder.toString());
                        insideBuilder.setLength(0);
                    }
                    allBuilder.insert(0, "```sql\n");
                    allBuilder.append("\n```");
                    event.getChannel().sendMessage(allBuilder.toString()).queue();


                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                    String a = sqlException.getMessage();
                    if(a != null) event.getChannel().sendMessage(a).queue();
                    return;
                }
                break;
        }
    }

    @Override
    public String getHelp() {
        return "null";
    }

    @Override
    public String getInvoke() {
        return "sqlite";
    }

    @Override
    public String getSmallHelp() {
        return "(개발자 전용) SQL SQL";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isOwnerOnly() {
        return true;
    }

    public void insertDeleteUpdateQuery(@Language("SQLite") String sqlQuery, EventPackage event) {
        try {
            sqliteConnector.Insert_Query(sqlQuery, new int[0], new String[0]);

        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
            logger.error(sqlException.getSQLState());
            String a = sqlException.getMessage();
            if(a != null) event.getChannel().sendMessage(a).queue();
            sqlException.printStackTrace();
            return;
        }
        event.getChannel().sendMessage("실행 완료").queue();
    }
}
