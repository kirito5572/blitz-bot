package me.kirito5572.commands.music;

import me.kirito5572.App;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class leaveCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull SlashCommandEvent event) {
        TextChannel channel = event.getTextChannel();
        AudioManager audioManager = Objects.requireNonNull(event.getGuild()).getAudioManager();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
        Member selfMember = event.getGuild().getSelfMember();
        if(!selfMember.hasPermission(Permission.VOICE_CONNECT)) {
            channel.sendMessage("보이스채널 권한이 없습니다..").queue();
            return;
        }
        if(!audioManager.isConnected()) {
            channel.sendMessage("나갈 보이스 채널이 없습니다.").queue();
            return;
        }
        /*VoiceChannel voiceChannel = audioManager.getConnectedChannel();

        if(!voiceChannel.getMembers().contains(event.getMember())) {
            channel.sendMessage("봇과 같은 보이스 채널에 있어야 합니다.").queue();
            return;
        }*/

        audioManager.closeAudioConnection();
        musicManager.scheduler.getQueue().clear();

        channel.sendMessage("보이스채널을 떠납니다.").queue();
    }

    @NotNull
    @Override
    public String getHelp() {
        return "노래를 정지하고 나갑니다" +
                "사용법 : '" + App.getPREFIX() + getInvoke() + "'";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "leave";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "음성채널에서 봇이 나갑니다";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
