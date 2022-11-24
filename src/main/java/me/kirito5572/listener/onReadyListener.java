package me.kirito5572.listener;

import com.google.gson.Gson;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.App;
import me.kirito5572.objects.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class onReadyListener extends ListenerAdapter {
    private final MySQLConnector mySqlConnector;
    private final SQLITEConnector sqliteConnector;
    private final WargamingAPI wargamingAPI;
    private final GoogleAPI googleAPI;
    private int i = 0;
    private final Logger logger = LoggerFactory.getLogger(onReadyListener.class);

    public onReadyListener(MySQLConnector mySqlConnector, SQLITEConnector sqliteConnector,
                           WargamingAPI wargamingAPI, GoogleAPI googleAPI) {
        this.mySqlConnector = mySqlConnector;
        this.sqliteConnector = sqliteConnector;
        this.wargamingAPI = wargamingAPI;
        this.googleAPI = googleAPI;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        startUpGetData();

        if(App.appMode == App.APP_ALPHA) {
            Objects.requireNonNull(event.getJDA().getPresence()).setActivity(Activity.watching("알파 테스트에 오신것을 환영합니다."));
        } else if(App.appMode == App.APP_BETA) {
            Objects.requireNonNull(event.getJDA().getPresence()).setActivity(Activity.watching("베타 테스트에 오신것을 환영합니다."));
        } else if(App.appMode == App.APP_STABLE) {
            autoActivityChangeModule(event);
        }
        autoTranslationDetector(event);
        TimerTask module = new TimerTask() {
            @Override
            public void run() {
                /*
                try {
                    autoTranslationDetector(event);
                    wargamingAutoTokenListenerModule(new Date(), event);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }

                 */
            }
        };
        new Timer().scheduleAtFixedRate(module, 0, 1000);

        final int[] i = {0};
        try {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    muteListenerModule(event);
                    giveRoleListenerModule();
                    i[0]++;
                    if(i[0] > 21600) {
                        i[0] = 0;
                        try {
                            MySQLConnector.reConnection();
                        } catch (SQLException sqlException) {
                            logger.error(sqlException.getMessage());
                        }
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 0, 1000);
            Calendar date = new GregorianCalendar();
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            TimerTask timertask = new TimerTask() {
                @Override
                public void run() {
                    wargamingUserDataListenerModule(date.getTime());
                    date.add(Calendar.DAY_OF_MONTH, 1);
                }
            };
            timer.scheduleAtFixedRate(timertask, date.getTime(), 86400000L);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void wargamingAutoTokenListenerModule(@NotNull Date date,@NotNull ReadyEvent event) throws SQLException {
        long time = date.getTime();
        ResultSet resultSet = sqliteConnector.Select_Query_Wargaming("SELECT * FROM accountInfomation WHERE renew_time < ?",
                new int[]{sqliteConnector.INTEGER},
                new String[]{String.valueOf(time)});
        if(resultSet.next()) {
            boolean isSuccess = wargamingAPI.reNewToken(resultSet.getString("token"));
            if(!isSuccess) {
                logger.error("워게이밍 유저 토큰 갱신에 실패했습니다.");
            }
        }
        resultSet = sqliteConnector.Select_Query_Wargaming("SELECT * FROM accountInfomation WHERE end_time < ?",
                new int[]{sqliteConnector.INTEGER},
                new String[]{String.valueOf(time)});
        if(resultSet.next()) {
            Objects.requireNonNull(event.getJDA().getUserById(resultSet.getString("Id"))).openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("등록하신 워게이밍 계정의 토큰키가 자동 갱신에 실패하여 만료되었습니다.\n" +
                        "부득이한 경우지만 다시한번 갱신을 부탁드리겠습니다.").queue();
            });
        }
    }

    private void wargamingUserDataListenerModule(@NotNull Date date) {
        ResultSet resultSet;
        try {
            resultSet = sqliteConnector.Select_Query_Wargaming("SELECT * FROM accountInfomation", new int[]{}, new String[]{});
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (resultSet.next()) {
                            String userId = resultSet.getString("Id");
                            String token = resultSet.getString("token");
                            WargamingAPI.DataObject dataObject;
                            if(token == null) {
                                dataObject = wargamingAPI.getUserPersonalData(userId);
                            } else {
                                dataObject = wargamingAPI.getUserPersonalData(userId, token);
                            }
                            Gson gson = new Gson();
                            String json = gson.toJson(dataObject);
                            sqliteConnector.Insert_Query_Wargaming("INSERT INTO `" + userId + "` (input_time, data) VALUES (?, ?)",
                                    new int[]{sqliteConnector.TEXT, sqliteConnector.TEXT},
                                    new String[]{String.valueOf(date.getTime()), json});
                        } else {
                            timer.cancel();
                        }
                    } catch (@NotNull SQLException exception) {
                        exception.printStackTrace();
                    }
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 100);
        } catch (@NotNull SQLException sqlException) {
            sqlException.printStackTrace();
            try {
                sqliteConnector.reConnectionWargaming();
            } catch (SQLException throwable) {
                logger.error(sqlException.getMessage());
                throwable.printStackTrace();
            }
        }
    }


    /**
     * {@link net.dv8tion.jda.api.events.ReadyEvent} for {@link me.kirito5572.listener.MuteListener}
     */

    private void muteListenerModule(@NotNull ReadyEvent event) {
        try {
            ResultSet resultSet;
            try {
                if(mySqlConnector.isConnectionClosed()){
                    MySQLConnector.reConnection();
                }
                resultSet = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.MuteTable WHERE isEnd = 0", new int[]{}, new String[]{});
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
                    mySqlConnector.Insert_Query("UPDATE blitz_bot.MuteTable SET isEnd = 1 WHERE userId = ?",
                            new int[]{mySqlConnector.STRING}, new String[]{resultSet.getString("userId")});
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
                MySQLConnector.reConnection();
            }
        } catch (Exception e) {
            try {
                MySQLConnector.reConnection();
            } catch (SQLException sqlException) {
                logger.error(sqlException.getMessage());
            }
            logger.error(e.getMessage());
        }
    }

    /**
     * {@link net.dv8tion.jda.api.events.ReadyEvent} for {@link me.kirito5572.listener.giveRoleListener}
     */

    private void giveRoleListenerModule() {
        long time = System.currentTimeMillis() / 1000;
        try (ResultSet resultSet = sqliteConnector.Select_Query_Sqlite(
                "SELECT * FROM GiveRoleBanTable WHERE endTime < ?;",
                new int[]{sqliteConnector.TEXT}, new String[]{String.valueOf(time)})) {
            while (resultSet.next()) {
                sqliteConnector.Insert_Query_Sqlite(
                        "DELETE FROM GiveRoleBanTable WHERE userId = ?;",
                        new int[] {sqliteConnector.TEXT}, new String[]{resultSet.getString("userId")});
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            try {
                sqliteConnector.reConnectionSqlite();
            } catch (SQLException throwable) {
                logger.error(sqlException.getMessage());
                throwable.printStackTrace();
            }
            logger.error(sqlException.getMessage());
        }
    }

    /**
     * Automatically change activity of bot
     * @param event {@link net.dv8tion.jda.api.events.ReadyEvent}
     */

    private void autoActivityChangeModule(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                switch (i) {
                    case 0,2,4 -> Objects.requireNonNull(jda.getPresence()).setActivity(Activity.watching("월드오브탱크블리츠 공식 한국어 디스코드"));
                    case 1 -> Objects.requireNonNull(jda.getPresence()).setActivity(Activity.listening(App.getVersion()));
                    case 3 -> Objects.requireNonNull(jda.getPresence()).setActivity(Activity.playing("버그/개선 사항은 DM 부탁드립니다."));
                    case 5 -> Objects.requireNonNull(jda.getPresence()).setActivity(Activity.streaming("kirito5572#5572 제작","https://github.com/kirito5572"));
                }
                i++;
                if (i > 5) {
                    i = 0;
                }
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 5000);
    }

    private void autoTranslationDetector(ReadyEvent event) {
        final String inputGuildId = "665581943382999048";
        final String outputGuildId = "665581943382999048";  //826704284003205160

        final String[] inputChannels = new String[] {
                "1039543294306287687", "827542008112480297", "1039543108888711178"
        };
        final String[] outputChannelsDebugs = new String[] {
                "671515746119188492", "671515746119188492", "671515746119188492"
        };

        final String[] outputChannels = new String[] {
                "827040899174236171", "827040924722397216", "827040988488925185"
        };

        //TODO 디텍터 link 시키고 디버깅해보기
        Guild inputGuild = event.getJDA().getGuildById(inputGuildId);
        Guild outputGuild = event.getJDA().getGuildById(outputGuildId);

        if(inputGuild == null) {
            return;
        }
        if(outputGuild == null) {
            return;
        }
        TextChannel inputNoticeChannel = inputGuild.getTextChannelById(inputChannels[0]);
        TextChannel inputGameNewsChannel = inputGuild.getTextChannelById(inputChannels[1]);
        TextChannel inputWorkOnProgressChannel = inputGuild.getTextChannelById(inputChannels[2]);
        if(inputNoticeChannel == null || inputGameNewsChannel == null || inputWorkOnProgressChannel == null) {
            return;
        }

        TextChannel outputNoticeChannel = outputGuild.getTextChannelById(outputChannelsDebugs[0]);
        TextChannel outputGameNewsChannel = outputGuild.getTextChannelById(outputChannelsDebugs[1]);
        TextChannel outputWorkOnProgressChannel = outputGuild.getTextChannelById(outputChannelsDebugs[2]);
        if(outputNoticeChannel == null || outputGameNewsChannel == null || outputWorkOnProgressChannel == null) {
            return;
        }

        try {
            //messageCheckingModule(inputNoticeChannel, outputNoticeChannel);
            messageCheckingModule(inputGameNewsChannel, outputGameNewsChannel);
            //messageCheckingModule(inputWorkOnProgressChannel, outputWorkOnProgressChannel);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void messageCheckingModule(TextChannel input, TextChannel output) throws ExecutionException, InterruptedException {
        if(input.hasLatestMessage()) {
            Message message = input.getIterableHistory()
                    .takeAsync(1)
                    .get().get(0);
            String inputMessage = message.getContentDisplay();
            List<Message.Attachment> file = message.getAttachments();
            List<File> downloadFile = new ArrayList<>();
            for(Message.Attachment attachment : file) {
                if(attachment.isImage()) {
                    downloadFile.add(attachment.downloadToFile().get());
                } else if(attachment.isVideo()) {
                    downloadFile.add(attachment.downloadToFile().get());
                }
            }
            //TODO 부가적으로 <@id>에 대한 멘션 처리가 제대로 진행되지 않는 문제 또한 수정이 필요합니다.

            String outputMessage = googleAPI.googleTranslateModule(inputMessage);
            MessageAction messageAction = output.sendMessage(input.getName() + "\n" + outputMessage);
            for(File uploadFile : downloadFile) {
                messageAction = messageAction.addFile(uploadFile);
            }
            messageAction.override(true).queue();
            for(File uploadFile : downloadFile) {
                uploadFile.delete();
            }
            //message.delete().queue();
        }
    }

    private void startUpGetData() {
        List<String> complainData = new ArrayList<>();
        try (ResultSet resultSet = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainBan;", new int[]{}, new String[]{})) {
            while (resultSet.next()) {
                complainData.add(resultSet.getString("userId"));
            }
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
        }
        OptionData.setComplainBanUserList(complainData);
    }


}
