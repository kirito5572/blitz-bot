package me.kirito5572.commands.moderator;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.SQLITEConnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.ResultSet;
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
        Member member = event.getMember();
        assert member != null;
        if (!member.getRoles().contains(Objects.requireNonNull(event.getGuild()).getRoleById("827009999145926657"))) {
            if (!member.getRoles().contains(event.getGuild().getRoleById("827010848442548254"))) {
                return;
            }
        }
        String channelId = event.getChannel().getId();
        String messageId;
        try (ResultSet resultSet = sqliteConnector.Select_Query("SELECT * FROM Pin WHERE channelId=?;", new int[]{sqliteConnector.STRING}, new String[]{channelId})) {
            if(resultSet.next()) {
                event.getChannel().deleteMessageById(resultSet.getString("messageId")).queue();
                sqliteConnector.Insert_Query("DELETE FROM Pin WHERE channelId=?;" , new int[]{sqliteConnector.STRING}, new String[]{channelId});
                event.getChannel().sendMessage("핀이 해제되었습니다.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            } else {
                EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                        .setTitle("고정된 메세지")
                        .setColor(Color.GREEN)
                        .setDescription(event.getMessage().getContentRaw());
                messageId = event.getChannel().sendMessageEmbeds(builder.build()).complete().getId();
                sqliteConnector.Insert_Query("INSERT INTO Pin (channelId, messageId) VALUES (?, ?)", new int[]{sqliteConnector.STRING, sqliteConnector.STRING}, new String[]{channelId, messageId});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.getTextChannel().deleteMessageById(event.getMessage().getId()).queue();
    }

    @Override
    public String getHelp() {
        return "메세지를 고정합니다.";
    }

    @Override
    public String getInvoke() {
        return "핀";
    }

    @Override
    public String getSmallHelp() {
        return "메세지 고정";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }
}
