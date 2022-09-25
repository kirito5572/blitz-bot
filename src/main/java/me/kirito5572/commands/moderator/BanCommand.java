package me.kirito5572.commands.moderator;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BanCommand implements ICommand {
    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {

        TextChannel channel = event.getTextChannel();
        Member member = event.getMember();
        Member selfMember = event.getGuild().getSelfMember();
        if(args.isEmpty()) {
            event.getChannel().sendMessage("유저명이 입력되지 않았습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }
        List<Member> foundMember = FinderUtil.findMembers(String.join(" ", args.get(0)), event.getGuild());
        if (foundMember.isEmpty()) {
            event.getChannel().sendMessage("해당 유저를 봇이 찾을수 없거나, 인수가 잘못 입력되었습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }
        Member target = foundMember.get(0);
        String reason = String.join(" ", args.subList(1, args.size()));

        assert member != null;
        if (!member.hasPermission(Permission.BAN_MEMBERS) || !member.canInteract(target)) {
            channel.sendMessage("이 명령어를 사용하기 위한 권한이 없습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }

        if (!selfMember.hasPermission(Permission.BAN_MEMBERS) || !selfMember.canInteract(target)) {
            channel.sendMessage("봇이 이 명령어를 사용하기 위한 권한이 없습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }

        event.getGuild().ban(target, 1)
                .reason(String.format("밴 한 사람: %#s, 이유: %s", event.getAuthor(), reason)).queue();

        channel.sendMessage(target.getAsMention() + ", 밴 완료").queue();
    }

    @NotNull
    @Override
    public String getHelp() {
        return "유저를 서버에서 밴합니다.";
    }

    @NotNull
    @Override
    public String[] getInvoke() {
        return new String[]{"밴", "ban", "b"};
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "유저를 서버에서 밴합니다.";
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
