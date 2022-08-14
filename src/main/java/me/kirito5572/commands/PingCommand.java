package me.kirito5572.commands;

import me.kirito5572.App;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.MySQLConnector;
import me.kirito5572.objects.SQLITEConnector;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PingCommand implements ICommand {
    private final MySQLConnector mySqlConnector;
    private final SQLITEConnector sqliteConnector;

    public PingCommand(MySQLConnector mySqlConnector, SQLITEConnector sqliteConnector) {
        this.mySqlConnector = mySqlConnector;
        this.sqliteConnector = sqliteConnector;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        long a = event.getJDA().getRestPing().complete();
        long b = event.getJDA().getGatewayPing();
        long mysqlStart = System.currentTimeMillis();
        long mysqlEnd = System.currentTimeMillis();
        long sqliteStart = System.currentTimeMillis();
        long sqliteEnd = System.currentTimeMillis();
        try (ResultSet ignored = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainBan", new int[]{}, new String[]{})) {
            mysqlEnd = System.currentTimeMillis();
        } catch (SQLException e){
            e.printStackTrace();
        }
        try (ResultSet ignored = sqliteConnector.Select_Query("SELECT * FROM Pin", new int[]{}, new String[]{})) {
            sqliteEnd = System.currentTimeMillis();
        } catch (SQLException e){
            e.printStackTrace();
        }
        String mysqlTimeString;
        String sqliteTimeString;
        long mysqlTime = (mysqlEnd - mysqlStart);
        long sqliteTime = (sqliteEnd - sqliteStart);
        if(mysqlTime < 0) {
            mysqlTimeString = "접속 에러";
        } else if(mysqlTime == 1 || mysqlTime == 0) {
            mysqlTimeString = "<1";
        } else {
            mysqlTimeString = String.valueOf(mysqlTime);
        }
        if(sqliteTime < 0) {
            sqliteTimeString = "접속 에러";
        } else if(sqliteTime == 1 || sqliteTime == 0) {
            sqliteTimeString = "<1";
        } else {
            sqliteTimeString = String.valueOf(sqliteTime);
        }
        event.getTextChannel().sendMessage("blitz_bot\n"
                + "API 응답요청 소요시간(udp 연결): " + b + "ms\n"
                + "API 응답요청후 반환소요 시간(tcp 연결): " + a+ "ms\n"
                + "MySQL 서버 명령어 처리시간(tcp 연결): " + mysqlTimeString + "ms\n"
                + "SQLite 파일 명령어 처리시간: " + sqliteTimeString + "ms\n").queue();

    }

    @NotNull
    @Override
    public String getHelp() {
        return "레이턴시 ㄴㅇㄱ!\n" +
                "명령어: `" + App.getPREFIX() + getInvoke() + "`";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "핑";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "other";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}