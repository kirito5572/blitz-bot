package BOT.Commands.Music;

import BOT.App;
import BOT.Music.GuildMusicManager;
import BOT.Music.PlayerManager;
import BOT.Objects.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StopClearCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull GuildMessageReceivedEvent event) {
        PlayerManager playerManager = PlayerManager.getInstance();
        AudioManager audioManager = event.getGuild().getAudioManager();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());
        if(!audioManager.isConnected()) {
            event.getChannel().sendMessage("음성 채널에 연결되어있지 않아 사용이 불가능합니다.").queue();

            return;
        }

        musicManager.scheduler.getQueue().clear();
        musicManager.player.stopTrack();
        musicManager.player.setPaused(false);

        Member selfMember = event.getGuild().getSelfMember();
        if(!selfMember.hasPermission(Permission.VOICE_CONNECT)) {
            event.getChannel().sendMessage("보이스채널 권한이 없습니다.").queue();
            return;
        }

        event.getChannel().sendMessage("노래 재생을 멈추고 재생목록을 비웁니다.").queue();

    }

    @NotNull
    @Override
    public String getHelp() {
        return "노래를 정지하고 재생목록을 초기화 하며 봇이 나갑니다\n" +
                "단축어: sc" +
                "사용법:`" + App.getPREFIX() + getInvoke() + "`";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "stopclear";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "재생 정지후 재생목록 초기화, 단축어: sc";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
