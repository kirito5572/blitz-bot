package me.kirito5572.commands;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.App;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BotInfoCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                .setTitle(event.getJDA().getSelfUser().getName() + "에 대한 정보")
                .addField("봇 버젼", App.getVersion(), true)
                .addField("빌드 시간", App.getBuild_time(), true)
                .addField("빌드 JDK 버젼", App.getBuild_jdk(), true);

        event.getChannel().sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getHelp() {
        return "봇에 대한 정보를 출력합니다.";
    }

    @Override
    public String getInvoke() {
        return "봇정보";
    }

    @Override
    public String getSmallHelp() {
        return "other";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
