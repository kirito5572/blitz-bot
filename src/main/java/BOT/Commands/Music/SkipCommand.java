package BOT.Commands.Music;

import BOT.App;
import BOT.Music.GuildMusicManager;
import BOT.Music.PlayerManager;
import BOT.Music.TrackScheduler;
import BOT.Objects.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SkipCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
        TrackScheduler scheduler = musicManager.scheduler;
        AudioPlayer player = musicManager.player;

        Member selfMember = event.getGuild().getSelfMember();
        if(!selfMember.hasPermission(Permission.VOICE_CONNECT)) {
            channel.sendMessage("보이스채널 권한이 없습니다..").queue();
            return;
        }

        if (player.getPlayingTrack() == null) {
            channel.sendMessage("노래를 재생하고 있지 않습니다.").queue();

            return;
        }
        if (scheduler.getQueue().size() < 2) {
            channel.sendMessage("스킵할 노래가 존재하지 않습니다.").queue();

            return;
        }

        try {
            scheduler.nextTrack();
        } catch (IllegalStateException e) {
            try {
                scheduler.nextTrack();
            } catch (IllegalStateException e1) {
                try {
                    scheduler.nextTrack();
                } catch (IllegalStateException e2) {
                    StackTraceElement[] element = e.getStackTrace();
                    StringBuilder builder = new StringBuilder();
                    for(StackTraceElement traceElement : element) {
                        builder.append(traceElement.toString()).append("\n");
                    }
                    event.getChannel().sendMessage("에러가 발생했습니다.\n" +
                            builder.toString()).queue();
                }
            }
        }

        channel.sendMessage("다음 노래로 넘깁니다.").queue();
    }

    @NotNull
    @Override
    public String getHelp() {
        return "이 노래를 그만 재생합니다." +
                "사용법: `" + App.getPREFIX() + getInvoke() + "`";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "skip";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "노래를 스킵합니다";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
