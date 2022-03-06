package BOT.Commands;

import BOT.Listener.filterListener;
import BOT.Objects.ICommand;
import BOT.Objects.SQLConnector;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
public class FilterWordAddCommand implements ICommand {
    private final SQLConnector sqlConnector;

    public FilterWordAddCommand(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }
    @Override
    public void handle(List<String> args, @NotNull SlashCommandEvent event) {
        if(event.getGuild().getId().equals("826704284003205160")) {
            Member member = event.getMember();
            assert member != null;
            if (!member.getRoles().contains(event.getGuild().getRoleById("827009999145926657"))) {
                if (!member.getRoles().contains(event.getGuild().getRoleById("827010848442548254"))) {
                    return;
                }
            }
            if(args.isEmpty()) {
                event.getChannel().sendMessage("필터링 단어를 입력하여주십시오").queue();
                return;
            }
            try ( ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.FilterWord WHERE Word=?;", new int[]{sqlConnector.STRING}, new String[]{args.get(0)})) {
                if(resultSet.next()) {
                    event.getChannel().sendMessage("이미 목록에 해당 단어가 존재합니다.").queue();
                } else {
                    sqlConnector.Insert_Query("INSERT INTO blitz_bot.FilterWord (Word) VALUES (?);", new int[]{sqlConnector.STRING}, new String[]{args.get(0)});
                }
            } catch (SQLException ignored) {
            }
            try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.FilterWord;", new int[0], new String[0])) {
                if(resultSet.last()) {
                    return;
                }
                String[] a = new String[resultSet.getRow()];
                resultSet.first();
                int i = 0;
                while(resultSet.next()) {
                    a[i] = resultSet.getString("Word");
                }
                filterListener.setFilter_data(a);
            } catch (Exception e) {
                e.printStackTrace();
            }

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
