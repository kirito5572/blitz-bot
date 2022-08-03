package me.kirito5572.commands;

import me.kirito5572.App;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.SQLConnector;
import me.kirito5572.objects.EventPackage;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PingCommand implements ICommand {
    private final SQLConnector sqlConnector;

    public PingCommand(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        long a = event.getJDA().getRestPing().complete();
        long b = event.getJDA().getGatewayPing();
        long start=System.currentTimeMillis();
        long end = System.currentTimeMillis();
        try (ResultSet ignored = sqlConnector.Select_Query("SELECT * FROM blitz_bot.Pin", new int[]{}, new String[]{})) {
            end = System.currentTimeMillis();
        } catch (SQLException e){
            e.printStackTrace();
        }
        String sqlTimeString;
        long sqlTime = (end - start);
        if(sqlTime < 4) {
            sqlTimeString = "접속 에러";
        } else {
            sqlTimeString = String.valueOf(sqlTime);
        }
        event.getTextChannel().sendMessage("blitz_bot\n" + "rest 요청(udp) ping: " + b + "ms\n"
                +  "rest 요청(tcp) ping: " + a+ "ms\n" + "sqlServer ping: " + sqlTimeString + "ms").queue();

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