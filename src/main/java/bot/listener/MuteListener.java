package bot.listener;

import bot.objects.SQLConnector;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MuteListener extends ListenerAdapter {
    private final SQLConnector sqlConnector;

    public MuteListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.MuteTable WHERE isEnd = 0", new int[]{}, new String[]{})) {
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
                            sqlConnector.Insert_Query("UPDATE blitz_bot.MuteTable SET isEnd = 1 WHERE userId = ?",
                                    new int[]{sqlConnector.STRING}, new String[]{resultSet.getString("userId")});
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
                        sqlConnector.reConnection();
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    sqlConnector.reConnection();
                    e.printStackTrace();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.MuteTable WHERE userId = ?",
        new int[]{sqlConnector.STRING}, new String[]{event.getMember().getId()})) {
            if(resultSet.next()) {
                long endTimeData;
                Date date = new Date();
                endTimeData = resultSet.getLong("endTime");
                if(date.getTime() < endTimeData) {
                    Role role = event.getGuild().getRoleById("827098219061444618");
                    assert role != null;
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
