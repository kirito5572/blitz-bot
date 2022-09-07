package me.kirito5572.commands.moderator;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.MySQLConnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MuteCommand implements ICommand {
    private final MySQLConnector mySqlConnector;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd aa hh:mm:ss z");
    private final Logger logger = LoggerFactory.getLogger(MuteCommand.class);

    public MuteCommand(MySQLConnector mySqlConnector) {
        this.mySqlConnector = mySqlConnector;
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
        Role role = event.getGuild().getRoleById("827098219061444618");
        if(!event.getGuild().getId().equals("826704284003205160")) {
            return;
        }
        if(args.isEmpty()) {
            event.getChannel().sendMessage("유저명을 입력해주십시오").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }
        List<Member> foundMember = FinderUtil.findMembers(args.get(0), event.getGuild());
        if(foundMember.isEmpty()) {
            event.getChannel().sendMessage("서버에 그런 유저는 존재하지 않습니다.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }
        if(args.size() == 1) {
            event.getChannel().sendMessage("시간을 입력해주십시오").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }
        Calendar return_time = time_convert(args.get(1));
        if(return_time == null) {
            event.getChannel().sendMessage("입력된 시간값을 처리할 수 없습니다.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }
        if(args.size() == 2) {
            event.getChannel().sendMessage("사유를 입력하여 주십시오.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 2; i < args.size(); i++) {
            stringBuilder.append(args.get(i)).append(" ");
        }
        try (ResultSet resultSet = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.MuteTable WHERE userId = ? AND isEnd = 0",
        new int[] {mySqlConnector.STRING}, new String[] {foundMember.get(0).getId()})){
            if(resultSet.next()) {
                event.getChannel().sendMessage("이미 해당 유저는 제재를 받고있습니다.").queue();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        assert role != null;
        event.getGuild().addRoleToMember(foundMember.get(0), role).queue();
        try {
            mySqlConnector.Insert_Query("INSERT INTO blitz_bot.MuteTable (userId, DBWriteTime, endTime, reason, isEnd) VALUES (?, ?, ?, ?, ?)",
                    new int[]{mySqlConnector.STRING, mySqlConnector.STRING, mySqlConnector.STRING, mySqlConnector.STRING, mySqlConnector.INT},
                    new String[]{foundMember.get(0).getId(), String.valueOf(System.currentTimeMillis() / 1000), String.valueOf(return_time.getTimeInMillis() / 1000), stringBuilder.toString(), "0"});
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
        }

        EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                .setTitle("사용자 제재")
                .setColor(Color.RED)
                .addField("제재 대상", foundMember.get(0).getAsMention(), false)
                .addField("제재 종료시간", format.format(return_time.getTime()), false)
                .addField("제재 사유", stringBuilder.toString(), false)
                .addField("담당자", member.getAsMention(), false);
        Objects.requireNonNull(event.getGuild().getTextChannelById("827097881239355392")).sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getHelp() {
        return "제재";
    }

    @Override
    public String getInvoke() {
        return "제재";
    }

    @Override
    public String getSmallHelp() {
        return "(관리자 전용) 서버에서 사용자를 제재합니다.";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean isOwnerOnly() {
        return false;
    }

    public Calendar time_convert(String time) {
        Date date = new Date();
        int temp_time;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (time.contains("분")) {
            time = time.substring(0,time.indexOf("분"));
            try {
                temp_time = Integer.parseInt(time);
            } catch (Exception e) {
                return null;
            }
            cal.add(Calendar.MINUTE, temp_time);
        } else if(time.contains("시간")) {
            time = time.substring(0,time.indexOf("시간"));
            try {
                temp_time = Integer.parseInt(time);
            } catch (Exception e) {
                return null;
            }
            cal.add(Calendar.HOUR, temp_time);
        } else if(time.contains("일")) {
            time = time.substring(0,time.indexOf("일"));
            try {
                temp_time = Integer.parseInt(time);
            } catch (Exception e) {
                return null;
            }
            cal.add(Calendar.DATE, temp_time);
        } else if(time.contains("월")) {
            time = time.substring(0,time.indexOf("월"));
            try {
                temp_time = Integer.parseInt(time);
            } catch (Exception e) {
                return null;
            }
            cal.add(Calendar.MONTH, temp_time);
        } else if(time.contains("년")) {
            time = time.substring(0,time.indexOf("년"));
            try {
                temp_time = Integer.parseInt(time);
            } catch (Exception e) {
                return null;
            }
            cal.add(Calendar.YEAR, temp_time);
        } else {
            return null;
        }
        return cal;
    }
}
