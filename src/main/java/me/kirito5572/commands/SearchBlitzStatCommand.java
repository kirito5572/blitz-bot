package me.kirito5572.commands;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.SQLITEConnector;
import me.kirito5572.objects.WargamingAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchBlitzStatCommand implements ICommand{
    private final Logger logger = LoggerFactory.getLogger(SearchBlitzStatCommand.class);
    private final SQLITEConnector wargamingConnector;
    private final WargamingAPI wargamingAPI;

    public SearchBlitzStatCommand(SQLITEConnector wargamingConnector, WargamingAPI wargamingAPI) {
        this.wargamingConnector = wargamingConnector;
        this.wargamingAPI = wargamingAPI;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        String id = null;
        int option = 5;
        final int ALL = 0;
        final int MONTH1 = 1;
        final int MONTH2 = 2;
        final int MONTH3 = 3;
        final int IMAGE = 4;

        int game_type = 3;
        final int AVG = 1;
        final int RANK = 2;
        if(args.isEmpty()) {
            try {
                ResultSet resultSet = wargamingConnector.Select_Query_Wargaming("SELECT * FROM wargamingUserId WHERE discordId = ?",
                        new int[]{wargamingConnector.STRING}, new String[]{event.getAuthor().getId()});
                if(resultSet.next()) {
                    id = resultSet.getString("userId");
                } else {
                    event.getChannel().sendMessage("검색할 유저명을 입력해주십시오").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                    return;
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
        if(id == null) {
            if(args.size() == 1) {
                try {
                    id = wargamingAPI.getWargamingPlayer(args.get(0));
                    option = ALL;
                    game_type = ALL;
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                    event.getChannel().sendMessage("유저 검색중 에러가 발생했습니다.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                }
            }
            if(args.size() >= 2) {
                try {
                    id = wargamingAPI.getWargamingPlayer(args.get(0));
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                    event.getChannel().sendMessage("유저 검색중 에러가 발생했습니다.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                }
                game_type = ALL;
                switch (args.get(1)) {
                    case "전체" -> option = ALL;
                    case "30일" -> option = MONTH1;
                    case "60일" -> option = MONTH2;
                    case "90일" -> option = MONTH3;
                    case "이미지" -> option = IMAGE;
                    case "일반" -> game_type = AVG;
                    case "랭크" -> game_type = RANK;
                }
            }
            if(args.size() >= 3) {
                try {
                    id = wargamingAPI.getWargamingPlayer(args.get(0));
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                    event.getChannel().sendMessage("유저 검색중 에러가 발생했습니다.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                }
                switch (args.get(1)) {
                    case "전체" -> option = ALL;
                    case "30일" -> option = MONTH1;
                    case "60일" -> option = MONTH2;
                    case "90일" -> option = MONTH3;
                    case "이미지" -> option = IMAGE;
                }
                switch (args.get(2)) {
                    case "일반" -> game_type = AVG;
                    case "랭크" -> game_type = RANK;
                }
            }
        } else {
            if(args.size() >= 1) {
                game_type = ALL;
                switch (args.get(0)) {
                    case "전체" -> option = ALL;
                    case "30일" -> option = MONTH1;
                    case "60일" -> option = MONTH2;
                    case "90일" -> option = MONTH3;
                    case "이미지" -> option = IMAGE;
                    case "일반" -> game_type = AVG;
                    case "랭크" -> game_type = RANK;
                }
            }
            if(args.size() >= 2) {
                switch (args.get(0)) {
                    case "전체" -> option = ALL;
                    case "30일" -> option = MONTH1;
                    case "60일" -> option = MONTH2;
                    case "90일" -> option = MONTH3;
                    case "이미지" -> option = IMAGE;
                }
                switch (args.get(1)) {
                    case "일반" -> game_type = AVG;
                    case "랭크" -> game_type = RANK;
                }
            }
        }

        logger.info("option :" + option + ", game_type: "+ game_type + ", id:" + id);

        //TODO 이제... 대망의 값 가져오기!
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
        if(option == ALL) {
            WargamingAPI.DataObject dataObject = wargamingAPI.getUserPersonalData(id, today);
            if(game_type == ALL) {
                int battles = dataObject.allDataObject.battles;
                int wins = dataObject.allDataObject.wins;
                double accuracy = ((double) dataObject.allDataObject.hits / (double) dataObject.allDataObject.shots);
                long frags = dataObject.allDataObject.frags;
                long spotted = dataObject.allDataObject.spotted;
                int survived = dataObject.allDataObject.survived;
                builder.setTitle("전적 조회(전체 전투)")
                        .addField("전투", String.valueOf(battles), true)
                        .addField("승률", String.valueOf(((double) wins / (double) battles) * 10000 / 100.0), true)
                        .addField("생존률", String.valueOf(((double) survived / (double) battles) * 10000 / 100.0), true)
                        .addField("명중률", String.valueOf(accuracy * 100 / 100.0), true)
                        .addField("전투당 스팟율", String.valueOf(((double) spotted / (double) battles) * 100 / 100.0), true)
                        .addField("전투당 격파율", String.valueOf(((double) frags / (double) battles) * 100 / 100.0), true)
                        .setFooter(args.get(0));
            } else if(game_type == AVG) {
                int battles = dataObject.allDataObject.battles - dataObject.ratingDataObject.battles;
                int wins = dataObject.allDataObject.wins - dataObject.ratingDataObject.wins;
                double accuracy = ((double) (dataObject.allDataObject.hits - dataObject.ratingDataObject.hits)) /
                        ((double) (dataObject.allDataObject.shots - dataObject.ratingDataObject.hits));
                long frags = dataObject.allDataObject.frags - dataObject.ratingDataObject.frags;
                long spotted = dataObject.allDataObject.spotted - dataObject.ratingDataObject.spotted;
                int survived = dataObject.allDataObject.survived - dataObject.ratingDataObject.survived;
                builder.setTitle("전적 조회(일반모드)")
                        .addField("전투", String.valueOf(battles), true)
                        .addField("승률", String.valueOf(((double) wins / (double) battles) * 10000 / 100.0), true)
                        .addField("생존률", String.valueOf(((double) survived / (double) battles) * 10000 / 100.0), true)
                        .addField("명중률", String.valueOf(accuracy * 100 / 100.0), true)
                        .addField("전투당 스팟율", String.valueOf(((double) spotted / (double) battles) * 100 / 100.0), true)
                        .addField("전투당 격파율", String.valueOf(((double) frags / (double) battles) * 100 / 100.0), true)
                        .setFooter(args.get(0));
            } else if(game_type == RANK) {
                int battles = dataObject.ratingDataObject.battles;
                int wins = dataObject.ratingDataObject.wins;
                double accuracy = ((double) dataObject.ratingDataObject.hits / (double) dataObject.ratingDataObject.shots);
                long frags = dataObject.ratingDataObject.frags;
                long spotted = dataObject.ratingDataObject.spotted;
                int survived = dataObject.ratingDataObject.survived;
                int rating = dataObject.ratingDataObject.rating;
                int season = dataObject.ratingDataObject.current_season;
                boolean reCal= dataObject.ratingDataObject.reCalibration;
                long reCalStartTime = dataObject.ratingDataObject.reCalibrationTime;
                int reCalBattleLeft = dataObject.ratingDataObject.reCalibrationBattleLeft;
                Date date = new Date(reCalStartTime);
                SimpleDateFormat sdf = new SimpleDateFormat("");
                builder.setTitle("전적 조회(랭크모드)")
                        .addField("전투", String.valueOf(battles), true)
                        .addField("승률", String.valueOf(((double) wins / (double) battles) * 10000 / 100.0), true)
                        .addField("생존률", String.valueOf(((double) survived / (double) battles) * 10000 / 100.0), true)
                        .addField("명중률", String.valueOf(accuracy * 100 / 100.0), true)
                        .addField("전투당 스팟율", String.valueOf(((double) spotted / (double) battles) * 100 / 100.0), true)
                        .addField("전투당 격파율", String.valueOf(((double) frags / (double) battles) * 100 / 100.0), true)
                        .addField("랭크 전투 시즌", String.valueOf(season), true)
                        .addField("랭크 MMR", String.valueOf(rating), true)
                        .setFooter(args.get(0));
                if(!reCal) {
                    builder.addField("검증 전투 완료까지 남은 횟수", String.valueOf(reCalBattleLeft), false)
                            .addField("검증 전투 최초 시작 시간", sdf.format(date), false);
                }
            } else {
                event.getChannel().sendMessage("").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                return;
            }
        } else if(option == MONTH1) {
            return;
        } else {
            return;
        }
        event.getChannel().sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getHelp() {
        return "전적을 조회하는 명령어입니다.";
    }

    @Override
    public String[] getInvoke() {
        return new String[] {"전적", "blitzstats", "bs"};
    }

    @Override
    public String getSmallHelp() {
        return "전적을 조회하는 명령어입니다.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
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
