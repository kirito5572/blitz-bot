package me.kirito5572.commands;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.MySQLConnector;
import me.kirito5572.objects.SQLITEConnector;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PingCommand implements ICommand {
    private final MySQLConnector mySqlConnector;
    private final SQLITEConnector sqliteConnector;

    public PingCommand(MySQLConnector mySqlConnector, SQLITEConnector sqliteConnector) {
        this.mySqlConnector = mySqlConnector;
        this.sqliteConnector = sqliteConnector;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        long a;
        try {
            a = event.getJDA().getRestPing().submit().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }
        long b = event.getJDA().getGatewayPing();
        long Start = System.currentTimeMillis();
        long mysqlEnd = System.currentTimeMillis();
        long sqliteEnd = System.currentTimeMillis();
        long wargamingEnd = System.currentTimeMillis();
        try (ResultSet ignored = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainBan", new int[]{}, new String[]{})) {
            mysqlEnd = System.currentTimeMillis();
        } catch (SQLException e){
            e.printStackTrace();
        }
        try (ResultSet ignored = sqliteConnector.Select_Query_Sqlite("SELECT * FROM Pin", new int[]{}, new String[]{})) {
            sqliteEnd = System.currentTimeMillis();
        } catch (SQLException e){
            e.printStackTrace();
        }
        try (ResultSet ignored = sqliteConnector.Select_Query_Wargaming("SELECT * FROM wargamingUserId WHERE discordId = 5", new int[]{}, new String[]{})) {
            wargamingEnd = System.currentTimeMillis();
        } catch (SQLException e){
            e.printStackTrace();
        }
        String mysqlTimeString;
        String sqliteTimeString;
        String wargamingTimeString;
        long mysqlTime = (mysqlEnd - Start);
        long sqliteTime = (sqliteEnd - Start);
        long wargamingTime = (wargamingEnd - Start);
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
        if(wargamingTime < 0) {
            wargamingTimeString = "접속 에러";
        } else if(wargamingTime == 1 || wargamingTime == 0) {
            wargamingTimeString = "<1";
        } else {
            wargamingTimeString = String.valueOf(wargamingTime);
        }
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed()
                .setTitle("ping-pong!")
                .addField("API 응답 시간 (UDP/TCP)", b + "ms / " + a + "ms", false)
                .addField("SQL 명령어 처리 시간  (MySQL / sqlite.db / wargaming.db)", mysqlTimeString +
                        "ms / " + sqliteTimeString + "ms / " + wargamingTimeString + "ms", false)
                .setFooter("1분후 삭제됩니다.");

        event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));

    }

    @NotNull
    @Override
    public String getHelp() {
        return "BlitzBot의 핑을 조회합니다. 봇에 연결된 SQL 서버 또한 함께 조회합니다.";
    }

    @NotNull
    @Override
    public String[] getInvoke() {
        return new String[]{"핑", "ping"};
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "BlitzBot의 핑을 조회합니다.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isOwnerOnly() {
        return false;
    }

    @Override
    public boolean isMusicOnly() {
        return false;
    }
}