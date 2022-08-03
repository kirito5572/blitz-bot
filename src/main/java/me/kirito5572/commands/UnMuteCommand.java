package me.kirito5572.commands;

import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.SQLConnector;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class UnMuteCommand implements ICommand {
    private final SQLConnector sqlConnector;

    public UnMuteCommand(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void handle(List<String> args, @NotNull SlashCommandEvent event) {
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
            event.getChannel().sendMessage("유저명을 입력해주십시오").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }
        List<Member> foundMember = FinderUtil.findMembers(args.get(0), event.getGuild());
        if(foundMember.isEmpty()) {
            event.getChannel().sendMessage("서버에 그런 유저는 존재하지 않습니다.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }

        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.MuteTable WHERE userId = ? AND isEnd = 0",
                new int[] {sqlConnector.STRING}, new String[] {foundMember.get(0).getId()})){
            if(resultSet.next()) {
                sqlConnector.Insert_Query("UPDATE blitz_bot.MuteTable SET isEnd = 1 WHERE userId = ?",
                        new int[] {sqlConnector.STRING}, new String[] {resultSet.getString("userId")});
            } else {
                event.getChannel().sendMessage("해당 유저는 현재 제재중이지 않습니다.").queue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getHelp() {
        return "제재";
    }

    @Override
    public String getInvoke() {
        return "제재해제";
    }

    @Override
    public String getSmallHelp() {
        return "제재 해제";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }
}
