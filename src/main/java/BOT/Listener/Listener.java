package BOT.Listener;

import BOT.Objects.CommandManager;
import BOT.Objects.SQLConnector;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Listener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(Listener.class);
    private final CommandManager manager;
    private final SQLConnector sqlConnector;

    public Listener(CommandManager manager, SQLConnector sqlConnector) {
        this.manager = manager;
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("로그인 성공:" + event.getJDA().getSelfUser());
        System.out.printf("로그인 성공: %#s%n", event.getJDA().getSelfUser());
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        if(event.getAuthor().isBot()) {
            return;
        }
        if(message.isWebhookMessage()) {
            return;
        }
        manager.handleCommand(event);
    }

}
