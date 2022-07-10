package bot.listener;

import bot.objects.SQLConnector;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class giveRoleListener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(giveRoleListener.class);
    private final SQLConnector sqlConnector;
    private static final String Chatting = "830514311939751967";

    public giveRoleListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        Guild guild = event.getGuild();
        if (guild.getId().equals("826704284003205160")) {
            Role role = guild.getRoleById("827207197183180821");
            Member member = event.getMember();
            if (event.getMessageId().equals(Chatting)) {
                assert role != null;
                guild.addRoleToMember(member, role).complete();
                int result = sqlConnector.Insert_Query("INSERT INTO blitz_bot.JoinData_Table (userId, approveTime, rejectTime) VALUES(?, ? ,?);",
                        new int[] {sqlConnector.STRING, sqlConnector.STRING, sqlConnector.STRING},
                        new String[] {member.getId(), String.valueOf(System.currentTimeMillis() / 1000), "0"});
                if (result != 0) {
                    logger.warn("sql insert error #1");
                }
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        Guild guild = event.getGuild();
        if (guild.getId().equals("826704284003205160")) {
            Role role = guild.getRoleById("827207197183180821");
            Member member = event.getMember();
            if (event.getMessageId().equals(Chatting)) {
                assert role != null;
                assert member != null;
                guild.removeRoleFromMember(member, role).complete();
                int result = sqlConnector.Insert_Query("UPDATE blitz_bot.JoinData_Table SET rejectTime =? WHERE userId = ? AND rejectTime = ?",
                        new int[] {sqlConnector.STRING, sqlConnector.STRING, sqlConnector.STRING},
                        new String[] {String.valueOf(System.currentTimeMillis() / 1000), member.getId(), "0"});
                if (result != 0) {
                    logger.warn("sql update error #1");
                }
            }
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        if (guild.getId().equals("826704284003205160")) {
            Member member = event.getMember();
            assert member != null;
            int result = sqlConnector.Insert_Query("UPDATE blitz_bot.JoinData_Table SET rejectTime =? WHERE userId = ? AND rejectTime = ?",
                    new int[]{sqlConnector.STRING, sqlConnector.STRING, sqlConnector.STRING},
                    new String[] {String.valueOf(System.currentTimeMillis() / 1000),  member.getId(), "1"});
            if (result != 0) {
                logger.warn("sql update error #2");
            }

        }
    }
}
