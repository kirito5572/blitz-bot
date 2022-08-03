package me.kirito5572.commands.music;

import me.kirito5572.App;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import me.kirito5572.objects.EventPackage;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class StopCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        PlayerManager playerManager = PlayerManager.getInstance();
        AudioManager audioManager = Objects.requireNonNull(event.getGuild()).getAudioManager();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());

        if(!audioManager.isConnected()) {
            event.getChannel().sendMessage("음성 채널에 연결되어있지 않아 사용이 불가능합니다.").queue();

            return;
        }

        musicManager.player.stopTrack();
        musicManager.player.setPaused(false);

        Member selfMember = event.getGuild().getSelfMember();
        if(!selfMember.hasPermission(Permission.VOICE_CONNECT)) {
            event.getChannel().sendMessage("보이스채널 권한이 없습니다..").queue();
            return;
        }

        event.getChannel().sendMessage("노래 재생을 멈춥니다.").queue();

    }

    @NotNull
    @Override
    public String getHelp() {
        return "노래를 정지하고 봇이 나갑니다" +
                "사용법:`" + App.getPREFIX() + getInvoke() + "`";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "stop";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "재생 정지";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
