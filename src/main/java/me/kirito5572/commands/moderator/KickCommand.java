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

public class KickCommand implements ICommand {
    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
        TextChannel channel = event.textChannel();

        if(args.isEmpty()) {
            event.getChannel().sendMessage("유저명이 입력되지 않았습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }

        if (!event.getGuild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
            channel.sendMessage("봇이 이 명령어를 사용할 권한이 없습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }

        List<Member> foundMember = FinderUtil.findMembers(String.join(" ", args), event.getGuild());
        if (foundMember.isEmpty()) {
            event.getChannel().sendMessage("해당 유저를 봇이 찾을수 없거나, 인수가 잘못 입력되었습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }
        Member target = foundMember.get(0);
        String reason = String.join(" ", args.subList(1, args.size()));



        event.getGuild().kick(target, String.format("킥한사람: %#s, 사유: %s",
                event.getAuthor(), reason)).queue();

        channel.sendMessage("추방 성공!").queue();

    }

    @NotNull
    @Override
    public String getHelp() {
        return "서버에서 유저를 추방합니다.";
    }

    @NotNull
    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"추방", "kick", "k"};
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "서버에서 유저를 추방합니다.";
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