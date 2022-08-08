package me.kirito5572;

import me.kirito5572.listener.*;
import me.kirito5572.objects.CommandManager;
import me.kirito5572.objects.SQLConnector;
import me.kirito5572.objects.UnsupportedOSException;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    private final static String PREFIX = "!";

    private static String[] FilterList = new String[0];

    private final static String OSStringData = System.getProperty("os.name").toLowerCase();
    private static int OS = 3;
    private final static int WINDOWS = 0;
    private final static int MAC = 1;
    private final static int UNIX = 2;
    private final static int UNSUPPORTED = 3;

    private static String version = null;
    private static String build_time = null;
    private static String build_os = null;
    private static String build_jdk = null;
    private static Date date;

    public static String openFileData(String Data) {
        StringBuilder reader = new StringBuilder();
        try {
            String sep = File.separator;
            String path;
            path = switch (OS) {
                case WINDOWS -> "C:" + sep + "DiscordServerBotSecrets" + sep + "blitz_bot" + sep + Data + ".txt";
                case MAC -> sep + "etc" + sep + "DiscordServerBotSecrets" + sep + "blitz_bot" + sep + Data + ".txt";
                case UNIX -> sep + "home" + sep + "DiscordServerBotSecrets" + sep + "blitz_bot" + sep + Data + ".txt";
                default -> throw new UnsupportedOSException("이 운영체제는 지원하지 않습니다.");
            };
            File file = new File(path);
            FileReader fileReader = new FileReader(file);
            int signalCh;
            while ((signalCh = fileReader.read()) != -1) {
                reader.append((char) signalCh);
            }
        } catch (Exception e) {
            logger.error("예외 발생:\n" + e);
            System.exit(-1);
        }
        return reader.toString();
    }

    public App() {
        if(OSStringData.contains("win")) {
            OS = WINDOWS;
        } else if(OSStringData.contains("mac")) {
            OS = MAC;
        } else if(OSStringData.contains("nix") || OSStringData.contains("nux") || OSStringData.contains("aix")) {
            OS = UNIX;
        } else {
            OS = UNSUPPORTED;
        }

        date = new Date();
        try {
            String location = new File(getClass().getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getAbsolutePath();

            Attributes attribute = new JarFile(location).getManifest().getMainAttributes();
            version = attribute.getValue("Version");
            build_time = attribute.getValue("BuildDate");
            build_os = attribute.getValue("BuildOS");
            build_jdk = attribute.getValue("BuildJDK");

        } catch (URISyntaxException | IOException e){
            logger.error(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
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
        onReadyListener onReadyListener = new onReadyListener(sqlConnector);
        DirectMessageListener directMessageListener = new DirectMessageListener(sqlConnector);

        try {
            logger.info("부팅");
            JDABuilder.createDefault(openFileData("TOKEN"))
                    .setAutoReconnect(true)
                    .addEventListeners(listener, giveRoleListener, noticeAutoTransListener, logListener,
                            muteListener, messagePinListener, onReadyListener, directMessageListener)
                    .setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .build().awaitReady();
            logger.info("부팅 완료");
        } catch (LoginException | InterruptedException e) {
            logger.error("의도치 않은 예외 발생" + e);
        }
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static void main(String[] args) {
        new App();
    }

    public static String getPREFIX() {
        return PREFIX;
    }

    public static String[] getFilterList() {
        return FilterList;
    }

    public static void setFilterList(String[] filterList) {
        FilterList = filterList;
    }

    public static String getVersion() {
        return version;
    }

    public static String getBuild_time() {
        return build_time;
    }

    public static String getBuild_os() {
        return build_os;
    }

    public static String getBuild_jdk() {
        return build_jdk;
    }

    public static Date getDate() {
        return date;
    }
}
