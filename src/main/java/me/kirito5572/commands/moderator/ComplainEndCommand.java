package me.kirito5572.commands.moderator;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.MySQLConnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ComplainEndCommand implements ICommand {
    private final MySQLConnector mySqlConnector;
    private final Logger logger = LoggerFactory.getLogger(ComplainEndCommand.class);

    public ComplainEndCommand(MySQLConnector mySqlConnector) {
        this.mySqlConnector = mySqlConnector;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        List<TextChannel> textChannels = Objects.requireNonNull(event.getGuild().getCategoryById("1005116641509650482")).getTextChannels();
        for(TextChannel textChannel : textChannels) {
            if(textChannel.getId().equals(event.getChannel().getId())) {
                User user = event.getJDA().getUserById(event.getChannel().getName());
                if(user == null) {
                    event.getChannel().sendMessage("""
                                    봇이 이 유저를 더 이상 찾을수 없습니다.
                                    \\종료 또는 !종료를 사용하여 채팅을 종료하여 주세요.""").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
                    break;
                }

                long endTime = System.currentTimeMillis();
                EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
                try (ResultSet resultSet = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainLog" +
                                " WHERE userId = ? ORDER BY Complain_int DESC LIMIT 1",
                        new int[]{mySqlConnector.STRING},
                        new String[]{event.textChannel().getName()})) {
                    if(!resultSet.next()) {
                        return;
                    }
                    mySqlConnector.Insert_Query("UPDATE blitz_bot.ComplainLog SET endTime = ? WHERE Complain_int = ?",
                            new int[]{mySqlConnector.LONG, mySqlConnector.STRING},
                            new String[]{String.valueOf(endTime / 1000), resultSet.getString("Complain_int")});

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(resultSet.getLong("createTime") * 1000);

                    int mYear = calendar.get(Calendar.YEAR), mMonth = calendar.get(Calendar.MONTH) + 1, mDay = calendar.get(Calendar.DAY_OF_MONTH),
                            mHour = calendar.get(Calendar.HOUR_OF_DAY), mMin = calendar.get(Calendar.MINUTE), mSec = calendar.get(Calendar.SECOND);
                    String startDate = mYear + "년 " + mMonth + "월 " + mDay + "일 " + mHour + "시 " + mMin + "분 " + mSec + "초";

                    calendar.setTimeInMillis(endTime);
                    mYear = calendar.get(Calendar.YEAR);
                    mMonth = calendar.get(Calendar.MONTH) + 1;
                    mDay = calendar.get(Calendar.DAY_OF_MONTH);
                    mHour = calendar.get(Calendar.HOUR_OF_DAY);
                    mMin = calendar.get(Calendar.MINUTE);
                    mSec = calendar.get(Calendar.SECOND);
                    String endDate = mYear + "년 " + mMonth + "월 " + mDay + "일 " + mHour + "시 " + mMin + "분 " + mSec + "초";

                    builder.setTitle("번호: " + resultSet.getString("Complain_int"))
                            .addField("대상 유저","<@" + resultSet.getString("userId") + ">",true)
                            .addField("시작 시간", startDate, true)
                            .addField("종료 시간", endDate,true)
                            .setFooter(event.member().getNickname(), event.member().getAvatarUrl());

                } catch (SQLException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                    return;
                }


                Objects.requireNonNull(event.getGuild().getTextChannelById("1005116735164264488")).sendMessageEmbeds(builder.build()).queue();

                event.textChannel().delete().queue();

                user.openPrivateChannel().flatMap(channel -> channel.sendMessage("""
                                채팅이 종료되었습니다.
                                추가적인 채팅을 원하실경우 다시 이모지를 추가해주십시오.""")).queue();
                break;
            }
        }
    }

    @Override
    public @NotNull String getHelp() {
        return "null";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"종료", "exit"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "(관리자 전용) 신고/건의사항/이의제기등의 상담 채팅을 종료하는 명령어입니다.";
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
