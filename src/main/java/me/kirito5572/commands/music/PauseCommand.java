package me.kirito5572.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.kirito5572.App;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/** @noinspection unused*/
public class PauseCommand implements ICommand {
    /** @noinspection unused*/
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        MessageChannel channel = event.getMessageChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild()));
        AudioPlayer player = musicManager.player;

        if(player.getPlayingTrack() != null) {
            player.setPaused(true);
            channel.sendMessage("일시 정지 되었습니다.").queue();
        } else {
            channel.sendMessage("노래가 재생중이 아닙니다.").queue();
        }
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getHelp() {
        return "재생중인 노래를 일시정지 합니다." +
                "사용법 `" + App.getPREFIX() + getInvoke() + "`";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "pause";
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getSmallHelp() {
        return "재생중인 노래를 일시정지 합니다.";
    }

    /** @noinspection unused*/
    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
