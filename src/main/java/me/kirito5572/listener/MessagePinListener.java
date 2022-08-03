package me.kirito5572.listener;

import me.kirito5572.objects.SQLConnector;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.sql.ResultSet;

public class MessagePinListener extends ListenerAdapter {
    private final SQLConnector sqlConnector;

    public MessagePinListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getAuthor().getId().equals("592987181186940931")) {
            return;
        }
        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.Pin WHERE channelId=?;", new int[]{sqlConnector.STRING}, new String[]{event.getChannel().getId()})) {
            if(resultSet.next()) {
                Message message;
                try {
                    message = event.getChannel().retrieveMessageById(resultSet.getString("messageId")).complete();
                } catch (ErrorResponseException e) {
                    sqlConnector.Insert_Query("DELETE FROM blitz_bot.Pin WHERE channelId=?", new int[]{sqlConnector.STRING}, new String[]{event.getChannel().getId()});
                    return;
                }
                MessageEmbed embed = message.getEmbeds().get(0);
                message.delete().queue();
                String messageId = event.getChannel().sendMessageEmbeds(embed).complete().getId();
                sqlConnector.Insert_Query("UPDATE blitz_bot.Pin SET messageId =? WHERE channelId = ?;", new int[]{sqlConnector.STRING, sqlConnector.STRING}, new String[]{messageId, event.getChannel().getId()});
            }
        } catch (Exception e) {
            e.printStackTrace();
            sqlConnector.reConnection();
        }
    }
}
