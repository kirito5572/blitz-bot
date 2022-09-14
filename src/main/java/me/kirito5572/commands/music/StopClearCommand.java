package me.kirito5572.commands.music;

import me.kirito5572.App;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** @noinspection unused*/
public class StopClearCommand implements ICommand {
    /** @noinspection unused*/
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        PlayerManager playerManager = PlayerManager.getInstance();
        AudioManager audioManager = Objects.requireNonNull(event.getGuild()).getAudioManager();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
        if(!audioManager.isConnected()) {
            event.getChannel().sendMessage("음성 채널에 연결되어있지 않아 사용이 불가능합니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));

            return;
        }

        musicManager.scheduler.getQueue().clear();
        musicManager.player.stopTrack();
        musicManager.player.setPaused(false);

        Member selfMember = event.getGuild().getSelfMember();
        if(!selfMember.hasPermission(Permission.VOICE_CONNECT)) {
            event.getChannel().sendMessage("보이스채널 권한이 없습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }

        event.getChannel().sendMessage("플레이어를 초기화 합니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));

    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getHelp() {
        return "노래를 정지하고 재생목록을 초기화 하며 봇이 나갑니다\n" +
                "단축어: sc" +
                "사용법:`" + App.getPREFIX() + getInvoke() + "`";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "stopclear";
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getSmallHelp() {
        return "재생 정지후 재생목록 초기화, 단축어: sc";
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
