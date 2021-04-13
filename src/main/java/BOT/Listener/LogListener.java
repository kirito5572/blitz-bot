package BOT.Listener;

import BOT.Objects.SQLConnector;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class LogListener extends ListenerAdapter {
    private final SQLConnector sqlConnector;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy년 MM월dd일 HH시mm분ss초");
    public LogListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        if(event.getAuthor().isBot()) {
            return;
        }
        if(message.isWebhookMessage()) {
            return;
        }

        sqlConnector.Insert_Query("INSERT INTO blitz_bot.ChattingDataTable (messageId, userId, messageRaw) VALUES (?, ?, ?)",
                new int[]{sqlConnector.STRING,sqlConnector.STRING, sqlConnector.STRING},
                new String[] {message.getId(), message.getAuthor().getId(), message.getContentRaw()});
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if(event.getAuthor().isBot()) {
            return;
        }
        if(event.getMessage().isWebhookMessage()) {
            return;
        }
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed();
        Date time = new Date();
        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.ChattingUpdateDataTable WHERE messageId=?;", new int[]{sqlConnector.STRING}, new String[]{event.getMessageId()})) {
            if (resultSet.next()) {
                embedBuilder.setTitle("수정된 메세지")
                        .setColor(Color.ORANGE)
                        .setDescription("이 메세지는 1회 이상 수정된 메세지입니다. [메세지 이동](" + event.getMessage().getJumpUrl() + ")")
                        .addField("메세지 작성자", event.getChannel().getAsMention(), false)
                        .addField("수정전 내용", resultSet.getString("messageRaw"), false)
                        .addField("수정 시간", timeFormat.format(time), false);

                sqlConnector.Insert_Query("UPDATE blitz_bot.ChattingUpdateDataTable SET messageRaw= ? WHERE messageId= ?;",
                        new int[] {sqlConnector.STRING, sqlConnector.STRING},
                        new String[] {event.getMessage().getContentRaw(), event.getMessageId()});
            } else {
                embedBuilder.setTitle("수정된 메세지")
                        .setColor(Color.ORANGE)
                        .setDescription("[메세지 이동](" + event.getMessage().getJumpUrl() + ")")
                        .addField("메세지 작성자", event.getChannel().getAsMention(), false)
                        .addField("수정전 내용", "메세지 내용을 알 수 없습니다.", false)
                        .addField("수정 시간", timeFormat.format(time), false);
                sqlConnector.Insert_Query("INSERT INTO blitz_bot.ChattingDataTable (messageId, userId, messageRaw) VALUES (?, ?, ?);",
                        new int[]{sqlConnector.STRING,sqlConnector.STRING, sqlConnector.STRING},
                        new String[] {event.getMessageId(), event.getAuthor().getId(), event.getMessage().getContentRaw()});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(event.getGuild().getTextChannelById("829023428019355688")).sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed();
        Date time = new Date();
        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.ChattingDataTable WHERE messageId=?;",
                new int[] {sqlConnector.STRING}, new String[] {event.getMessageId()})) {
            if (resultSet.next()) {
                try (ResultSet resultSet1 = sqlConnector.Select_Query("SELECT * FROM blitz_bot.ChattingUpdateDataTable WHERE messageId=?;",
                        new int[] {sqlConnector.STRING}, new String[] {event.getMessageId()})) {
                    Member member = event.getGuild().getMemberById(resultSet.getString("userId"));
                    if(resultSet1.next()) {
                        embedBuilder.setTitle("삭제된 메세지")
                                .setColor(Color.RED)
                                .setDescription("이 삭제된 메세지는 수정된 적있는 메세지입니다.");
                        if (member == null) {
                            embedBuilder.addField("메세지 작성자", "서버에 없는 유저", false);
                        } else {
                            embedBuilder.addField("메세지 작성자", member.getAsMention(), false);
                        }
                        embedBuilder.addField("삭제된 내용", resultSet1.getString("messageRaw"), false)
                                .addField("메세지 ID", event.getMessageId(), false)
                                .addField("삭제 시간", timeFormat.format(time), false);
                    } else {
                        embedBuilder.setTitle("삭제된 메세지")
                                .setColor(Color.RED);
                        if (member == null) {
                            embedBuilder.addField("메세지 작성자", "서버에 없는 유저", false);
                        } else {
                            embedBuilder.addField("메세지 작성자", member.getAsMention(), false);
                        }
                        embedBuilder.addField("삭제된 내용", resultSet.getString("messageRaw"), false)
                                .addField("메세지 ID", event.getMessageId(), false)
                                .addField("삭제 시간", timeFormat.format(time), false);
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            } else {
                embedBuilder.setTitle("삭제된 메세지")
                        .setColor(Color.RED)
                        .addField("메세지 작성자", "작성자를 알수 없습니다.", false)
                        .addField("삭제된 내용", "내용을 알수 없습니다.", false)
                        .addField("메세지 ID", event.getMessageId(), false)
                        .addField("삭제 시간", timeFormat.format(time), false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(event.getGuild().getTextChannelById("829023428019355688")).sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {

    }
}
