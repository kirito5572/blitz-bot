package me.kirito5572.listener;

import me.kirito5572.App;
import me.kirito5572.objects.CommandManager;
import me.kirito5572.objects.MySQLConnector;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Objects;


public class Listener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(Listener.class);
    private final CommandManager manager;

    public Listener(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("로그인 성공:" + event.getJDA().getSelfUser());
        System.out.printf("로그인 성공: %#s%n", event.getJDA().getSelfUser());

    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().startsWith(App.getPREFIX() + "setup") && Objects.requireNonNull(event.getMember()).getId().equals("284508374924787713")) {
            try {
                manager.getCommands().forEach(iCommand -> event.getGuild().upsertCommand(iCommand.getInvoke()[0], iCommand.getSmallHelp()).queue());
            } catch (ErrorResponseException e) {
                event.getChannel().sendMessage(e.getMessage()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getChannel().sendMessage("에러 발생! 명령어 등록 또는 갱신 실패").queue();
            }
            event.getChannel().sendMessage("명령어 등록 또는 갱신 완료").queue();
        } else if (event.getMessage().getContentRaw().startsWith(App.getPREFIX() + "update") && Objects.requireNonNull(event.getMember()).getId().equals("284508374924787713")){
            try {
                event.getGuild().upsertCommand("종료","(관리자 전용) 신고/건의사항/이의제기등의 상담 채팅을 종료하는 명령어입니다.").queue();
                event.getGuild().upsertCommand("청소", "(관리자 전용) 메세지를 입력한 숫자만큼 대량 삭제 합니다.").queue();
                event.getGuild().upsertCommand("eval","(개발자 전용) 살충제! 살충제!").queue();
                event.getGuild().upsertCommand("봇정보", "봇에 대한 정보를 표시합니다.").queue();
                event.getGuild().upsertCommand("로그","(관리자 전용) 신고/건의사항/이의제기등의 상담 채팅 로그를 조회하는 명령어입니다.").queue();
                event.getGuild().upsertCommand("신고차단","(관리자 전용) 신고/건의사항/이의제기등의 상담 채팅을 못하게 유저 차단하는 명령어입니다.").queue();
                event.getGuild().upsertCommand("신고해제","(관리자 전용) 신고/건의사항/이의제기등의 상담 채팅을 다시 할수 있도록 차단을 해제하는 명령어입니다.").queue();
                event.getGuild().upsertCommand("SQLITE", "(개발자 전용) SQL SQL").queue();
                event.getGuild().upsertCommand("유저정보","(관리자 전용) 서버에 있는 유저의 정보를 불러옵니다.").queue();
            } catch (ErrorResponseException e) {
                event.getChannel().sendMessage(e.getMessage()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getChannel().sendMessage("에러 발생! 명령어 등록 또는 갱신 실패").queue();
            }
        }


        if (!event.getAuthor().isBot()) {
            System.out.println("여기 실행됨");
            if (!event.getMessage().isWebhookMessage()) {
                System.out.println("여기도 실행됨");
                System.out.println(event.getMessage().getContentRaw());
                if(event.getMessage().getContentRaw().startsWith(App.getPREFIX())) {
                    this.manager.handleCommand(event);
                    System.out.println("여기까지 실행됨");
                }
            }
        }
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        manager.handleCommand(event);
    }

    @Override
    public void onResumed(@NotNull ResumedEvent event) {
        try {
            MySQLConnector.reConnection();
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
        }
    }
}
