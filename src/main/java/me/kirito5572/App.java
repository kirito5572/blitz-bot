package me.kirito5572;

import me.duncte123.botcommons.web.WebUtils;
import me.kirito5572.listener.*;
import me.kirito5572.objects.*;
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
import java.sql.SQLException;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    private static String PREFIX;
    private final static int APP_STABLE = 0;
    private final static int APP_BETA = 1;
    private final static int APP_ALPHA = 2;
    private final static int APP_UNKNOWN = 3;

    private final static String OSStringData = System.getProperty("os.name").toLowerCase();
    private static int appMode = 3;
    public static int OS = 3;
    public final static int WINDOWS = 0;
    public final static int MAC = 1;
    public final static int UNIX = 2;
    public final static int UNSUPPORTED = 3;

    private static String version = null;
    private static String build_time = null;
    private static String build_os = null;
    private static String build_jdk = null;
    private static Date date;


    private static final String[] moderator = new String[]{
            //administrator
            "241540160389382145",   //김증권#2950
            "454484285035118607",   //Nya-gaming#0099
            //moderator
            "284508374924787713",   //kirito5572#5572
            "321535660576210954",   //CHERRY_PICKER#9999
            //contributor
            "265095112789327872",   //nakdo#7495
            "234326875982397452",   //OlosTan#1439(KamP0n)
            //Wargaming(Game STAFF)
            "268251437224427521",   //Cincin#2050(Duan)
            "742717525363523655",   //Jobasim#0302
            "651303118239432704",   //[WG]Summer#6183
            "208385750146744321"    //Cynical Silicon#8458
    };

    /**
     * open file.txt and get inside data
     * @param Data the name of open .txt file name
     * @return the string data that inside it
     */
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

    public App() throws SQLException, ClassNotFoundException, URISyntaxException {
        logger.info("Start up");
        if(OSStringData.contains("win")) {
            OS = WINDOWS;
        } else if(OSStringData.contains("mac")) {
            OS = MAC;
        } else if(OSStringData.contains("nix") || OSStringData.contains("nux") || OSStringData.contains("aix")) {
            OS = UNIX;
        } else {
            OS = UNSUPPORTED;
        }
        logger.info("OS: " + OSStringData);

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
        if(getVersion().contains("STABLE") || getVersion().contains("stable")) {
            appMode = APP_STABLE;
            PREFIX = "!";
            logger.info("program version: " + getVersion());
        } else if(getVersion().contains("BETA") || getVersion().contains("beta")) {
            appMode = APP_BETA;
            PREFIX = "#";
            logger.warn("beta program version: " + getVersion());
        } else if(getVersion().contains("ALPHA") || getVersion().contains("alpha")) {
            appMode = APP_ALPHA;
            PREFIX = "#";
            logger.error("alpha program version: " + getVersion());
        } else {
            logger.error("unknown program version, shutdown program");
            System.exit(-1);
        }
        String TOKEN = null;
        switch (appMode) {
            case APP_STABLE -> TOKEN = openFileData("TOKEN");
            case APP_BETA -> TOKEN = openFileData("BETA_TOKEN");
            case APP_ALPHA -> TOKEN = openFileData("ALPHA_TOKEN");
        }
        WebUtils.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) JDA/4.4.0_352");
        logger.info("Connecting to SQL Server/File");
        MySQLConnector mySqlConnector = new MySQLConnector();
        SQLITEConnector sqliteConnector = new SQLITEConnector("sqlite.db",mySqlConnector);
        SQLITEConnector wargamingConnector = new SQLITEConnector("wargaming.db",mySqlConnector);

        logger.info("Connect Success with SQL Server/File \n Starting Objects link to main");
        
        FilterSystem filterSystem = new FilterSystem(mySqlConnector);
        WargamingAPI wargamingAPI = new WargamingAPI(sqliteConnector);
        
        logger.info("Objects linked!, Loading Listeners");
        
        CommandManager commandManager = new CommandManager(mySqlConnector, sqliteConnector, filterSystem, wargamingAPI, wargamingConnector);
        Listener listener = new Listener(commandManager);
        filterListener filterListener = new filterListener(filterSystem);
        LogListener logListener = new LogListener(mySqlConnector);
        MuteListener muteListener = new MuteListener(mySqlConnector);
        onReadyListener onReadyListener = new onReadyListener(mySqlConnector, sqliteConnector, wargamingAPI, wargamingConnector);
        DirectMessageListener directMessageListener = new DirectMessageListener(mySqlConnector);

        EventListener eventListener = new EventListener(mySqlConnector);

        MessagePinListener messagePinListener = new MessagePinListener(sqliteConnector);
        giveRoleListener giveRoleListener = new giveRoleListener(sqliteConnector);
        logger.info("Listener Loaded, JDA start up, Connecting to discord.com");
        try {
            JDABuilder.createDefault(TOKEN)
                    .setAutoReconnect(true)
                    .addEventListeners(listener, giveRoleListener, filterListener, logListener,
                            muteListener, messagePinListener, onReadyListener, directMessageListener, eventListener)
                    .setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .build().awaitReady();
            logger.info("Connect success with discord.com");

            logger.info("Boot Complete");
        } catch (LoginException | InterruptedException e) {
            logger.error("의도치 않은 예외 발생" + e);
        }
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static void main(String[] args) throws SQLException, ClassNotFoundException, URISyntaxException {
        date = new Date();
        new App();
    }

    /**
     * return prefix
     * @return the prefix of this discord bot
     */
    public static String getPREFIX() {
        return PREFIX;
    }

    /**
     * return discord bot version
     * @return the version of discord bot
     */

    public static String getVersion() {
        return version;
    }

    /**
     * return discord bot build time
     * @return the build time of discord bot
     */

    public static String getBuild_time() {
        return build_time;
    }

    /**
     * return discord bot build os
     * @return the build os of discord bot
     */

    public static String getBuild_os() {
        return build_os;
    }

    /**
     * return discord bot build jdk
     * @return the build jdk of discord bot
     */

    public static String getBuild_jdk() {
        return build_jdk;
    }

    /**
     * return discord bot start-up time
     * @return the start-up time of discord bot
     */

    public static Date getDate() {
        return date;
    }

    /**
     * return moderator discord id list
     * @return the string array that moderator discord id
     */

    public static String[] getModerator() {
        return moderator;
    }

}
