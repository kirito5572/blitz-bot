package me.kirito5572.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.kirito5572.App;
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
import java.util.concurrent.BlockingQueue;

/** @noinspection unused*/
public class QueueDetectCommand implements ICommand {
    /** @noinspection unused*/
    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
        new Thread(() -> {
            TextChannel channel = event.getTextChannel();
            PlayerManager playerManager = PlayerManager.getInstance();
            GuildMusicManager musicManager = playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild()));
            BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

            String joined = String.join("", args);
            Member selfMember = event.getGuild().getSelfMember();
            if (!selfMember.hasPermission(Permission.VOICE_CONNECT)) {
                channel.sendMessage("보이스채널 권한이 없습니다..").queue();
                return;
            }

            if (queue.isEmpty()) {
                channel.sendMessage("재생목록이 비었습니다.").queue();

                return;
            }

            System.out.println(queue.size());
            String a = channel.sendMessage("재생목록을 비우는 중입니다.").complete().getId();

            if (joined.equals("")) {
                musicManager.scheduler.getQueue().clear();
                channel.editMessageById(a, "재생목록을 초기화 했습니다.").queue();
            } else {
                for (int i = 0; i < Integer.parseInt(joined); i++) {
                    musicManager.scheduler.nextTrack();
                    channel.editMessageById(a, "재생목록에서" + joined + "개의 노래를 삭제했습니다.").queue();
                }
            }

        }).start();
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getHelp() {
        return "재생목록 앞에서 부터\n" +
                "숫자만큼 노래를 없앱니다.\n" +
                "(숫자 미 입력시 전체 삭제)\n" +
                "단축어: qd\n" +
                "사용법: '" + App.getPREFIX() + getInvoke() + "'(숫자)";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "queuedel";
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getSmallHelp() {
        return "재생목록을 정리합니다, 단축어: qd";
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
}
