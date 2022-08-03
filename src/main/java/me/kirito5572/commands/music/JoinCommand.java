package me.kirito5572.commands.music;

import me.kirito5572.App;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class JoinCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull SlashCommandEvent event) {
        TextChannel channel = event.getTextChannel();
        AudioManager audioManager = Objects.requireNonNull(event.getGuild()).getAudioManager();

        if(audioManager.isConnected()) {
            channel.sendMessage("이미 보이스채널에 들어왔습니다.").queue();
            return;
        }

        GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if(!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("먼저 보이스 채널에 들어오세요").queue();
            return;
        }

        VoiceChannel voiceChannel = memberVoiceState.getChannel();
        Member selfMember = event.getGuild().getSelfMember();

        assert voiceChannel != null;
        if(!selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
            channel.sendMessageFormat("%s 보이스 채널에 들어올 권한이 없습니다.",voiceChannel).queue();
            return;
        }

        audioManager.openAudioConnection(voiceChannel);
        channel.sendMessage("보이스채널에 들어왔습니다.").queue();
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
                    autoPaused(event, audioManager, voiceChannel, musicManager);
                }
            }
        });
        thread.start();
    }

    static void autoPaused(@NotNull SlashCommandEvent event, AudioManager audioManager, VoiceChannel voiceChannel, GuildMusicManager musicManager) {
        if (voiceChannel.getMembers().size() < 2) {
            musicManager.player.isPaused();
            event.getChannel().sendMessage("사람이 아무도 없어, 노래가 일시 정지 되었습니다.\n" +
                    "다시 재생하려면 `" + App.getPREFIX() + "재생` 을 입력해주세요").queue();
            musicManager.player.setPaused(true);

            //TODO 이거 https://fruitdev.tistory.com/135 참고해서 작업하기
            int sleep = 750;
            final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            AtomicInteger i = new AtomicInteger();

            executor.scheduleAtFixedRate(() -> {
                if (voiceChannel.getMembers().size() < 2) {
                    i.getAndIncrement();
                } else {
                    executor.shutdown();
                }
                if(i.get() > 120) {
                    event.getChannel().sendMessage("오랫동안 사람이 아무도 없어, 노래 재생이 정지 되었습니다.").queue();
                    audioManager.closeAudioConnection();
                    executor.shutdown();
                }
            }, 0, sleep, TimeUnit.MILLISECONDS);
        }
    }

    @NotNull
    @Override
    public String getHelp() {
        return "노래 틀도록 하기 위해 거치는 과정" +
                "사용법 : '" + App.getPREFIX() + getInvoke() + "'";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "join";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "음성채널에 봇이 들어옵니다.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
