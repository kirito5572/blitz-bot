package me.kirito5572.commands;

import me.kirito5572.listener.filterListener;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.MySQLConnector;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class FilterWordRemoveCommand implements ICommand {
    private final MySQLConnector mySqlConnector;

    public FilterWordRemoveCommand(MySQLConnector mySqlConnector) {
        this.mySqlConnector = mySqlConnector;
    }
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        if(Objects.requireNonNull(event.getGuild()).getId().equals("826704284003205160")) {
            if (FilterCommandAuthorityCheck(args, event)) return;
            try ( ResultSet resultSet = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.FilterWord WHERE Word=?;", new int[]{mySqlConnector.STRING}, new String[] {args.get(0)})) {
                if(resultSet.next()) {
                    mySqlConnector.Insert_Query("DELETE FROM blitz_bot.FilterWord WHERE Word=?;", new int[]{mySqlConnector.STRING}, new String[]{args.get(0)});
                } else {
                    event.getChannel().sendMessage("목록에 해당 단어가 존재하지 않습니다.").queue();
                }
            } catch (SQLException ignored) {
            }
            filterListener.getFilterDataFromDB(mySqlConnector);
        }
    }

    static boolean FilterCommandAuthorityCheck(List<String> args, @NotNull EventPackage event) {
        Member member = event.getMember();
        assert member != null;
        Guild guild = event.getGuild();
        if(guild == null) {
            return false;
        }
        Role role = guild.getRoleById("827009999145926657");
        if(role == null) {
            return false;
        }
        if (!member.getRoles().contains(role)) {
            if (!member.getRoles().contains(event.getGuild().getRoleById("827010848442548254"))) {
                return true;
            }
        }
        if(args.isEmpty()) {
            event.getChannel().sendMessage("필터링 단어를 입력하여주십시오").queue();
            return true;
        }
        return false;
    }

    @Override
    public String getHelp() {
        return "null";
    }

    @Override
    public String getInvoke() {
        return "단어삭제";
    }

    @Override
    public String getSmallHelp() {
        return "필터링 단어를 삭제합니다";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }
}
