package BOT.Commands.Music;

import BOT.App;
import BOT.Music.GuildMusicManager;
import BOT.Music.PlayerManager;
import BOT.Objects.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PauseCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
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
