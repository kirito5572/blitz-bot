package me.kirito5572.objects;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class WargamingAPI {
    /** @noinspection unused*/
    private final static Logger logger = LoggerFactory.getLogger(WargamingAPI.class);
    private final @NotNull String token;
    private final SQLITEConnector wargamingConnector;
    private final Comparator<Integer> comparatorInteger = Comparator.naturalOrder();
    private final Comparator<String> comparator = Comparator.naturalOrder();

    private final Map<String, AchievementData> achievementMap = new TreeMap<>(comparator);
    private final Map<Integer, TankData> tankDataMap = new TreeMap<>(comparatorInteger);

    public WargamingAPI(SQLITEConnector wargamingConnector) {
        token = "54634622f8be6ecf13cf721dfe133f32";     //App.openFileData("wargaming");
        //only use 2 ips token(development token)
        this.wargamingConnector = wargamingConnector;
        tankDataParser();
    }

    /** @noinspection unused*/
    public static void main(String[] args) {
        MySQLConnector mySQLConnector;      //TODO 실행전 주석처리, jar 빌드전 주석 해제!
        SQLITEConnector sqliteConnector = null;
        try {
            mySQLConnector = new MySQLConnector();
            sqliteConnector = new SQLITEConnector(mySQLConnector);
        } catch (@NotNull ClassNotFoundException | SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
        WargamingAPI wargamingAPI = new WargamingAPI(sqliteConnector);
        DataObject dataObject = wargamingAPI.getUserPersonalData("2011403181");

    }

    public String @NotNull [] tankIdBuilder() {
        int size = (tankDataMap.size() / 100) + 1;
        String[] return_data = new String[size];
        StringBuilder builder = new StringBuilder();
        int i = 0;
        int j = 0;
        for(TankData data : tankDataMap.values()) {
            builder.append(data.tank_id);
            i++;
            if(i == 100) {
                return_data[j] = builder.toString();
                builder = new StringBuilder();
                j++;
                i = 0;
            } else {
                builder.append(", ");
            }
        }
        return_data[j] = builder.substring(0, builder.toString().length() - 2);
        return return_data;
    }

    /** @noinspection unused*/
    public @Nullable Map<Integer, DataObject_UserDamage> getUserAvgDamageData(String id) {
        Map<Integer, DataObject_UserDamage> tankDamageMap = new TreeMap<>(comparatorInteger);
        String[] tankIdBuilder = tankIdBuilder();
        for(String tankId : tankIdBuilder) {
            String apiURL = "https://api.wotblitz.asia/wotb/tanks/stats/";
            apiURL += "?application_id=" + token;
            apiURL += "&fields=tank_id, all.damage_dealt, all.damage_received&language=en&account_id=" + id;
            apiURL += "&tank_id=" + tankId;
            try {
                JsonElement element = GET(apiURL);
                JsonArray jsonArray = element.getAsJsonObject().get("data").getAsJsonObject().get(id).getAsJsonArray();
                System.out.println(element);
                for (JsonElement jsonElement : jsonArray) {
                    DataObject_UserDamage userDamage = new DataObject_UserDamage();
                    JsonObject data = jsonElement.getAsJsonObject();
                    int tank_id = data.get("tank_id").getAsInt();
                    userDamage.damage_dealt = data.get("all").getAsJsonObject().get("damage_dealt").getAsLong();
                    userDamage.damage_received = data.get("all").getAsJsonObject().get("damage_received").getAsLong();
                    tankDamageMap.put(tank_id, userDamage);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return tankDamageMap;
    }

    public JsonElement getTankData(String nation) {
        String apiURL = "https://api.wotblitz.asia/wotb/encyclopedia/vehicles/";
        apiURL += "?application_id=" + token;
        apiURL += "&language=en&nation=" + nation;
        apiURL += "&fields=tier, tank_id, name, nation";
        try {
            return GET(apiURL);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void tankDataParser() {
        //usa
        JsonElement element = getTankData("usa");
        JsonObject data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //ussr
        element = getTankData("ussr");
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //germany
        element = getTankData("germany");
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //uk
        element = getTankData("uk");
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //japan
        element = getTankData("japan");
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //france
        element = getTankData("france");
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //european
        element = getTankData("european");
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //china
        element = getTankData("china");
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //other
        element = getTankData("other");
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
    }

    /** @noinspection unused*/
    public void AchievementDataParser() {
        JsonElement element;
        String apiURL = "https://api.wotblitz.asia/wotb/encyclopedia/achievements/";
        apiURL += "?application_id=" + token;
        apiURL += "language=ko&fields=achievement_id, section, name";
        try {
            element =  GET(apiURL);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        JsonObject data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            AchievementData achievementData = new AchievementData();
            achievementData.achievement_id = object.get("achievement_id").getAsString();

            achievementData.section = object.get("section").getAsString();
            achievementData.name = object.get("name").getAsString();
            achievementMap.put(value, achievementData);
        }
    }

    public @Nullable String getWargamingPlayer(String nickname) throws SQLException {
        String id;
        ResultSet resultSet = wargamingConnector.Select_Query_Wargaming("SELECT * FROM wargamingUserId WHERE nickname = ?",
                new int[]{wargamingConnector.STRING}, new String[]{nickname});
        if (resultSet.next()) {
            id = resultSet.getString("userId");
        } else {
            String apiURL = "https://api.wotblitz.asia/wotb/account/list/";
            apiURL += "?application_id=" + token;
            apiURL += "&search=" + nickname;
            try {
                JsonElement element = GET(apiURL);
                JsonArray array = element.getAsJsonObject().get("data").getAsJsonArray();
                if (array.size() == 1) {
                    id = array.get(0).getAsJsonObject().get("account_id").getAsString();
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            ResultSet resultSet1 = wargamingConnector.Select_Query_Wargaming("SELECT * FROM wargamingUserId WHERE userId = ?",
                    new int[]{wargamingConnector.STRING}, new String[]{id});
            if (resultSet1.next()) {
                wargamingConnector.Insert_Query_Wargaming("UPDATE wargamingUserId SET nickname = ? WHERE userId = ?",
                        new int[]{wargamingConnector.STRING, wargamingConnector.STRING}, new String[]{nickname, id});
            } else {
                wargamingConnector.Insert_Query_Wargaming("INSERT INTO wargamingUserId (nickname, userId) VALUES (?, ?)",
                        new int[]{wargamingConnector.STRING, wargamingConnector.STRING}, new String[]{nickname, id});
                wargamingConnector.Insert_Query_Wargaming("create table `" + id + "` \n" +
                                "(\n" +
                                "\tinput_time text,\n" +
                                "\tdata text \n"+
                                ");",
                        new int[]{}, new String[]{});
            }
        }
        return id;
    }

    public @Nullable DataObject getUserPersonalData(String id, @NotNull Date date) {
        DataObject dataObject = new DataObject();
        ResultSet resultSet = null;
        try {
            resultSet = wargamingConnector.Select_Query_Wargaming("SELECT * FROM `" + id + "` WHERE input_time = ?",
                    new int[]{wargamingConnector.STRING},
                    new String[]{ String.valueOf(date.getTime())});
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            //최초 조회 유저!
            dataObject = getUserPersonalData(id);
            String json = new Gson().toJson(dataObject);
            try {
                wargamingConnector.Insert_Query_Wargaming("INSERT INTO `" + id + "` (input_time, data) VALUES (?, ?)",
                        new int[]{wargamingConnector.STRING, wargamingConnector.STRING},
                        new String[]{String.valueOf(date.getTime()), json});
            } catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        }
        if(resultSet != null) {
            try {
                Calendar calendar = new GregorianCalendar();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                if(resultSet.next()) {
                    //조회를 하려고 하는 날(date)에 데이터가 있을 경우
                    if(date.getTime() == calendar.getTime().getTime()) {
                        //조회를 하려고 하는 날(date)이 오늘 인 경우
                        dataObject = getUserPersonalData(id);
                    } else {
                        dataObject = new Gson().fromJson(resultSet.getString("data"), DataObject.class);
                    }
                } else {
                    //조회를 하려고 하는 날(date)에 데이터가 없을 경우
                    //그런데 그 날이 오늘 인 경우
                    if(date.getTime() == calendar.getTime().getTime()) {
                        dataObject = getUserPersonalData(id);
                        String json = new Gson().toJson(dataObject);
                        wargamingConnector.Insert_Query_Wargaming("INSERT INTO `" + id + "` (input_time, data) VALUES (?, ?)",
                                new int[]{wargamingConnector.STRING, wargamingConnector.STRING},
                                new String[]{String.valueOf(date.getTime()), json});
                    } else if(date.getTime() < calendar.getTime().getTime()) {
                        //인접한 날짜의 데이터를 가져온다
                        try {
                            ResultSet resultSet1 = wargamingConnector.Select_Query_Wargaming(
                                    "SELECT input_time, ABS(input_time - " + date.getTime() + ") AS Distance " +
                                            "FROM `" + id + "` ORDER BY Distance LIMIT 1",
                                    new int[]{}, new String[]{});
                            if (resultSet1.next()) {
                                //데이터가 존재할 경우
                                dataObject = new Gson().fromJson(resultSet.getString("data"), DataObject.class);
                            }
                        } catch (SQLException ignored) {
                            //TODO 30일 조회했을때 인접 데이터 조회에 실패했음! 원인 파악후 수정해야함
                        }
                        //만약 데이터가 존재하지 않을 경우
                        // = 오늘 최초 조회를 진행 한 경우이므로 위의 if에 빠지게 된다
                    }
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        } else {
            //테이블이 아예 존재하지 않을 경우
            return null;
        }
        return dataObject;
    }

    public @Nullable DataObject getUserPersonalData(String id) {
        DataObject dataObject = new DataObject();
        String apiURL = "https://api.wotblitz.asia/wotb/account/info/";
        apiURL += "?application_id=" + token;
        apiURL += "&account_id=" + id;
        apiURL += "&extra=statistics.rating&fields=statistics.all, statistics.clan, statistics.rating&language=ko";
        try {
            JsonElement element = GET(apiURL);
            JsonObject statistics = element.getAsJsonObject().get("data").getAsJsonObject().get(id).getAsJsonObject().get("statistics").getAsJsonObject();
            JsonObject clan = statistics.get("clan").getAsJsonObject();
            JsonObject rating = statistics.get("rating").getAsJsonObject();
            RatingDataObject ratingDataObject = new RatingDataObject();
            ratingDataObject.battles = rating.get("battles").getAsInt();
            ratingDataObject.wins = rating.get("wins").getAsInt();
            ratingDataObject.losses = rating.get("losses").getAsInt();
            ratingDataObject.survived = rating.get("survived_battles").getAsInt();
            ratingDataObject.frags = rating.get("frags").getAsLong();
            ratingDataObject.spotted = rating.get("spotted").getAsInt();
            ratingDataObject.hits = rating.get("hits").getAsLong();
            ratingDataObject.shots = rating.get("shots").getAsLong();
            ratingDataObject.current_season = rating.get("current_season").getAsInt();
            ratingDataObject.reCalibration = rating.get("is_recalibration").getAsBoolean();
            ratingDataObject.reCalibrationTime = rating.get("recalibration_start_time").getAsLong();
            ratingDataObject.reCalibrationBattleLeft = rating.get("calibration_battles_left").getAsInt();
            ratingDataObject.rating = rating.get("mm_rating").getAsInt();
            ratingDataObject.damage_received = rating.get("damage_received").getAsLong();
            ratingDataObject.damage_dealt = rating.get("damage_dealt").getAsLong();
            dataObject.ratingDataObject = ratingDataObject;
            JsonObject all = statistics.get("all").getAsJsonObject();
            AllDataObject allDataObject = new AllDataObject();
            allDataObject.battles = all.get("battles").getAsInt();
            allDataObject.wins = all.get("wins").getAsInt();
            allDataObject.losses = all.get("losses").getAsInt();
            allDataObject.survived = all.get("survived_battles").getAsInt();
            allDataObject.frags = all.get("frags").getAsInt();
            allDataObject.spotted = all.get("spotted").getAsInt();
            allDataObject.hits = all.get("hits").getAsLong();
            allDataObject.shots = all.get("shots").getAsLong();
            allDataObject.damage_received = rating.get("damage_received").getAsLong();
            allDataObject.damage_dealt = rating.get("damage_dealt").getAsLong();
            dataObject.allDataObject = allDataObject;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return dataObject;
    }

    /** @noinspection unused*/
    public @NotNull Achievement getUserAchievementData(String id) {
        Achievement achievement = new Achievement();
        String apiURL = "https://api.wotblitz.asia/wotb/account/achievements/";
        apiURL += "?application_id=" + token;
        apiURL += "&account_id=" + id;
        apiURL += "&language=ko";
        try {
            JsonElement element = GET(apiURL);
            JsonObject achievements = element.getAsJsonObject().get("data").getAsJsonObject().get(id).getAsJsonObject().get("achievements").getAsJsonObject();
            {
                achievement.armorPiercer = achievements.get("armorPiercer").getAsInt();
                achievement.medalFadin = achievements.get("medalFadin").getAsInt();
                achievement.medalCarius = achievements.get("medalCarius").getAsInt();
                achievement.medalEkins = achievements.get("medalEkins").getAsInt();
                achievement.collectorGuP = achievements.get("collectorGuP").getAsInt();
                achievement.medalHalonen = achievements.get("medalHalonen").getAsInt();
                achievement.heroesOfRassenay = achievements.get("heroesOfRassenay").getAsInt();
                achievement.firstVictory = achievements.get("firstVictory").getAsInt();
                achievement.defender = achievements.get("defender").getAsInt();
                achievement.creative = achievements.get("creative").getAsInt();
                achievement.eSportFinal = achievements.get("eSportFinal").getAsInt();
                achievement.supporter = achievements.get("supporter").getAsInt();
                achievement.goldClanRibbonSEA = achievements.get("goldClanRibbonSEA").getAsInt();
                achievement.platinumTwisterMedalSEA = achievements.get("platinumTwisterMedalSEA").getAsInt();
                achievement.medalLehvaslaiho = achievements.get("medalLehvaslaiho").getAsInt();
                achievement.tankExpert = achievements.get("tankExpert").getAsInt();
                achievement.eSportQualification = achievements.get("eSportQualification").getAsInt();
                achievement.MarkI = achievements.get("MarkI").getAsInt();
                achievement.medalSupremacy = achievements.get("medalSupremacy").getAsInt();
                achievement.participantofWGFest2017 = achievements.get("participantofWGFest2017").getAsInt();
                achievement.medalTournamentOffseason1 = achievements.get("medalTournamentOffseason1").getAsInt();
                achievement.jointVictory = achievements.get("jointVictory").getAsInt();
                achievement.platinumClanRibbonRU = achievements.get("platinumClanRibbonRU").getAsInt();
                achievement.medalTournamentOffseason4 = achievements.get("medalTournamentOffseason4").getAsInt();
                achievement.sniper = achievements.get("sniper").getAsInt();
                achievement.titleSniper = achievements.get("titleSniper").getAsInt();
                achievement.medalCrucialContribution = achievements.get("medalCrucialContribution").getAsInt();
                achievement.scout = achievements.get("scout").getAsInt();
                achievement.goldTwisterMedalRU = achievements.get("goldTwisterMedalRU").getAsInt();
                achievement.tankExpert3 = achievements.get("tankExpert3").getAsInt();
                achievement.tankExpert2 = achievements.get("tankExpert2").getAsInt();
                achievement.tankExpert1 = achievements.get("tankExpert1").getAsInt();
                achievement.tankExpert0 = achievements.get("tankExpert0").getAsInt();
                achievement.markOfMastery = achievements.get("markOfMastery").getAsInt();
                achievement.tankExpert6 = achievements.get("tankExpert6").getAsInt();
                achievement.tankExpert5 = achievements.get("tankExpert5").getAsInt();
                achievement.tankExpert4 = achievements.get("tankExpert4").getAsInt();
                achievement.goldTwisterMedalEU = achievements.get("goldTwisterMedalEU").getAsInt();
                achievement.ChristmasTreeLevelUpNY2019 = achievements.get("ChristmasTreeLevelUpNY2019").getAsInt();
                achievement.medalLavrinenko = achievements.get("medalLavrinenko").getAsInt();
                achievement.medalKolobanov = achievements.get("medalKolobanov").getAsInt();
                achievement.medalLafayettePool = achievements.get("medalLafayettePool").getAsInt();
                achievement.goldClanRibbonEU = achievements.get("goldClanRibbonEU").getAsInt();
                achievement.olimpicGolden = achievements.get("olimpicGolden").getAsInt();
                achievement.medalKnispel = achievements.get("medalKnispel").getAsInt();
                achievement.invader = achievements.get("invader").getAsInt();
                achievement.goldTwisterMedalNA = achievements.get("goldTwisterMedalNA").getAsInt();
                achievement.mechanicEngineer = achievements.get("mechanicEngineer").getAsInt();
                achievement.markOfMasteryII = achievements.get("markOfMasteryII").getAsInt();
                achievement.firstBlood = achievements.get("firstBlood").getAsInt();
                achievement.medalKay = achievements.get("medalKay").getAsInt();
                achievement.medalOrlik = achievements.get("medalOrlik").getAsInt();
                achievement.medalBrothersInArms = achievements.get("medalBrothersInArms").getAsInt();
                achievement.medalAbrams = achievements.get("medalAbrams").getAsInt();
                achievement.medalAtgm = achievements.get("medalAtgm").getAsInt();
                achievement.mainGun = achievements.get("mainGun").getAsInt();
                achievement.ironMan = achievements.get("ironMan").getAsInt();
                achievement.platinumClanRibbonEU = achievements.get("platinumClanRibbonEU").getAsInt();
                achievement.platinumClanRibbonSEA = achievements.get("platinumClanRibbonSEA").getAsInt();
                achievement.warrior = achievements.get("warrior").getAsInt();
                achievement.goldClanRibbonRU = achievements.get("goldClanRibbonRU").getAsInt();
                achievement.medalRadleyWalters = achievements.get("medalRadleyWalters").getAsInt();
                achievement.raider = achievements.get("raider").getAsInt();
                achievement.participantofNewStart = achievements.get("participantofNewStart").getAsInt();
                achievement.diamondClanRibbon = achievements.get("diamondClanRibbon").getAsInt();
                achievement.medalBillotte = achievements.get("medalBillotte").getAsInt();
                achievement.platinumTwisterMedalEU = achievements.get("platinumTwisterMedalEU").getAsInt();
                achievement.diehard = achievements.get("diehard").getAsInt();
                achievement.masterofContinents = achievements.get("masterofContinents").getAsInt();
                achievement.evileye = achievements.get("evileye").getAsInt();
                achievement.cadet = achievements.get("cadet").getAsInt();
                achievement.medalBlitzMasters = achievements.get("medalBlitzMasters").getAsInt();
                achievement.supremacyHunter = achievements.get("supremacyHunter").getAsInt();
                achievement.newbieT3485Win = achievements.get("newbieT3485Win").getAsInt();
                achievement.continentalContender = achievements.get("continentalContender").getAsInt();
                achievement.steelwall = achievements.get("steelwall").getAsInt();
                achievement.supremacyLegend = achievements.get("supremacyLegend").getAsInt();
                achievement.punisher = achievements.get("punisher").getAsInt();
                achievement.eSport = achievements.get("eSport").getAsInt();
                achievement.platinumTwisterMark = achievements.get("platinumTwisterMark").getAsInt();
                achievement.goldClanRibbonNA = achievements.get("goldClanRibbonNA").getAsInt();
                achievement.medalPoppel = achievements.get("medalPoppel").getAsInt();
                achievement.mechanicEngineer6 = achievements.get("mechanicEngineer6").getAsInt();
                achievement.mechanicEngineer4 = achievements.get("mechanicEngineer4").getAsInt();
                achievement.goldTwisterMedalSEA = achievements.get("goldTwisterMedalSEA").getAsInt();
                achievement.mechanicEngineer2 = achievements.get("mechanicEngineer2").getAsInt();
                achievement.mechanicEngineer3 = achievements.get("mechanicEngineer3").getAsInt();
                achievement.mechanicEngineer0 = achievements.get("mechanicEngineer0").getAsInt();
                achievement.mechanicEngineer1 = achievements.get("mechanicEngineer1").getAsInt();
                achievement.mechanicEngineer5 = achievements.get("mechanicEngineer5").getAsInt();
                achievement.medalTarczay = achievements.get("medalTarczay").getAsInt();
                achievement.sinai = achievements.get("sinai").getAsInt();
                achievement.pattonValley = achievements.get("pattonValley").getAsInt();
                achievement.newbieDoubleWin = achievements.get("newbieDoubleWin").getAsInt();
                achievement.medalDeLanglade = achievements.get("medalDeLanglade").getAsInt();
                achievement.diamondTwisterMedal = achievements.get("diamondTwisterMedal").getAsInt();
                achievement.beasthunter = achievements.get("beasthunter").getAsInt();
                achievement.supremacyVeteran = achievements.get("supremacyVeteran").getAsInt();
                achievement.newbieShermanWin = achievements.get("newbieShermanWin").getAsInt();
                achievement.kamikaze = achievements.get("kamikaze").getAsInt();
                achievement.olimpicBronze = achievements.get("olimpicBronze").getAsInt();
                achievement.newbieType58TUWin = achievements.get("newbieType58TUWin").getAsInt();
                achievement.medalTournamentOffseason3 = achievements.get("medalTournamentOffseason3").getAsInt();
                achievement.medalTournamentOffseason2 = achievements.get("medalTournamentOffseason2").getAsInt();
                achievement.medalOskin = achievements.get("medalOskin").getAsInt();
                achievement.invincible = achievements.get("invincible").getAsInt();
                achievement.platinumClanRibbonNA = achievements.get("platinumClanRibbonNA").getAsInt();
                achievement.platinumTwisterMedalRU = achievements.get("platinumTwisterMedalRU").getAsInt();
                achievement.newbieTrippleWin = achievements.get("newbieTrippleWin").getAsInt();
                achievement.continentalViceChampion = achievements.get("continentalViceChampion").getAsInt();
                achievement.olimpicSilver = achievements.get("olimpicSilver").getAsInt();
                achievement.markOfMasteryI = achievements.get("markOfMasteryI").getAsInt();
                achievement.continentalCompetitor = achievements.get("continentalCompetitor").getAsInt();
                achievement.newbieTigerWin = achievements.get("newbieTigerWin").getAsInt();
                achievement.medalTournamentSummerSeason = achievements.get("medalTournamentSummerSeason").getAsInt();
                achievement.mousebane = achievements.get("mousebane").getAsInt();
                achievement.medalBrunoPietro = achievements.get("medalBrunoPietro").getAsInt();
                achievement.medalTournamentSpringSeason = achievements.get("medalTournamentSpringSeason").getAsInt();
                achievement.goldTwisterMark = achievements.get("goldTwisterMark").getAsInt();
                achievement.collectorWarhammer = achievements.get("collectorWarhammer").getAsInt();
                achievement.markOfMasteryIII = achievements.get("markOfMasteryIII").getAsInt();
                achievement.medalLeClerc = achievements.get("medalLeClerc").getAsInt();
                achievement.medalTournamentProfessional = achievements.get("medalTournamentProfessional").getAsInt();
                achievement.medalCommunityChampion = achievements.get("medalCommunityChampion").getAsInt();
                achievement.diamondTwisterMark = achievements.get("diamondTwisterMark").getAsInt();
                achievement.platinumTwisterMedalNA = achievements.get("platinumTwisterMedalNA").getAsInt();
                achievement.handOfDeath = achievements.get("handOfDeath").getAsInt();
                achievement.medalTournamentWinterSeason = achievements.get("medalTournamentWinterSeason").getAsInt();
                achievement.huntsman = achievements.get("huntsman").getAsInt();
                achievement.camper = achievements.get("camper").getAsInt();
                achievement.medalNikolas = achievements.get("medalNikolas").getAsInt();
                achievement.androidTest = achievements.get("androidTest").getAsInt();
                achievement.sturdy = achievements.get("sturdy").getAsInt();
                achievement.medalTwitch = achievements.get("medalTwitch").getAsInt();
                achievement.medalWGfestTicket = achievements.get("medalWGfestTicket").getAsInt();
                achievement.championofNewStart = achievements.get("championofNewStart").getAsInt();
                achievement.medalTournamentAutumnSeason = achievements.get("medalTournamentAutumnSeason").getAsInt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return achievement;
    }

    /** @noinspection unused*/
    public TournamentData @Nullable [] getTournamentData() {
        TournamentData[] data = null;
        String apiURL = "https://api.wotblitz.asia/wotb/tournaments/list/";
        apiURL += "?application_id=" + token;
        apiURL += "&language=en&limit=10&status=upcoming&fields=start_at, end_at, registration_start_at, title, logo.original, registration_end_at";
        try {
            JsonElement element = GET(apiURL);
            JsonArray jsonArray = element.getAsJsonObject().get("data").getAsJsonArray();
            int i = 0;
            data = new TournamentData[jsonArray.size()];
            for (JsonElement element1 : jsonArray) {
                TournamentData tournamentData = new TournamentData();
                tournamentData.title = element1.getAsJsonObject().get("title").getAsString();
                tournamentData.logo = element1.getAsJsonObject().get("logo").getAsJsonObject().get("original").getAsString();
                tournamentData.start_at = element1.getAsJsonObject().get("start_at").getAsLong();
                tournamentData.end_at = element1.getAsJsonObject().get("end_at").getAsLong();
                tournamentData.registration_start_at = element1.getAsJsonObject().get("registration_start_at").getAsLong();
                tournamentData.registration_end_at = element1.getAsJsonObject().get("registration_end_at").getAsLong();
                data[i] = tournamentData;
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }

    public @Nullable ClanSearchData[] getClanSearchData(@NotNull String name) {
        ClanSearchData[] returnData = new ClanSearchData[] {null, null, null, null, null};
        String apiURL = "https://api.wotblitz.asia/wotb/clans/list/";
        apiURL += "?application_id=" + token;
        apiURL += "&language=en&search=" + name;
        try {
            JsonArray data = GET(apiURL).getAsJsonObject().get("data").getAsJsonArray();
            for (int i = 0; i < data.size(); i++) {
                if(i < 5) {
                    ClanSearchData clanData = new Gson().fromJson(data.get(i).getAsJsonObject().toString(), ClanSearchData.class);
                    returnData[i] = clanData;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return returnData;
    }

    public @Nullable ClanData getClanData(@NotNull int clanId) {
        ClanData returnData = new ClanData();
        String apiURL = "https://api.wotblitz.asia/wotb/clans/info/";
        apiURL += "?application_id=" + token;
        apiURL += "&language=ko&extra=members&clan_id=" + clanId;
        try {
            JsonObject data = GET(apiURL).getAsJsonObject().get("data").getAsJsonObject().get(String.valueOf(clanId)).getAsJsonObject();
            returnData.recruiting_options = new Gson().fromJson(data.get("recruiting_options"), ClanRecruitingOption.class);
            returnData.members_count = data.get("members_count").getAsInt();
            returnData.tag = data.get("tag").getAsString();
            returnData.name = data.get("name").getAsString();
            returnData.created_at = data.get("created_at").getAsLong();
            returnData.updated_at = data.get("updated_at").getAsLong();
            returnData.leader_name = data.get("leader_name").getAsString();
            returnData.recruiting_policy = data.get("recruiting_policy").getAsString();
            returnData.motto = data.get("motto").getAsString();
            returnData.description = data.get("description").getAsString();
            JsonArray MemberId = data.get("members_ids").getAsJsonArray();
            if(MemberId==null) {
                returnData.members_ids = null;
                returnData.members = null;
            } else {
                long[] arr = new long[MemberId.size()];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = MemberId.get(i).getAsLong();
                }
                returnData.members_ids = arr;
                Map<Long, ClanMembers> clanMembersMap = new TreeMap<>();
                JsonObject memberDataObject = data.get("members").getAsJsonObject();
                for(String value : memberDataObject.keySet()) {
                    ClanMembers members1 = new ClanMembers();
                    JsonObject object = memberDataObject.get(value).getAsJsonObject();
                    members1.role = object.get("role").getAsString();
                    members1.account_name = object.get("account_name").getAsString();

                    clanMembersMap.put(Long.valueOf(value), members1);
                }
                returnData.members = clanMembersMap;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return returnData;
    }

    private JsonElement GET(@NotNull String apiURL) throws IOException {
        URL url = new URL(apiURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }
        br.close();
        return JsonParser.parseString(response.toString());
    }

    public static class TournamentData {
        public String title;
        public String logo;
        public long start_at;
        public long end_at;
        public long registration_start_at;
        public long registration_end_at;
    }

    public static class RatingDataObject {
        public int battles;                 //전투수
        public int wins;                    //승리
        public int losses;                  //패배
        public int survived;                //생존
        public long frags;                  //격파
        public long spotted;                //스팟
        public long hits;                   //명중한 탄
        public long shots;                  //발사한 탄
        public int current_season;          //현재 시즌
        public boolean reCalibration;       //검증전투 여부
        public long reCalibrationTime;      //검증전투 시작시간
        public int reCalibrationBattleLeft; //검증전투 남은횟수
        public int rating;                  //현재 MMR
        public long damage_received;        //받은 대미지
        public long damage_dealt;           //가한 대미지
    }

    public static class AllDataObject {
        public int battles;                 //전투수
        public int wins;                    //승리
        public int losses;                  //패배
        public int survived;                //생존
        public long frags;                   //격파
        public long spotted;                 //스팟
        public long hits;                   //명중한 탄
        public long shots;                  //발사한 탄
        public long damage_received;        //받은 대미지
        public long damage_dealt;           //가한 대미지
    }

    /** @noinspection unused*/
    public static class Achievement {
        /** @noinspection unused*/
        public int armorPiercer;
        /** @noinspection unused*/
        public int medalFadin;
        /** @noinspection unused*/
        public int medalCarius;
        /** @noinspection unused*/
        public int medalEkins;
        /** @noinspection unused*/
        public int collectorGuP;
        /** @noinspection unused*/
        public int medalHalonen;
        /** @noinspection unused*/
        public int heroesOfRassenay;
        /** @noinspection unused*/
        public int firstVictory;
        /** @noinspection unused*/
        public int defender;
        /** @noinspection unused*/
        public int creative;
        /** @noinspection unused*/
        public int eSportFinal;
        /** @noinspection unused*/
        public int supporter;
        /** @noinspection unused*/
        public int goldClanRibbonSEA;
        /** @noinspection unused*/
        public int platinumTwisterMedalSEA;
        /** @noinspection unused*/
        public int medalLehvaslaiho;
        /** @noinspection unused*/
        public int tankExpert;
        /** @noinspection unused*/
        public int eSportQualification;
        /** @noinspection unused*/
        public int MarkI;
        /** @noinspection unused*/
        public int medalSupremacy;
        /** @noinspection unused*/
        public int participantofWGFest2017;
        /** @noinspection unused*/
        public int medalTournamentOffseason1;
        /** @noinspection unused*/
        public int jointVictory;
        /** @noinspection unused*/
        public int platinumClanRibbonRU;
        /** @noinspection unused*/
        public int medalTournamentOffseason4;
        /** @noinspection unused*/
        public int sniper;
        /** @noinspection unused*/
        public int titleSniper;
        /** @noinspection unused*/
        public int medalCrucialContribution;
        /** @noinspection unused*/
        public int scout;
        /** @noinspection unused*/
        public int goldTwisterMedalRU;
        /** @noinspection unused*/
        public int tankExpert3;
        /** @noinspection unused*/
        public int tankExpert2;
        /** @noinspection unused*/
        public int tankExpert1;
        /** @noinspection unused*/
        public int tankExpert0;
        /** @noinspection unused*/
        public int markOfMastery;
        /** @noinspection unused*/
        public int tankExpert6;
        /** @noinspection unused*/
        public int tankExpert5;
        /** @noinspection unused*/
        public int tankExpert4;
        /** @noinspection unused*/
        public int goldTwisterMedalEU;
        /** @noinspection unused*/
        public int ChristmasTreeLevelUpNY2019;
        /** @noinspection unused*/
        public int medalLavrinenko;
        /** @noinspection unused*/
        public int medalKolobanov;
        /** @noinspection unused*/
        public int medalLafayettePool;
        /** @noinspection unused*/
        public int goldClanRibbonEU;
        /** @noinspection unused*/
        public int olimpicGolden;
        /** @noinspection unused*/
        public int medalKnispel;
        /** @noinspection unused*/
        public int invader;
        /** @noinspection unused*/
        public int goldTwisterMedalNA;
        /** @noinspection unused*/
        public int mechanicEngineer;
        /** @noinspection unused*/
        public int markOfMasteryII;
        /** @noinspection unused*/
        public int firstBlood;
        /** @noinspection unused*/
        public int medalKay;
        /** @noinspection unused*/
        public int medalOrlik;
        /** @noinspection unused*/
        public int medalBrothersInArms;
        /** @noinspection unused*/
        public int medalAbrams;
        /** @noinspection unused*/
        public int medalAtgm;
        /** @noinspection unused*/
        public int mainGun;
        /** @noinspection unused*/
        public int ironMan;
        /** @noinspection unused*/
        public int platinumClanRibbonEU;
        /** @noinspection unused*/
        public int platinumClanRibbonSEA;
        /** @noinspection unused*/
        public int warrior;
        /** @noinspection unused*/
        public int goldClanRibbonRU;
        /** @noinspection unused*/
        public int medalRadleyWalters;
        /** @noinspection unused*/
        public int raider;
        /** @noinspection unused*/
        public int participantofNewStart;
        /** @noinspection unused*/
        public int diamondClanRibbon;
        /** @noinspection unused*/
        public int medalBillotte;
        /** @noinspection unused*/
        public int platinumTwisterMedalEU;
        /** @noinspection unused*/
        public int diehard;
        /** @noinspection unused*/
        public int masterofContinents;
        /** @noinspection unused*/
        public int evileye;
        /** @noinspection unused*/
        public int cadet;
        /** @noinspection unused*/
        public int medalBlitzMasters;
        /** @noinspection unused*/
        public int supremacyHunter;
        /** @noinspection unused*/
        public int newbieT3485Win;
        /** @noinspection unused*/
        public int continentalContender;
        /** @noinspection unused*/
        public int steelwall;
        /** @noinspection unused*/
        public int supremacyLegend;
        /** @noinspection unused*/
        public int punisher;
        /** @noinspection unused*/
        public int eSport;
        /** @noinspection unused*/
        public int platinumTwisterMark;
        /** @noinspection unused*/
        public int goldClanRibbonNA;
        /** @noinspection unused*/
        public int medalPoppel;
        /** @noinspection unused*/
        public int mechanicEngineer6;
        /** @noinspection unused*/
        public int mechanicEngineer4;
        /** @noinspection unused*/
        public int goldTwisterMedalSEA;
        /** @noinspection unused*/
        public int mechanicEngineer2;
        /** @noinspection unused*/
        public int mechanicEngineer3;
        /** @noinspection unused*/
        public int mechanicEngineer0;
        /** @noinspection unused*/
        public int mechanicEngineer1;
        /** @noinspection unused*/
        public int mechanicEngineer5;
        /** @noinspection unused*/
        public int medalTarczay;
        /** @noinspection unused*/
        public int sinai;
        /** @noinspection unused*/
        public int pattonValley;
        /** @noinspection unused*/
        public int newbieDoubleWin;
        /** @noinspection unused*/
        public int medalDeLanglade;
        /** @noinspection unused*/
        public int diamondTwisterMedal;
        /** @noinspection unused*/
        public int beasthunter;
        /** @noinspection unused*/
        public int supremacyVeteran;
        /** @noinspection unused*/
        public int newbieShermanWin;
        /** @noinspection unused*/
        public int kamikaze;
        /** @noinspection unused*/
        public int olimpicBronze;
        /** @noinspection unused*/
        public int newbieType58TUWin;
        /** @noinspection unused*/
        public int medalTournamentOffseason3;
        /** @noinspection unused*/
        public int medalTournamentOffseason2;
        /** @noinspection unused*/
        public int medalOskin;
        /** @noinspection unused*/
        public int invincible;
        /** @noinspection unused*/
        public int platinumClanRibbonNA;
        /** @noinspection unused*/
        public int platinumTwisterMedalRU;
        /** @noinspection unused*/
        public int newbieTrippleWin;
        /** @noinspection unused*/
        public int continentalViceChampion;
        /** @noinspection unused*/
        public int olimpicSilver;
        /** @noinspection unused*/
        public int markOfMasteryI;
        /** @noinspection unused*/
        public int continentalCompetitor;
        /** @noinspection unused*/
        public int newbieTigerWin;
        /** @noinspection unused*/
        public int medalTournamentSummerSeason;
        /** @noinspection unused*/
        public int mousebane;
        /** @noinspection unused*/
        public int medalBrunoPietro;
        /** @noinspection unused*/
        public int medalTournamentSpringSeason;
        /** @noinspection unused*/
        public int goldTwisterMark;
        /** @noinspection unused*/
        public int collectorWarhammer;
        /** @noinspection unused*/
        public int markOfMasteryIII;
        /** @noinspection unused*/
        public int medalLeClerc;
        /** @noinspection unused*/
        public int medalTournamentProfessional;
        /** @noinspection unused*/
        public int medalCommunityChampion;
        /** @noinspection unused*/
        public int diamondTwisterMark;
        /** @noinspection unused*/
        public int platinumTwisterMedalNA;
        /** @noinspection unused*/
        public int handOfDeath;
        /** @noinspection unused*/
        public int medalTournamentWinterSeason;
        /** @noinspection unused*/
        public int huntsman;
        /** @noinspection unused*/
        public int camper;
        /** @noinspection unused*/
        public int medalNikolas;
        /** @noinspection unused*/
        public int androidTest;
        /** @noinspection unused*/
        public int sturdy;
        /** @noinspection unused*/
        public int medalTwitch;
        /** @noinspection unused*/
        public int medalWGfestTicket;
        /** @noinspection unused*/
        public int championofNewStart;
        /** @noinspection unused*/
        public int medalTournamentAutumnSeason;
    }

    public static class DataObject {
        public AllDataObject allDataObject;
        public RatingDataObject ratingDataObject;
    }

    public static class AchievementData {
        public String achievement_id;
        public String section;
        public String name;
    }

    public static class DataObject_UserDamage {
        public long damage_received;
        public long damage_dealt;
    }

    public static class TankData {
        public int tier;
        public int tank_id;
        public String name;
        public String nation;
    }

    public static class ClanSearchData {
        public String tag;
        public String name;
        public int members_count;
        public long created_at;
        public int clan_id;
    }

    public static class ClanData {
        public ClanRecruitingOption recruiting_options;
        public int members_count;
        public String tag;
        public String name;
        public long created_at;
        public long updated_at;
        public String leader_name;
        public long[] members_ids;
        public String recruiting_policy;
        public String motto;
        public String description;
        public Map<Long, ClanMembers> members;
    }

    public static class ClanRecruitingOption {
        public int vehicles_level;
        public int wins_ratio;
        public int average_battles_per_day;
        public int battles;
        public int average_damage;
    }

    public static class ClanMembers {
        public String role;
        public String account_name;
    }

    public Map<String, AchievementData> getAchievementMap() {
        return achievementMap;
    }

}
