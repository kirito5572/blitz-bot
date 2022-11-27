package me.kirito5572.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/** @noinspection unused*/
public class QueueMixCommand implements ICommand {
    /** @noinspection unused*/
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        Random random = new Random();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild()));
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();
        List<AudioTrack> queueList = new ArrayList<>(queue);
        Collections.shuffle(queueList, random);
        queue.clear();
        queue.addAll(queueList);
        event.textChannel().sendMessage("재생 목록이 셔플되었습니다.").queue(message -> message.delete().queueAfter(7, TimeUnit.SECONDS));
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getHelp() {
        return "재생목록을 셔플 합니다.\n" +
                "단축어: qm";
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"셔플", "queuemix", "qm"};
    }

    /** @noinspection unused*/
    @NotNull
    @Override
    public String getSmallHelp() {
        return "재생 목록 셔플, 단축어: qm";
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
