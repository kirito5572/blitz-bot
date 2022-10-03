package me.kirito5572.commands.moderator;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.MySQLConnector;
import me.kirito5572.objects.OptionData;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ComplainUnBanCommand implements ICommand {
    private final MySQLConnector mySqlConnector;
    private final Logger logger = LoggerFactory.getLogger(ComplainUnBanCommand.class);

    public ComplainUnBanCommand(MySQLConnector mySqlConnector) {
        this.mySqlConnector = mySqlConnector;
    }

    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
        if(args.size() == 0) {
            event.getChannel().sendMessage("""
                    차단 해제할 유저를 입력해주세요.
                    <유저명/@유저/유저명#숫자/유저ID>등
                    예시: kirito5572, `@kirito5572#5572`, kirito5572#5572, 284508374924787713 등""").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
            return;
        }
        List<Member> foundMembers = FinderUtil.findMembers(args.get(0), event.getGuild());
        if(foundMembers.size() == 0) {
            event.getChannel().sendMessage("""
                    차단해제할 유저를 찾을수 없습니다.
                    다시한번 확인후에 입력해주세요.
                    예시: kirito5572, `@kirito5572#5572`, kirito5572#5572, 284508374924787713 등""").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
            return;
        } else if(foundMembers.size() != 1) {
            event.getChannel().sendMessage("""
                    검색 된 유저가 2명 이상입니다.
                    이름으로 입력이 아닌, ID나 멘션을 하여 입력해주시기 바랍니다.
                    예시: `@kirito5572#5572`, kirito5572#5572, 284508374924787713 등""").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
            return;
        }
        List<String> complainBanUserList = OptionData.getComplainBanUserList();
        complainBanUserList.remove(foundMembers.get(0).getId());
        OptionData.setComplainBanUserList(complainBanUserList);
        try {
            mySqlConnector.Insert_Query("DELETE FROM blitz_bot.ComplainBan WHERE userId = ?",
                    new int[]{mySqlConnector.STRING},
                    new String[]{foundMembers.get(0).getId()});
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
            event.getChannel().sendMessage("SQL 에러로 명령어가 정상실행 되지 않았습니다.").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
            return;
        }

        event.getChannel().sendMessage(foundMembers.get(0).getAsMention() + " 차단 해제 완료").queue();
    }

    @Override
    public @NotNull String getHelp() {
        return "null";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"신고해제", "complainpardon", "cp"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "(관리자 전용) 신고/건의사항/이의제기등의 상담 채팅을 다시 할수 있도록 차단을 해제하는 명령어입니다";
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
