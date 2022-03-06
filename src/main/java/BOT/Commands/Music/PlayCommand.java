package BOT.Commands.Music;

import BOT.App;
import BOT.Music.GuildMusicManager;
import BOT.Music.PlayerManager;
import BOT.Objects.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public class PlayCommand implements ICommand {
    @Override
    public void handle(@NotNull List<String> args, @NotNull SlashCommandEvent event) {

        TextChannel channel = event.getTextChannel();
        AudioManager audioManager = event.getGuild().getAudioManager();
        GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();
        assert memberVoiceState != null;
        VoiceChannel voiceChannel = memberVoiceState.getChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
        AudioPlayer player = musicManager.player;
        if(!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("먼저 보이스 채널에 들어오세요").queue();
            return;
        }
        if(!audioManager.isConnected()) {

            Member selfMember = event.getGuild().getSelfMember();

            assert voiceChannel != null;
            if(!selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
                channel.sendMessageFormat("%s 채널에 들어올 권한이 없습니다.",voiceChannel).queue();
                return;
            }

        }
        if(player.isPaused()) {
            if(player.getPlayingTrack() != null) {
                player.setPaused(false);
                channel.sendMessage("일시정지 된 노래가 다시 재생됩니다.").queue();

                return;
            } else {
                player.setPaused(false);
                if(args.isEmpty()) {
                    channel.sendMessage("URL을 입력헤주세요").queue();

                    return;
                }
            }
        }

        String input = String.join(" ", args);

        if(!isUrl(input) && !input.startsWith("ytsearch:")) {
            channel.sendMessage("정확한 유튜브, 유튜브 뮤직, soundcloud 또는 bandcamp의 링크를 보내주세요.").queue();

            return;
        }
        if(input.contains("music.youtube.com")) {
            input = input.replaceFirst("music.", "");
        }

        PlayerManager manager = PlayerManager.getInstance();
        if(!audioManager.isConnected()) {
            audioManager.openAudioConnection(voiceChannel);
            Thread thread = new Thread(() -> {
                AudioManager audioManager1 = event.getGuild().getAudioManager();
                PlayerManager playerManager1 = PlayerManager.getInstance();
                GuildMusicManager musicManager1 = playerManager1.getGuildMusicManager(event.getGuild());
                while(true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!audioManager1.isConnected()) {
                        break;
                    }
                    if(!musicManager1.player.isPaused()) {
                        assert voiceChannel != null;
                        if (voiceChannel.getMembers().size() < 2) {
                            event.getChannel().sendMessage("사람이 아무도 없어, 노래가 일시 정지 되었습니다.").queue();
                            musicManager1.player.setPaused(true);
                            new Thread(() -> {
                                int i = 0;
                                while (true) {
                                    try {
                                        Thread.sleep(750);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (voiceChannel.getMembers().size() < 2) {
                                        i++;
                                    } else {
                                        break;
                                    }
                                    if(i > 120) {
                                        event.getChannel().sendMessage("오랫동안 사람이 아무도 없어, 노래 재생이 정지 되었습니다.").queue();
                                        audioManager.closeAudioConnection();
                                        break;
                                    }
                                }
                            }).start();
                        }
                    }
                }
            });
            thread.start();
        }
        manager.loadAndPlay(event.getTextChannel(), input);

        musicManager = manager.getGuildMusicManager(event.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

        if(queue.isEmpty()) {
            manager.getGuildMusicManager(event.getGuild()).player.setVolume(50);
        }
    }

    private boolean isUrl(@NotNull String input) {
        try {
            new URL(input);

            return true;
        } catch (MalformedURLException ignored) {
            return false;
        }
    }

    @NotNull
    @Override
    public String getHelp() {
        return "URL의 노래를 재생합니다\n" +
                "단축어: p\n" +
                "사용법 : '" + App.getPREFIX() + getInvoke() + "'[URL]\n" +
                "사용가능 URL: youtube, soundcloud, bandcamp, youtube music";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "play";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "URL의 노래를 재생합니다, 단축어: p";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
