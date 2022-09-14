package me.kirito5572.commands.music;

import me.kirito5572.App;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** @noinspection unused*/
public class leaveCommand implements ICommand {
    /** @noinspection unused*/
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        TextChannel channel = event.getTextChannel();
        AudioManager audioManager = Objects.requireNonNull(event.getGuild()).getAudioManager();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
        Member selfMember = event.getGuild().getSelfMember();
        if(!selfMember.hasPermission(Permission.VOICE_CONNECT)) {
            channel.sendMessage("보이스채널 권한이 없습니다..").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }
        VoiceChannel voiceChannel = audioManager.getConnectedChannel();
        if((voiceChannel == null) || (!audioManager.isConnected())) {
            channel.sendMessage("봇이 보이스 채널에 있지 않습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }
        if(!voiceChannel.getMembers().contains(event.getMember())) {
            channel.sendMessage("봇과 같은 보이스 채널에 있어야 합니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }

        audioManager.closeAudioConnection();
        musicManager.scheduler.getQueue().clear();

        channel.sendMessage("보이스채널을 떠납니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getHelp() {
        return "노래를 정지하고 나갑니다" +
                "사용법 : '" + App.getPREFIX() + getInvoke() + "'";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "leave";
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getSmallHelp() {
        return "음성채널에서 봇이 나갑니다";
    }

    /** @noinspection unused*/
    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isOwnerOnly() {
        return false;
    }
}
