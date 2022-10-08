package me.kirito5572.commands.blitz;

import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.WargamingAPI;
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
                    boolean isSuccess = wargamingAPI.getToken();
                    if(isSuccess) {
                        message.editMessage("""
                                        워게이밍넷과 계정 연결을 시도합니다....
                                        계정 연결에 성공하였습니다!
                                """).queue();
                    } else {
                        message.editMessage("""
                                        워게이밍넷과 계정 연결을 시도합니다....
                                        계정 연결에 실패하였습니다!
                                """).queue();
                    }
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
