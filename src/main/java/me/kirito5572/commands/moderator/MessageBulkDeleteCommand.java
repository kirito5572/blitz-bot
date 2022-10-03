package me.kirito5572.commands.moderator;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MessageBulkDeleteCommand implements ICommand {
    //https://github.com/DV8FromTheWorld/JDA/wiki/10)-FAQ#what-is-the-best-way-to-delete-messages-from-history

    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
        TextChannel channel = event.getTextChannel();
        Member selfMember = Objects.requireNonNull(event.getGuild()).getSelfMember();
        Member member = event.getMember();
        if(!selfMember.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("봇이 메세지를 삭제할 권한이 없습니다.").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
            return;
        }
        assert member != null;
        if(member.hasPermission(Permission.MESSAGE_MANAGE)) {
            String deleteCount = args.get(0);
            Member deleteMember = null;
            if(args.size() == 2) {
                String deleteMemberString = args.get(1);
                List<Member> foundMembers = FinderUtil.findMembers(deleteMemberString, event.getGuild());
                if(foundMembers.size() == 0) {
                    event.getChannel().sendMessage("""
                    차단할 유저를 찾을수 없습니다.
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
                deleteMember = foundMembers.get(0);
            }
            if (deleteCount.equals("")) {
                channel.sendMessage("삭제할 메세지 숫자만큼 명령어 뒤에 숫자를 입력해주세요").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
            }
            if (Integer.parseInt(deleteCount) < 1) {
                channel.sendMessage("1보다 큰 숫자를 입력해주세요").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
            } else if (Integer.parseInt(deleteCount) > 100) {
                channel.sendMessage("100보다 작은 숫자를 입력해주세요").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
            }
            if(deleteMember == null) {
                channel.getIterableHistory()
                        .takeAsync(Integer.parseInt(deleteCount))
                        .thenAccept(channel::purgeMessages)
                        .whenCompleteAsync((count, thr) ->
                                channel.sendMessageFormat("`%d` 개의 채팅 삭제 완료", count).queue((message) -> {
                                    if (message != null) {
                                        message.delete().queueAfter(10, TimeUnit.SECONDS);
                                    }
                                }))
                        .exceptionally((thr) -> {
                            String cause = "";

                            if (thr.getCause() != null) {
                                cause = " 에러 발생 사유: " + thr.getCause().getMessage();
                            }

                            channel.sendMessageFormat("에러: %s%s", thr.getMessage(), cause).queue();
                            return null;
                        });
            } else {
                List<Message> messages = new ArrayList<>();
                User DeleteUser = deleteMember.getUser();
                channel.getIterableHistory()
                        .forEachAsync(m -> {
                            if (m.getAuthor().equals(DeleteUser)) messages.add(m);
                            return messages.size() < Integer.parseInt(deleteCount);
                        })
                        .thenRun(() -> channel.purgeMessages(messages))
                        .whenCompleteAsync((count, thr) ->
                                channel.sendMessageFormat("`%d` 개의 채팅 삭제 완료", count).queue((message) -> {
                                    if (message != null) {
                                        message.delete().queueAfter(10, TimeUnit.SECONDS);
                                    }
                                }))
                        .exceptionally((thr) -> {
                            String cause = "";

                            if (thr.getCause() != null) {
                                cause = " 에러 발생 사유: " + thr.getCause().getMessage();
                            }

                            channel.sendMessageFormat("에러: %s%s", thr.getMessage(), cause).queue();
                            return null;
                        });
            }

        } else {
            channel.sendMessage("이 명령어를 사용할 권한이 없습니다.").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
        }
    }

    @Override
    public @NotNull String getHelp() {
        return "null";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[]{"청소", "delete", "d"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "(관리자 전용) 메세지를 입력한 숫자만큼 대량 삭제 합니다.";
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
