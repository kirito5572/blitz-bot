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
import java.util.concurrent.TimeUnit;

public class FilterWordAddCommand implements ICommand {
    private final Logger logger = LoggerFactory.getLogger(FilterWordAddCommand.class);
    private final FilterSystem filterSystem;

    public FilterWordAddCommand(FilterSystem filterSystem) {
        this.filterSystem = filterSystem;
    }
    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
        if (Objects.requireNonNull(event.getGuild()).getId().equals("826704284003205160")) {
            boolean isSuccess = filterSystem.commandAuthorityCheck(args, event, false);
            if(isSuccess) {
                try {
                    filterSystem.wordUpdate(false, true, new String[]{args.get(0)});
                    event.getChannel().sendMessage("단어 추가가 완료되었습니다.").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
                } catch (SQLException sqlException) {
                    logger.error(sqlException.getMessage());
                    logger.error(sqlException.getSQLState());
                    sqlException.printStackTrace();
                }
            }
        }
    }

    @Override
    public @NotNull String getHelp() {
        return "null";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"단어추가", "filteradd", "fa"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "(관리자 전용) 필터링 단어 목록에서 단어를 추가합니다.\"";
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
