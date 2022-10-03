package me.kirito5572.objects;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public interface ICommand {

    void handle(List<String> args, @Nonnull EventPackage event);

    @NotNull String getHelp();

    String @NotNull [] getInvoke();

    @NotNull String getSmallHelp();

    boolean isAdminOnly();

    boolean isOwnerOnly();

    boolean isMusicOnly();

}
