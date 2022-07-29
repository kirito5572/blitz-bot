package bot.listener;

import bot.objects.CommandManager;
import bot.objects.SQLConnector;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class Listener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(Listener.class);
    private final CommandManager manager;
    private final SQLConnector sqlConnector;

    public Listener(CommandManager manager, SQLConnector sqlConnector) {
        this.manager = manager;
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("로그인 성공:" + event.getJDA().getSelfUser());
        System.out.printf("로그인 성공: %#s%n", event.getJDA().getSelfUser());
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().startsWith("!setup") && Objects.requireNonNull(event.getMember()).getId().equals("284508374924787713")) {
            try {
                event.getGuild().upsertCommand("핑", "BlitzBot의 핑을 조회합니다. 봇에 연결된 SQL 서버 또한 함께 조회합니다.").queue();
                event.getGuild().upsertCommand("단어삭제", "(관리자 전용) 필터링 단어 목록에서 단어를 삭제합니다.").queue();
                event.getGuild().upsertCommand("단어추가", "(관리자 전용) 필터링 단어 목록에서 단어를 추가합니다.").queue();
                event.getGuild().upsertCommand("명령어", "모르는 명령어의 사용법을 조회합니다. /명령어 (조회가 필요한 명령어)").queue();
                event.getGuild().upsertCommand("핀", "(관리자 전용)메세지를 고정합니다.").queue();
                event.getGuild().upsertCommand("제재", "(관리자 전용) 서버에서 사용자를 제재합니다.").queue();
                event.getGuild().upsertCommand("제재해제", "(관리자 전용) 서버에서 제재한 사용자에 대한 제재를 해제합니다.").queue();
                event.getGuild().upsertCommand("eval","(개발자 전용) 살충제! 살충제!").queue();
            } catch (ErrorResponseException e) {
                event.getChannel().sendMessage(e.getMessage()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getChannel().sendMessage("에러 발생! 명령어 등록 또는 갱신 실패").queue();
            }
            event.getChannel().sendMessage("명령어 등록 또는 갱신 완료").queue();
        }
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        manager.handleCommand(event);
    }

    @Override
    public void onResumed(@NotNull ResumedEvent event) {
        sqlConnector.reConnection();
    }
}
