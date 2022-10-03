package me.kirito5572.commands.blitz;

import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SearchPlayerInfoCommand implements ICommand {
    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {

    }

    @Override
    public @NotNull String getHelp() {
        return null;
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[0];
    }

    @Override
    public @NotNull String getSmallHelp() {
        return null;
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
