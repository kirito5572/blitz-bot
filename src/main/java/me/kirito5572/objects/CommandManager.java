package me.kirito5572.objects;

import me.kirito5572.App;
import me.kirito5572.commands.BotInfoCommand;
import me.kirito5572.commands.HelpCommand;
import me.kirito5572.commands.PingCommand;
import me.kirito5572.commands.admin.EvalCommand;
import me.kirito5572.commands.admin.SQLiteQueryCommand;
import me.kirito5572.commands.blitz.AccountLinkCommand;
import me.kirito5572.commands.blitz.SearchClanInfoCommand;
import me.kirito5572.commands.blitz.SearchPlayerOverAllStatCommand;
import me.kirito5572.commands.moderator.*;
import me.kirito5572.commands.music.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentLayout;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import org.apache.commons.collections4.Bag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/** @noinspection ALL*/
public class CommandManager {
    private final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandManager(MySQLConnector mySqlConnector, SQLITEConnector sqliteConnector, FilterSystem filterSystem, WargamingAPI wargamingAPI, GoogleAPI googleAPI) {
        addCommand(new HelpCommand(this));
        addCommand(new PingCommand(mySqlConnector, sqliteConnector));
        addCommand(new FilterWordAddCommand(filterSystem));
        addCommand(new FilterWordRemoveCommand(filterSystem));
        addCommand(new MuteCommand(mySqlConnector));
        addCommand(new UnMuteCommand(mySqlConnector));
        addCommand(new MessagePinCommand(sqliteConnector));
        addCommand(new EvalCommand());
        addCommand(new MessageBulkDeleteCommand());
        addCommand(new ComplainEndCommand(mySqlConnector));
        addCommand(new BotInfoCommand());
        addCommand(new ComplainMessageLogCommand(mySqlConnector));
        addCommand(new ComplainBanCommand(mySqlConnector));
        addCommand(new ComplainUnBanCommand(mySqlConnector));
        addCommand(new SQLiteQueryCommand(sqliteConnector));
        addCommand(new UserInfoCommand());
        addCommand(new BanCommand());
        addCommand(new UnBanCommand());
        addCommand(new KickCommand());

        addCommand(new SearchPlayerOverAllStatCommand(sqliteConnector, wargamingAPI));
        addCommand(new SearchClanInfoCommand(wargamingAPI));
        addCommand(new AccountLinkCommand(wargamingAPI));

        //music commands
        addCommand(new JoinCommand());
        addCommand(new leaveCommand());
        addCommand(new NowPlayingCommand());
        addCommand(new PauseCommand());
        addCommand(new PlayCommand());
        addCommand(new QueueCommand());
        addCommand(new QueueDetectCommand());
        addCommand(new QueueMixCommand());
        addCommand(new SearchCommand(googleAPI));
        addCommand(new SkipCommand());
        addCommand(new StopClearCommand());
        addCommand(new StopCommand());
        addCommand(new VolumeCommand());

    }

    /**
     * addCommand for prefix command system(prefix = !)
     * @param command command data, more: {@link me.kirito5572.objects.ICommand}
     */

    private void addCommand(@NotNull ICommand command) {
        for(String invoke : command.getInvoke()) {
            if (!commands.containsKey(invoke)) {
                commands.put(invoke, command);
            }
            sleep();
        }
    }

    /**
     * delay 10 mill
     */
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

    /**
     * get all Commands {@link me.kirito5572.objects.ICommand}
     * @return Commands Collection return
     */
    @NotNull
    public Collection<ICommand> getCommands() {
        return commands.values();
    }

    /**
     * get single Command's {@link me.kirito5572.objects.ICommand}
     * @param name command invoke
     * @return If the command exists, return it's {@link me.kirito5572.objects.ICommand}
     */
    public ICommand getCommand(@NotNull String name) {
        return commands.get(name);
    }

    /**
     * When input a command, the part to be linked to the command
     * use with prefix command system({@link net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent})
     * @param event {@link net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent}
     */
    public void handleCommand(@NotNull GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        String[] split = event.getMessage().getContentRaw().replaceFirst("(?i)" + Pattern.quote(App.getPREFIX()), "").split("\\s+");
        String invoke = split[0].toLowerCase();
        EventPackage eventPackage = new EventPackage(event.getChannel(), event.getMember(), event.getMessage());
        if (this.commands.containsKey(invoke)) {
            List<String> args = Arrays.asList(split).subList(1, split.length);
            channel.sendTyping().queue();
            this.commands.get(invoke).handle(args, eventPackage);
        }

    }

    /**
     * When input a command, the part to be linked to the command
     * use with slash command system({@link net.dv8tion.jda.api.events.interaction.SlashCommandEvent})
     * @param event {@link net.dv8tion.jda.api.events.interaction.SlashCommandEvent}
     */
    public void handleCommand(@NotNull final SlashCommandEvent event) {
        final TextChannel channel = event.getTextChannel();
        final String[] split = event.getCommandString().replaceFirst(
                "(?i)" + Pattern.quote("/"), "").split("\\s+");
        final String invoke = split[0].toLowerCase();

        if(commands.containsKey(invoke)) {
            final List<String> args = Arrays.asList(split).subList(1, split.length);
            channel.sendTyping().queue();
            Message message = new Message() {
                @Nullable
                public MessageReference getMessageReference() {
                    return null;
                }

                @NotNull
                public List<User> getMentionedUsers() {
                    return null;
                }

                @NotNull
                public Bag<User> getMentionedUsersBag() {
                    return null;
                }

                @NotNull
                public List<TextChannel> getMentionedChannels() {
                    return null;
                }

                @NotNull
                public Bag<TextChannel> getMentionedChannelsBag() {
                    return null;
                }

                @NotNull
                public List<Role> getMentionedRoles() {
                    return null;
                }

                @NotNull
                public Bag<Role> getMentionedRolesBag() {
                    return null;
                }

                @NotNull
                public List<Member> getMentionedMembers(@NotNull Guild guild) {
                    return null;
                }

                @NotNull
                public List<Member> getMentionedMembers() {
                    return null;
                }

                @NotNull
                public List<IMentionable> getMentions(@NotNull MentionType @NotNull ... types) {
                    return null;
                }

                public boolean isMentioned(@NotNull IMentionable mentionable, @NotNull MentionType @NotNull ... types) {
                    return false;
                }

                public boolean mentionsEveryone() {
                    return false;
                }

                public boolean isEdited() {
                    return false;
                }

                @Nullable
                public OffsetDateTime getTimeEdited() {
                    return null;
                }

                @NotNull
                public User getAuthor() {
                    return event.getUser();
                }

                @Nullable
                public Member getMember() {
                    return event.getMember();
                }

                @NotNull
                public String getJumpUrl() {
                    return null;
                }

                @NotNull
                public String getContentDisplay() {
                    return event.getCommandString();
                }

                @NotNull
                public String getContentRaw() {
                    return event.getCommandString();
                }

                @NotNull
                public String getContentStripped() {
                    return event.getCommandString();
                }

                @NotNull
                public List<String> getInvites() {
                    return null;
                }

                @Nullable
                public String getNonce() {
                    return null;
                }

                public boolean isFromType(@NotNull ChannelType type) {
                    return type == ChannelType.TEXT;
                }

                @NotNull
                public ChannelType getChannelType() {
                    return event.getChannelType();
                }

                public boolean isWebhookMessage() {
                    return false;
                }

                @NotNull
                public MessageChannel getChannel() {
                    return event.getChannel();
                }

                @NotNull
                public PrivateChannel getPrivateChannel() {
                    return null;
                }

                @NotNull
                public TextChannel getTextChannel() {
                    return event.getTextChannel();
                }

                @Nullable
                public Category getCategory() {
                    return null;
                }

                @NotNull
                public Guild getGuild() {
                    return Objects.requireNonNull(event.getGuild());
                }

                @NotNull
                public List<Attachment> getAttachments() {
                    return null;
                }

                @NotNull
                public List<MessageEmbed> getEmbeds() {
                    return null;
                }

                @NotNull
                public List<ActionRow> getActionRows() {
                    return null;
                }

                @NotNull
                public List<Emote> getEmotes() {
                    return null;
                }

                @NotNull
                public Bag<Emote> getEmotesBag() {
                    return null;
                }

                @NotNull
                public List<MessageReaction> getReactions() {
                    return null;
                }

                @NotNull
                public List<MessageSticker> getStickers() {
                    return null;
                }

                public boolean isTTS() {
                    return false;
                }

                @Nullable
                public MessageActivity getActivity() {
                    return null;
                }

                @NotNull
                public MessageAction editMessage(@NotNull CharSequence newContent) {
                    return null;
                }

                @NotNull
                public MessageAction editMessageEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
                    return null;
                }

                @NotNull
                public MessageAction editMessageComponents(@NotNull Collection<? extends ComponentLayout> components) {
                    return null;
                }

                @NotNull
                public MessageAction editMessageFormat(@NotNull String format, @NotNull Object @NotNull ... args) {
                    return null;
                }

                @NotNull
                public MessageAction editMessage(@NotNull Message newContent) {
                    return null;
                }

                @NotNull
                public AuditableRestAction<Void> delete() {
                    return null;
                }

                @NotNull
                public JDA getJDA() {
                    return event.getJDA();
                }

                public boolean isPinned() {
                    return false;
                }

                @NotNull
                public RestAction<Void> pin() {
                    return null;
                }

                @NotNull
                public RestAction<Void> unpin() {
                    return null;
                }

                @NotNull
                public RestAction<Void> addReaction(@NotNull Emote emote) {
                    return null;
                }

                @NotNull
                public RestAction<Void> addReaction(@NotNull String unicode) {
                    return null;
                }

                @NotNull
                public RestAction<Void> clearReactions() {
                    return null;
                }

                @NotNull
                public RestAction<Void> clearReactions(@NotNull String unicode) {
                    return null;
                }

                @NotNull
                public RestAction<Void> clearReactions(@NotNull Emote emote) {
                    return null;
                }

                @NotNull
                public RestAction<Void> removeReaction(@NotNull Emote emote) {
                    return null;
                }

                @NotNull
                public RestAction<Void> removeReaction(@NotNull Emote emote, @NotNull User user) {
                    return null;
                }

                @NotNull
                public RestAction<Void> removeReaction(@NotNull String unicode) {
                    return null;
                }

                @NotNull
                public RestAction<Void> removeReaction(@NotNull String unicode, @NotNull User user) {
                    return null;
                }

                @NotNull
                public ReactionPaginationAction retrieveReactionUsers(@NotNull Emote emote) {
                    return null;
                }

                @NotNull
                public ReactionPaginationAction retrieveReactionUsers(@NotNull String unicode) {
                    return null;
                }

                @Nullable
                public MessageReaction.ReactionEmote getReactionByUnicode(@NotNull String unicode) {
                    return null;
                }

                @Nullable
                public MessageReaction.ReactionEmote getReactionById(@NotNull String id) {
                    return null;
                }

                @Nullable
                public MessageReaction.ReactionEmote getReactionById(long id) {
                    return null;
                }

                @NotNull
                public AuditableRestAction<Void> suppressEmbeds(boolean suppressed) {
                    return null;
                }

                @NotNull
                public RestAction<Message> crosspost() {
                    return null;
                }

                public boolean isSuppressedEmbeds() {
                    return false;
                }

                @NotNull
                public EnumSet<MessageFlag> getFlags() {
                    return null;
                }

                public long getFlagsRaw() {
                    return 0L;
                }

                public boolean isEphemeral() {
                    return false;
                }

                @NotNull
                public MessageType getType() {
                    return null;
                }

                @Nullable
                public Interaction getInteraction() {
                    return null;
                }

                public void formatTo(Formatter formatter, int flags, int width, int precision) {
                }

                public long getIdLong() {
                    return 0L;
                }
            };
            EventPackage eventPackage = new EventPackage(event.getTextChannel(), event.getMember(), message);
            event.reply("").queue(interactionHook -> interactionHook.deleteOriginal().queueAfter(3L, TimeUnit.SECONDS));
            this.commands.get(invoke).handle(args, eventPackage);
        }
    }
}
