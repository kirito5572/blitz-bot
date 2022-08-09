package me.kirito5572.commands.music;

import me.kirito5572.music.GuildMusicManager;
import me.kirito5572.music.PlayerManager;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.kirito5572.objects.EventPackage;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class QueueMixCommand implements ICommand {
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
        event.getTextChannel().sendMessage("재생 목록이 셔플되었습니다.").queue();
    }

    @NotNull
    @Override
    public String getHelp() {
        return "재생목록을 셔플 합니다.\n" +
                "단축어: qm";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "queuemix";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "재생 목록 셔플, 단축어: qm";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}