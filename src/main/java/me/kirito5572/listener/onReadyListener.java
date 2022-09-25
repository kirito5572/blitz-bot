package me.kirito5572.listener;

import com.google.gson.Gson;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.App;
import me.kirito5572.objects.MySQLConnector;
import me.kirito5572.objects.OptionData;
import me.kirito5572.objects.SQLITEConnector;
import me.kirito5572.objects.WargamingAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

public class onReadyListener extends ListenerAdapter {
    private final MySQLConnector mySqlConnector;
    private final SQLITEConnector sqliteConnector;
    private final WargamingAPI wargamingAPI;
    private int i = 0;
    private final Logger logger = LoggerFactory.getLogger(onReadyListener.class);

    public onReadyListener(MySQLConnector mySqlConnector, SQLITEConnector sqliteConnector,
                           WargamingAPI wargamingAPI) {
        this.mySqlConnector = mySqlConnector;
        this.sqliteConnector = sqliteConnector;
        this.wargamingAPI = wargamingAPI;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        startUpGetData();
        autoActivityChangeModule(event);

        final int[] i = {0};
        try {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    muteListenerModule(event);
                    giveRoleListenerModule();
                    i[0]++;
                    if(i[0] > 21600) {
                        i[0] = 0;
                        try {
                            MySQLConnector.reConnection();
                        } catch (SQLException sqlException) {
                            logger.error(sqlException.getMessage());
                        }
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 0, 1000);
            Calendar date = new GregorianCalendar();
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            TimerTask timertask = new TimerTask() {
                @Override
                public void run() {
                    wargamingUserDataListenerModule(date.getTime());
                    date.add(Calendar.DAY_OF_MONTH, 1);
                }
            };
            timer.scheduleAtFixedRate(timertask, date.getTime(), 86400000L);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void wargamingUserDataListenerModule(@NotNull Date date) {
        ResultSet resultSet;
        try {
            resultSet = sqliteConnector.Select_Query_Wargaming("SELECT * FROM wargamingUserId", new int[]{}, new String[]{});
            while(resultSet.next()) {
                Thread.sleep(200);
                String userId = resultSet.getString("userId");
                WargamingAPI.DataObject dataObject = wargamingAPI.getUserPersonalData(userId);
                Gson gson = new Gson();
                String json = gson.toJson(dataObject);
                sqliteConnector.Insert_Query_Wargaming("INSERT INTO `" + userId + "` (input_time, data) VALUES (?, ?)",
                        new int[]{sqliteConnector.STRING, sqliteConnector.STRING},
                        new String[]{String.valueOf(date.getTime()), json});
            }
        } catch (SQLException | InterruptedException sqlException) {
            sqlException.printStackTrace();
        }
    }


    /**
     * {@link net.dv8tion.jda.api.events.ReadyEvent} for {@link me.kirito5572.listener.MuteListener}
     */

    public void muteListenerModule(@NotNull ReadyEvent event) {
        try {
            ResultSet resultSet;
            try {
                if(mySqlConnector.isConnectionClosed()){
                    MySQLConnector.reConnection();
                }
                resultSet = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.MuteTable WHERE isEnd = 0", new int[]{}, new String[]{});
                if (resultSet == null) {
                    return;
                }
                while (resultSet.next()) {
                    long endTime = resultSet.getLong("endTime");
                    long time = System.currentTimeMillis() / 1000;
                    if (time < endTime) {
                        continue;
                    }
                    Guild guild = event.getJDA().getGuildById("826704284003205160");
                    assert guild != null;
                    Role role = guild.getRoleById("827098219061444618");
                    TextChannel textChannel = guild.getTextChannelById("827097881239355392");
                    Member member = guild.getMemberById(resultSet.getString("userId"));
                    mySqlConnector.Insert_Query("UPDATE blitz_bot.MuteTable SET isEnd = 1 WHERE userId = ?",
                            new int[]{mySqlConnector.STRING}, new String[]{resultSet.getString("userId")});
                    if (member != null) {
                        assert role != null;
                        assert textChannel != null;
                        guild.removeRoleFromMember(member, role).queue();
                        EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                                .setTitle("사용자 제재 해제")
                                .setColor(Color.GREEN)
                                .addField("제재 대상", member.getAsMention(), false);
                        textChannel.sendMessageEmbeds(builder.build()).queue();
                    }
                }
            } catch (SQLException e) {
                MySQLConnector.reConnection();
            }
        } catch (Exception e) {
            try {
                MySQLConnector.reConnection();
            } catch (SQLException sqlException) {
                logger.error(sqlException.getMessage());
            }
            logger.error(e.getMessage());
        }
    }

    /**
     * {@link net.dv8tion.jda.api.events.ReadyEvent} for {@link me.kirito5572.listener.giveRoleListener}
     */

    public void giveRoleListenerModule() {
        long time = System.currentTimeMillis() / 1000;
        try (ResultSet resultSet = sqliteConnector.Select_Query_Sqlite(
                "SELECT * FROM GiveRoleBanTable WHERE endTime < ?;",
                new int[]{sqliteConnector.STRING}, new String[]{String.valueOf(time)})) {
            while (resultSet.next()) {
                sqliteConnector.Insert_Query_Sqlite(
                        "DELETE FROM GiveRoleBanTable WHERE userId = ?;",
                        new int[] {sqliteConnector.STRING}, new String[]{resultSet.getString("userId")});
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            try {
                sqliteConnector.reConnectionSqlite();
            } catch (SQLException throwable) {
                logger.error(sqlException.getMessage());
                throwable.printStackTrace();
            }
            logger.error(sqlException.getMessage());
        }
    }

    /**
     * Automatically change activity of bot
     * @param event {@link net.dv8tion.jda.api.events.ReadyEvent}
     */

    public void autoActivityChangeModule(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                switch (i) {
                    case 0,2,4 -> Objects.requireNonNull(jda.getPresence()).setActivity(Activity.watching("월드오브탱크블리츠 공식 한국어 디스코드"));
                    case 1 -> Objects.requireNonNull(jda.getPresence()).setActivity(Activity.listening(App.getVersion()));
                    case 3 -> Objects.requireNonNull(jda.getPresence()).setActivity(Activity.playing("버그/개선 사항은 DM 부탁드립니다."));
                    case 5 -> Objects.requireNonNull(jda.getPresence()).setActivity(Activity.streaming("kirito5572#5572 제작","https://github.com/kirito5572"));
                }
                i++;
                if (i > 5) {
                    i = 0;
                }
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 5000);
    }

    public void startUpGetData() {
        List<String> complainData = new ArrayList<>();
        try (ResultSet resultSet = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainBan;", new int[]{}, new String[]{})) {
            while (resultSet.next()) {
                complainData.add(resultSet.getString("userId"));
            }
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
        }
        OptionData.setComplainBanUserList(complainData);
    }
}
