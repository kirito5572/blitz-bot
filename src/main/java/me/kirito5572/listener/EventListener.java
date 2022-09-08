package me.kirito5572.listener;

import me.kirito5572.objects.MySQLConnector;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class EventListener extends ListenerAdapter {
    private final MySQLConnector mySQLConnector;

    public EventListener(MySQLConnector mySQLConnector) {
        this.mySQLConnector = mySQLConnector;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        Guild guild = event.getGuild();
        if (guild.getId().equals("826704284003205160")) {
            if (event.getMessageId().equals("1017430875480268820")) {
                try {
                    mySQLConnector.Insert_Query("INSERT IGNORE INTO blitz_bot.Event (userId) VALUES ?",
                            new int[]{mySQLConnector.STRING},
                            new String[]{event.getMember().getId()});
                    event.getMember().getUser().openPrivateChannel().complete().sendMessage("이벤트에 정상 참여되었습니다.").queue();
                } catch (SQLException sqlException) {
                    event.getMember().getUser().openPrivateChannel().complete().sendMessage("""
                            이벤트에 정상 참여되지 않았습니다.
                            이미 이벤트에 참여되었거나 혹은 에러가 발생한 것입니다.
                            다시 한번 이모지를 클릭하여 보시고, 그래도 되지 않을 경우 문의 부탁드립니다.""").queue();
                }
            }
        }
    }
}
