package me.kirito5572.commands.moderator;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class UserInfoCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        User user;
        Member member;
        Guild guild = event.getGuild();

        if (args.isEmpty()) {
            user = Objects.requireNonNull(event.getMember()).getUser();
            member = event.getMember();
        } else {
            String joined = String.join(" ", args);
            List<Member> foundMember;
            foundMember = FinderUtil.findMembers(joined, guild);
            if (foundMember.isEmpty()) {
                event.getChannel().sendMessage("해당 유저를 봇이 찾을수 없거나, 인수가 잘못 입력되었습니다.").queue();
                return;
            }
            user = foundMember.get(0).getUser();
            member = foundMember.get(0);

        }
        StringBuilder serverRole = new StringBuilder();
        List<Role> role = member.getRoles();
        for (Role value : role) {
            serverRole.append(value.getAsMention()).append("\n");
        }

        MessageEmbed embed = EmbedUtils.getDefaultEmbed()
                .setColor(member.getColor())
                .setThumbnail(user.getEffectiveAvatarUrl())
                .addField("유저이름#번호", String.format("%#s", user), false)
                .addField("서버 표시 이름", member.getEffectiveName(), false)
                .addField("서버장여부 ", member.isOwner() ? "예" : "아니요", false)
                .addField("유저 ID + 언급 멘션", String.format("%s (%s)", user.getId(), member.getAsMention()), false)
                .addField("디스코드 가입 일자", user.getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())), false)
                .addField("서버 초대 일자", member.getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())), false)
                .addField("서버 부여 역할", serverRole.toString(), false)
                .addField("온라인 상태", member.getOnlineStatus().name().toLowerCase().replaceAll("_", " "), false)
                .addField("봇 여부", user.isBot() ? "예" : "아니요", false)
                .build();

        event.getChannel().sendMessageEmbeds(embed).queue();

    }

    @NotNull
    @Override
    public String getHelp() {
        return "유저정보";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "유저정보";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "(관리자 전용) 서버에 있는 유저의 정보를 불러옵니다.";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean isOwnerOnly() {
        return false;
    }
}
