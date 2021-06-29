package BOT;

import BOT.Listener.*;
import BOT.Objects.CommandManager;
import BOT.Objects.SQLConnector;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileReader;

public class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    private final static String PREFIX = "!";

    private static String[] FilterList = new String[0];

    public App() {
        StringBuilder TOKENreader = new StringBuilder();
        try {
            File file = new File("C:\\DiscordServerBotSecrets\\blitz_bot\\TOKEN.txt");
            FileReader fileReader = new FileReader(file);
            int singalCh;
            while ((singalCh = fileReader.read()) != -1) {
                TOKENreader.append((char) singalCh);
            }
        } catch (Exception e) {
            logger.error("의도치 않은 예외 발생" + e);
        }

        WebUtils.setUserAgent("Chrome 89.0.4389.114 discord bot");

        SQLConnector sqlConnector = new SQLConnector();

        CommandManager commandManager = new CommandManager(sqlConnector);
        Listener listener = new Listener(commandManager, sqlConnector);
        giveRoleListener giveRoleListener = new giveRoleListener(sqlConnector);
        filterListener noticeAutoTransListener = new filterListener(sqlConnector);
        LogListener logListener = new LogListener(sqlConnector);
        MuteListener muteListener = new MuteListener(sqlConnector);
        MessagePinListener messagePinListener = new MessagePinListener(sqlConnector);

        try {
            logger.info("부팅");
            JDABuilder.createDefault(TOKENreader.toString())
                    .setAutoReconnect(true)
                    .addEventListeners(listener, giveRoleListener, noticeAutoTransListener, logListener, muteListener, messagePinListener)
                    .setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .build().awaitReady();
            logger.info("부팅 완료");
        } catch (LoginException | InterruptedException e) {
            logger.error("의도치 않은 예외 발생" + e);
        }
    }

    public static void main(String[] args) {
        new App();
    }

    public static String getPREFIX() {
        return PREFIX;
    }

    public static void setFilterList(String[] filterList) {
        FilterList = filterList;
    }

    public static String[] getFilterList() {
        return FilterList;
    }
}
