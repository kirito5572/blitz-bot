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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MuteListener extends ListenerAdapter {
    private final SQLConnector sqlConnector;

    private final Logger logger = LoggerFactory.getLogger(MuteListener.class);

    public MuteListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
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
