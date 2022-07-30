package bot.listener;

import bot.objects.SQLConnector;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class giveRoleListener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(giveRoleListener.class);
    private final SQLConnector sqlConnector;
    private static final String Chatting = "830514311939751967";

    public giveRoleListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        Guild guild = event.getGuild();
        if (guild.getId().equals("826704284003205160")) {
            Role role = guild.getRoleById("827207197183180821");
            Member member = event.getMember();
            if (event.getMessageId().equals(Chatting)) {
                boolean isBan = isBan(member);
                if(isBan) {
                    member.getUser().openPrivateChannel().complete().sendMessage("현재 쿨타임 중입니다.").queue();
                    return;
                }
                String confirmBan = confirmBan(member);
                if(confirmBan.contains("#")) {
                    switch (confirmBan.split("#")[0]) {
                        case "true/10" -> member.getUser().openPrivateChannel().complete().sendMessage("10초 동안 " + confirmBan.split("#")[1] + "회 이상 역할 부여를 시도하여 5분간 쿨타임에 걸렸습니다.").queue();
                        case "true/3600" -> member.getUser().openPrivateChannel().complete().sendMessage("1시간 동안 " + confirmBan.split("#")[1] + "회 이상 역할 부여를 시도하여 6시간동안 쿨타임에 걸렸습니다.").queue();
                        case "true/86400" -> member.getUser().openPrivateChannel().complete().sendMessage("하루 동안 " + confirmBan.split("#")[1] + "회 이상 역할 부여를 시도하여 7일동안 쿨타임에 걸렸습니다.").queue();
                    }
                } else {
                    if(confirmBan.contains("ban")) {
                        event.getGuild().ban(member, 0, "역할 스팸으로 밴").queue();
                        member.getUser().openPrivateChannel().complete().sendMessage("30일 동안 40회 이상 역할 부여를 시도하여 서버에서 밴되었습니다. 관련 문의는 <@284508374924787713> 에게 부탁드립니다.").queue();
                    }
                }
                assert role != null;
                guild.addRoleToMember(member, role).complete();
                int result = sqlConnector.Insert_Query("INSERT INTO blitz_bot.JoinData_Table (userId, approveTime, rejectTime) VALUES(?, ? ,?);",
                        new int[] {sqlConnector.STRING, sqlConnector.STRING, sqlConnector.STRING},
                        new String[] {member.getId(), String.valueOf(System.currentTimeMillis() / 1000), "0"});
                if (result != 0) {
                    logger.warn("sql insert error #1");
                }
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        Guild guild = event.getGuild();
        if (guild.getId().equals("826704284003205160")) {
            Role role = guild.getRoleById("827207197183180821");
            Member member = event.getMember();
            if (event.getMessageId().equals(Chatting)) {
                assert role != null;
                assert member != null;
                guild.removeRoleFromMember(member, role).complete();
                int result = sqlConnector.Insert_Query("UPDATE blitz_bot.JoinData_Table SET rejectTime =? WHERE userId = ? AND rejectTime = ?",
                        new int[] {sqlConnector.STRING, sqlConnector.STRING, sqlConnector.STRING},
                        new String[] {String.valueOf(System.currentTimeMillis() / 1000), member.getId(), "0"});
                if (result != 0) {
                    logger.warn("sql update error #1");
                }
            }
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        if (guild.getId().equals("826704284003205160")) {
            Member member = event.getMember();
            assert member != null;
            int result = sqlConnector.Insert_Query("UPDATE blitz_bot.JoinData_Table SET rejectTime =? WHERE userId = ? AND rejectTime = ?",
                    new int[]{sqlConnector.STRING, sqlConnector.STRING, sqlConnector.STRING},
                    new String[] {String.valueOf(System.currentTimeMillis() / 1000),  member.getId(), "1"});
            if (result != 0) {
                logger.warn("sql update error #2");
            }

        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                long time = System.currentTimeMillis() / 1000;
                try (ResultSet resultSet = sqlConnector.Select_Query(
                        "SELECT * FROM blitz_bot.GiveRoleBanTable WHERE endTime < ?;",
                        new int[]{sqlConnector.STRING}, new String[]{String.valueOf(time)})) {
                    while (resultSet.next()) {
                        sqlConnector.Insert_Query(
                                "DELETE FROM blitz_bot.GiveRoleBanTable WHERE userId = ?;",
                                new int[] {sqlConnector.STRING}, new String[]{resultSet.getString("userId")});
                    }

                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                    sqlConnector.reConnection();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    private String confirmBan(Member member) {
        long time = System.currentTimeMillis() / 1000;
        int min = 60, hour = 3600, day = 86400;
        try {
            //[0] = 확인할 시간, [1] = 이모지 반복 횟수 [2] = 처벌시간  3*day = 3일 6*hour = 6시간
            int[][] check_time = {{10, 3, 0}, {hour, 10, 0}, {day, 20, 0}, {30*day, 40, 0}};
            check_time[0][2] = 5*min;
            check_time[1][2] = 6*hour;
            check_time[2][2] = 7*day;
            for(int i = 0; i < 4; i++) {
                ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.JoinData_Table where approveTime > ? AND approveTime < ?;",
                        new int[]{sqlConnector.STRING, sqlConnector.STRING},
                        new String[]{String.valueOf(time - check_time[i][0]), String.valueOf(time)});
                resultSet.last();
                if (resultSet.getRow() >= check_time[i][1]) {
                    if(i == 3) {
                        return "ban";
                    }
                    long end_time = (System.currentTimeMillis() / 1000) + check_time[i][2];
                    sqlConnector.Insert_Query("INSERT INTO blitz_bot.GiveRoleBanTable (userId, endTime) VALUES(?,?);",
                            new int[]{sqlConnector.STRING, sqlConnector.STRING},
                            new String[]{member.getId(), String.valueOf(end_time)});
                    return "true/" + check_time[i][0] + "#" + resultSet.getRow();
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            logger.error("에러발생!! giveRoleListener#onGuildMessageReactionAdd#cool-time");
            return "error";
        }
        return "false";
    }

    private boolean isBan(Member member) {
        try {
            ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.GiveRoleBanTable where userId = ?;",
                    new int[]{sqlConnector.STRING},
                    new String[]{member.getId()});

            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            logger.error("에러발생!! giveRoleListener#onGuildMessageReactionAdd#cool-time");
        }
        return false;
    }
}


