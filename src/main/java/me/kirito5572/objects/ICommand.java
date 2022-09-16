package me.kirito5572.objects;

import javax.annotation.Nonnull;
import java.util.List;

public interface ICommand {

    void handle(List<String> args, @Nonnull EventPackage event);

    String getHelp();

    String[] getInvoke();

    String getSmallHelp();

    boolean isAdminOnly();

    boolean isOwnerOnly();

    boolean isMusicOnly();

}
