package me.kirito5572.listener;

import me.kirito5572.App;
import me.kirito5572.objects.SQLConnector;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class filterListener extends ListenerAdapter {
    private static String[] filter_data;
    private static String[][] white_list_data;
    private final SQLConnector sqlConnector;

    public filterListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if (getFilterDataFromDB(sqlConnector)) return;
        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.WhiteListWord;", new int[0], new String[0])) {
            if (resultSet.last()) {
                return;
            }
            String[][] a = new String[resultSet.getRow()][2];
            resultSet.first();
            int i = 0;
            while (resultSet.next()) {
                a[i][0] = resultSet.getString("FilterWord");
                a[i][1] = resultSet.getString("Word");
            }
            filterListener.setWhite_list_data(a);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        assert member != null;
        filter_data(member, event.getMessage());
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        Member member = event.getMember();
        assert member != null;
        filter_data(member, event.getMessage());
    }

    /**
     * Get prohibited word from DataBase
     *
     * @param sqlConnector {@link me.kirito5572.objects.SQLConnector}
     *
     * @return if true, get data success
     *         if false, get data fail
     */

    public static boolean getFilterDataFromDB(@NotNull SQLConnector sqlConnector) {
        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.FilterWord;", new int[0], new String[0])) {
            if (resultSet.last()) {
                return true;
            }
            String[] a = new String[resultSet.getRow()];
            resultSet.first();
            int i = 0;
            while (resultSet.next()) {
                a[i] = resultSet.getString("Word");
            }
            filterListener.setFilter_data(a);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * Thread execution to check prohibited words
     * @param member the member who need check he/her message
     * @param message the message which is checked
     *
     */

    private void filter_data(@NotNull Member member,@NotNull Message message) {
        Guild guild = member.getGuild();
        if (guild.getId().equals("826704284003205160")) {
            filter_data = App.getFilterList();
            try {
                String message_raw = message.getContentRaw();
                if (message_raw.length() > 1) {
                    new Thread(() -> filter(Objects.requireNonNull(member), message)).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The part that checks prohibited words by executing as a thread
     * call from filter_data()
     *
     * @param member the member who need check he/her message
     * @param message the message which is checked
     *
     */

    private void filter(@NotNull Member member, @NotNull Message message) {
        String rawMessage = message.getContentRaw();
        if (member.getPermissions().contains(Permission.ADMINISTRATOR) |
                member.getPermissions().contains(Permission.MESSAGE_MANAGE) |
                member.getPermissions().contains(Permission.KICK_MEMBERS) |
                member.getPermissions().contains(Permission.BAN_MEMBERS) |
                member.getPermissions().contains(Permission.MANAGE_PERMISSIONS) |
                member.getPermissions().contains(Permission.MANAGE_CHANNEL) |
                member.getPermissions().contains(Permission.MANAGE_EMOTES) |
                member.getPermissions().contains(Permission.MANAGE_SERVER) |
                member.getPermissions().contains(Permission.MANAGE_WEBHOOKS)) {
            return;
        }
        if (member.getUser().isBot()) {
            return;
        }
        rawMessage = rawMessage.trim().replaceAll(" +", " ");
        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        rawMessage = rawMessage.replaceAll(match, "");
        boolean filter = false;
        boolean filter_continue = false;
        for (String data : filter_data) {
            if (rawMessage.contains(data)) {
                for(String[] a : white_list_data) {
                    if(data.equals(a[0])) {
                        if(rawMessage.contains(a[1])) {
                            filter_continue = true;
                            break;
                        }
                    }
                }
                if(filter_continue) {
                    filter_continue = false;
                    continue;
                }
                filter = true;
                break;
            }
        }
        if(filter) {
            message.getTextChannel().deleteMessageById(message.getId()).complete();
            message.getTextChannel().sendMessage(member.getAsMention() + ", 금지어 사용에 주의하여주십시오.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
        }
    }

    /**
     * Setting prohibited word list
     *
     * @param filter_data prohibited data list
     *
     */

    public static void setFilter_data(@NotNull String[] filter_data) {
        filterListener.filter_data = filter_data;
    }

    /**
     * Setting non-prohibited word list
     *
     * @param white_list_data non-prohibited data list
     *
     */

    public static void setWhite_list_data(@NotNull String[][] white_list_data) {
        filterListener.white_list_data = white_list_data;
    }
}
