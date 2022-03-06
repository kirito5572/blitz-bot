package BOT.Commands.Music;

import BOT.App;
import BOT.Music.GuildMusicManager;
import BOT.Music.PlayerManager;
import BOT.Objects.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QueueCommand implements ICommand {

    @Override
    public void handle(@NotNull List<String> args, @NotNull SlashCommandEvent event) {
        TextChannel channel = event.getTextChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();
        AudioPlayer player = musicManager.player;

        String joined = String.join("", args);

        if(joined.equals("")) {
            joined = "1";
        }

        if(queue.isEmpty()) {
            if(player.getPlayingTrack() == null) {
                channel.sendMessage("재생목록이 비었습니다.").queue();

                return;
            }
        }
        int maxTrackCount;
        int minTrackCount;
        if(joined.equals("1")) {
            maxTrackCount = Math.min(queue.size(), (20 * Integer.parseInt(joined)) - 1) + 2;
            minTrackCount = Math.min(queue.size(), (20 * (Integer.parseInt(joined) - 1)));
        } else {
            maxTrackCount = Math.min(queue.size(), (20 * Integer.parseInt(joined)) - 1) - 1;
            minTrackCount = Math.min(queue.size(), (20 * (Integer.parseInt(joined) - 1)) + 1);
        }
        List<AudioTrack> tracks = new ArrayList<>(queue);
        if(queue.size() < maxTrackCount) {
            maxTrackCount = queue.size();
        }
        if(minTrackCount >= queue.size()) {
            channel.sendMessage( "`" + App.getPREFIX() + "queue " + joined + "`는 비어있습니다.\n`" +
                    App.getPREFIX() + "queue " + (int)Math.ceil((queue.size() + 1) / 20.0) +
                    "`까지 재생목록이 존재합니다.").queue();

            return;
        }
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                .setTitle("현재 재생목록 (총합: " + (queue.size() - 1) + ") 페이지: " + joined);
        if(!queue.isEmpty()) {
            AudioTrackInfo info = player.getPlayingTrack().getInfo();
            builder.appendDescription("현재 재생중: " + info.title + " - " + info.author + "\n");
            for (int i = minTrackCount; i < maxTrackCount; i++) {
                try {
                    AudioTrack track = tracks.get(i);
                    info = track.getInfo();
                    builder.appendDescription(String.format(
                            (i) + ". %s - %s\n",
                            info.title,
                            info.author
                    ));

                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        if(queue.size() > maxTrackCount) {
            builder.appendDescription("다음 재생목록 확인: `"+ App.getPREFIX() + getInvoke() + " " + (Integer.parseInt(joined) + 1) + "`");
        }

        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @NotNull
    @Override
    public String getHelp() {
        return "앞으로 재생될 남은 노래 목록\n" +
                "사용법 `" + App.getPREFIX() + getInvoke() + "`";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "queue";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "재생목록 출력";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
