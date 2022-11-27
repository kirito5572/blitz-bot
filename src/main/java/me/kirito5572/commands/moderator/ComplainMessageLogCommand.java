package me.kirito5572.commands.moderator;

import me.kirito5572.App;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.MySQLConnector;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ComplainMessageLogCommand implements ICommand {
    private final MySQLConnector mySqlConnector;
    private final Logger logger = LoggerFactory.getLogger(ComplainMessageLogCommand.class);


    public ComplainMessageLogCommand(MySQLConnector mySqlConnector) {
        this.mySqlConnector = mySqlConnector;
    }

    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
        StringBuilder messageBuilder = new StringBuilder();
        int ComplainInt;
        int dataPage = 0;
        if(args.size() == 0) {
            event.textChannel().sendMessage("명령어 뒤에 조회할 번호를 입력해주세요").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
            return;
        } else {
            boolean isDigit = args.get(0).chars().allMatch( Character::isDigit );
            if(isDigit) {
                ComplainInt = Integer.parseInt(args.get(0));
            } else {
                event.textChannel().sendMessage("""
                            명령어 뒤에 값은 숫자만 입력해주세요.
                            예시: !로그 1""").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
                return;
            }
        }
        if (args.size() == 2){
            boolean isDigit = args.get(1).chars().allMatch( Character::isDigit );
            if(isDigit) {
                dataPage = (Integer.parseInt(args.get(1)) - 1);
            } else {
                event.textChannel().sendMessage("""
                            명령어 뒤에 값은 숫자만 입력해주세요.
                            예시: !로그 1 2""").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
                return;
            }
            if(dataPage <= 0) {
                event.textChannel().sendMessage("""
                             2번째 인수의 값이 1보다 작은 값이 들어올수 없습니다.
                             최소 1 이상 입력해주세요""").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
                return;
            }
        }
        try (ResultSet resultSet = mySqlConnector.Select_Query("SELECT * FROM blitz_bot.ComplainMessageLog WHERE ComplainInt = ?",
                new int[]{mySqlConnector.INT},
                new String[]{String.valueOf(ComplainInt)})) {
            while(resultSet.next()) {
                String userId = resultSet.getString("userId");
                boolean adminChat = false;
                for(String Id : App.getModerator()) {
                    if(Id.contains(userId)) {
                        adminChat = true;
                        break;
                    }
                }
                if(adminChat) {
                    Member member = event.getGuild().getMemberById(userId);
                    if(member == null) {
                        messageBuilder.append("<@").append(userId).append(">").append("\n");
                    } else {
                        String a = member.getNickname();
                        String name = a == null ? member.getUser().getName() : a;
                        messageBuilder.append(name).append("\n");
                    }
                } else {
                    messageBuilder.append("<@").append(userId).append(">").append("\n");
                }
                messageBuilder.append(resultSet.getString("messageRaw")).append("\n");

            }
            String sendData = messageBuilder.toString();
            if(sendData.length() < (dataPage * 2000)) {
                event.textChannel().sendMessage("""
                            해당 페이지만큼 글자수가 많지 않습니다.
                            예시: !로그 1""").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
                return;
            }
            int endLength = (((dataPage + 1) * 2000));
            if(endLength > sendData.length()) {
                endLength = sendData.length();
            }
            event.getChannel().sendMessage(
                    sendData.substring(dataPage * 2000, endLength)).queue();
            if(endLength < sendData.length()) {
                event.getChannel().sendMessage(" 로그 데이터 량이 2000자를 초과하여 다음페이지가 존재합니다.\n" +
                        "현재 페이지: " + (dataPage + 1) + " / 마지막 페이지: " + (int) Math.ceil(sendData.length() / 2000.0)).queue();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull String getHelp() {
        return "null";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"로그", "log"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "(관리자 전용) 신고/건의사항/이의제기등의 상담 채팅 로그를 조회하는 명령어입니다.";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean isOwnerOnly() {
        return false;
    }

    @Override
    public boolean isMusicOnly() {
        return false;
    }
}
