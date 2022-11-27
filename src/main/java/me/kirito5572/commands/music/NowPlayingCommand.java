package me.kirito5572.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** @noinspection unused*/
public class NowPlayingCommand implements ICommand {
    /** @noinspection unused*/
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        TextChannel channel = event.textChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild()));
        AudioPlayer player = musicManager.player;

        Member selfMember = event.getGuild().getSelfMember();
        if(!selfMember.hasPermission(Permission.VOICE_CONNECT)) {
            channel.sendMessage("봇에게 보이스 채널 입장 권한을 부여해 주세요.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }

        if (player.getPlayingTrack() == null) {
            channel.sendMessage("아무 노래도 재생하고 있지 않습니다.").queue();

            return;
        }

        AudioTrackInfo info = player.getPlayingTrack().getInfo();

        channel.sendMessageEmbeds(EmbedUtils.embedMessage(String.format(
                "**" + (player.isPaused() ? "일시정지중" : "재생중") + ": ** [%s](%s)\n%s %s/%s",
                info.title,
                info.uri,
                player.isPaused() ? "\u23F8" : "▶",
                formatTime(player.getPlayingTrack().getPosition()),
                formatTime(player.getPlayingTrack().getDuration())
        )).build()).queue();
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getHelp() {
        return "현재 재생중인 음악목록을 알려줍니다.";
    }

    @NotNull
    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"재생중","nowplaying", "np"};
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getSmallHelp() {
        return "현재 재생중인 음악목록을 알려줍니다 단축어: np";
    }

    private String formatTime(long timeInMillis) {
        final long hours = timeInMillis / TimeUnit.HOURS.toMillis(1) % 24;
        final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1) % 60;
        final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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

    @Override
    public boolean isMusicOnly() {
        return true;
    }
}
