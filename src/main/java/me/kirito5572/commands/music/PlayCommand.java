package me.kirito5572.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/** @noinspection unused*/
public class PlayCommand implements ICommand {
    /** @noinspection unused*/
    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {

        TextChannel channel = event.getTextChannel();
        AudioManager audioManager = Objects.requireNonNull(event.getGuild()).getAudioManager();
        GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();
        assert memberVoiceState != null;
        VoiceChannel voiceChannel = memberVoiceState.getChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
        AudioPlayer player = musicManager.player;
        if(!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("먼저 보이스 채널에 들어오세요").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
            return;
        }
        if(!audioManager.isConnected()) {

            Member selfMember = event.getGuild().getSelfMember();

            assert voiceChannel != null;
            if(!selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
                channel.sendMessageFormat("%s 채널에 들어올 권한이 없습니다.",voiceChannel).queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
                return;
            }

        }
        if(player.isPaused()) {
            if(player.getPlayingTrack() != null) {
                player.setPaused(false);
                channel.sendMessage("일시정지 된 노래가 다시 재생됩니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));

                return;
            } else {
                player.setPaused(false);
                if(args.isEmpty()) {
                    channel.sendMessage("URL을 입력헤주세요").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));

                    return;
                }
            }
        }

        String input = String.join(" ", args);

        if(!isUrl(input) && !input.startsWith("ytsearch:")) {
            channel.sendMessage("""
                    아래 적혀져있는 플랫폼만 지원합니다..
                    Youtube / Youtube music
                    SoundCloud / Bandcamp
                    Vimeo / Twitch(stream only)
                    HTTP URL(지원 확장자: MP3, FLAC, WAV, MP4(with AAC), OGG/AAC streams)
                    """).queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
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
                        this.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!audioManager1.isConnected()) {
                        break;
                    }
                    if(!musicManager1.player.isPaused()) {
                        assert voiceChannel != null;
                        if (voiceChannel.getMembers().size() < 2) {
                            event.getChannel().sendMessage("사람이 아무도 없어, 노래가 일시 정지 되었습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
                            musicManager1.player.setPaused(true);
                            new Thread(() -> {
                                int i = 0;
                                while (true) {
                                    try {
                                        this.wait(750);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (voiceChannel.getMembers().size() < 2) {
                                        i++;
                                    } else {
                                        break;
                                    }
                                    if(i > 120) {
                                        event.getChannel().sendMessage("오랫동안 사람이 아무도 없어, 노래 재생이 정지 되었습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
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

        musicManager = manager.getGuildMusicManager(Objects.requireNonNull(event.getGuild()));
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

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getHelp() {
        return "URL의 노래를 재생합니다";
    }

    @NotNull
    @Override
    public String[] getInvoke() {
        return new String[] {"재생","play","p"};
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getSmallHelp() {
        return "URL의 노래를 재생합니다, 단축어: p";
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
