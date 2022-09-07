package me.kirito5572.commands;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.App;
import me.kirito5572.objects.CommandManager;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
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

        Member member = event.getMember();

        if(command.isAdminOnly()) {
            if(Objects.requireNonNull(event.getGuild()).getId().equals("826704284003205160")) {
                if(member.getRoles().contains(event.getGuild().getRoleById("827010848442548254")) ||                  //R:모더레이터
                        member.getRoles().contains(event.getGuild().getRoleById("827009999145926657")) ||             //R:Administrator
                        member.getRoles().contains(event.getGuild().getRoleById("827011445187280906"))) {             //R:컨트리뷰터
                    event.getChannel().sendMessage( " `"+joined + "`는 존재하지 않는 명령어 입니다.\n" +
                                "`" + App.getPREFIX() + getInvoke() + "` 를 사용해 명령어 리스트를 확인하세요.").queue();
                    return;
                }

            } else {
                event.getChannel().sendMessage( " `"+joined + "`는 존재하지 않는 명령어 입니다.\n" +
                        "`" + App.getPREFIX() + getInvoke() + "` 를 사용해 명령어 리스트를 확인하세요.").queue();
                return;
            }
        }

        if(command.isOwnerOnly()) {
            if(!event.getAuthor().getId().equals("284508374924787713")) {
                event.getChannel().sendMessage( " `"+joined + "`는 존재하지 않는 명령어 입니다.\n" +
                        "`" + App.getPREFIX() + getInvoke() + "` 를 사용해 명령어 리스트를 확인하세요.").queue();
                return;
            }
        }

        String message = "`" + command.getInvoke() + "` 에 대한 설명\n" + command.getHelp();

        event.getChannel().sendMessage(message).queue();
    }

    private void generateAndSendEmbed(@NotNull EventPackage event) {
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed().setTitle("명령어 리스트:");
        EmbedBuilder builder1 = EmbedUtils.getDefaultEmbed().setTitle("관리 명령어 리스트:");
        EmbedBuilder builder2 = EmbedUtils.getDefaultEmbed().setTitle("전용 명령어 리스트:");

        builder.appendDescription(App.getPREFIX() + getInvoke() + " <명령어>를 입력하면 명령어별 상세 정보를 볼 수 있습니다.");
        Commands.forEach(iCommand -> {
            if(iCommand.isAdminOnly()) {
                builder1.addField(iCommand.getInvoke(), iCommand.getSmallHelp(), false);
            } else if(iCommand.isOwnerOnly()) {
                builder2.addField(iCommand.getInvoke(), iCommand.getSmallHelp(), false);
            } else {
                builder.addField(iCommand.getInvoke(), iCommand.getSmallHelp(), false);
            }
        });
        Member member = event.getMember();
        assert member != null;
        event.getChannel().sendMessageEmbeds(builder.build()).queue();

        if(member.getRoles().contains(event.getGuild().getRoleById("827010848442548254")) ||                  //R:모더레이터
                member.getRoles().contains(event.getGuild().getRoleById("827009999145926657")) ||             //R:Administrator
                member.getRoles().contains(event.getGuild().getRoleById("827011445187280906"))) {             //R:컨트리뷰터
            event.getChannel().sendMessageEmbeds(builder1.build()).queue();
        }
        if(member.getId().equals("284508374924787713")) {
            event.getChannel().sendMessageEmbeds(builder2.build()).queue();
        }
    }

    @NotNull
    @Override
    public String getHelp() {
        return "모르는 명령어의 사용법을 조회합니다. /명령어 (조회가 필요한 명령어)";
    }

    @NotNull
    @Override
    public String getInvoke() {
        return "명령어";
    }

    @NotNull
    @Override
    public String getSmallHelp() {
        return "모르는 명령어의 사용법을 조회합니다. /명령어 (조회가 필요한 명령어)";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isOwnerOnly() {
        return false;
    }
}
