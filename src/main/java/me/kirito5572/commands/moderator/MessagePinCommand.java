package me.kirito5572.commands.moderator;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.SQLITEConnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MessagePinCommand implements ICommand {
    private final SQLITEConnector sqliteConnector;

    public MessagePinCommand(SQLITEConnector sqliteConnector) {
        this.sqliteConnector = sqliteConnector;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        Member member = event.member();
        assert member != null;
        if (!member.getRoles().contains(Objects.requireNonNull(event.getGuild()).getRoleById("827009999145926657"))) {
            if (!member.getRoles().contains(event.getGuild().getRoleById("827010848442548254"))) {
                return;
            }
        }
        String channelId = event.getChannel().getId();
        try (ResultSet resultSet = sqliteConnector.Select_Query_Sqlite("SELECT * FROM Pin WHERE channelId=?;", new int[]{sqliteConnector.TEXT}, new String[]{channelId})) {
            if(resultSet.next()) {
                event.getChannel().deleteMessageById(resultSet.getString("messageId")).queue();
                sqliteConnector.Insert_Query_Sqlite("DELETE FROM Pin WHERE channelId=?;" , new int[]{sqliteConnector.TEXT}, new String[]{channelId});
                event.getChannel().sendMessage("핀이 해제되었습니다.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
            } else {
                EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                        .setTitle("고정된 메세지")
                        .setColor(Color.GREEN)
                        .setDescription(event.message().getContentRaw().substring(2));
                List<Message.Attachment> attachments = event.message().getAttachments();
                if(!attachments.isEmpty()) {
                    if(attachments.get(0).isImage()) {
                        builder.setImage(attachments.get(0).getUrl());
                    }
                }
                event.getChannel().sendMessageEmbeds(builder.build()).queue(message -> {
                        String messageId = message.getId();
                    try {
                        sqliteConnector.Insert_Query_Sqlite("INSERT INTO Pin (channelId, messageId) VALUES (?, ?)",
                                new int[]{sqliteConnector.TEXT, sqliteConnector.TEXT},
                                new String[]{channelId, messageId});
                    } catch (SQLException sqlException) {
                        sqlException.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.textChannel().deleteMessageById(event.message().getId()).queue();
    }

    @Override
    public @NotNull String getHelp() {
        return "메세지를 고정합니다.";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[]{"핀", "pin"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "(관리자 전용)메세지를 고정합니다.";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
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
