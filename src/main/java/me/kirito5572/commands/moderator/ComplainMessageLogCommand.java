package me.kirito5572.commands.moderator;

import me.kirito5572.App;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.SQLConnector;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ComplainMessageLogCommand implements ICommand {
    private final SQLConnector sqlConnector;
    private final Logger logger = LoggerFactory.getLogger(ComplainMessageLogCommand.class);


    public ComplainMessageLogCommand(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        StringBuilder messageBuilder = new StringBuilder();
        int ComplainInt;
        if(args.size() == 0) {
            event.getTextChannel().sendMessage("명령어 뒤에 조회할 번호를 입력해주세요").queue();
            return;
        } else {
            boolean isDigit = args.get(0).chars().allMatch( Character::isDigit );
            if(isDigit) {
                ComplainInt = Integer.parseInt(args.get(0));
            } else {
                event.getTextChannel().sendMessage("명령어 뒤에 값은 숫자만 입력해주세요.").queue();
                return;
            }
        }
        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainMessageLog WHERE ComplainInt = ?",
                new int[]{sqlConnector.INT},
                new String[]{String.valueOf(ComplainInt)})) {
            while(resultSet.next()) {
                String userId = resultSet.getString("userId");
                boolean adminChat = false;
                for(String Id : App.getModerator()) {
                    if(Id.contains(userId)) {
                        adminChat = true;
                        break;
                    }
                }
                if(adminChat) {
                    Member member = event.getGuild().getMemberById(userId);
                    if(member == null) {
                        messageBuilder.append("<@").append(userId).append(">").append("\n");
                    } else {
                        String a = member.getNickname();
                        String name = a == null ? member.getUser().getName() : a;
                        messageBuilder.append(name).append("\n");
                    }
                } else {
                    messageBuilder.append("<@").append(userId).append(">").append("\n");
                }
                messageBuilder.append(resultSet.getString("messageRaw")).append("\n");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getHelp() {
        return "null";
    }

    @Override
    public String getInvoke() {
        return "로그";
    }

    @Override
    public String getSmallHelp() {
        return "신고/건의사항/이의제기 채팅 로그 조회 명령어";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }
}
