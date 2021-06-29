package BOT.Objects;

import BOT.App;
import BOT.Commands.*;
import BOT.Commands.Music.*;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class CommandManager {
    private final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandManager(SQLConnector sqlConnector) {
        addCommand(new HelpCommand(this));
        addCommand(new PingCommand(sqlConnector));
        addCommand(new FilterWordAddCommand(sqlConnector));
        addCommand(new FilterWordRemoveCommand(sqlConnector));
        addCommand(new MuteCommand(sqlConnector));
        addCommand(new UnMuteCommand(sqlConnector));
        addCommand(new MessagePinCommand(sqlConnector));

        /*
        addCommand(new JoinCommand());
        addCommand(new leaveCommand());
        addCommand(new NowPlayingCommand());
        addCommand(new NowPlayingCommand() {
            @Override
            public @NotNull String getInvoke() {
                return "np";
            }
        });
        addCommand(new PauseCommand());
        addCommand(new PlayCommand());
        addCommand(new PlayCommand() {
            @Override
            public @NotNull String getInvoke() {
                return "p";
            }
        });
        addCommand(new QueueCommand());
        addCommand(new QueueDelectCommand());
        addCommand(new QueueDelectCommand() {
            @Override
            public @NotNull String getInvoke() {
                return "qd";
            }
        });
        addCommand(new QueueMixCommand());
        addCommand(new QueueMixCommand() {
            @Override
            public @NotNull String getInvoke() {
                return "qm";
            }
        });
        addCommand(new SearchCommand());
        addCommand(new SkipCommand());
        addCommand(new StopClearCommand());
        addCommand(new StopClearCommand() {
            @Override
            public @NotNull String getInvoke() {
                return "sc";
            }
        });
        addCommand(new StopCommand());
        addCommand(new VolumeCommand());
        addCommand(new VolumeCommand() {
            @Override
            public @NotNull String getInvoke() {
                return "v";
            }
        });
         */

    }

    private void addCommand(@NotNull ICommand command) {
        if(!commands.containsKey(command.getInvoke())) {
            commands.put(command.getInvoke(), command);
        }
        sleep();
    }

    private void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {

            StackTraceElement[] eStackTrace = e.getStackTrace();
            StringBuilder a = new StringBuilder();
            for (StackTraceElement stackTraceElement : eStackTrace) {
                a.append(stackTraceElement).append("\n");
            }
            logger.warn(a.toString());
        }
    }

    @NotNull
    public Collection<ICommand> getCommands() {
        return commands.values();
    }

    public ICommand getCommand(String name) {
        return commands.get(name);
    }

    public void handleCommand(@NotNull GuildMessageReceivedEvent event) {
        System.out.println("a");
        final TextChannel channel = event.getChannel();
        final String[] split = event.getMessage().getContentRaw().replaceFirst(
                "(?i)" + Pattern.quote(App.getPREFIX()), "").split("\\s+");
        final String invoke = split[0].toLowerCase();
        System.out.println("b");

        if(commands.containsKey(invoke)) {
            final List<String> args = Arrays.asList(split).subList(1, split.length);
            channel.sendTyping().queue();
            commands.get(invoke).handle(args, event);
        }
    }
}
