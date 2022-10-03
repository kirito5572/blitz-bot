package me.kirito5572.commands.moderator;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.MySQLConnector;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class UnMuteCommand implements ICommand {
    private final MySQLConnector mySqlConnector;

    public UnMuteCommand(MySQLConnector mySqlConnector) {
        this.mySqlConnector = mySqlConnector;
    }

    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
        Member member = event.getMember();
        assert member != null;
        if (!member.getRoles().contains(Objects.requireNonNull(event.getGuild()).getRoleById("827009999145926657"))) {
            if (!member.getRoles().contains(event.getGuild().getRoleById("827010848442548254"))) {
                return;
            }
        }
        if(!event.getGuild().getId().equals("826704284003205160")) {
            return;
        }
        if(args.isEmpty()) {
            event.getChannel().sendMessage("유저명을 입력해주십시오").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }
        List<Member> foundMember = FinderUtil.findMembers(args.get(0), event.getGuild());
        if(foundMember.isEmpty()) {
            event.getChannel().sendMessage("서버에 그런 유저는 존재하지 않습니다.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }

        try (ResultSet resultSet = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.MuteTable WHERE userId = ? AND isEnd = 0",
                new int[] {mySqlConnector.STRING}, new String[] {foundMember.get(0).getId()})){
            if(resultSet.next()) {
                mySqlConnector.Insert_Query("UPDATE blitz_bot.MuteTable SET isEnd = 1 WHERE userId = ?",
                        new int[] {mySqlConnector.STRING}, new String[] {resultSet.getString("userId")});
            } else {
                event.getChannel().sendMessage("해당 유저는 현재 제재중이지 않습니다.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull String getHelp() {
        return "제재";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[]{"제재해제", "unmute", "um"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "(관리자 전용) 서버에서 제재한 사용자에 대한 제재를 해제합니다.";
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
