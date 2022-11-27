package me.kirito5572.commands.moderator;

import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class UnBanCommand implements ICommand {
    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {

        TextChannel channel = event.textChannel();

        if (!Objects.requireNonNull(event.member()).hasPermission(Permission.BAN_MEMBERS)) {
            channel.sendMessage("이 명령어를 사용할 권한이 없습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }

        if (!event.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
            channel.sendMessage("봇이 이 명령어를 사용할 권한이 없습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }

        if (args.isEmpty()) {
            event.getChannel().sendMessage("유저명이 입력되지 않았습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }

        String argsJoined = String.join(" ", args);


        event.getGuild().retrieveBanList().queue((bans) -> {

            List<User> goodUsers = bans.stream().filter((ban) -> isCorrectUser(ban, argsJoined))
                    .map(Guild.Ban::getUser).toList();

            if (goodUsers.isEmpty()) {
                channel.sendMessage("해당 유저는 밴 되지 않았습니다.").queue();
                return;
            }

            User target = goodUsers.get(0);

            String mod = String.format("%#s", event.getAuthor());
            String bannedUser = String.format("%#s", target);

            event.getGuild().unban(target)
                    .reason("언밴한 유저: " + mod).queue();

            channel.sendMessage("유저 " + bannedUser + " 가 언밴되었습니다.").queue();

        });
    }

    @NotNull
    @Override
    public String getHelp() {
        return "서버에서 밴한 유저를 취소합니다.";
    }

    @NotNull
    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"언밴", "unban", "ub"};
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "서버에서 밴한 유저를 취소합니다.";
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

    private boolean isCorrectUser(@NotNull Guild.Ban ban, String arg) {
        User bannedUser = ban.getUser();

        return bannedUser.getName().equalsIgnoreCase(arg) || bannedUser.getId().equals(arg)
                || String.format("%#s", bannedUser).equalsIgnoreCase(arg);
    }
}