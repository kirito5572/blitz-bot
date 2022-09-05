package me.kirito5572.commands.music;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.App;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.getYoutubeSearch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** @noinspection unused*/
public class SearchCommand implements ICommand {
    private final Logger logger = LoggerFactory.getLogger(SearchCommand.class);
    /** @noinspection unused*/
    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
        new Thread(() -> {
            AudioManager audioManager = Objects.requireNonNull(event.getGuild()).getAudioManager();
            TextChannel channel = event.getTextChannel();
            GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();
            assert memberVoiceState != null;
            VoiceChannel voiceChannel = memberVoiceState.getChannel();
            if(!audioManager.isConnected()) {
                Member selfMember = event.getGuild().getSelfMember();

                if(voiceChannel == null) {
                    channel.sendMessage("먼저 보이스 채널에 들어오세요").queue();
                    return;
                }
                if(!selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
                    channel.sendMessageFormat("%s 보이스 채널에 들어올 권한이 없습니다.",voiceChannel).queue();
                    return;
                }
            }
            if(!memberVoiceState.inVoiceChannel()) {
                channel.sendMessage("먼저 보이스 채널에 들어오세요").queue();
                return;
            }
            try {
                String name = String.join("+", args);
                String[][] data = getYoutubeSearch.Search(name);
                if(data == null) {
                    event.getChannel().sendMessage("youtube 검색에 문제가 발생했습니다").queue();
                    return;
                }
                StringBuilder builder = new StringBuilder();
                for(int i = 0; i < 10; i++) {
                    builder.append(i + 1).append(". ").append(data[i][0]).append("\n");
                }
                EmbedBuilder builder1 = EmbedUtils.getDefaultEmbed()
                        .setTitle("검색 결과")
                        .setDescription(builder.toString());

                Message message = channel.sendMessageEmbeds(builder1.build()).complete();

                for(int i = 0; i < 11; i++) {
                    Thread.sleep(1000);
                    try {
                        int a = Integer.parseInt(event.getChannel().retrieveMessageById(event.getChannel().getLatestMessageId()).complete().getContentRaw());
                        if(!audioManager.isConnected()) {
                            audioManager.openAudioConnection(voiceChannel);
                            Thread thread = new Thread(() -> {
                                AudioManager audioManager1 = event.getGuild().getAudioManager();
                                PlayerManager playerManager = PlayerManager.getInstance();
                                GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
                                while(true) {
                                    try {
                                        this.wait(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if(!audioManager1.isConnected()) {
                                        break;
                                    }
                                    if(!musicManager.player.isPaused()) {
                                        assert voiceChannel != null;
                                        JoinCommand.autoPaused(event, audioManager, voiceChannel, musicManager);
                                    }
                                }
                            });
                            thread.start();
                        }
                        PlayerManager manager = PlayerManager.getInstance();
                        message.delete().complete();
                        message = channel.sendMessage("노래가 추가되었습니다.").complete();
                        message.delete().queueAfter(5, TimeUnit.SECONDS);
                        manager.loadAndPlay(channel, "https://youtu.be/" + data[a - 1][1]);
                        return;

                    } catch (Exception ignored) {

                    }
                }
                message.delete().complete();
                message = channel.sendMessage("대기 시간이 초과되어 삭제되었습니다.").complete();
                message.delete().queueAfter(5,TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                Message message = channel.sendMessage("ErrorCode : 0x5734 THREAD ERROR").complete();
                message.delete().queueAfter(7,TimeUnit.SECONDS);

                StackTraceElement[] eStackTrace = e.getStackTrace();
                StringBuilder a = new StringBuilder();
                for (StackTraceElement stackTraceElement : eStackTrace) {
                    a.append(stackTraceElement).append("\n");
                }
                logger.warn(a.toString());
            }
        }).start();
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getHelp() {
        return "유튜브에서 노래를 검색합니다\n" +
                "사용법 : '" + App.getPREFIX() + getInvoke() + "'[검색할 노래]";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "search";
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getSmallHelp() {
        return "유튜브에서 노래를 검색합니다";
    }

    /** @noinspection unused*/
    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
