package me.kirito5572.listener;

import me.kirito5572.objects.FilterSystem;
import me.kirito5572.objects.MySQLConnector;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class filterListener extends ListenerAdapter {
    private final Logger logger = LoggerFactory.getLogger(filterListener.class);
    private final MySQLConnector mySqlConnector;
    private final FilterSystem filterSystem;

    public filterListener(MySQLConnector mySqlConnector, FilterSystem filterSystem) {
        this.mySqlConnector = mySqlConnector;
        this.filterSystem = filterSystem;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        filterSystem.filterRefresh();
        filterSystem.whiteFilterRefresh();
        logger.info("필터 ONLINE");
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
     *
     * Thread execution to check prohibited words
     * @param member the member who need check he/her message
     * @param message the message which is checked
     *
     */

    private void filter_data(@NotNull Member member,@NotNull Message message) {
        Guild guild = member.getGuild();
        if (guild.getId().equals("826704284003205160")) {
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
        if (member.getUser().isBot()) {
            return;
        }
        rawMessage = rawMessage.trim().replaceAll(" +", " ");
        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        rawMessage = rawMessage.replaceAll(match, "");
        boolean filter = false;
        boolean filter_continue = false;
        for (String data : filterSystem.getFilterList()) {
            if (rawMessage.contains(data)) {
                for(String[] a : filterSystem.getWhiteFilterList()) {
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
            if (member.getPermissions().contains(Permission.ADMINISTRATOR) |
                    member.getPermissions().contains(Permission.MESSAGE_MANAGE) |
                    member.getPermissions().contains(Permission.KICK_MEMBERS) |
                    member.getPermissions().contains(Permission.BAN_MEMBERS) |
                    member.getPermissions().contains(Permission.MANAGE_PERMISSIONS) |
                    member.getPermissions().contains(Permission.MANAGE_CHANNEL) |
                    member.getPermissions().contains(Permission.MANAGE_EMOTES) |
                    member.getPermissions().contains(Permission.MANAGE_SERVER) |
                    member.getPermissions().contains(Permission.MANAGE_WEBHOOKS)) {
                logger.info("특정 등급 이상 권한 부여자가 필터링에 걸리는 단어를 사용하였으나 통과되었습니다.");
                return;
            }
            message.getTextChannel().deleteMessageById(message.getId()).complete();
            message.getTextChannel().sendMessage(member.getAsMention() + ", 금지어 사용에 주의하여주십시오.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
        }
    }
}
