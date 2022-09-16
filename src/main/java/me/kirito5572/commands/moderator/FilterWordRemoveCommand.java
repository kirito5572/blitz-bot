package me.kirito5572.commands.moderator;

import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.FilterSystem;
import me.kirito5572.objects.ICommand;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class FilterWordRemoveCommand implements ICommand {
    private final Logger logger = LoggerFactory.getLogger(FilterWordAddCommand.class);
    private final FilterSystem filterSystem;

    public FilterWordRemoveCommand(FilterSystem filterSystem) {
        this.filterSystem = filterSystem;
    }
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        if(Objects.requireNonNull(event.getGuild()).getId().equals("826704284003205160")) {
            boolean isSuccess = filterSystem.commandAuthorityCheck(args, event, false);
            if(isSuccess) {
                try {
                    filterSystem.wordUpdate(false, false, new String[]{args.get(0)});
                    event.getChannel().sendMessage("단어 삭제가 완료되었습니다.").queue();
                } catch (SQLException sqlException) {
                    logger.error(sqlException.getMessage());
                    logger.error(sqlException.getSQLState());
                    sqlException.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getHelp() {
        return "null";
    }

    @Override
    public String[] getInvoke() {
        return new String[]{"단어삭제", "filterremove", "fr"};
    }

    @Override
    public String getSmallHelp() {
        return "(관리자 전용) 필터링 단어 목록에서 단어를 삭제합니다.\"";
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
