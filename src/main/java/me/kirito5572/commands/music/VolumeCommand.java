package me.kirito5572.commands.music;

import me.kirito5572.App;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class VolumeCommand implements ICommand {
    @Override
    public void handle(@NotNull List<String> args, @NotNull SlashCommandEvent event) {
        MessageChannel channel = event.getMessageChannel();
        PlayerManager manager = PlayerManager.getInstance();
        AudioManager audioManager = Objects.requireNonNull(event.getGuild()).getAudioManager();
        String joined = String.join("", args);

        Member selfMember = event.getGuild().getSelfMember();
        if(!selfMember.hasPermission(Permission.VOICE_CONNECT)) {
            channel.sendMessage("보이스채널 권한이 없습니다..").queue();
            return;
        }
        if(!audioManager.isConnected()) {
            channel.sendMessage("봇을 먼저 보이스채널에 들어오게 하세요.").queue();
            return;
        }

        GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if(!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("먼저 보이스 채널에 들어오세요").queue();
            return;
        }
        if(Integer.parseInt(joined) < 10) {
            channel.sendMessage("최소 볼륨은 10입니다. 10보다 큰 수를 입력해주세요.").queue();

            return;
        } else if(Integer.parseInt(joined) > 100) {
            channel.sendMessage("최대 볼륨은 100입니다. 100보다 작은 수를 입력해주세요.").queue();

            return;
        }

        manager.getGuildMusicManager(event.getGuild()).player.setVolume(Integer.parseInt(joined));

        channel.sendMessage("볼륨이 " + joined + "으로 변경되었습니다.").queue();
    }

    @NotNull
    @Override
    public String getHelp() {
        return "노래 소리 조절\n" +
                "단축어: v\n"+
                "사용법: `" + App.getPREFIX() + getInvoke() + "`(숫자)";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "vol";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "볼륨 조절, 단축어: v";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
