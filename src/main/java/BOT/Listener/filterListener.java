package BOT.Listener;

import BOT.App;
import BOT.Objects.SQLConnector;
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
    private final SQLConnector sqlConnector;

    public filterListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.FilterWord;", new int[0], new String[0]);
            if(resultSet.last()) {
                return;
            }
            String[] a = new String[resultSet.getRow()];
            resultSet.first();
            int i = 0;
            while(resultSet.next()) {
                a[i] = resultSet.getString("Word");
            }
            filterListener.setFilter_data(a);
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

    private void filter_data(Member member, Message message) {
        Guild guild = member.getGuild();
        if(guild.getId().equals("826704284003205160")) {
            filter_data =  App.getFilterList();
            try {
                String message_raw = message.getContentRaw();
                if (message_raw.length() > 1) {
                    filter(Objects.requireNonNull(member), message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void filter(@NotNull Member member, @NotNull Message message) {
        String rawMessage = message.getContentRaw();
        if(member.getPermissions().contains(Permission.ADMINISTRATOR) |
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
        if(member.getUser().isBot()) {
            return;
        }
        rawMessage = rawMessage.trim().replaceAll(" +", " ");
        for(String data : filter_data) {
            if(rawMessage.contains(data)) {
                message.getTextChannel().deleteMessageById(message.getId()).complete();
                message.getTextChannel().sendMessage(member.getAsMention() + ", 금지어 사용에 주의하여주십시오.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            }
        }
    }

    public static void setFilter_data(String[] filter_data) {
        filterListener.filter_data = filter_data;
    }
}
