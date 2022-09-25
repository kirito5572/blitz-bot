package me.kirito5572.listener;

import me.kirito5572.objects.SQLITEConnector;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public class giveRoleListener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(giveRoleListener.class);
    private final SQLITEConnector sqliteConnector;
    private static final String Chatting = "830514311939751967";

    public giveRoleListener(SQLITEConnector sqliteConnector) {
        this.sqliteConnector = sqliteConnector;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        Guild guild = event.getGuild();
        if (guild.getId().equals("826704284003205160")) {
            if (event.getMessageId().equals(Chatting)) {
                Role role = guild.getRoleById("827207197183180821");
                Member member = event.getMember();
                long banTime = isBan(member);
                if(banTime != 0) {
                    long time = banTime * 1000;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(time);

                    int mYear = calendar.get(Calendar.YEAR), mMonth = calendar.get(Calendar.MONTH) + 1, mDay = calendar.get(Calendar.DAY_OF_MONTH),
                            mHour = calendar.get(Calendar.HOUR_OF_DAY), mMin = calendar.get(Calendar.MINUTE), mSec = calendar.get(Calendar.SECOND);
                    String Date = mYear + "년 " + mMonth + "월 " + mDay + "일 " + mHour + "시 " + mMin + "분 " + mSec + "초";
                    member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("현재 쿨타임 중입니다.\n 쿨타임 해제 시간: " + Date)).queue();
                    return;
                }
                String confirmBan = confirmCoolDown(member);
                if(confirmBan.contains("#")) {
                    switch (confirmBan.split("#")[0]) {
                        case "true/10" -> member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("10초 동안 " + confirmBan.split("#")[1] + "회 이상 역할 부여를 시도하여 5분간 쿨타임에 걸렸습니다.")).queue();
                        case "true/3600" -> member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("1시간 동안 " + confirmBan.split("#")[1] + "회 이상 역할 부여를 시도하여 6시간동안 쿨타임에 걸렸습니다.")).queue();
                        case "true/86400" -> member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("하루 동안 " + confirmBan.split("#")[1] + "회 이상 역할 부여를 시도하여 7일동안 쿨타임에 걸렸습니다.")).queue();
                    }
                } else {
                    if(confirmBan.contains("ban")) {
                        event.getGuild().ban(member, 0, "역할 스팸으로 밴").queue();
                        member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("30일 동안 40회 이상 역할 부여를 시도하여 서버에서 밴되었습니다. 관련 문의는 <@284508374924787713> 에게 부탁드립니다.")).queue();
                    }
                }
                assert role != null;
                guild.addRoleToMember(member, role).submit();
                boolean result = true;
                try {
                    result = sqliteConnector.Insert_Query_Sqlite("INSERT INTO JoinDataTable (userId, approveTime, rejectTime) VALUES(?, ? ,?);",
                            new int[] {sqliteConnector.STRING, sqliteConnector.STRING, sqliteConnector.STRING},
                            new String[] {member.getId(), String.valueOf(System.currentTimeMillis() / 1000), "0"});
                } catch (SQLException sqlException) {
                    logger.error(sqlException.getMessage());
                }
                if (result) {
                    logger.warn("sql insert error #1");
                }
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        Guild guild = event.getGuild();
        if (guild.getId().equals("826704284003205160")) {
            if (event.getMessageId().equals(Chatting)) {
            Role role = guild.getRoleById("827207197183180821");
            Member member = event.getMember();
                assert role != null;
                assert member != null;
                guild.removeRoleFromMember(member, role).submit();
                boolean result = true;
                try {
                    result = sqliteConnector.Insert_Query_Sqlite("UPDATE JoinDataTable SET rejectTime =? WHERE userId = ? AND rejectTime = ?",
                            new int[] {sqliteConnector.STRING, sqliteConnector.STRING, sqliteConnector.STRING},
                            new String[] {String.valueOf(System.currentTimeMillis() / 1000), member.getId(), "0"});
                } catch (SQLException sqlException) {
                    logger.error(sqlException.getMessage());
                }
                if (result) {
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
            boolean result = true;
            try {
                result = sqliteConnector.Insert_Query_Sqlite("UPDATE JoinDataTable SET rejectTime =? WHERE userId = ? AND rejectTime = ?",
                        new int[]{sqliteConnector.STRING, sqliteConnector.STRING, sqliteConnector.STRING},
                        new String[] {String.valueOf(System.currentTimeMillis() / 1000),  member.getId(), "1"});
            } catch (SQLException sqlException) {
                logger.error(sqlException.getMessage());
            }
            if (result) {
                logger.warn("sql update error #2");
            }

        }
    }

    /**
     * Check if the member is on cooldown(쿨타임)
     * @param member the member who check
     *
     * @return ban/error/true/false
     *     if ban, need ban
     *     if error, Unknown Member
     *     if true, Member is cooldown now, and return with time data
     *     if false, Member is clear
     */
    @NotNull
    private String confirmCoolDown(@NotNull Member member) {
        long time = System.currentTimeMillis() / 1000;
        int min = 60, hour = 3600, day = 86400;
        try {
            //[0] = 확인할 시간, [1] = 이모지 반복 횟수 [2] = 처벌시간  3*day = 3일 6*hour = 6시간
            int[][] check_time = {{10, 3, 0}, {hour, 10, 0}, {day, 20, 0}, {30*day, 40, 0}};
            check_time[0][2] = 5*min;
            check_time[1][2] = 6*hour;
            check_time[2][2] = 7*day;
            for(int i = 0; i < 4; i++) {
                ResultSet resultSet = sqliteConnector.Select_Query_Sqlite("SELECT COUNT(*) FROM JoinDataTable where approveTime > ? AND approveTime < ? AND userId = ?;;",
                        new int[]{sqliteConnector.STRING, sqliteConnector.STRING, sqliteConnector.STRING},
                        new String[]{String.valueOf(time - check_time[i][0]), String.valueOf(time), member.getId()});
                resultSet.next();
                if (resultSet.getInt(1) >= check_time[i][1]) {
                    if(i == 3) {
                        return "ban";
                    }
                    long end_time = (System.currentTimeMillis() / 1000) + check_time[i][2];
                    sqliteConnector.Insert_Query_Sqlite("INSERT INTO GiveRoleBanTable (userId, endTime) VALUES(?,?);",
                            new int[]{sqliteConnector.STRING, sqliteConnector.STRING},
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

    /**
     * Check if the member is on cooldown(쿨타임)
     * @param member the member who need check
     *
     * @return 0 or timeData(unix time)
     * if 0, no cooldown
     * if timeData, the time the cooldown is end
     */

    private long isBan(@NotNull Member member) {
        try {
            ResultSet resultSet = sqliteConnector.Select_Query_Sqlite("SELECT * FROM GiveRoleBanTable where userId = ?;",
                    new int[]{sqliteConnector.STRING},
                    new String[]{member.getId()});

            if (resultSet.next()) {
                return resultSet.getLong("endTime");
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            logger.error("에러발생!! giveRoleListener#onGuildMessageReactionAdd#cool-time");
        }
        return 0;
    }
}


