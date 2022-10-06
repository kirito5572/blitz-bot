package me.kirito5572.commands.blitz;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.*;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchPlayerOverAllStatCommand implements ICommand{
    private final Logger logger = LoggerFactory.getLogger(SearchPlayerOverAllStatCommand.class);
    private final SQLITEConnector wargamingConnector;
    private final WargamingAPI wargamingAPI;
    private final int ALL = 0;
    /** @noinspection FieldCanBeLocal*/
    private final int MONTH1 = 1;
    /** @noinspection FieldCanBeLocal*/
    private final int MONTH2 = 2;
    /** @noinspection FieldCanBeLocal*/
    private final int MONTH3 = 3;
    /** @noinspection FieldCanBeLocal*/
    private final int IMAGE = 4;
    private final int AVG = 1;
    private final int RANK = 2;

    public SearchPlayerOverAllStatCommand(SQLITEConnector wargamingConnector, WargamingAPI wargamingAPI) {
        this.wargamingConnector = wargamingConnector;
        this.wargamingAPI = wargamingAPI;
    }

    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
        String id = null;
        int option = 5;

        int game_type = 3;
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

        //이제... 대망의 값 가져오기!
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
        if(option == ALL) {
            WargamingAPI.DataObject dataObject = wargamingAPI.getUserPersonalData(id, today);
            if(dataObject == null) {
                event.getChannel().sendMessage("전적을 불러오던중 에러가 발생했습니다.").queue(message ->
                        message.delete().queueAfter(10, TimeUnit.SECONDS));
                return;
            }
            if(game_type == ALL) {
                int battles = dataObject.allDataObject.battles;
                int wins = dataObject.allDataObject.wins;
                double accuracy = ((double) dataObject.allDataObject.hits / (double) dataObject.allDataObject.shots);
                long frags = dataObject.allDataObject.frags;
                long spotted = dataObject.allDataObject.spotted;
                int survived = dataObject.allDataObject.survived;
                long damageAvg = dataObject.allDataObject.damage_dealt / dataObject.allDataObject.battles;
                double damageRadio = ((double) dataObject.allDataObject.damage_dealt / (double) dataObject.allDataObject.damage_received);
                builder.setTitle("전적 조회(전체 전투)")
                        .addField("전투", String.valueOf(battles), true)
                        .addField("승률", String.format("%.2f", ((double) wins / (double) battles) * 10000 / 100.0), true)
                        .addField("생존률", String.format("%.2f", ((double) survived / (double) battles) * 10000 / 100.0), true)
                        .addField("명중률", String.format("%.2f", accuracy * 100 / 100.0), true)
                        .addField("전투당 스팟율", String.format("%.2f", ((double) spotted / (double) battles) * 100 / 100.0), true)
                        .addField("전투당 격파율", String.format("%.2f", ((double) frags / (double) battles) * 100 / 100.0), true)
                        .addField("평균 대미지", String.valueOf(damageAvg), true)
                        .addField("피해 비율", String.format("%.2f", damageRadio * 100 / 100.0), true)
                        .setFooter(args.get(0));
            } else if(game_type == AVG) {
                int battles = dataObject.allDataObject.battles - dataObject.ratingDataObject.battles;
                int wins = dataObject.allDataObject.wins - dataObject.ratingDataObject.wins;
                double accuracy = ((double) (dataObject.allDataObject.hits - dataObject.ratingDataObject.hits)) /
                        ((double) (dataObject.allDataObject.shots - dataObject.ratingDataObject.shots));
                long frags = dataObject.allDataObject.frags - dataObject.ratingDataObject.frags;
                long spotted = dataObject.allDataObject.spotted - dataObject.ratingDataObject.spotted;
                int survived = dataObject.allDataObject.survived - dataObject.ratingDataObject.survived;
                long damageAvg = (dataObject.allDataObject.damage_dealt - dataObject.ratingDataObject.damage_dealt ) /
                        (dataObject.allDataObject.battles - dataObject.ratingDataObject.battles);
                double damageRadio = ((double) (dataObject.allDataObject.damage_dealt - dataObject.ratingDataObject.damage_dealt) /
                        (double) (dataObject.allDataObject.damage_received - dataObject.ratingDataObject.damage_received));
                builder.setTitle("전적 조회(일반모드)")
                        .addField("전투", String.valueOf(battles), true)
                        .addField("승률", String.format("%.2f", ((double) wins / (double) battles) * 10000 / 100.0), true)
                        .addField("생존률", String.format("%.2f", ((double) survived / (double) battles) * 10000 / 100.0), true)
                        .addField("명중률", String.format("%.2f", accuracy * 100 / 100.0), true)
                        .addField("전투당 스팟율", String.format("%.2f", ((double) spotted / (double) battles) * 100 / 100.0), true)
                        .addField("전투당 격파율", String.format("%.2f", ((double) frags / (double) battles) * 100 / 100.0), true)
                        .addField("평균 대미지", String.valueOf(damageAvg), true)
                        .addField("피해 비율", String.format("%.2f", damageRadio * 100 / 100.0), true)
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
                long damageAvg = dataObject.ratingDataObject.damage_dealt / dataObject.ratingDataObject.battles;
                double damageRadio = ((double) dataObject.ratingDataObject.damage_dealt / (double) dataObject.ratingDataObject.damage_received);
                Date date = new Date(reCalStartTime);
                SimpleDateFormat sdf = new SimpleDateFormat("");
                builder.setTitle("전적 조회(랭크모드)")
                        .addField("전투", String.valueOf(battles), true)
                        .addField("승률", String.format("%.2f", ((double) wins / (double) battles) * 10000 / 100.0), true)
                        .addField("생존률", String.format("%.2f", ((double) survived / (double) battles) * 10000 / 100.0), true)
                        .addField("명중률", String.format("%.2f", accuracy * 100 / 100.0), true)
                        .addField("전투당 스팟율", String.format("%.2f", ((double) spotted / (double) battles) * 100 / 100.0), true)
                        .addField("전투당 격파율", String.format("%.2f", ((double) frags / (double) battles) * 100 / 100.0), true)
                        .addField("랭크 전투 시즌", String.valueOf(season), true)
                        .addField("랭크 MMR", String.valueOf(rating), true)
                        .addField("평균 대미지", String.valueOf(damageAvg), true)
                        .addField("피해 비율", String.format("%.2f", damageRadio * 100 / 100.0), true)
                        .setFooter(args.get(0));
                if(!reCal) {
                    builder.addField("검증 전투 완료까지 남은 횟수", String.valueOf(reCalBattleLeft), false)
                            .addField("검증 전투 최초 시작 시간", sdf.format(date), false);
                }
            } else {
                event.getChannel().sendMessage("게임 모드가 정확히 선택되지 않았습니다.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                return;
            }
        } else if(option == MONTH1) {
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date month = calendar.getTime();
            logger.info("30일 builder 들어감");
            builder = monthBuilder(game_type, id, today, month, builder, args, "30일");
            if(builder == null) {
                event.getChannel().sendMessage("데이터 조회에 실패했습니다.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                return;
            }
        } else if(option == MONTH2) {
            calendar.add(Calendar.DAY_OF_MONTH, -60);
            Date month = calendar.getTime();
            builder = monthBuilder(game_type, id, today, month, builder, args, "60일");
            if(builder == null) {
                event.getChannel().sendMessage("데이터 조회에 실패했습니다.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                return;
            }
        } else if(option == MONTH3) {
            calendar.add(Calendar.DAY_OF_MONTH, -90);
            Date month = calendar.getTime();
            builder = monthBuilder(game_type, id, today, month, builder, args, "90일");
            if(builder == null) {
                event.getChannel().sendMessage("데이터 조회에 실패했습니다.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                return;
            }
        } else if(option == IMAGE) {
            ImageCreator imageCreator;
            try {
                imageCreator = new ImageCreator();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date month1 = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date month2 = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date month3 = calendar.getTime();
            WargamingAPI.DataObject today_data = wargamingAPI.getUserPersonalData(id);
            WargamingAPI.DataObject month1_data = wargamingAPI.getUserPersonalData(id, month1);
            WargamingAPI.DataObject month2_data = wargamingAPI.getUserPersonalData(id, month2);
            WargamingAPI.DataObject month3_data = wargamingAPI.getUserPersonalData(id, month3);
            if(today_data == null || month1_data == null || month2_data == null || month3_data == null) {
                event.getChannel().sendMessage("전적을 불러오던중 에러가 발생했습니다.").queue(message ->
                        message.delete().queueAfter(10, TimeUnit.SECONDS));
                return;
            }
            try {
                File file = imageCreator.drawTextWithImage(month1_data, month2_data, month3_data, today_data, args.get(0));
                event.getChannel().sendFile(file).queue(message -> {
                    if(file.delete()) {
                        logger.warn("전적 이미지 생성 파일 삭제에 실패했습니다.");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return;
        }
        event.getChannel().sendMessageEmbeds(builder.build()).queue();
    }

    private @Nullable EmbedBuilder monthBuilder(int game_type, String id, @NotNull Date today, @NotNull Date month, @NotNull EmbedBuilder builder, @NotNull List<String> args, String title) {
        WargamingAPI.DataObject today_data = wargamingAPI.getUserPersonalData(id, today);
        logger.info("today_data 불러옴");
        WargamingAPI.DataObject month_data = wargamingAPI.getUserPersonalData(id, month);
        logger.info("month_data 불러옴");
        if(month_data == null) {
            //X일 이전 데이터가 없을 경우
            month_data = wargamingAPI.getUserPersonalData(id, month);
        }
        if(today_data == null || month_data == null) {
            if(today_data == null) logger.error("today_data is null(SearchPlayerOverAllStatCommand:303)");
            if(month_data == null) logger.error("month_data is null(SearchPlayerOverAllStatCommand:304)");
            return null;
        }
        //X일 이전 데이터가 있을 경우
        if(game_type == ALL) {
            int battles = today_data.allDataObject.battles - month_data.allDataObject.battles;
            int wins = today_data.allDataObject.wins - month_data.allDataObject.wins;
            double accuracy = ((double) (today_data.allDataObject.hits - month_data.allDataObject.hits) /
                    (double) (today_data.allDataObject.shots - month_data.allDataObject.shots));
            long frags = today_data.allDataObject.frags - month_data.allDataObject.frags;
            long spotted = today_data.allDataObject.spotted - month_data.allDataObject.spotted;
            int survived = today_data.allDataObject.survived - month_data.allDataObject.survived;
            long damageAvg = (today_data.allDataObject.damage_dealt - month_data.allDataObject.damage_dealt) /
                    (today_data.allDataObject.battles - month_data.allDataObject.battles);
            double damageRadio = ((double) (today_data.allDataObject.damage_dealt - month_data.allDataObject.damage_dealt) /
                    (double) (today_data.allDataObject.damage_received - month_data.allDataObject.damage_received));
            builder.setTitle("전적 조회(전체 전투)")
                    .addField("전투", String.valueOf(battles), true)
                    .addField("승률", String.format("%.2f", ((double) wins / (double) battles) * 10000 / 100.0), true)
                    .addField("생존률", String.format("%.2f", ((double) survived / (double) battles) * 10000 / 100.0), true)
                    .addField("명중률", String.format("%.2f", accuracy * 100 / 100.0), true)
                    .addField("전투당 스팟율", String.format("%.2f", ((double) spotted / (double) battles) * 100 / 100.0), true)
                    .addField("전투당 격파율", String.format("%.2f", ((double) frags / (double) battles) * 100 / 100.0), true)
                    .addField("평균 대미지", String.valueOf(damageAvg), true)
                    .addField("피해 비율", String.format("%.2f", damageRadio * 100 / 100.0), true)
                    .setFooter(args.get(0));
        } else if(game_type == AVG) {
            int today_battles = today_data.allDataObject.battles - today_data.ratingDataObject.battles;
            int month_battles = month_data.allDataObject.battles - today_data.ratingDataObject.battles;
            int battles = today_battles - month_battles;
            int today_wins = today_data.allDataObject.wins - today_data.ratingDataObject.wins;
            int month_wins = month_data.allDataObject.wins - month_data.ratingDataObject.wins;
            int wins = today_wins - month_wins;
            long today_hits = today_data.allDataObject.hits - today_data.ratingDataObject.hits;
            long month_hits = month_data.allDataObject.hits - month_data.ratingDataObject.hits;
            long today_shots = today_data.allDataObject.shots - today_data.ratingDataObject.shots;
            long month_shots = month_data.allDataObject.shots - month_data.ratingDataObject.shots;
            double accuracy = ((double) (today_hits - month_hits) /
                    (double) (today_shots - month_shots));
            long today_frags = today_data.allDataObject.frags - today_data.ratingDataObject.frags;
            long month_frags = month_data.allDataObject.frags - month_data.ratingDataObject.frags;
            long frags = today_frags - month_frags;
            long today_spotted = today_data.allDataObject.spotted - today_data.ratingDataObject.spotted;
            long month_spotted = month_data.allDataObject.spotted - month_data.ratingDataObject.spotted;
            long spotted = today_spotted - month_spotted;
            int today_survived = today_data.allDataObject.survived - today_data.ratingDataObject.survived;
            int month_survived = month_data.allDataObject.survived - month_data.ratingDataObject.survived;
            int survived = today_survived - month_survived;
            long today_damage_dealt = today_data.allDataObject.damage_dealt - today_data.ratingDataObject.damage_dealt;
            long month_damage_dealt = month_data.allDataObject.damage_dealt - month_data.ratingDataObject.damage_dealt;
            long damageAvg = ((today_damage_dealt - month_damage_dealt) / battles);
            long today_damage_received = today_data.allDataObject.damage_received - today_data.ratingDataObject.damage_received;
            long month_damage_received = month_data.allDataObject.damage_received - month_data.ratingDataObject.damage_received;
            double damageRadio = ((double) (today_damage_dealt - month_damage_dealt) /
                    (double) (today_damage_received - month_damage_received));
            builder.setTitle("전적 조회 (" + title + " / 일반모드)")
                    .addField("전투", String.valueOf(battles), true)
                    .addField("승률", String.format("%.2f", ((double) wins / (double) battles) * 10000 / 100.0), true)
                    .addField("생존률", String.format("%.2f", ((double) survived / (double) battles) * 10000 / 100.0), true)
                    .addField("명중률", String.format("%.2f", accuracy * 100 / 100.0), true)
                    .addField("전투당 스팟율", String.format("%.2f", ((double) spotted / (double) battles) * 100 / 100.0), true)
                    .addField("전투당 격파율", String.format("%.2f", ((double) frags / (double) battles) * 100 / 100.0), true)
                    .addField("평균 대미지", String.valueOf(damageAvg), true)
                    .addField("피해 비율", String.format("%.2f", damageRadio * 100 / 100.0), true)
                    .setFooter(args.get(0));
        } else if(game_type == RANK) {
            int battles = today_data.ratingDataObject.battles - month_data.ratingDataObject.battles;
            int wins = today_data.ratingDataObject.wins - month_data.ratingDataObject.wins;
            double accuracy = ((double) (today_data.ratingDataObject.hits - month_data.ratingDataObject.hits) /
                    (double) (today_data.ratingDataObject.shots - month_data.ratingDataObject.shots));
            long frags = today_data.ratingDataObject.frags - month_data.ratingDataObject.frags;
            long spotted = today_data.ratingDataObject.spotted - month_data.ratingDataObject.spotted;
            int survived = today_data.ratingDataObject.survived - month_data.ratingDataObject.survived;
            int rating = today_data.ratingDataObject.rating - month_data.ratingDataObject.rating;
            long damageAvg = (today_data.ratingDataObject.damage_dealt - month_data.ratingDataObject.damage_dealt) /
                    (today_data.ratingDataObject.battles - month_data.ratingDataObject.battles);
            double damageRadio = ((double) (today_data.ratingDataObject.damage_dealt - month_data.ratingDataObject.damage_dealt) /
                    (double) (today_data.ratingDataObject.damage_received - month_data.ratingDataObject.damage_received));
            builder.setTitle("전적 조회(" + title + " / 랭크모드)")
                    .addField("전투", String.valueOf(battles), true)
                    .addField("승률", String.format("%.2f", ((double) wins / (double) battles) * 10000 / 100.0), true)
                    .addField("생존률", String.format("%.2f", ((double) survived / (double) battles) * 10000 / 100.0), true)
                    .addField("명중률", String.format("%.2f", accuracy * 100 / 100.0), true)
                    .addField("전투당 스팟율", String.format("%.2f", ((double) spotted / (double) battles) * 100 / 100.0), true)
                    .addField("전투당 격파율", String.format("%.2f", ((double) frags / (double) battles) * 100 / 100.0), true)
                    .addField("평균 대미지", String.valueOf(damageAvg), true)
                    .addField("피해 비율", String.format("%.2f", damageRadio * 100 / 100.0), true)
                    .addField("랭크 MMR", String.valueOf(rating), true)
                    .setFooter(args.get(0));
        } else {
            return null;
        }
        return builder;
    }

    @Override
    public @NotNull String getHelp() {
        return "플레이어 개인 전적을 조회하는 명령어입니다.";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"전적", "blitzstats", "bs"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "플레이어 개인 전적을 조회하는 명령어입니다.";
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
