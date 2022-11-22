package me.kirito5572.listener;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.MySQLConnector;
import me.kirito5572.objects.OptionData;
import me.kirito5572.objects.SQLITEConnector;
import me.kirito5572.objects.WargamingAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
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
    private final MySQLConnector mySqlConnector;
    private final SQLITEConnector wargamingConnector;
    private final WargamingAPI wargamingAPI;

    public DirectMessageListener(MySQLConnector mySqlConnector, SQLITEConnector wargamingConnector, WargamingAPI wargamingAPI) {
        this.mySqlConnector = mySqlConnector;
        this.wargamingConnector = wargamingConnector;
        this.wargamingAPI = wargamingAPI;
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        if(event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            return;
        }
        if(event.getMessage().getContentRaw().startsWith("https://asia.wotblitz.com/ko/?")) {
            String message = event.getMessage().getContentRaw().replaceFirst("https://asia.wotblitz.com/ko/\\?/", "");
            message =  message.substring(message.indexOf("status=") + 7);
            boolean status = message.startsWith("ok");
            if(!status) {
                String errorCode = message.substring(message.indexOf("code:") + 5);
                String errorMessage = message.substring(message.indexOf("message:") + 5);
                event.getChannel().sendMessage("결과를 불러오는데 실패했습니다. 다시 시도해주시기 바랍니다.\n" +
                        "에러코드: " + errorCode + "\n 에러메세지: " + errorMessage).queue();
            }
            String temp = message.substring(message.indexOf("account_id=") + 11);
            long account_id = Long.parseLong(temp.substring(0, temp.indexOf("&expires_at")));
            temp = message.substring(message.indexOf("access_token=") + 13);
            String access_token = temp.substring(0, temp.indexOf("&"));

            WargamingAPI.TokenData tokenData = wargamingAPI.reNewTokenOnce(access_token);
            if(tokenData == null) {
                event.getChannel().sendMessage("토큰 갱신시에 에러가 발생했습니다. 인증 과정을 다시 시도하여 주십시오.").queue();
                return;
            }
            if(account_id != tokenData.id) {
                event.getChannel().sendMessage("입력한 토큰키의 소유 계정 ID와 신규 발급된 토큰키의 소유 계정 ID가 일치 하지 않습니다.").queue();
            }
            try {
                ResultSet resultSet = wargamingConnector.Select_Query_Wargaming("SELECT * FROM accountInfomation WHERE Id = ?",
                        new int[]{wargamingConnector.INTEGER}, new String[]{String.valueOf(tokenData.id)});
                if(resultSet.next()) {
                   //기존 조회했던 기록이 있거나, 등록된 wargamingID가 있는 경우 tokenData와 디코ID 업데이트
                    wargamingConnector.Insert_Query_Wargaming("UPDATE accountInfomation SET token = ? , end_time = ?, renew_time = ?, discordId = ? WHERE Id = ?",
                            new int[]{wargamingConnector.TEXT, wargamingConnector.INTEGER, wargamingConnector.INTEGER, wargamingConnector.INTEGER, wargamingConnector.INTEGER},
                            new String[]{tokenData.token, String.valueOf(tokenData.endTime), String.valueOf(tokenData.reNewTime), event.getAuthor().getId(), String.valueOf(tokenData.id)});
                } else {
                    //기존 등록된 wargamingID가 없는 경우
                    ResultSet resultSet1 = wargamingConnector.Select_Query_Wargaming("SELECT * FROM accountInfomation WHERE discordId = ?",
                            new int[]{wargamingConnector.INTEGER}, new String[]{event.getAuthor().getId()});
                    if(resultSet1.next()) {
                        //그런데 discordID는 있는 경우 = 디코에 신규 ID로 덮어쓰기 할 경우
                        wargamingConnector.Insert_Query_Wargaming("UPDATE accountInfomation SET token = ? , end_time = ?, renew_time = ?, Id = ? WHERE discordId = ?",
                                new int[]{wargamingConnector.TEXT, wargamingConnector.INTEGER, wargamingConnector.INTEGER, wargamingConnector.INTEGER, wargamingConnector.INTEGER},
                                new String[]{tokenData.token, String.valueOf(tokenData.endTime), String.valueOf(tokenData.reNewTime), String.valueOf(tokenData.id), event.getAuthor().getId()});
                    } else {
                        //기존 token 값이나 discordId 연동도 안되어 있는 경우
                        wargamingConnector.Insert_Query_Wargaming("INSERT INTO accountInfomation (Id, token, end_time, renew_time) VALUES (?, ?, ?, ?)",
                                new int[]{wargamingConnector.INTEGER, wargamingConnector.TEXT, wargamingConnector.INTEGER, wargamingConnector.INTEGER},
                                new String[]{String.valueOf(tokenData.id), tokenData.token, String.valueOf(tokenData.endTime), String.valueOf(tokenData.reNewTime)});
                    }
                }
                } catch (SQLException sqlException) {
                sqlException.printStackTrace();
                event.getChannel().sendMessage("에러가 발생했습니다.").queue();
                return;
            }
            event.getChannel().sendMessage("유저 등록 처리가 완료되었습니다.").queue();
            return;
        }
        List<String> complainBanUserList = OptionData.getComplainBanUserList();
        if(complainBanUserList.contains(event.getAuthor().getId())){
            event.getChannel().sendMessage("당신은 해당 기능이 차단되어 사용할수 없습니다.").queue();
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
            boolean i = insertMessageData(event.getAuthor(), textChannel, event.getMessage());
            if(i) {
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

        int i = 0;
        try {
            i = mySqlConnector.Insert_Query("INSERT INTO blitz_bot.ComplainLog " +
                            "(userId, createTime, endTime) VALUES (?, ?, ?);",
                    new int[]{mySqlConnector.STRING, mySqlConnector.LONG, mySqlConnector.LONG},
                    new String[]{event.getUserId(), String.valueOf(System.currentTimeMillis() / 1000), "0"});
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
        }
        if(i == 1) {
            event.getChannel().sendMessage("""
                    처리 과정에서 에러가 발생하였습니다. 
                    <@284508374924787713>에게 문의부탁드립니다.""").queue();
            return;
        }

        try (ResultSet resultSet = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainLog" +
                        " WHERE userId = ? ORDER BY Complain_int DESC LIMIT 1",
                new int[]{mySqlConnector.STRING},
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
                .createTextChannel(event.getUserId()).queue(textChannel -> textChannel.sendMessageEmbeds(builder1.build()).queue());
        event.getChannel().sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            return;
        }
        if(event.getMessage().getContentRaw().contains("!종료") || event.getMessage().getContentRaw().contains("!종료")) {
            return;
        }
        if (!event.getGuild().getId().equals("826704284003205160")) {
            return;
        }
        List<TextChannel> textChannels = Objects.requireNonNull(event.getGuild().getCategoryById("1005116641509650482")).getTextChannels();
        for(TextChannel textChannel : textChannels) {
            if(textChannel.getId().equals(event.getChannel().getId())) {
                User user = event.getJDA().getUserById(event.getChannel().getId());
                if(user == null) {
                    event.getChannel().sendMessage("""
                                    봇이 이 유저를 더 이상 찾을수 없습니다. 
                                    \\종료 또는 !종료를 사용하여 채팅을 종료하여 주세요.""").queue();
                    break;
                }
                insertMessageData(event.getAuthor(), textChannel, event.getMessage());
                user.openPrivateChannel().flatMap(channel -> channel.sendMessage(event.getMessage())).queue();

                break;
            }
        }
    }

    /**
     * Upload MessageData to DataBase
     * @param user the user who chatting message
     * @param textChannel the textChannel which is chatting channel
     * @param message the message which is chatting message
     * @return if false, upload success/ if true, upload fail
     */

    private boolean insertMessageData(@NotNull User user,@NotNull TextChannel textChannel,@NotNull Message message) {
        int complainInt = 0;
        try (ResultSet resultSet =
                     mySqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainLog" +
                                     " WHERE userId = ? ORDER BY Complain_int DESC LIMIT 1;",
                             new int[]{mySqlConnector.STRING},
                             new String[]{user.getId()})) {
            if(resultSet.next()) {
                complainInt = resultSet.getInt("Complain_int");
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        if(complainInt == 0) {
            textChannel.delete().queue();
        }
        int i = 0;
        try {
            i = mySqlConnector.Insert_Query("INSERT INTO blitz_bot.ComplainMessageLog " +
                            "(userId, ComplainInt, messageRaw) VALUES (?, ?, ?);",
                    new int[]{mySqlConnector.STRING, mySqlConnector.LONG, mySqlConnector.STRING},
                    new String[]{user.getId(),
                            String.valueOf(complainInt),
                            message.getContentRaw()});
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
        }
        return i == 1;
    }
}
