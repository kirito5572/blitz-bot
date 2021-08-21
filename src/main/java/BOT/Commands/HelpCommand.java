package BOT.Commands;

import BOT.App;
import BOT.Objects.CommandManager;
import BOT.Objects.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HelpCommand implements ICommand {

    @NotNull
    private final CommandManager manager;
    @NotNull
    private final Collection<ICommand> Commands;

    public HelpCommand(@NotNull CommandManager manager) {
        this.manager = manager;
        Commands = manager.getCommands();
    }

    @Override
    public void handle(@NotNull List<String> args, @NotNull GuildMessageReceivedEvent event) {
        String joined = String.join(" ", args);

        if(joined.equals("")) {
            generateAndSendEmbed(event);
            return;
        }

        ICommand command = manager.getCommand(joined);

        if(command == null) {
            event.getChannel().sendMessage( " `"+joined + "`는 존재하지 않는 명령어 입니다.\n" +
                    "`" + App.getPREFIX() + getInvoke() + "` 를 사용해 명령어 리스트를 확인하세요.").queue();
            return;
        }


        if(command.isAdminOnly()) {
            if(event.getGuild().getId().equals("826704284003205160")) {
                if (!Objects.requireNonNull(event.getMember()).getRoles().contains(event.getGuild().getRoleById("827009999145926657"))) {
                    if (!event.getMember().getRoles().contains(event.getGuild().getRoleById("82701084844254825"))) {
                        event.getChannel().sendMessage( " `"+joined + "`는 존재하지 않는 명령어 입니다.\n" +
                                "`" + App.getPREFIX() + getInvoke() + "` 를 사용해 명령어 리스트를 확인하세요.").queue();
                        return;
                    }
                }
            } else {
                event.getChannel().sendMessage( " `"+joined + "`는 존재하지 않는 명령어 입니다.\n" +
                        "`" + App.getPREFIX() + getInvoke() + "` 를 사용해 명령어 리스트를 확인하세요.").queue();
                return;
            }
        }
        String message = "`" + command.getInvoke() + "` 에 대한 설명\n" + command.getHelp();

        event.getChannel().sendMessage(message).queue();
    }

    private void generateAndSendEmbed(@NotNull GuildMessageReceivedEvent event) {
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed().setTitle("명령어 리스트:");
        EmbedBuilder builder1 = EmbedUtils.getDefaultEmbed().setTitle("관리 명령어 리스트:");

        builder.appendDescription(App.getPREFIX() + getInvoke() + " <명령어>를 입력하면 명령어별 상세 정보를 볼 수 있습니다.");
        Commands.forEach(iCommand -> {
            if(!iCommand.isAdminOnly()) {
                builder.addField(iCommand.getInvoke(), iCommand.getSmallHelp(), false);
            } else {
                builder1.addField(iCommand.getInvoke(), iCommand.getSmallHelp(), false);
            }
        });
        Member member = event.getMember();
        assert member != null;
        if (!member.getRoles().contains(event.getGuild().getRoleById("827009999145926657"))) {
            if (!member.getRoles().contains(event.getGuild().getRoleById("827010848442548254"))) {
                event.getChannel().sendMessageEmbeds(builder.build()).queue();
                return;
            }
        }
        event.getChannel().sendMessageEmbeds(builder.build()).queue();
        event.getChannel().sendMessageEmbeds(builder1.build()).queue();
    }

    @NotNull
    @Override
    public String getHelp() {
        return "모르는 명령어는 어디서? 여기서.\n" +
                "명령어: `" + App.getPREFIX() + getInvoke() + " [command]`";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "명령어";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "other";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
