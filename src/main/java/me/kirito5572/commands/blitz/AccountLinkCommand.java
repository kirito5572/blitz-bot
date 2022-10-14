package me.kirito5572.commands.blitz;

import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.WargamingAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AccountLinkCommand implements ICommand {
    private final WargamingAPI wargamingAPI;

    public AccountLinkCommand(WargamingAPI wargamingAPI) {
        this.wargamingAPI = wargamingAPI;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        event.getChannel().sendMessage("워게이밍넷과 계정 연결을 시도합니다....").queue(
                message -> {
                    EmbedBuilder builder = wargamingAPI.getToken();
                    message.getChannel().sendMessageEmbeds(builder.build()).queue();
                    message.delete().queue();
                }
        );
    }

    @Override
    public @NotNull String getHelp() {
        return "워게이밍 계정을 봇과 연결합니다!";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"계정연결", "alink", "al"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "워게이밍 계정을 봇과 연결 합니다.";
    }

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
        return false;
    }
}
