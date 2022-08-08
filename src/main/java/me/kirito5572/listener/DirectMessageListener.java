package me.kirito5572.listener;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.SQLConnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class DirectMessageListener extends ListenerAdapter {
    private final Logger logger = LoggerFactory.getLogger(DirectMessageListener.class);
    private final SQLConnector sqlConnector;

    public DirectMessageListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        if(event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            return;
        }
        boolean isChannelOpened = false;
        TextChannel textChannel = null;

        List<TextChannel> textChannels = Objects.requireNonNull(Objects.requireNonNull(event.getJDA().getGuildById("826704284003205160"))
                .getCategoryById("1005116641509650482")).getTextChannels();
        for(TextChannel tc :textChannels) {
            if(tc.getName().equals(event.getChannel().getUser().getId())) {
                textChannel = tc;
                isChannelOpened = true;
                break;
            }
        }
        if(isChannelOpened) {
            int complainInt = 0;
            try (ResultSet resultSet =
                         sqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainLog" +
                                         " WHERE userId = ? ORDER BY ROWID DESC LIMIT 1;",
                                 new int[]{sqlConnector.STRING},
                                 new String[]{event.getAuthor().getId()})) {
                if(resultSet.next()) {
                    complainInt = resultSet.getInt("Complain_int");
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }

            if(complainInt == 0) {
                textChannel.delete().queue();
            }
            int i = sqlConnector.Insert_Query("INSERT INTO blitz_bot.ComplainMessageLog " +
                            "(userId, ComplainInt, messageRaw) VALUES (?, ?, ?);",
                    new int[]{sqlConnector.STRING, sqlConnector.LONG, sqlConnector.STRING},
                    new String[]{event.getAuthor().getId(),
                            String.valueOf(complainInt),
                            event.getMessage().getContentRaw()});
            if(i == 0) {
                event.getChannel().sendMessage("""
                        처리 과정에서 에러가 발생하였습니다.
                        메세지가 정상적으로 전송되지 않았습니다.
                        <@284508374924787713>에게 문의부탁드립니다.""").queue();
                return;
            }

            textChannel.sendMessage(event.getMessage()).queue();
        } else {
            EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                    .setTitle("안내")
                    .setDescription("신고/건의사항/이의제기를 시작하려면 이모지를 추가해주세요");
            event.getChannel().sendMessageEmbeds(builder.build()).queue();
        }
    }

    @Override
    public void onPrivateMessageReactionAdd(@NotNull PrivateMessageReactionAddEvent event) {
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                .setTitle("주의")
                .setDescription("""
                                지금부터 전송하는 모든 메세지, 이미지등은 전부 관리자에게 전송됨을 알려드립니다.
                                또한 메세지 수정, 삭제등을 진행하여도 반영되지 않으니 이점 참고하여주시기 바랍니다.
                                또한 전송되는 모든 메세지(이미지/파일 미포함)만 별도 서버에 1년간 저장됨을 명시드립니다.""");

        EmbedBuilder builder1 = EmbedUtils.getDefaultEmbed();

        int i = sqlConnector.Insert_Query("INSERT INTO blitz_bot.ComplainLog " +
                        "(userId, createtime, endtime) VALUES (?, ?, ?);",
                new int[]{sqlConnector.STRING, sqlConnector.LONG, sqlConnector.LONG},
                new String[]{event.getUserId(), String.valueOf(System.currentTimeMillis() / 1000), "0"});
        if(i == 0) {
            event.getChannel().sendMessage("""
                    처리 과정에서 에러가 발생하였습니다. 
                    <@284508374924787713>에게 문의부탁드립니다.""").queue();
            return;
        }

        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainLog" +
                        " WHERE userId = ? ORDER BY ROWID DESC LIMIT 1",
                new int[]{sqlConnector.STRING},
                new String[]{event.getUserId()})) {
            if(resultSet.next()) {
                builder.addField("처리 번호",resultSet.getString("Complain_int"),false);
                builder1.setTitle("처리 번호: " + resultSet.getString("Complain_int"));
            } else {
                event.getChannel().sendMessage("""
                    처리 과정에서 에러가 발생하였습니다.
                    다시 한번 시도해 주시고, 그래도 안될경우 
                    <@284508374924787713>에게 문의부탁드립니다.""").queue();
                return;
            }
        } catch (SQLException e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        builder1.setFooter(Objects.requireNonNull(event.getUser()).getName(),
                event.getUser().getAvatarUrl() != null ? event.getUser().getAvatarUrl() : event.getUser().getEffectiveAvatarUrl());

        Objects.requireNonNull(Objects.requireNonNull(event.getJDA().getGuildById("826704284003205160"))
                .getCategoryById("1005116641509650482"))
                .createTextChannel(event.getUserId()).complete().sendMessageEmbeds(builder1.build()).queue();

        event.getChannel().sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            return;
        }
        List<TextChannel> textChannels = Objects.requireNonNull(event.getGuild().getCategoryById("1005116641509650482")).getTextChannels();
        for(TextChannel textChannel : textChannels) {
            if(textChannel.getId().equals(event.getChannel().getId())) {
                User user = event.getJDA().getUserById(event.getChannel().getName());
                if(user == null) {
                    event.getChannel().sendMessage("""
                                    봇이 이 유저를 더 이상 찾을수 없습니다. 
                                    \\종료 또는 !종료를 사용하여 채팅을 종료하여 주세요.""").queue();
                    break;
                }
                user.openPrivateChannel().complete().sendMessage(event.getMessage()).queue();
                break;
            }
        }
    }
}
