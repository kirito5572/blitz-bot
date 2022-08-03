package me.kirito5572.commands.music;

import me.kirito5572.App;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class PauseCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull SlashCommandEvent event) {
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

    @NotNull
    @Override
    public String getSmallHelp() {
        return "재생중인 노래를 일시정지 합니다.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
