package BOT.Objects;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.annotation.Nonnull;
import java.util.List;

public interface ICommand {

    void handle(List<String> args, @Nonnull SlashCommandEvent event);

    String getHelp();

    String getInvoke();

    String getSmallHelp();

    boolean isAdminOnly();

}
