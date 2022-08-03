package me.kirito5572.commands.moderator;

import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ClearCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        TextChannel channel = event.getTextChannel();
        Member selfMember = Objects.requireNonNull(event.getGuild()).getSelfMember();
        Member member = event.getMember();
        if(!selfMember.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("봇이 메세지를 삭제할 권한이 없습니다.").queue();
            return;
        }
        assert member != null;
        if(member.hasPermission(Permission.MESSAGE_MANAGE)) {
            String joined = String.join("", args);
            if (joined.equals("")) {
                channel.sendMessage("삭제할 메세지 숫자만큼 명령어 뒤에 숫자를 입력해주세요").queue();
            }
            if (Integer.parseInt(joined) < 1) {
                channel.sendMessage("1보다 큰 숫자를 입력해주세요").queue();
            } else if (Integer.parseInt(joined) > 100) {
                channel.sendMessage("100보다 작은 숫자를 입력해주세요").queue();
            }
            channel.getIterableHistory()
                    .takeAsync(Integer.parseInt(joined))
                    .thenApplyAsync((messages) -> {
                        List<Message> goodMessages = messages.stream()
                                .filter((m) -> m.getTimeCreated().isBefore(
                                        OffsetDateTime.now().plus(2, ChronoUnit.WEEKS)
                                ))
                                .collect(Collectors.toList());

                        channel.purgeMessages(goodMessages);

                        return goodMessages.size();
                    })
                    .whenCompleteAsync(
                            (count, thr) -> channel.sendMessageFormat("`%d` 개의 채팅 삭제 완료", count).queue((message) -> {
                                if(message != null) {
                                    message.delete().queueAfter(10, TimeUnit.SECONDS);
                                }
                            })
                    )
                    .exceptionally((thr) -> {
                        String cause = "";

                        if (thr.getCause() != null) {
                            cause = " 에러 발생 사유: " + thr.getCause().getMessage();
                        }

                        channel.sendMessageFormat("에러: %s%s", thr.getMessage(), cause).queue();

                        return 0;
                    });

        } else {
            channel.sendMessage("이 명령어를 사용할 권한이 없습니다.").queue();
        }
    }

    @Override
    public String getHelp() {
        return "null";
    }

    @Override
    public String getInvoke() {
        return "clear";
    }

    @Override
    public String getSmallHelp() {
        return "채팅 청소용";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }
}
