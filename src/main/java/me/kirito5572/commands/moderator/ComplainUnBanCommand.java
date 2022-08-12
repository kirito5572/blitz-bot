package me.kirito5572.commands.moderator;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.SQLConnector;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class ComplainUnBanCommand implements ICommand {
    private final SQLConnector sqlConnector;
    private final Logger logger = LoggerFactory.getLogger(ComplainUnBanCommand.class);

    public ComplainUnBanCommand(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        if(args.size() == 0) {
            event.getChannel().sendMessage("""
                    차단 해제할 유저를 입력해주세요.
                    <유저명/@유저/유저명#숫자/유저ID>등
                    예시: kirito5572, `@kirito5572#5572`, kirito5572#5572, 284508374924787713 등""").queue();
            return;
        }
        List<Member> foundMembers = FinderUtil.findMembers(args.get(0), event.getGuild());
        if(foundMembers.size() == 0) {
            event.getChannel().sendMessage("""
                    차단해제할 유저를 찾을수 없습니다.
                    다시한번 확인후에 입력해주세요.
                    예시: kirito5572, `@kirito5572#5572`, kirito5572#5572, 284508374924787713 등""").queue();
            return;
        } else if(foundMembers.size() != 1) {
            event.getChannel().sendMessage("""
                    검색 된 유저가 2명 이상입니다.
                    이름으로 입력이 아닌, ID나 멘션을 하여 입력해주시기 바랍니다.
                    예시: `@kirito5572#5572`, kirito5572#5572, 284508374924787713 등""").queue();
            return;
        }
        try {
            sqlConnector.Insert_Query("DELETE FROM blitz_bot.ComplainBan WHERE userId = ?",
                    new int[]{sqlConnector.STRING},
                    new String[]{foundMembers.get(0).getId()});
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
            event.getChannel().sendMessage("SQL 에러로 명령어가 정상실행 되지 않았습니다.").queue();
            return;
        }

        event.getChannel().sendMessage(foundMembers.get(0).getAsMention() + " 차단 해제 완료").queue();
    }

    @Override
    public String getHelp() {
        return "null";
    }

    @Override
    public String getInvoke() {
        return "신고해제";
    }

    @Override
    public String getSmallHelp() {
        return "신고/건의사항/이의제기을 다시 할수 있도록 유저 차단 해제";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }
}
