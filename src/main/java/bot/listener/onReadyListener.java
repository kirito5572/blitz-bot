package bot.listener;

import bot.objects.SQLConnector;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class onReadyListener extends ListenerAdapter {
    private final SQLConnector sqlConnector;

    private final Logger logger = LoggerFactory.getLogger(MuteListener.class);

    public onReadyListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    muteListenerModule(event);
                    giveRoleListenerModule();
                }
            };
            timer.scheduleAtFixedRate(task, 0, 1000);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void muteListenerModule(ReadyEvent event) {
        try {
            try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.MuteTable WHERE isEnd = 0", new int[]{}, new String[]{})) {
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
            }
        } catch (Exception e) {
            sqlConnector.reConnection();
            logger.error(e.getMessage());
        }
    }

    public void giveRoleListenerModule() {
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
            sqlConnector.reConnection();
            logger.error(sqlException.getMessage());
        }
    }
}
