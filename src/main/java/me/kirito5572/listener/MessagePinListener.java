package me.kirito5572.listener;

import me.kirito5572.objects.SQLITEConnector;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessagePinListener extends ListenerAdapter {
    private final SQLITEConnector sqliteConnector;
    private final Logger logger = LoggerFactory.getLogger(MessagePinListener.class);

    public MessagePinListener(SQLITEConnector sqliteConnector) {
        this.sqliteConnector = sqliteConnector;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getAuthor().getId().equals(event.getGuild().getSelfMember().getId())) {
            return;
        }
        try (ResultSet resultSet = sqliteConnector.Select_Query("SELECT * FROM Pin WHERE channelId=?;", new int[]{sqliteConnector.STRING}, new String[]{event.getChannel().getId()})) {
            if(resultSet.next()) {
                try {
                    //TODO beta3
                    event.getChannel().retrieveMessageById(resultSet.getString("messageId")).queue(message -> {
                        MessageEmbed embed = message.getEmbeds().get(0);
                        message.delete().queue();
                        String messageId = event.getChannel().sendMessageEmbeds(embed).complete().getId();
                        try {
                            sqliteConnector.Insert_Query("UPDATE Pin SET messageId =? WHERE channelId = ?;", new int[]{sqliteConnector.STRING, sqliteConnector.STRING}, new String[]{messageId, event.getChannel().getId()});
                        } catch (SQLException sqlException) {
                            logger.error(sqlException.getMessage());
                            sqlException.printStackTrace();
                        }
                    });
                } catch (ErrorResponseException e) {
                    sqliteConnector.Insert_Query("DELETE FROM Pin WHERE channelId=?", new int[]{sqliteConnector.STRING}, new String[]{event.getChannel().getId()});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                sqliteConnector.reConnection();
            } catch (SQLException sqlException) {
                logger.error(sqlException.getMessage());
            }
        }
    }
}
