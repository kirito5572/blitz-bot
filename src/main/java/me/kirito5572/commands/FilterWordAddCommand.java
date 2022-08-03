package me.kirito5572.commands;

import me.kirito5572.listener.filterListener;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.SQLConnector;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class FilterWordAddCommand implements ICommand {
    private final SQLConnector sqlConnector;

    public FilterWordAddCommand(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }
    @Override
    public void handle(List<String> args, @NotNull SlashCommandEvent event) {
        if (Objects.requireNonNull(event.getGuild()).getId().equals("826704284003205160")) {
            if (FilterWordRemoveCommand.FilterCommandAuthorityCheck(args, event)) return;
            try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.FilterWord WHERE Word=?;", new int[]{sqlConnector.STRING}, new String[]{args.get(0)})) {
                if (resultSet.next()) {
                    event.getChannel().sendMessage("이미 목록에 해당 단어가 존재합니다.").queue();
                } else {
                    sqlConnector.Insert_Query("INSERT INTO blitz_bot.FilterWord (Word) VALUES (?);", new int[]{sqlConnector.STRING}, new String[]{args.get(0)});
                }
            } catch (SQLException ignored) {
            }
            event.getChannel().sendMessage("단어 등록 완료").queue();
            filterListener.getFilterDataFromDB(sqlConnector);
        }
    }

    @Override
    public String getHelp() {
        return "null";
    }

    @Override
    public String getInvoke() {
        return "단어추가";
    }

    @Override
    public String getSmallHelp() {
        return "필터링 단어를 추가합니다.";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }
}
