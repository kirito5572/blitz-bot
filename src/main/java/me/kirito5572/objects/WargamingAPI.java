package me.kirito5572.objects;

import com.google.gson.*;
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
    private final static Logger logger = LoggerFactory.getLogger(WargamingAPI.class);
    private final String token;
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

    public static void main(String[] args) {
        MySQLConnector mySQLConnector;      //TODO 실행전 주석처리, jar 빌드전 주석 해제!
        SQLITEConnector sqliteConnector = null;
        try {
            mySQLConnector = new MySQLConnector();
            sqliteConnector = new SQLITEConnector(mySQLConnector);
        } catch (ClassNotFoundException | SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
        WargamingAPI wargamingAPI = new WargamingAPI(sqliteConnector);
        DataObject dataObject = wargamingAPI.getUserPersonalData("2011403181");
        Gson gson = new Gson();
        String json = gson.toJson(dataObject);
        DataObject dataObject1 = gson.fromJson(json, DataObject.class);

    }

    public String[] tankIdBuilder() {
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

    public Map<Integer, DataObject_UserDamage> getUserAvgDamageData(String id) {
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

    public void tankDataParser() {
        //usa
        JsonElement element = JsonParser.parseString(usaTankData);
        JsonObject data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //ussr
        element = JsonParser.parseString(ussrTankData);
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //germany
        element = JsonParser.parseString(germanyTankData);
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //uk
        element = JsonParser.parseString(ukTankData);
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //japan
        element = JsonParser.parseString(japanTankData);
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //france
        element = JsonParser.parseString(franceTankData);
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //european
        element = JsonParser.parseString(europeanTankData);
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //china
        element = JsonParser.parseString(chinaTankData);
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
        //other
        element = JsonParser.parseString(otherTankData);
        data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            TankData tankData = new Gson().fromJson(object, TankData.class);
            tankDataMap.put(Integer.parseInt(value), tankData);
        }
    }

    public Map<String, AchievementData> AchievementDataParser() {
        JsonElement element = JsonParser.parseString(achievementJsonData);
        JsonObject data = element.getAsJsonObject().get("data").getAsJsonObject();
        for (String value : data.keySet()) {
            JsonObject object = data.get(value).getAsJsonObject();
            AchievementData achievementData = new AchievementData();
            achievementData.achievement_id = object.get("achievement_id").getAsString();

            achievementData.section = object.get("section").getAsString();
            achievementData.name = object.get("name").getAsString();
            achievementMap.put(value, achievementData);
        }
        return achievementMap;
    }

    public String getWargamingPlayer(String nickname) throws SQLException {
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

    public DataObject getUserPersonalData(String id, Date date) {
        DataObject dataObject = new DataObject();
        ResultSet resultSet = null;
        try {
            resultSet = wargamingConnector.Select_Query_Wargaming("SELECT * FROM `" + id + "` WHERE input_time = ?",
                    new int[]{wargamingConnector.STRING},
                    new String[]{ String.valueOf(date.getTime())});
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
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
                    dataObject = new Gson().fromJson(resultSet.getString("data"), DataObject.class);
                    if(date.getTime() == calendar.getTime().getTime()) {
                        //조회를 하려고 하는 날(date)이 오늘 인 경우
                        dataObject = getUserPersonalData(id);
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
                        ResultSet resultSet1 = wargamingConnector.Select_Query_Wargaming(
                                "SELECT input_time, ABS(input_time - `" + date.getTime() + "`) AS Distance " +
                                        "FROM `" + id + "` ORDER BY Distance LIMIT 1",
                                new int[]{}, new String[]{});
                        if(resultSet1.next()) {
                            //데이터가 존재할 경우
                            dataObject = new Gson().fromJson(resultSet.getString("data"), DataObject.class);
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

    public DataObject getUserPersonalData(String id) {
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
        }


        return dataObject;
    }

    public Achievement getUserAchievementData(String id) {
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

    public TournamentData[] getTournamentData() {
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
        }
        return data;
    }

    private JsonElement GET(String apiURL) throws IOException {
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

    public static class Achievement {
        public int armorPiercer;
        public int medalFadin;
        public int medalCarius;
        public int medalEkins;
        public int collectorGuP;
        public int medalHalonen;
        public int heroesOfRassenay;
        public int firstVictory;
        public int defender;
        public int creative;
        public int eSportFinal;
        public int supporter;
        public int goldClanRibbonSEA;
        public int platinumTwisterMedalSEA;
        public int medalLehvaslaiho;
        public int tankExpert;
        public int eSportQualification;
        public int MarkI;
        public int medalSupremacy;
        public int participantofWGFest2017;
        public int medalTournamentOffseason1;
        public int jointVictory;
        public int platinumClanRibbonRU;
        public int medalTournamentOffseason4;
        public int sniper;
        public int titleSniper;
        public int medalCrucialContribution;
        public int scout;
        public int goldTwisterMedalRU;
        public int tankExpert3;
        public int tankExpert2;
        public int tankExpert1;
        public int tankExpert0;
        public int markOfMastery;
        public int tankExpert6;
        public int tankExpert5;
        public int tankExpert4;
        public int goldTwisterMedalEU;
        public int ChristmasTreeLevelUpNY2019;
        public int medalLavrinenko;
        public int medalKolobanov;
        public int medalLafayettePool;
        public int goldClanRibbonEU;
        public int olimpicGolden;
        public int medalKnispel;
        public int invader;
        public int goldTwisterMedalNA;
        public int mechanicEngineer;
        public int markOfMasteryII;
        public int firstBlood;
        public int medalKay;
        public int medalOrlik;
        public int medalBrothersInArms;
        public int medalAbrams;
        public int medalAtgm;
        public int mainGun;
        public int ironMan;
        public int platinumClanRibbonEU;
        public int platinumClanRibbonSEA;
        public int warrior;
        public int goldClanRibbonRU;
        public int medalRadleyWalters;
        public int raider;
        public int participantofNewStart;
        public int diamondClanRibbon;
        public int medalBillotte;
        public int platinumTwisterMedalEU;
        public int diehard;
        public int masterofContinents;
        public int evileye;
        public int cadet;
        public int medalBlitzMasters;
        public int supremacyHunter;
        public int newbieT3485Win;
        public int continentalContender;
        public int steelwall;
        public int supremacyLegend;
        public int punisher;
        public int eSport;
        public int platinumTwisterMark;
        public int goldClanRibbonNA;
        public int medalPoppel;
        public int mechanicEngineer6;
        public int mechanicEngineer4;
        public int goldTwisterMedalSEA;
        public int mechanicEngineer2;
        public int mechanicEngineer3;
        public int mechanicEngineer0;
        public int mechanicEngineer1;
        public int mechanicEngineer5;
        public int medalTarczay;
        public int sinai;
        public int pattonValley;
        public int newbieDoubleWin;
        public int medalDeLanglade;
        public int diamondTwisterMedal;
        public int beasthunter;
        public int supremacyVeteran;
        public int newbieShermanWin;
        public int kamikaze;
        public int olimpicBronze;
        public int newbieType58TUWin;
        public int medalTournamentOffseason3;
        public int medalTournamentOffseason2;
        public int medalOskin;
        public int invincible;
        public int platinumClanRibbonNA;
        public int platinumTwisterMedalRU;
        public int newbieTrippleWin;
        public int continentalViceChampion;
        public int olimpicSilver;
        public int markOfMasteryI;
        public int continentalCompetitor;
        public int newbieTigerWin;
        public int medalTournamentSummerSeason;
        public int mousebane;
        public int medalBrunoPietro;
        public int medalTournamentSpringSeason;
        public int goldTwisterMark;
        public int collectorWarhammer;
        public int markOfMasteryIII;
        public int medalLeClerc;
        public int medalTournamentProfessional;
        public int medalCommunityChampion;
        public int diamondTwisterMark;
        public int platinumTwisterMedalNA;
        public int handOfDeath;
        public int medalTournamentWinterSeason;
        public int huntsman;
        public int camper;
        public int medalNikolas;
        public int androidTest;
        public int sturdy;
        public int medalTwitch;
        public int medalWGfestTicket;
        public int championofNewStart;
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

    public String achievementJsonData;
    {
        achievementJsonData = """
                {
                  "status": "ok",
                  "meta": {
                    "count": 137
                  },
                  "data": {
                    "armorPiercer": {
                      "achievement_id": "armorPiercer",
                      "section": "title",
                      "name": "사격의 달인 (armorPiercer)"
                    },
                    "medalFadin": {
                      "achievement_id": "medalFadin",
                      "section": "epic",
                      "name": "파딘 훈장 (medalFadin)"
                    },
                    "medalCarius": {
                      "achievement_id": "medalCarius",
                      "section": "step",
                      "name": "카리우스 훈장"
                    },
                    "medalEkins": {
                      "achievement_id": "medalEkins",
                      "section": "step",
                      "name": "에킨스 훈장"
                    },
                    "collectorGuP": {
                      "achievement_id": "collectorGuP",
                      "section": "commemorative",
                      "name": "GuP: 수집가 (collectorGuP)"
                    },
                    "medalHalonen": {
                      "achievement_id": "medalHalonen",
                      "section": "epic",
                      "name": "할로넨 훈장 (medalHalonen)"
                    },
                    "heroesOfRassenay": {
                      "achievement_id": "heroesOfRassenay",
                      "section": "epic",
                      "name": "라세이니 훈장 (heroesOfRassenay)"
                    },
                    "firstVictory": {
                      "achievement_id": "firstVictory",
                      "section": "commemorative",
                      "name": "첫 승리 (firstVictory)"
                    },
                    "defender": {
                      "achievement_id": "defender",
                      "section": "battle",
                      "name": "수비수 (defender)"
                    },
                    "creative": {
                      "achievement_id": "creative",
                      "section": "commemorative",
                      "name": "콘테스트 제패 (creative)"
                    },
                    "eSportFinal": {
                      "achievement_id": "eSportFinal",
                      "section": "commemorative",
                      "name": "블리츠 트위스터 컵 온라인 결선 진출 (eSportFinal)"
                    },
                    "supporter": {
                      "achievement_id": "supporter",
                      "section": "battle",
                      "name": "연맹 (supporter)"
                    },
                    "goldClanRibbonSEA": {
                      "achievement_id": "goldClanRibbonSEA",
                      "section": "commemorative",
                      "name": "시즌 골드 수장 (goldClanRibbonSEA)"
                    },
                    "platinumTwisterMedalSEA": {
                      "achievement_id": "platinumTwisterMedalSEA",
                      "section": "commemorative",
                      "name": "시즌 플래티넘 휘장 (platinumTwisterMedalSEA)"
                    },
                    "medalLehvaslaiho": {
                      "achievement_id": "medalLehvaslaiho",
                      "section": "epic",
                      "name": "레배슬라이호 훈장 (medalLehvaslaiho)"
                    },
                    "tankExpert": {
                      "achievement_id": "tankExpert",
                      "section": "title",
                      "name": "전문가 (tankExpert)"
                    },
                    "eSportQualification": {
                      "achievement_id": "eSportQualification",
                      "section": "commemorative",
                      "name": "블리츠 트위스터 컵 예선 참가 (eSportQualification)"
                    },
                    "MarkI": {
                      "achievement_id": "MarkI",
                      "section": "commemorative",
                      "name": "전차 100주년 (MarkI)"
                    },
                    "medalSupremacy": {
                      "achievement_id": "medalSupremacy",
                      "section": "step",
                      "name": "쟁탈전 훈장"
                    },
                    "participantofWGFest2017": {
                      "achievement_id": "participantofWGFest2017",
                      "section": "commemorative",
                      "name": "WG Fest 2017 참여 (participantofWGFest2017)"
                    },
                    "medalTournamentOffseason1": {
                      "achievement_id": "medalTournamentOffseason1",
                      "section": "commemorative",
                      "name": "오프 시즌 참여 (medalTournamentOffseason1)"
                    },
                    "jointVictory": {
                      "achievement_id": "jointVictory",
                      "section": "platoon",
                      "name": "소대전 승리"
                    },
                    "platinumClanRibbonRU": {
                      "achievement_id": "platinumClanRibbonRU",
                      "section": "commemorative",
                      "name": "시즌 플래티넘 수장 (platinumClanRibbonRU)"
                    },
                    "medalTournamentOffseason4": {
                      "achievement_id": "medalTournamentOffseason4",
                      "section": "commemorative",
                      "name": "오프 시즌 참여 (medalTournamentOffseason4)"
                    },
                    "sniper": {
                      "achievement_id": "sniper",
                      "section": "title",
                      "name": "저격수 (sniper)"
                    },
                    "titleSniper": {
                      "achievement_id": "titleSniper",
                      "section": "title",
                      "name": "명사수 (titleSniper)"
                    },
                    "medalCrucialContribution": {
                      "achievement_id": "medalCrucialContribution",
                      "section": "platoon",
                      "name": "최고 공헌자 (medalCrucialContribution)"
                    },
                    "scout": {
                      "achievement_id": "scout",
                      "section": "battle",
                      "name": "정찰병 (scout)"
                    },
                    "goldTwisterMedalRU": {
                      "achievement_id": "goldTwisterMedalRU",
                      "section": "commemorative",
                      "name": "시즌 골드 휘장 (goldTwisterMedalRU)"
                    },
                    "tankExpert3": {
                      "achievement_id": "tankExpert3",
                      "section": "title",
                      "name": "전문가: 중국 (tankExpert3)"
                    },
                    "tankExpert2": {
                      "achievement_id": "tankExpert2",
                      "section": "title",
                      "name": "전문가: 미국 (tankExpert2)"
                    },
                    "tankExpert1": {
                      "achievement_id": "tankExpert1",
                      "section": "title",
                      "name": "전문가: 독일 (tankExpert1)"
                    },
                    "tankExpert0": {
                      "achievement_id": "tankExpert0",
                      "section": "title",
                      "name": "전문가: 소련 (tankExpert0)"
                    },
                    "markOfMastery": {
                      "achievement_id": "markOfMastery",
                      "section": "battle",
                      "name": "숙련의 증표: 전차 에이스 (markOfMastery)"
                    },
                    "tankExpert6": {
                      "achievement_id": "tankExpert6",
                      "section": "title",
                      "name": "전문가: 일본 (tankExpert6)"
                    },
                    "tankExpert5": {
                      "achievement_id": "tankExpert5",
                      "section": "title",
                      "name": "전문가: 영국 (tankExpert5)"
                    },
                    "tankExpert4": {
                      "achievement_id": "tankExpert4",
                      "section": "title",
                      "name": "전문가: 프랑스 (tankExpert4)"
                    },
                    "goldTwisterMedalEU": {
                      "achievement_id": "goldTwisterMedalEU",
                      "section": "commemorative",
                      "name": "시즌 골드 휘장 (goldTwisterMedalEU)"
                    },
                    "ChristmasTreeLevelUpNY2019": {
                      "achievement_id": "ChristmasTreeLevelUpNY2019",
                      "section": "commemorative",
                      "name": "신년 나무 명예 장식자 (ChristmasTreeLevelUpNY2019)"
                    },
                    "medalLavrinenko": {
                      "achievement_id": "medalLavrinenko",
                      "section": "step",
                      "name": "라브리넨코 훈장"
                    },
                    "medalKolobanov": {
                      "achievement_id": "medalKolobanov",
                      "section": "epic",
                      "name": "콜로바노프 훈장 (medalKolobanov)"
                    },
                    "medalLafayettePool": {
                      "achievement_id": "medalLafayettePool",
                      "section": "epic",
                      "name": "풀 훈장 (medalLafayettePool)"
                    },
                    "goldClanRibbonEU": {
                      "achievement_id": "goldClanRibbonEU",
                      "section": "commemorative",
                      "name": "시즌 골드 수장 (goldClanRibbonEU)"
                    },
                    "olimpicGolden": {
                      "achievement_id": "olimpicGolden",
                      "section": "commemorative",
                      "name": "블리츠 게임즈: 금메달 (olimpicGolden)"
                    },
                    "medalKnispel": {
                      "achievement_id": "medalKnispel",
                      "section": "step",
                      "name": "크니스펠 훈장"
                    },
                    "invader": {
                      "achievement_id": "invader",
                      "section": "battle",
                      "name": "침략자 (invader)"
                    },
                    "goldTwisterMedalNA": {
                      "achievement_id": "goldTwisterMedalNA",
                      "section": "commemorative",
                      "name": "시즌 골드 휘장 (goldTwisterMedalNA)"
                    },
                    "mechanicEngineer": {
                      "achievement_id": "mechanicEngineer",
                      "section": "title",
                      "name": "수석 전차 개발자 (mechanicEngineer)"
                    },
                    "markOfMasteryII": {
                      "achievement_id": "markOfMasteryII",
                      "section": "battle",
                      "name": "숙련의 증표: 2급 (markOfMasteryII)"
                    },
                    "firstBlood": {
                      "achievement_id": "firstBlood",
                      "section": "commemorative",
                      "name": "첫 격파 (firstBlood)"
                    },
                    "medalKay": {
                      "achievement_id": "medalKay",
                      "section": "step",
                      "name": "케이 훈장"
                    },
                    "medalOrlik": {
                      "achievement_id": "medalOrlik",
                      "section": "epic",
                      "name": "오를리크 훈장 (medalOrlik)"
                    },
                    "medalBrothersInArms": {
                      "achievement_id": "medalBrothersInArms",
                      "section": "platoon",
                      "name": "전우애 (medalBrothersInArms)"
                    },
                    "medalAbrams": {
                      "achievement_id": "medalAbrams",
                      "section": "step",
                      "name": "에이브럼스 훈장"
                    },
                    "medalAtgm": {
                      "achievement_id": "medalAtgm",
                      "section": "commemorative",
                      "name": "미사일맨 (medalAtgm)"
                    },
                    "mainGun": {
                      "achievement_id": "mainGun",
                      "section": "battle",
                      "name": "능력자 (mainGun)"
                    },
                    "ironMan": {
                      "achievement_id": "ironMan",
                      "section": "commemorative",
                      "name": "냉철함 (ironMan)"
                    },
                    "platinumClanRibbonEU": {
                      "achievement_id": "platinumClanRibbonEU",
                      "section": "commemorative",
                      "name": "시즌 플래티넘 수장 (platinumClanRibbonEU)"
                    },
                    "platinumClanRibbonSEA": {
                      "achievement_id": "platinumClanRibbonSEA",
                      "section": "commemorative",
                      "name": "시즌 플래티넘 수장 (platinumClanRibbonSEA)"
                    },
                    "warrior": {
                      "achievement_id": "warrior",
                      "section": "battle",
                      "name": "탑건 (warrior)"
                    },
                    "goldClanRibbonRU": {
                      "achievement_id": "goldClanRibbonRU",
                      "section": "commemorative",
                      "name": "시즌 골드 수장 (goldClanRibbonRU)"
                    },
                    "medalRadleyWalters": {
                      "achievement_id": "medalRadleyWalters",
                      "section": "epic",
                      "name": "래들리-월터스 훈장 (medalRadleyWalters)"
                    },
                    "raider": {
                      "achievement_id": "raider",
                      "section": "title",
                      "name": "침입자 (raider)"
                    },
                    "participantofNewStart": {
                      "achievement_id": "participantofNewStart",
                      "section": "commemorative",
                      "name": "새로운 시작 참여 (participantofNewStart)"
                    },
                    "diamondClanRibbon": {
                      "achievement_id": "diamondClanRibbon",
                      "section": "commemorative",
                      "name": "시즌 다이아몬드 수장 (diamondClanRibbon)"
                    },
                    "medalBillotte": {
                      "achievement_id": "medalBillotte",
                      "section": "epic",
                      "name": "비요트 훈장 (medalBillotte)"
                    },
                    "platinumTwisterMedalEU": {
                      "achievement_id": "platinumTwisterMedalEU",
                      "section": "commemorative",
                      "name": "시즌 플래티넘 휘장 (platinumTwisterMedalEU)"
                    },
                    "diehard": {
                      "achievement_id": "diehard",
                      "section": "title",
                      "name": "생존자 (diehard)"
                    },
                    "masterofContinents": {
                      "achievement_id": "masterofContinents",
                      "section": "commemorative",
                      "name": "최강자 (masterofContinents)"
                    },
                    "evileye": {
                      "achievement_id": "evileye",
                      "section": "battle",
                      "name": "정찰 임무 (evileye)"
                    },
                    "cadet": {
                      "achievement_id": "cadet",
                      "section": "commemorative",
                      "name": "후보생 (cadet)"
                    },
                    "medalBlitzMasters": {
                      "achievement_id": "medalBlitzMasters",
                      "section": "commemorative",
                      "name": "블리츠 마스터 2022 참가자 (medalBlitzMasters)"
                    },
                    "supremacyHunter": {
                      "achievement_id": "supremacyHunter",
                      "section": "commemorative",
                      "name": "최고 격파왕 (supremacyHunter)"
                    },
                    "newbieT3485Win": {
                      "achievement_id": "newbieT3485Win",
                      "section": "commemorative",
                      "name": "T-34-85: 첫 승리 (newbieT3485Win)"
                    },
                    "continentalContender": {
                      "achievement_id": "continentalContender",
                      "section": "commemorative",
                      "name": "도전자 (continentalContender)"
                    },
                    "steelwall": {
                      "achievement_id": "steelwall",
                      "section": "battle",
                      "name": "철벽 (steelwall)"
                    },
                    "supremacyLegend": {
                      "achievement_id": "supremacyLegend",
                      "section": "commemorative",
                      "name": "최고 공격수 (supremacyLegend)"
                    },
                    "punisher": {
                      "achievement_id": "punisher",
                      "section": "platoon",
                      "name": "복수자"
                    },
                    "eSport": {
                      "achievement_id": "eSport",
                      "section": "commemorative",
                      "name": "블리츠 트위스터 컵의 영광 (eSport)"
                    },
                    "platinumTwisterMark": {
                      "achievement_id": "platinumTwisterMark",
                      "section": "commemorative",
                      "name": "시즌 플래티넘 훈장 (platinumTwisterMark)"
                    },
                    "goldClanRibbonNA": {
                      "achievement_id": "goldClanRibbonNA",
                      "section": "commemorative",
                      "name": "시즌 골드 수장 (goldClanRibbonNA)"
                    },
                    "medalPoppel": {
                      "achievement_id": "medalPoppel",
                      "section": "step",
                      "name": "포펠 훈장"
                    },
                    "mechanicEngineer6": {
                      "achievement_id": "mechanicEngineer6",
                      "section": "title",
                      "name": "전차 개발자: 일본 (mechanicEngineer6)"
                    },
                    "mechanicEngineer4": {
                      "achievement_id": "mechanicEngineer4",
                      "section": "title",
                      "name": "전차 개발자: 프랑스 (mechanicEngineer4)"
                    },
                    "goldTwisterMedalSEA": {
                      "achievement_id": "goldTwisterMedalSEA",
                      "section": "commemorative",
                      "name": "시즌 골드 휘장 (goldTwisterMedalSEA)"
                    },
                    "mechanicEngineer2": {
                      "achievement_id": "mechanicEngineer2",
                      "section": "title",
                      "name": "전차 개발자: 미국 (mechanicEngineer2)"
                    },
                    "mechanicEngineer3": {
                      "achievement_id": "mechanicEngineer3",
                      "section": "title",
                      "name": "전차 개발자: 중국 (mechanicEngineer3)"
                    },
                    "mechanicEngineer0": {
                      "achievement_id": "mechanicEngineer0",
                      "section": "title",
                      "name": "전차 개발자: 소련 (mechanicEngineer0)"
                    },
                    "mechanicEngineer1": {
                      "achievement_id": "mechanicEngineer1",
                      "section": "title",
                      "name": "전차 개발자: 독일 (mechanicEngineer1)"
                    },
                    "mechanicEngineer5": {
                      "achievement_id": "mechanicEngineer5",
                      "section": "title",
                      "name": "전차 개발자: 영국 (mechanicEngineer5)"
                    },
                    "medalTarczay": {
                      "achievement_id": "medalTarczay",
                      "section": "epic",
                      "name": "타르차이 훈장 (medalTarczay)"
                    },
                    "sinai": {
                      "achievement_id": "sinai",
                      "section": "title",
                      "name": "시나이 반도의 사자 (sinai)"
                    },
                    "pattonValley": {
                      "achievement_id": "pattonValley",
                      "section": "title",
                      "name": "패튼의 골짜기 (pattonValley)"
                    },
                    "newbieDoubleWin": {
                      "achievement_id": "newbieDoubleWin",
                      "section": "commemorative",
                      "name": "더블 (newbieDoubleWin)"
                    },
                    "medalDeLanglade": {
                      "achievement_id": "medalDeLanglade",
                      "section": "epic",
                      "name": "드 랑글라드 훈장 (medalDeLanglade)"
                    },
                    "diamondTwisterMedal": {
                      "achievement_id": "diamondTwisterMedal",
                      "section": "commemorative",
                      "name": "시즌 다이아몬드 휘장 (diamondTwisterMedal)"
                    },
                    "beasthunter": {
                      "achievement_id": "beasthunter",
                      "section": "title",
                      "name": "사냥꾼 (beasthunter)"
                    },
                    "supremacyVeteran": {
                      "achievement_id": "supremacyVeteran",
                      "section": "commemorative",
                      "name": "최고 전문가 (supremacyVeteran)"
                    },
                    "newbieShermanWin": {
                      "achievement_id": "newbieShermanWin",
                      "section": "commemorative",
                      "name": "Sherman Easy 8: 첫 승리 (newbieShermanWin)"
                    },
                    "kamikaze": {
                      "achievement_id": "kamikaze",
                      "section": "title",
                      "name": "카미카제 (kamikaze)"
                    },
                    "olimpicBronze": {
                      "achievement_id": "olimpicBronze",
                      "section": "commemorative",
                      "name": "블리츠 게임즈: 동메달 (olimpicBronze)"
                    },
                    "newbieType58TUWin": {
                      "achievement_id": "newbieType58TUWin",
                      "section": "commemorative",
                      "name": "Type 58: 첫 승리 (newbieType58TUWin)"
                    },
                    "medalTournamentOffseason3": {
                      "achievement_id": "medalTournamentOffseason3",
                      "section": "commemorative",
                      "name": "오프 시즌 참여 (medalTournamentOffseason3)"
                    },
                    "medalTournamentOffseason2": {
                      "achievement_id": "medalTournamentOffseason2",
                      "section": "commemorative",
                      "name": "오프 시즌 참여 (medalTournamentOffseason2)"
                    },
                    "medalOskin": {
                      "achievement_id": "medalOskin",
                      "section": "epic",
                      "name": "오스킨 훈장 (medalOskin)"
                    },
                    "invincible": {
                      "achievement_id": "invincible",
                      "section": "title",
                      "name": "천하무적 (invincible)"
                    },
                    "platinumClanRibbonNA": {
                      "achievement_id": "platinumClanRibbonNA",
                      "section": "commemorative",
                      "name": "시즌 플래티넘 수장 (platinumClanRibbonNA)"
                    },
                    "platinumTwisterMedalRU": {
                      "achievement_id": "platinumTwisterMedalRU",
                      "section": "commemorative",
                      "name": "시즌 플래티넘 휘장 (platinumTwisterMedalRU)"
                    },
                    "newbieTrippleWin": {
                      "achievement_id": "newbieTrippleWin",
                      "section": "commemorative",
                      "name": "트리플 (newbieTrippleWin)"
                    },
                    "continentalViceChampion": {
                      "achievement_id": "continentalViceChampion",
                      "section": "commemorative",
                      "name": "이인자 (continentalViceChampion)"
                    },
                    "olimpicSilver": {
                      "achievement_id": "olimpicSilver",
                      "section": "commemorative",
                      "name": "블리츠 게임즈: 은메달 (olimpicSilver)"
                    },
                    "markOfMasteryI": {
                      "achievement_id": "markOfMasteryI",
                      "section": "battle",
                      "name": "숙련의 증표: 1급 (markOfMasteryI)"
                    },
                    "continentalCompetitor": {
                      "achievement_id": "continentalCompetitor",
                      "section": "commemorative",
                      "name": "참가자 (continentalCompetitor)"
                    },
                    "newbieTigerWin": {
                      "achievement_id": "newbieTigerWin",
                      "section": "commemorative",
                      "name": "Tiger I: 첫 승리 (newbieTigerWin)"
                    },
                    "medalTournamentSummerSeason": {
                      "achievement_id": "medalTournamentSummerSeason",
                      "section": "commemorative",
                      "name": "여름 시즌 참여자 (medalTournamentSummerSeason)"
                    },
                    "mousebane": {
                      "achievement_id": "mousebane",
                      "section": "title",
                      "name": "쥐사냥꾼 (mousebane)"
                    },
                    "medalBrunoPietro": {
                      "achievement_id": "medalBrunoPietro",
                      "section": "epic",
                      "name": "브루노 훈장 (medalBrunoPietro)"
                    },
                    "medalTournamentSpringSeason": {
                      "achievement_id": "medalTournamentSpringSeason",
                      "section": "commemorative",
                      "name": "봄 시즌 참여자 (medalTournamentSpringSeason)"
                    },
                    "goldTwisterMark": {
                      "achievement_id": "goldTwisterMark",
                      "section": "commemorative",
                      "name": "시즌 골드 훈장 (goldTwisterMark)"
                    },
                    "collectorWarhammer": {
                      "achievement_id": "collectorWarhammer",
                      "section": "commemorative",
                      "name": "Warhammer 40,000: 수집가 (collectorWarhammer)"
                    },
                    "markOfMasteryIII": {
                      "achievement_id": "markOfMasteryIII",
                      "section": "battle",
                      "name": "숙련의 증표: 3급 (markOfMasteryIII)"
                    },
                    "medalLeClerc": {
                      "achievement_id": "medalLeClerc",
                      "section": "step",
                      "name": "르클레르 훈장"
                    },
                    "medalTournamentProfessional": {
                      "achievement_id": "medalTournamentProfessional",
                      "section": "commemorative",
                      "name": "프로페셔널 (medalTournamentProfessional)"
                    },
                    "medalCommunityChampion": {
                      "achievement_id": "medalCommunityChampion",
                      "section": "commemorative",
                      "name": "커뮤니티 챔피언 (medalCommunityChampion)"
                    },
                    "diamondTwisterMark": {
                      "achievement_id": "diamondTwisterMark",
                      "section": "commemorative",
                      "name": "시즌 다이아몬드 훈장 (diamondTwisterMark)"
                    },
                    "platinumTwisterMedalNA": {
                      "achievement_id": "platinumTwisterMedalNA",
                      "section": "commemorative",
                      "name": "시즌 플래티넘 휘장 (platinumTwisterMedalNA)"
                    },
                    "handOfDeath": {
                      "achievement_id": "handOfDeath",
                      "section": "title",
                      "name": "사신 (handOfDeath)"
                    },
                    "medalTournamentWinterSeason": {
                      "achievement_id": "medalTournamentWinterSeason",
                      "section": "commemorative",
                      "name": "겨울 시즌 참여자 (medalTournamentWinterSeason)"
                    },
                    "huntsman": {
                      "achievement_id": "huntsman",
                      "section": "commemorative",
                      "name": "수색대 (huntsman)"
                    },
                    "camper": {
                      "achievement_id": "camper",
                      "section": "battle",
                      "name": "전차 저격수 (camper)"
                    },
                    "medalNikolas": {
                      "achievement_id": "medalNikolas",
                      "section": "epic",
                      "name": "니콜스 훈장 (medalNikolas)"
                    },
                    "androidTest": {
                      "achievement_id": "androidTest",
                      "section": "commemorative",
                      "name": "안드로이드에서 블리츠 테스트 (androidTest)"
                    },
                    "sturdy": {
                      "achievement_id": "sturdy",
                      "section": "commemorative",
                      "name": "스파르타인 (sturdy)"
                    },
                    "medalTwitch": {
                      "achievement_id": "medalTwitch",
                      "section": "commemorative",
                      "name": "Twitch 투사 (medalTwitch)"
                    },
                    "medalWGfestTicket": {
                      "achievement_id": "medalWGfestTicket",
                      "section": "commemorative",
                      "name": "WG Fest 2018 참여 (medalWGfestTicket)"
                    },
                    "championofNewStart": {
                      "achievement_id": "championofNewStart",
                      "section": "commemorative",
                      "name": "새로운 시작 챔피언 (championofNewStart)"
                    },
                    "medalTournamentAutumnSeason": {
                      "achievement_id": "medalTournamentAutumnSeason",
                      "section": "commemorative",
                      "name": "가을 시즌 참여자 (medalTournamentAutumnSeason)"
                    }
                  }
                }""";
    }

    public String usaTankData;
    {
        usaTankData = """
                {
                  "status": "ok",
                  "meta": {
                    "count": 90
                  },
                  "data": {
                    "33": {
                      "tier": 5,
                      "tank_id": 33,
                      "name": "T14",
                      "nation": "usa"
                    },
                    "289": {
                      "tier": 2,
                      "tank_id": 289,
                      "name": "M3 Stuart",
                      "nation": "usa"
                    },
                    "801": {
                      "tier": 6,
                      "tank_id": 801,
                      "name": "M6",
                      "nation": "usa"
                    },
                    "1057": {
                      "tier": 5,
                      "tank_id": 1057,
                      "name": "M4 Sherman",
                      "nation": "usa"
                    },
                    "1313": {
                      "tier": 6,
                      "tank_id": 1313,
                      "name": "M4A3E8 Sherman",
                      "nation": "usa"
                    },
                    "1569": {
                      "tier": 7,
                      "tank_id": 1569,
                      "name": "T20",
                      "nation": "usa"
                    },
                    "1825": {
                      "tier": 1,
                      "tank_id": 1825,
                      "name": "M2 Light Tank",
                      "nation": "usa"
                    },
                    "2593": {
                      "tier": 9,
                      "tank_id": 2593,
                      "name": "T30",
                      "nation": "usa"
                    },
                    "2849": {
                      "tier": 8,
                      "tank_id": 2849,
                      "name": "T34",
                      "nation": "usa"
                    },
                    "3105": {
                      "tier": 4,
                      "tank_id": 3105,
                      "name": "M3 Lee",
                      "nation": "usa"
                    },
                    "3361": {
                      "tier": 5,
                      "tank_id": 3361,
                      "name": "T1 Heavy Tank",
                      "nation": "usa"
                    },
                    "3873": {
                      "tier": 7,
                      "tank_id": 3873,
                      "name": "T29",
                      "nation": "usa"
                    },
                    "4385": {
                      "tier": 8,
                      "tank_id": 4385,
                      "name": "T32",
                      "nation": "usa"
                    },
                    "4897": {
                      "tier": 3,
                      "tank_id": 4897,
                      "name": "M2 Medium Tank",
                      "nation": "usa"
                    },
                    "5153": {
                      "tier": 3,
                      "tank_id": 5153,
                      "name": "M5 Stuart",
                      "nation": "usa"
                    },
                    "5409": {
                      "tier": 4,
                      "tank_id": 5409,
                      "name": "M7",
                      "nation": "usa"
                    },
                    "5665": {
                      "tier": 2,
                      "tank_id": 5665,
                      "name": "T2 Medium Tank",
                      "nation": "usa"
                    },
                    "5921": {
                      "tier": 8,
                      "tank_id": 5921,
                      "name": "M26 Pershing",
                      "nation": "usa"
                    },
                    "6177": {
                      "tier": 2,
                      "tank_id": 6177,
                      "name": "T18",
                      "nation": "usa"
                    },
                    "6433": {
                      "tier": 3,
                      "tank_id": 6433,
                      "name": "T82",
                      "nation": "usa"
                    },
                    "6689": {
                      "tier": 7,
                      "tank_id": 6689,
                      "name": "T49 ATM",
                      "nation": "usa"
                    },
                    "6945": {
                      "tier": 5,
                      "tank_id": 6945,
                      "name": "M10 Wolverine",
                      "nation": "usa"
                    },
                    "7201": {
                      "tier": 6,
                      "tank_id": 7201,
                      "name": "M36 Jackson",
                      "nation": "usa"
                    },
                    "7713": {
                      "tier": 4,
                      "tank_id": 7713,
                      "name": "T40",
                      "nation": "usa"
                    },
                    "8225": {
                      "tier": 8,
                      "tank_id": 8225,
                      "name": "T28",
                      "nation": "usa"
                    },
                    "8737": {
                      "tier": 9,
                      "tank_id": 8737,
                      "name": "T95",
                      "nation": "usa"
                    },
                    "8993": {
                      "tier": 9,
                      "tank_id": 8993,
                      "name": "M46 Patton",
                      "nation": "usa"
                    },
                    "9249": {
                      "tier": 7,
                      "tank_id": 9249,
                      "name": "T25 AT",
                      "nation": "usa"
                    },
                    "9505": {
                      "tier": 9,
                      "tank_id": 9505,
                      "name": "M103",
                      "nation": "usa"
                    },
                    "9761": {
                      "tier": 6,
                      "tank_id": 9761,
                      "name": "M24 Chaffee",
                      "nation": "usa"
                    },
                    "10017": {
                      "tier": 6,
                      "tank_id": 10017,
                      "name": "M4A3E2 Sherman Jumbo",
                      "nation": "usa"
                    },
                    "10273": {
                      "tier": 4,
                      "tank_id": 10273,
                      "name": "M8A1",
                      "nation": "usa"
                    },
                    "10529": {
                      "tier": 5,
                      "tank_id": 10529,
                      "name": "T67",
                      "nation": "usa"
                    },
                    "10785": {
                      "tier": 10,
                      "tank_id": 10785,
                      "name": "T110E5",
                      "nation": "usa"
                    },
                    "11041": {
                      "tier": 7,
                      "tank_id": 11041,
                      "name": "T25/2",
                      "nation": "usa"
                    },
                    "11297": {
                      "tier": 8,
                      "tank_id": 11297,
                      "name": "T28 Prototype",
                      "nation": "usa"
                    },
                    "11553": {
                      "tier": 6,
                      "tank_id": 11553,
                      "name": "M18 Hellcat",
                      "nation": "usa"
                    },
                    "11809": {
                      "tier": 7,
                      "tank_id": 11809,
                      "name": "T23E3",
                      "nation": "usa"
                    },
                    "12065": {
                      "tier": 8,
                      "tank_id": 12065,
                      "name": "T95E2",
                      "nation": "usa"
                    },
                    "12321": {
                      "tier": 4,
                      "tank_id": 12321,
                      "name": "T6E1 Grizzly",
                      "nation": "usa"
                    },
                    "13089": {
                      "tier": 10,
                      "tank_id": 13089,
                      "name": "T110E4",
                      "nation": "usa"
                    },
                    "13345": {
                      "tier": 8,
                      "tank_id": 13345,
                      "name": "T26E4 SuperPershing",
                      "nation": "usa"
                    },
                    "13857": {
                      "tier": 10,
                      "tank_id": 13857,
                      "name": "T110E3",
                      "nation": "usa"
                    },
                    "14113": {
                      "tier": 10,
                      "tank_id": 14113,
                      "name": "M48 Patton",
                      "nation": "usa"
                    },
                    "14625": {
                      "tier": 8,
                      "tank_id": 14625,
                      "name": "T69",
                      "nation": "usa"
                    },
                    "14881": {
                      "tier": 10,
                      "tank_id": 14881,
                      "name": "T57 Heavy Tank",
                      "nation": "usa"
                    },
                    "15137": {
                      "tier": 6,
                      "tank_id": 15137,
                      "name": "T21",
                      "nation": "usa"
                    },
                    "15393": {
                      "tier": 9,
                      "tank_id": 15393,
                      "name": "T54E1",
                      "nation": "usa"
                    },
                    "15649": {
                      "tier": 7,
                      "tank_id": 15649,
                      "name": "T71",
                      "nation": "usa"
                    },
                    "15905": {
                      "tier": 10,
                      "tank_id": 15905,
                      "name": "M60",
                      "nation": "usa"
                    },
                    "16673": {
                      "tier": 6,
                      "tank_id": 16673,
                      "name": "T37",
                      "nation": "usa"
                    },
                    "17953": {
                      "tier": 7,
                      "tank_id": 17953,
                      "name": "M41 Walker Bulldog",
                      "nation": "usa"
                    },
                    "18209": {
                      "tier": 8,
                      "tank_id": 18209,
                      "name": "T49",
                      "nation": "usa"
                    },
                    "18977": {
                      "tier": 10,
                      "tank_id": 18977,
                      "name": "T95E6",
                      "nation": "usa"
                    },
                    "19233": {
                      "tier": 8,
                      "tank_id": 19233,
                      "name": "Chrysler K",
                      "nation": "usa"
                    },
                    "19489": {
                      "tier": 8,
                      "tank_id": 19489,
                      "name": "T28 Defender",
                      "nation": "usa"
                    },
                    "19745": {
                      "tier": 8,
                      "tank_id": 19745,
                      "name": "T26E5",
                      "nation": "usa"
                    },
                    "20001": {
                      "tier": 9,
                      "tank_id": 20001,
                      "name": "T92E1",
                      "nation": "usa"
                    },
                    "20257": {
                      "tier": 10,
                      "tank_id": 20257,
                      "name": "XM551 Sheridan",
                      "nation": "usa"
                    },
                    "20513": {
                      "tier": 8,
                      "tank_id": 20513,
                      "name": "T54E2",
                      "nation": "usa"
                    },
                    "20769": {
                      "tier": 8,
                      "tank_id": 20769,
                      "name": "T25 Pilot Number 1",
                      "nation": "usa"
                    },
                    "21025": {
                      "tier": 7,
                      "tank_id": 21025,
                      "name": "T26E3 Eagle 7",
                      "nation": "usa"
                    },
                    "21281": {
                      "tier": 6,
                      "tank_id": 21281,
                      "name": "Rudolph",
                      "nation": "usa"
                    },
                    "21793": {
                      "tier": 10,
                      "tank_id": 21793,
                      "name": "XM551 Sheridan Missile",
                      "nation": "usa"
                    },
                    "22049": {
                      "tier": 6,
                      "tank_id": 22049,
                      "name": "Magnus",
                      "nation": "usa"
                    },
                    "22305": {
                      "tier": 9,
                      "tank_id": 22305,
                      "name": "AE Phase I",
                      "nation": "usa"
                    },
                    "22561": {
                      "tier": 8,
                      "tank_id": 22561,
                      "name": "TS-5",
                      "nation": "usa"
                    },
                    "22817": {
                      "tier": 10,
                      "tank_id": 22817,
                      "name": "M-VI-Yoh",
                      "nation": "usa"
                    },
                    "23073": {
                      "tier": 9,
                      "tank_id": 23073,
                      "name": "M-V-Yoh",
                      "nation": "usa"
                    },
                    "23329": {
                      "tier": 8,
                      "tank_id": 23329,
                      "name": "M-III-Yoh",
                      "nation": "usa"
                    },
                    "23585": {
                      "tier": 7,
                      "tank_id": 23585,
                      "name": "M-VII-Yoh",
                      "nation": "usa"
                    },
                    "23841": {
                      "tier": 7,
                      "tank_id": 23841,
                      "name": "Super Hellcat",
                      "nation": "usa"
                    },
                    "24097": {
                      "tier": 6,
                      "tank_id": 24097,
                      "name": "BLTZ9000",
                      "nation": "usa"
                    },
                    "24609": {
                      "tier": 10,
                      "tank_id": 24609,
                      "name": "Concept 1B",
                      "nation": "usa"
                    },
                    "24865": {
                      "tier": 8,
                      "tank_id": 24865,
                      "name": "Scepter",
                      "nation": "usa"
                    },
                    "25377": {
                      "tier": 8,
                      "tank_id": 25377,
                      "name": "T77",
                      "nation": "usa"
                    },
                    "25889": {
                      "tier": 6,
                      "tank_id": 25889,
                      "name": "Ranger",
                      "nation": "usa"
                    },
                    "26145": {
                      "tier": 5,
                      "tank_id": 26145,
                      "name": "High Score",
                      "nation": "usa"
                    },
                    "26401": {
                      "tier": 6,
                      "tank_id": 26401,
                      "name": "Enforcer",
                      "nation": "usa"
                    },
                    "51489": {
                      "tier": 2,
                      "tank_id": 51489,
                      "name": "T2 Light Tank",
                      "nation": "usa"
                    },
                    "51745": {
                      "tier": 5,
                      "tank_id": 51745,
                      "name": "Ram II",
                      "nation": "usa"
                    },
                    "52257": {
                      "tier": 5,
                      "tank_id": 52257,
                      "name": "M4A2E4 Sherman",
                      "nation": "usa"
                    },
                    "52513": {
                      "tier": 7,
                      "tank_id": 52513,
                      "name": "M6A2E1",
                      "nation": "usa"
                    },
                    "52769": {
                      "tier": 3,
                      "tank_id": 52769,
                      "name": "M22 Locust",
                      "nation": "usa"
                    },
                    "53025": {
                      "tier": 8,
                      "tank_id": 53025,
                      "name": "M6A2E1 EXP",
                      "nation": "usa"
                    },
                    "53537": {
                      "tier": 2,
                      "tank_id": 53537,
                      "name": "T1E6",
                      "nation": "usa"
                    },
                    "55073": {
                      "tier": 2,
                      "tank_id": 55073,
                      "name": "T7 Combat Car",
                      "nation": "usa"
                    },
                    "56097": {
                      "tier": 6,
                      "tank_id": 56097,
                      "name": "M4A3E8 Fury",
                      "nation": "usa"
                    },
                    "56609": {
                      "tier": 7,
                      "tank_id": 56609,
                      "name": "T28 Concept",
                      "nation": "usa"
                    },
                    "64801": {
                      "tier": 8,
                      "tank_id": 64801,
                      "name": "T34 (1776)",
                      "nation": "usa"
                    }
                  }
                }
                """;
    }

    public String ussrTankData;
    {
        ussrTankData = """
                {
                  "status": "ok",
                  "meta": {
                    "count": 92
                  },
                  "data": {
                    "1": {
                      "tier": 5,
                      "tank_id": 1,
                      "name": "T-34",
                      "nation": "ussr"
                    },
                    "257": {
                      "tier": 5,
                      "tank_id": 257,
                      "name": "SU-85",
                      "nation": "ussr"
                    },
                    "513": {
                      "tier": 7,
                      "tank_id": 513,
                      "name": "IS",
                      "nation": "ussr"
                    },
                    "769": {
                      "tier": 3,
                      "tank_id": 769,
                      "name": "BT-7",
                      "nation": "ussr"
                    },
                    "1025": {
                      "tier": 2,
                      "tank_id": 1025,
                      "name": "BT-2",
                      "nation": "ussr"
                    },
                    "1537": {
                      "tier": 4,
                      "tank_id": 1537,
                      "name": "T-28 mod. 1940",
                      "nation": "ussr"
                    },
                    "2049": {
                      "tier": 4,
                      "tank_id": 2049,
                      "name": "A-20",
                      "nation": "ussr"
                    },
                    "2305": {
                      "tier": 7,
                      "tank_id": 2305,
                      "name": "SU-152",
                      "nation": "ussr"
                    },
                    "2561": {
                      "tier": 6,
                      "tank_id": 2561,
                      "name": "T-34-85",
                      "nation": "ussr"
                    },
                    "2817": {
                      "tier": 6,
                      "tank_id": 2817,
                      "name": "KV-1S",
                      "nation": "ussr"
                    },
                    "3073": {
                      "tier": 3,
                      "tank_id": 3073,
                      "name": "T-46",
                      "nation": "ussr"
                    },
                    "3329": {
                      "tier": 1,
                      "tank_id": 3329,
                      "name": "MS-1 mod. 1",
                      "nation": "ussr"
                    },
                    "3585": {
                      "tier": 6,
                      "tank_id": 3585,
                      "name": "SU-100",
                      "nation": "ussr"
                    },
                    "4353": {
                      "tier": 8,
                      "tank_id": 4353,
                      "name": "T-44",
                      "nation": "ussr"
                    },
                    "4609": {
                      "tier": 1,
                      "tank_id": 4609,
                      "name": "T-26",
                      "nation": "ussr"
                    },
                    "5121": {
                      "tier": 2,
                      "tank_id": 5121,
                      "name": "AT-1",
                      "nation": "ussr"
                    },
                    "5377": {
                      "tier": 8,
                      "tank_id": 5377,
                      "name": "IS-3",
                      "nation": "ussr"
                    },
                    "5889": {
                      "tier": 7,
                      "tank_id": 5889,
                      "name": "KV-3",
                      "nation": "ussr"
                    },
                    "6145": {
                      "tier": 10,
                      "tank_id": 6145,
                      "name": "IS-4",
                      "nation": "ussr"
                    },
                    "6401": {
                      "tier": 3,
                      "tank_id": 6401,
                      "name": "SU-76",
                      "nation": "ussr"
                    },
                    "6657": {
                      "tier": 7,
                      "tank_id": 6657,
                      "name": "T-43",
                      "nation": "ussr"
                    },
                    "6913": {
                      "tier": 4,
                      "tank_id": 6913,
                      "name": "SU-85B",
                      "nation": "ussr"
                    },
                    "7425": {
                      "tier": 8,
                      "tank_id": 7425,
                      "name": "ISU-152",
                      "nation": "ussr"
                    },
                    "7937": {
                      "tier": 9,
                      "tank_id": 7937,
                      "name": "T-54",
                      "nation": "ussr"
                    },
                    "8193": {
                      "tier": 9,
                      "tank_id": 8193,
                      "name": "Object 704",
                      "nation": "ussr"
                    },
                    "8961": {
                      "tier": 7,
                      "tank_id": 8961,
                      "name": "KV-13",
                      "nation": "ussr"
                    },
                    "9217": {
                      "tier": 8,
                      "tank_id": 9217,
                      "name": "IS-6",
                      "nation": "ussr"
                    },
                    "9985": {
                      "tier": 8,
                      "tank_id": 9985,
                      "name": "SU-101",
                      "nation": "ussr"
                    },
                    "10241": {
                      "tier": 7,
                      "tank_id": 10241,
                      "name": "SU-100M1",
                      "nation": "ussr"
                    },
                    "10497": {
                      "tier": 6,
                      "tank_id": 10497,
                      "name": "KV-2",
                      "nation": "ussr"
                    },
                    "10753": {
                      "tier": 9,
                      "tank_id": 10753,
                      "name": "ST-I",
                      "nation": "ussr"
                    },
                    "11009": {
                      "tier": 8,
                      "tank_id": 11009,
                      "name": "KV-4",
                      "nation": "ussr"
                    },
                    "11265": {
                      "tier": 6,
                      "tank_id": 11265,
                      "name": "T-150",
                      "nation": "ussr"
                    },
                    "11521": {
                      "tier": 9,
                      "tank_id": 11521,
                      "name": "IS-8",
                      "nation": "ussr"
                    },
                    "11777": {
                      "tier": 5,
                      "tank_id": 11777,
                      "name": "KV-1",
                      "nation": "ussr"
                    },
                    "12033": {
                      "tier": 9,
                      "tank_id": 12033,
                      "name": "SU-122-54",
                      "nation": "ussr"
                    },
                    "12545": {
                      "tier": 9,
                      "tank_id": 12545,
                      "name": "K-91",
                      "nation": "ussr"
                    },
                    "13569": {
                      "tier": 10,
                      "tank_id": 13569,
                      "name": "Object 268",
                      "nation": "ussr"
                    },
                    "13825": {
                      "tier": 10,
                      "tank_id": 13825,
                      "name": "T-62A",
                      "nation": "ussr"
                    },
                    "14337": {
                      "tier": 10,
                      "tank_id": 14337,
                      "name": "Object 263",
                      "nation": "ussr"
                    },
                    "15617": {
                      "tier": 10,
                      "tank_id": 15617,
                      "name": "Object 907",
                      "nation": "ussr"
                    },
                    "16641": {
                      "tier": 6,
                      "tank_id": 16641,
                      "name": "MT-25",
                      "nation": "ussr"
                    },
                    "16897": {
                      "tier": 10,
                      "tank_id": 16897,
                      "name": "Object 140",
                      "nation": "ussr"
                    },
                    "18177": {
                      "tier": 8,
                      "tank_id": 18177,
                      "name": "T-54 ltwt.",
                      "nation": "ussr"
                    },
                    "18433": {
                      "tier": 7,
                      "tank_id": 18433,
                      "name": "LTTB",
                      "nation": "ussr"
                    },
                    "18689": {
                      "tier": 3,
                      "tank_id": 18689,
                      "name": "T-70/57",
                      "nation": "ussr"
                    },
                    "18945": {
                      "tier": 8,
                      "tank_id": 18945,
                      "name": "ISU-130",
                      "nation": "ussr"
                    },
                    "19713": {
                      "tier": 6,
                      "tank_id": 19713,
                      "name": "Loza's M4-A2 Sherman",
                      "nation": "ussr"
                    },
                    "19969": {
                      "tier": 10,
                      "tank_id": 19969,
                      "name": "T-22 medium",
                      "nation": "ussr"
                    },
                    "20481": {
                      "tier": 8,
                      "tank_id": 20481,
                      "name": "Object 252U",
                      "nation": "ussr"
                    },
                    "20737": {
                      "tier": 8,
                      "tank_id": 20737,
                      "name": "SU-130PM",
                      "nation": "ussr"
                    },
                    "20993": {
                      "tier": 8,
                      "tank_id": 20993,
                      "name": "T-2020",
                      "nation": "ussr"
                    },
                    "21249": {
                      "tier": 6,
                      "tank_id": 21249,
                      "name": "Thunder",
                      "nation": "ussr"
                    },
                    "21505": {
                      "tier": 7,
                      "tank_id": 21505,
                      "name": "T-44-85",
                      "nation": "ussr"
                    },
                    "21761": {
                      "tier": 8,
                      "tank_id": 21761,
                      "name": "STG",
                      "nation": "ussr"
                    },
                    "22273": {
                      "tier": 10,
                      "tank_id": 22273,
                      "name": "Object 260",
                      "nation": "ussr"
                    },
                    "22529": {
                      "tier": 8,
                      "tank_id": 22529,
                      "name": "LT-432",
                      "nation": "ussr"
                    },
                    "22785": {
                      "tier": 6,
                      "tank_id": 22785,
                      "name": "Triumphant",
                      "nation": "ussr"
                    },
                    "23041": {
                      "tier": 5,
                      "tank_id": 23041,
                      "name": "T-34 shielded",
                      "nation": "ussr"
                    },
                    "23553": {
                      "tier": 2,
                      "tank_id": 23553,
                      "name": "MS-1",
                      "nation": "ussr"
                    },
                    "23809": {
                      "tier": 9,
                      "tank_id": 23809,
                      "name": "Object 84",
                      "nation": "ussr"
                    },
                    "24065": {
                      "tier": 7,
                      "tank_id": 24065,
                      "name": "LTG",
                      "nation": "ussr"
                    },
                    "24321": {
                      "tier": 10,
                      "tank_id": 24321,
                      "name": "T-100 LT",
                      "nation": "ussr"
                    },
                    "24577": {
                      "tier": 10,
                      "tank_id": 24577,
                      "name": "Object 268 Version 4",
                      "nation": "ussr"
                    },
                    "25089": {
                      "tier": 9,
                      "tank_id": 25089,
                      "name": "Object 752",
                      "nation": "ussr"
                    },
                    "25345": {
                      "tier": 8,
                      "tank_id": 25345,
                      "name": "Object 274a",
                      "nation": "ussr"
                    },
                    "51201": {
                      "tier": 5,
                      "tank_id": 51201,
                      "name": "KV-220 Beta-Test",
                      "nation": "ussr"
                    },
                    "51457": {
                      "tier": 5,
                      "tank_id": 51457,
                      "name": "Matilda IV",
                      "nation": "ussr"
                    },
                    "51713": {
                      "tier": 5,
                      "tank_id": 51713,
                      "name": "Churchill III",
                      "nation": "ussr"
                    },
                    "52225": {
                      "tier": 3,
                      "tank_id": 52225,
                      "name": "BT-SV",
                      "nation": "ussr"
                    },
                    "52481": {
                      "tier": 4,
                      "tank_id": 52481,
                      "name": "Valentine II",
                      "nation": "ussr"
                    },
                    "52737": {
                      "tier": 3,
                      "tank_id": 52737,
                      "name": "M3 Light",
                      "nation": "ussr"
                    },
                    "52993": {
                      "tier": 4,
                      "tank_id": 52993,
                      "name": "A-32",
                      "nation": "ussr"
                    },
                    "53249": {
                      "tier": 8,
                      "tank_id": 53249,
                      "name": "KV-5",
                      "nation": "ussr"
                    },
                    "53505": {
                      "tier": 3,
                      "tank_id": 53505,
                      "name": "T-127",
                      "nation": "ussr"
                    },
                    "53761": {
                      "tier": 5,
                      "tank_id": 53761,
                      "name": "SU-85I",
                      "nation": "ussr"
                    },
                    "54273": {
                      "tier": 3,
                      "tank_id": 54273,
                      "name": "SU-76I",
                      "nation": "ussr"
                    },
                    "54529": {
                      "tier": 2,
                      "tank_id": 54529,
                      "name": "Tetrarch",
                      "nation": "ussr"
                    },
                    "54785": {
                      "tier": 6,
                      "tank_id": 54785,
                      "name": "SU-100Y",
                      "nation": "ussr"
                    },
                    "55297": {
                      "tier": 7,
                      "tank_id": 55297,
                      "name": "SU-122-44",
                      "nation": "ussr"
                    },
                    "56577": {
                      "tier": 3,
                      "tank_id": 56577,
                      "name": "LTP",
                      "nation": "ussr"
                    },
                    "58881": {
                      "tier": 8,
                      "tank_id": 58881,
                      "name": "IS-5 (Object 730)",
                      "nation": "ussr"
                    },
                    "59137": {
                      "tier": 7,
                      "tank_id": 59137,
                      "name": "IS-2 (1945)",
                      "nation": "ussr"
                    },
                    "59649": {
                      "tier": 7,
                      "tank_id": 59649,
                      "name": "ISU-122S",
                      "nation": "ussr"
                    },
                    "59905": {
                      "tier": 8,
                      "tank_id": 59905,
                      "name": "T-54 first prototype",
                      "nation": "ussr"
                    },
                    "60161": {
                      "tier": 8,
                      "tank_id": 60161,
                      "name": "IS-2Sh",
                      "nation": "ussr"
                    },
                    "60417": {
                      "tier": 8,
                      "tank_id": 60417,
                      "name": "IS-3 Defender",
                      "nation": "ussr"
                    },
                    "60929": {
                      "tier": 3,
                      "tank_id": 60929,
                      "name": "BT-7 artillery",
                      "nation": "ussr"
                    },
                    "62977": {
                      "tier": 8,
                      "tank_id": 62977,
                      "name": "T-44-100",
                      "nation": "ussr"
                    },
                    "64001": {
                      "tier": 7,
                      "tank_id": 64001,
                      "name": "T-34-85 Rudy",
                      "nation": "ussr"
                    },
                    "64257": {
                      "tier": 6,
                      "tank_id": 64257,
                      "name": "T-34-85 Victory",
                      "nation": "ussr"
                    },
                    "64769": {
                      "tier": 8,
                      "tank_id": 64769,
                      "name": "IS-6 Fearless",
                      "nation": "ussr"
                    }
                  }
                }
                """;
    }

    public String germanyTankData;
    {
        germanyTankData = """
                {
                  "status": "ok",
                  "meta": {
                    "count": 96
                  },
                  "data": {
                    "17": {
                      "tier": 5,
                      "tank_id": 17,
                      "name": "Pz.Kpfw. IV Ausf. G",
                      "nation": "germany"
                    },
                    "529": {
                      "tier": 7,
                      "tank_id": 529,
                      "name": "Tiger I",
                      "nation": "germany"
                    },
                    "785": {
                      "tier": 2,
                      "tank_id": 785,
                      "name": "Pz.Kpfw. 35 (t)",
                      "nation": "germany"
                    },
                    "1041": {
                      "tier": 5,
                      "tank_id": 1041,
                      "name": "StuG III Ausf. G",
                      "nation": "germany"
                    },
                    "1297": {
                      "tier": 7,
                      "tank_id": 1297,
                      "name": "Panther I",
                      "nation": "germany"
                    },
                    "1553": {
                      "tier": 6,
                      "tank_id": 1553,
                      "name": "Jagdpanzer IV",
                      "nation": "germany"
                    },
                    "1809": {
                      "tier": 4,
                      "tank_id": 1809,
                      "name": "Hetzer",
                      "nation": "germany"
                    },
                    "2065": {
                      "tier": 1,
                      "tank_id": 2065,
                      "name": "Pz.Kpfw. II",
                      "nation": "germany"
                    },
                    "2321": {
                      "tier": 6,
                      "tank_id": 2321,
                      "name": "VK 36.01 (H)",
                      "nation": "germany"
                    },
                    "2577": {
                      "tier": 5,
                      "tank_id": 2577,
                      "name": "VK 30.01 (H)",
                      "nation": "germany"
                    },
                    "3345": {
                      "tier": 3,
                      "tank_id": 3345,
                      "name": "Pz.Kpfw. 38 (t)",
                      "nation": "germany"
                    },
                    "3601": {
                      "tier": 2,
                      "tank_id": 3601,
                      "name": "Panzerjäger I",
                      "nation": "germany"
                    },
                    "3857": {
                      "tier": 7,
                      "tank_id": 3857,
                      "name": "Jagdpanther",
                      "nation": "germany"
                    },
                    "4113": {
                      "tier": 7,
                      "tank_id": 4113,
                      "name": "VK 30.02 (D)",
                      "nation": "germany"
                    },
                    "4369": {
                      "tier": 3,
                      "tank_id": 4369,
                      "name": "Pz.Kpfw. III",
                      "nation": "germany"
                    },
                    "4881": {
                      "tier": 3,
                      "tank_id": 4881,
                      "name": "Pz.Kpfw. III Ausf. A",
                      "nation": "germany"
                    },
                    "5137": {
                      "tier": 8,
                      "tank_id": 5137,
                      "name": "Tiger II",
                      "nation": "germany"
                    },
                    "5393": {
                      "tier": 5,
                      "tank_id": 5393,
                      "name": "VK 16.02 Leopard",
                      "nation": "germany"
                    },
                    "6161": {
                      "tier": 4,
                      "tank_id": 6161,
                      "name": "Pz.Kpfw. II Luchs",
                      "nation": "germany"
                    },
                    "6417": {
                      "tier": 5,
                      "tank_id": 6417,
                      "name": "Pz.Kpfw. III/IV",
                      "nation": "germany"
                    },
                    "6673": {
                      "tier": 3,
                      "tank_id": 6673,
                      "name": "Marder II",
                      "nation": "germany"
                    },
                    "6929": {
                      "tier": 10,
                      "tank_id": 6929,
                      "name": "Maus",
                      "nation": "germany"
                    },
                    "7185": {
                      "tier": 6,
                      "tank_id": 7185,
                      "name": "VK 30.01 (P)",
                      "nation": "germany"
                    },
                    "7441": {
                      "tier": 9,
                      "tank_id": 7441,
                      "name": "VK 45.02 (P) Ausf. B",
                      "nation": "germany"
                    },
                    "7697": {
                      "tier": 8,
                      "tank_id": 7697,
                      "name": "Ferdinand",
                      "nation": "germany"
                    },
                    "7953": {
                      "tier": 9,
                      "tank_id": 7953,
                      "name": "Jagdtiger",
                      "nation": "germany"
                    },
                    "8209": {
                      "tier": 4,
                      "tank_id": 8209,
                      "name": "Pz.Kpfw. 38 (t) n.A.",
                      "nation": "germany"
                    },
                    "8465": {
                      "tier": 8,
                      "tank_id": 8465,
                      "name": "Panther II",
                      "nation": "germany"
                    },
                    "9489": {
                      "tier": 10,
                      "tank_id": 9489,
                      "name": "E 100",
                      "nation": "germany"
                    },
                    "9745": {
                      "tier": 9,
                      "tank_id": 9745,
                      "name": "E 75",
                      "nation": "germany"
                    },
                    "10001": {
                      "tier": 6,
                      "tank_id": 10001,
                      "name": "VK 28.01",
                      "nation": "germany"
                    },
                    "10257": {
                      "tier": 9,
                      "tank_id": 10257,
                      "name": "E 50",
                      "nation": "germany"
                    },
                    "10513": {
                      "tier": 8,
                      "tank_id": 10513,
                      "name": "VK 45.02 (P) Ausf. A",
                      "nation": "germany"
                    },
                    "10769": {
                      "tier": 7,
                      "tank_id": 10769,
                      "name": "Tiger (P)",
                      "nation": "germany"
                    },
                    "11025": {
                      "tier": 7,
                      "tank_id": 11025,
                      "name": "Sturer Emil",
                      "nation": "germany"
                    },
                    "11281": {
                      "tier": 9,
                      "tank_id": 11281,
                      "name": "Kampfpanzer 70",
                      "nation": "germany"
                    },
                    "11537": {
                      "tier": 8,
                      "tank_id": 11537,
                      "name": "Jagdpanther II",
                      "nation": "germany"
                    },
                    "11793": {
                      "tier": 6,
                      "tank_id": 11793,
                      "name": "Nashorn",
                      "nation": "germany"
                    },
                    "12049": {
                      "tier": 10,
                      "tank_id": 12049,
                      "name": "Jagdpanzer E 100",
                      "nation": "germany"
                    },
                    "12305": {
                      "tier": 10,
                      "tank_id": 12305,
                      "name": "E 50 Ausf. M",
                      "nation": "germany"
                    },
                    "13073": {
                      "tier": 3,
                      "tank_id": 13073,
                      "name": "Pz.Kpfw. II Ausf. G",
                      "nation": "germany"
                    },
                    "13329": {
                      "tier": 4,
                      "tank_id": 13329,
                      "name": "Durchbruchswagen 2",
                      "nation": "germany"
                    },
                    "13841": {
                      "tier": 8,
                      "tank_id": 13841,
                      "name": "Indien-Panzer",
                      "nation": "germany"
                    },
                    "14097": {
                      "tier": 6,
                      "tank_id": 14097,
                      "name": "VK 30.01 (D)",
                      "nation": "germany"
                    },
                    "14609": {
                      "tier": 10,
                      "tank_id": 14609,
                      "name": "Leopard 1",
                      "nation": "germany"
                    },
                    "14865": {
                      "tier": 9,
                      "tank_id": 14865,
                      "name": "Leopard Prototyp A",
                      "nation": "germany"
                    },
                    "15889": {
                      "tier": 6,
                      "tank_id": 15889,
                      "name": "VK 30.02 (M)",
                      "nation": "germany"
                    },
                    "16145": {
                      "tier": 5,
                      "tank_id": 16145,
                      "name": "Pz.Sfl. IVc",
                      "nation": "germany"
                    },
                    "16401": {
                      "tier": 9,
                      "tank_id": 16401,
                      "name": "Waffenträger auf Pz. IV",
                      "nation": "germany"
                    },
                    "16657": {
                      "tier": 8,
                      "tank_id": 16657,
                      "name": "Rhm.-Borsig Waffenträger",
                      "nation": "germany"
                    },
                    "17169": {
                      "tier": 3,
                      "tank_id": 17169,
                      "name": "Pz.Kpfw. IV Ausf. A",
                      "nation": "germany"
                    },
                    "17425": {
                      "tier": 4,
                      "tank_id": 17425,
                      "name": "Pz.Kpfw. IV Ausf. D",
                      "nation": "germany"
                    },
                    "18449": {
                      "tier": 8,
                      "tank_id": 18449,
                      "name": "Ru 251",
                      "nation": "germany"
                    },
                    "18961": {
                      "tier": 7,
                      "tank_id": 18961,
                      "name": "Spähpanzer SP I C",
                      "nation": "germany"
                    },
                    "19217": {
                      "tier": 10,
                      "tank_id": 19217,
                      "name": "Grille 15",
                      "nation": "germany"
                    },
                    "19473": {
                      "tier": 5,
                      "tank_id": 19473,
                      "name": "Krupp-38(D)",
                      "nation": "germany"
                    },
                    "19729": {
                      "tier": 6,
                      "tank_id": 19729,
                      "name": "Tiger 131",
                      "nation": "germany"
                    },
                    "19985": {
                      "tier": 8,
                      "tank_id": 19985,
                      "name": "Skorpion G",
                      "nation": "germany"
                    },
                    "20497": {
                      "tier": 8,
                      "tank_id": 20497,
                      "name": "VK 100.01 (P)",
                      "nation": "germany"
                    },
                    "20753": {
                      "tier": 9,
                      "tank_id": 20753,
                      "name": "Mäuschen",
                      "nation": "germany"
                    },
                    "21009": {
                      "tier": 8,
                      "tank_id": 21009,
                      "name": "Panzer 58",
                      "nation": "germany"
                    },
                    "21265": {
                      "tier": 8,
                      "tank_id": 21265,
                      "name": "VK 168.01 (P)",
                      "nation": "germany"
                    },
                    "21521": {
                      "tier": 8,
                      "tank_id": 21521,
                      "name": "E 75 TS",
                      "nation": "germany"
                    },
                    "21777": {
                      "tier": 10,
                      "tank_id": 21777,
                      "name": "VK 90.01 (P)",
                      "nation": "germany"
                    },
                    "22033": {
                      "tier": 6,
                      "tank_id": 22033,
                      "name": "Agent",
                      "nation": "germany"
                    },
                    "22545": {
                      "tier": 8,
                      "tank_id": 22545,
                      "name": "Kanonenjagdpanzer 105",
                      "nation": "germany"
                    },
                    "22801": {
                      "tier": 6,
                      "tank_id": 22801,
                      "name": "Icebreaker",
                      "nation": "germany"
                    },
                    "23057": {
                      "tier": 7,
                      "tank_id": 23057,
                      "name": "Kunze Panzer",
                      "nation": "germany"
                    },
                    "23313": {
                      "tier": 10,
                      "tank_id": 23313,
                      "name": "Kampfpanzer 50 t",
                      "nation": "germany"
                    },
                    "23569": {
                      "tier": 5,
                      "tank_id": 23569,
                      "name": "Pz. IV Gargoyle",
                      "nation": "germany"
                    },
                    "23825": {
                      "tier": 7,
                      "tank_id": 23825,
                      "name": "Krupp-Steyr Waffenträger",
                      "nation": "germany"
                    },
                    "24081": {
                      "tier": 6,
                      "tank_id": 24081,
                      "name": "U-Panzer",
                      "nation": "germany"
                    },
                    "24337": {
                      "tier": 8,
                      "tank_id": 24337,
                      "name": "M48A2 Räumpanzer",
                      "nation": "germany"
                    },
                    "24593": {
                      "tier": 8,
                      "tank_id": 24593,
                      "name": "Keiler",
                      "nation": "germany"
                    },
                    "25105": {
                      "tier": 6,
                      "tank_id": 25105,
                      "name": "Barkhan",
                      "nation": "germany"
                    },
                    "51473": {
                      "tier": 5,
                      "tank_id": 51473,
                      "name": "Pz.Kpfw. V/IV",
                      "nation": "germany"
                    },
                    "51729": {
                      "tier": 3,
                      "tank_id": 51729,
                      "name": "Pz.Kpfw. II Ausf. J",
                      "nation": "germany"
                    },
                    "51985": {
                      "tier": 3,
                      "tank_id": 51985,
                      "name": "Pz.Kpfw. S35 739 (f)",
                      "nation": "germany"
                    },
                    "52241": {
                      "tier": 4,
                      "tank_id": 52241,
                      "name": "Pz.Kpfw. B2 740 (f)",
                      "nation": "germany"
                    },
                    "52497": {
                      "tier": 2,
                      "tank_id": 52497,
                      "name": "Pz.Kpfw. 38H 735 (f)",
                      "nation": "germany"
                    },
                    "54289": {
                      "tier": 8,
                      "tank_id": 54289,
                      "name": "Löwe",
                      "nation": "germany"
                    },
                    "54545": {
                      "tier": 5,
                      "tank_id": 54545,
                      "name": "T-25",
                      "nation": "germany"
                    },
                    "54801": {
                      "tier": 3,
                      "tank_id": 54801,
                      "name": "T-15",
                      "nation": "germany"
                    },
                    "55057": {
                      "tier": 5,
                      "tank_id": 55057,
                      "name": "Pz.Kpfw. IV hydrostat.",
                      "nation": "germany"
                    },
                    "55313": {
                      "tier": 8,
                      "tank_id": 55313,
                      "name": "8,8 cm Pak 43 Jagdtiger",
                      "nation": "germany"
                    },
                    "57105": {
                      "tier": 6,
                      "tank_id": 57105,
                      "name": "Dicker Max",
                      "nation": "germany"
                    },
                    "57361": {
                      "tier": 6,
                      "tank_id": 57361,
                      "name": "Pz.Kpfw. IV Schmalturm",
                      "nation": "germany"
                    },
                    "57617": {
                      "tier": 7,
                      "tank_id": 57617,
                      "name": "Panther/M10",
                      "nation": "germany"
                    },
                    "58641": {
                      "tier": 10,
                      "tank_id": 58641,
                      "name": "VK 72.01 (K)",
                      "nation": "germany"
                    },
                    "59665": {
                      "tier": 3,
                      "tank_id": 59665,
                      "name": "Großtraktor - Krupp",
                      "nation": "germany"
                    },
                    "60177": {
                      "tier": 8,
                      "tank_id": 60177,
                      "name": "Panther mit 8,8 cm L/71",
                      "nation": "germany"
                    },
                    "62737": {
                      "tier": 8,
                      "tank_id": 62737,
                      "name": "leKpz M 41 90 mm",
                      "nation": "germany"
                    },
                    "62993": {
                      "tier": 7,
                      "tank_id": 62993,
                      "name": "VK 45.03",
                      "nation": "germany"
                    },
                    "64017": {
                      "tier": 7,
                      "tank_id": 64017,
                      "name": "Tankenstein",
                      "nation": "germany"
                    },
                    "64273": {
                      "tier": 8,
                      "tank_id": 64273,
                      "name": "Snowstorm Jagdtiger 8.8",
                      "nation": "germany"
                    },
                    "64529": {
                      "tier": 7,
                      "tank_id": 64529,
                      "name": "E 25",
                      "nation": "germany"
                    }
                  }
                }
                """;
    }

    public String ukTankData;
    {
        ukTankData = """
                {
                  "status": "ok",
                  "meta": {
                    "count": 63
                  },
                  "data": {
                    "337": {
                      "tier": 2,
                      "tank_id": 337,
                      "name": "Vickers Medium Mk. II",
                      "nation": "uk"
                    },
                    "593": {
                      "tier": 6,
                      "tank_id": 593,
                      "name": "Sherman Firefly",
                      "nation": "uk"
                    },
                    "849": {
                      "tier": 4,
                      "tank_id": 849,
                      "name": "Matilda",
                      "nation": "uk"
                    },
                    "1105": {
                      "tier": 6,
                      "tank_id": 1105,
                      "name": "Cromwell",
                      "nation": "uk"
                    },
                    "1361": {
                      "tier": 6,
                      "tank_id": 1361,
                      "name": "Churchill Mk. VI",
                      "nation": "uk"
                    },
                    "1617": {
                      "tier": 5,
                      "tank_id": 1617,
                      "name": "Sherman V",
                      "nation": "uk"
                    },
                    "2129": {
                      "tier": 5,
                      "tank_id": 2129,
                      "name": "Crusader",
                      "nation": "uk"
                    },
                    "2385": {
                      "tier": 3,
                      "tank_id": 2385,
                      "name": "Vickers Medium Mk. III",
                      "nation": "uk"
                    },
                    "2897": {
                      "tier": 5,
                      "tank_id": 2897,
                      "name": "Churchill I",
                      "nation": "uk"
                    },
                    "3153": {
                      "tier": 7,
                      "tank_id": 3153,
                      "name": "Black Prince",
                      "nation": "uk"
                    },
                    "3921": {
                      "tier": 8,
                      "tank_id": 3921,
                      "name": "Caernarvon",
                      "nation": "uk"
                    },
                    "4433": {
                      "tier": 9,
                      "tank_id": 4433,
                      "name": "Conqueror",
                      "nation": "uk"
                    },
                    "4689": {
                      "tier": 6,
                      "tank_id": 4689,
                      "name": "Churchill VII",
                      "nation": "uk"
                    },
                    "4945": {
                      "tier": 4,
                      "tank_id": 4945,
                      "name": "Valentine Mk. IX",
                      "nation": "uk"
                    },
                    "5201": {
                      "tier": 2,
                      "tank_id": 5201,
                      "name": "Cruiser Mk. I",
                      "nation": "uk"
                    },
                    "5457": {
                      "tier": 7,
                      "tank_id": 5457,
                      "name": "Comet",
                      "nation": "uk"
                    },
                    "5713": {
                      "tier": 9,
                      "tank_id": 5713,
                      "name": "Centurion Mk. 7/1",
                      "nation": "uk"
                    },
                    "5969": {
                      "tier": 8,
                      "tank_id": 5969,
                      "name": "Centurion Mk. I",
                      "nation": "uk"
                    },
                    "6225": {
                      "tier": 10,
                      "tank_id": 6225,
                      "name": "FV215b",
                      "nation": "uk"
                    },
                    "6481": {
                      "tier": 4,
                      "tank_id": 6481,
                      "name": "Covenanter",
                      "nation": "uk"
                    },
                    "6993": {
                      "tier": 1,
                      "tank_id": 6993,
                      "name": "Cruiser Mk. II",
                      "nation": "uk"
                    },
                    "7249": {
                      "tier": 10,
                      "tank_id": 7249,
                      "name": "FV4202",
                      "nation": "uk"
                    },
                    "7505": {
                      "tier": 3,
                      "tank_id": 7505,
                      "name": "Cruiser Mk. IV",
                      "nation": "uk"
                    },
                    "7761": {
                      "tier": 2,
                      "tank_id": 7761,
                      "name": "Cruiser Mk. III",
                      "nation": "uk"
                    },
                    "8017": {
                      "tier": 3,
                      "tank_id": 8017,
                      "name": "Valentine AT",
                      "nation": "uk"
                    },
                    "8273": {
                      "tier": 2,
                      "tank_id": 8273,
                      "name": "Universal Carrier 2-pdr",
                      "nation": "uk"
                    },
                    "8529": {
                      "tier": 8,
                      "tank_id": 8529,
                      "name": "AT 15",
                      "nation": "uk"
                    },
                    "8785": {
                      "tier": 5,
                      "tank_id": 8785,
                      "name": "AT 2",
                      "nation": "uk"
                    },
                    "9041": {
                      "tier": 4,
                      "tank_id": 9041,
                      "name": "Alecto",
                      "nation": "uk"
                    },
                    "9297": {
                      "tier": 10,
                      "tank_id": 9297,
                      "name": "FV215b (183)",
                      "nation": "uk"
                    },
                    "9553": {
                      "tier": 6,
                      "tank_id": 9553,
                      "name": "AT 8",
                      "nation": "uk"
                    },
                    "9809": {
                      "tier": 6,
                      "tank_id": 9809,
                      "name": "Churchill Gun Carrier",
                      "nation": "uk"
                    },
                    "10065": {
                      "tier": 7,
                      "tank_id": 10065,
                      "name": "AT 7",
                      "nation": "uk"
                    },
                    "15441": {
                      "tier": 8,
                      "tank_id": 15441,
                      "name": "Chieftain/T95",
                      "nation": "uk"
                    },
                    "15697": {
                      "tier": 10,
                      "tank_id": 15697,
                      "name": "Chieftain Mk. 6",
                      "nation": "uk"
                    },
                    "15953": {
                      "tier": 7,
                      "tank_id": 15953,
                      "name": "FV201 (A45)",
                      "nation": "uk"
                    },
                    "17233": {
                      "tier": 9,
                      "tank_id": 17233,
                      "name": "FV4004 Conway",
                      "nation": "uk"
                    },
                    "17489": {
                      "tier": 8,
                      "tank_id": 17489,
                      "name": "Charioteer",
                      "nation": "uk"
                    },
                    "17745": {
                      "tier": 10,
                      "tank_id": 17745,
                      "name": "FV217 Badger",
                      "nation": "uk"
                    },
                    "18001": {
                      "tier": 10,
                      "tank_id": 18001,
                      "name": "FV4005",
                      "nation": "uk"
                    },
                    "18257": {
                      "tier": 7,
                      "tank_id": 18257,
                      "name": "Challenger",
                      "nation": "uk"
                    },
                    "18513": {
                      "tier": 8,
                      "tank_id": 18513,
                      "name": "Chimera",
                      "nation": "uk"
                    },
                    "18769": {
                      "tier": 8,
                      "tank_id": 18769,
                      "name": "Caernarvon Action X",
                      "nation": "uk"
                    },
                    "19025": {
                      "tier": 8,
                      "tank_id": 19025,
                      "name": "Defender Mk. 1",
                      "nation": "uk"
                    },
                    "19281": {
                      "tier": 10,
                      "tank_id": 19281,
                      "name": "Super Conqueror",
                      "nation": "uk"
                    },
                    "19537": {
                      "tier": 10,
                      "tank_id": 19537,
                      "name": "Vickers Light 105",
                      "nation": "uk"
                    },
                    "19793": {
                      "tier": 9,
                      "tank_id": 19793,
                      "name": "Vickers Cruiser",
                      "nation": "uk"
                    },
                    "20049": {
                      "tier": 8,
                      "tank_id": 20049,
                      "name": "FV301",
                      "nation": "uk"
                    },
                    "20305": {
                      "tier": 8,
                      "tank_id": 20305,
                      "name": "Centurion Mk. 5/1 RAAC",
                      "nation": "uk"
                    },
                    "20561": {
                      "tier": 8,
                      "tank_id": 20561,
                      "name": "Turtle Mk. I",
                      "nation": "uk"
                    },
                    "21073": {
                      "tier": 6,
                      "tank_id": 21073,
                      "name": "Dreadnought",
                      "nation": "uk"
                    },
                    "21329": {
                      "tier": 8,
                      "tank_id": 21329,
                      "name": "GSOR 1008",
                      "nation": "uk"
                    },
                    "21585": {
                      "tier": 6,
                      "tank_id": 21585,
                      "name": "Blasteroid",
                      "nation": "uk"
                    },
                    "52561": {
                      "tier": 9,
                      "tank_id": 52561,
                      "name": "Tortoise",
                      "nation": "uk"
                    },
                    "53585": {
                      "tier": 5,
                      "tank_id": 53585,
                      "name": "Matilda Black Prince",
                      "nation": "uk"
                    },
                    "53841": {
                      "tier": 6,
                      "tank_id": 53841,
                      "name": "TOG II*",
                      "nation": "uk"
                    },
                    "54097": {
                      "tier": 7,
                      "tank_id": 54097,
                      "name": "AT 15A",
                      "nation": "uk"
                    },
                    "54353": {
                      "tier": 5,
                      "tank_id": 54353,
                      "name": "Excelsior",
                      "nation": "uk"
                    },
                    "54865": {
                      "tier": 2,
                      "tank_id": 54865,
                      "name": "Light Mk. VIC",
                      "nation": "uk"
                    },
                    "55889": {
                      "tier": 6,
                      "tank_id": 55889,
                      "name": "Cromwell B",
                      "nation": "uk"
                    },
                    "64337": {
                      "tier": 6,
                      "tank_id": 64337,
                      "name": "AC IV Sentinel",
                      "nation": "uk"
                    },
                    "64593": {
                      "tier": 5,
                      "tank_id": 64593,
                      "name": "Angry Connor",
                      "nation": "uk"
                    },
                    "64849": {
                      "tier": 4,
                      "tank_id": 64849,
                      "name": "Sentinel AC I",
                      "nation": "uk"
                    }
                  }
                }
                """;
    }

    public String japanTankData;
    {
        japanTankData = """
                {
                  "status": "ok",
                  "meta": {
                    "count": 26
                  },
                  "data": {
                    "353": {
                      "tier": 2,
                      "tank_id": 353,
                      "name": "Chi-Ni",
                      "nation": "japan"
                    },
                    "865": {
                      "tier": 1,
                      "tank_id": 865,
                      "name": "Type 95 Ha-Go",
                      "nation": "japan"
                    },
                    "1121": {
                      "tier": 7,
                      "tank_id": 1121,
                      "name": "Type 5 Chi-Ri",
                      "nation": "japan"
                    },
                    "1377": {
                      "tier": 5,
                      "tank_id": 1377,
                      "name": "Type 3 Chi-Nu",
                      "nation": "japan"
                    },
                    "1633": {
                      "tier": 4,
                      "tank_id": 1633,
                      "name": "Type 1 Chi-He",
                      "nation": "japan"
                    },
                    "1889": {
                      "tier": 6,
                      "tank_id": 1889,
                      "name": "Type 4 Chi-To",
                      "nation": "japan"
                    },
                    "2145": {
                      "tier": 3,
                      "tank_id": 2145,
                      "name": "Type 97 Chi-Ha",
                      "nation": "japan"
                    },
                    "2401": {
                      "tier": 3,
                      "tank_id": 2401,
                      "name": "Type 98 Ke-Ni",
                      "nation": "japan"
                    },
                    "2657": {
                      "tier": 8,
                      "tank_id": 2657,
                      "name": "STA-1",
                      "nation": "japan"
                    },
                    "3425": {
                      "tier": 9,
                      "tank_id": 3425,
                      "name": "Type 61",
                      "nation": "japan"
                    },
                    "3681": {
                      "tier": 10,
                      "tank_id": 3681,
                      "name": "STB-1",
                      "nation": "japan"
                    },
                    "3937": {
                      "tier": 10,
                      "tank_id": 3937,
                      "name": "Ho-Ri Type III",
                      "nation": "japan"
                    },
                    "4193": {
                      "tier": 9,
                      "tank_id": 4193,
                      "name": "Ho-Ri Type II",
                      "nation": "japan"
                    },
                    "4449": {
                      "tier": 7,
                      "tank_id": 4449,
                      "name": "IS-2 Pravda SP",
                      "nation": "japan"
                    },
                    "4705": {
                      "tier": 6,
                      "tank_id": 4705,
                      "name": "Firefly Saunders SP",
                      "nation": "japan"
                    },
                    "4961": {
                      "tier": 8,
                      "tank_id": 4961,
                      "name": "Ho-Ri Type I",
                      "nation": "japan"
                    },
                    "5217": {
                      "tier": 7,
                      "tank_id": 5217,
                      "name": "Chi-To SPG",
                      "nation": "japan"
                    },
                    "5473": {
                      "tier": 5,
                      "tank_id": 5473,
                      "name": "Mitsu 108",
                      "nation": "japan"
                    },
                    "5729": {
                      "tier": 6,
                      "tank_id": 5729,
                      "name": "Ju-Nu",
                      "nation": "japan"
                    },
                    "5985": {
                      "tier": 7,
                      "tank_id": 5985,
                      "name": "Ju-To",
                      "nation": "japan"
                    },
                    "7009": {
                      "tier": 8,
                      "tank_id": 7009,
                      "name": "Type 57",
                      "nation": "japan"
                    },
                    "51809": {
                      "tier": 3,
                      "tank_id": 51809,
                      "name": "Type 98 Ke-Ni Otsu",
                      "nation": "japan"
                    },
                    "52065": {
                      "tier": 4,
                      "tank_id": 52065,
                      "name": "Hetzer Kame SP",
                      "nation": "japan"
                    },
                    "63585": {
                      "tier": 6,
                      "tank_id": 63585,
                      "name": "Tiger Kuromorimine SP",
                      "nation": "japan"
                    },
                    "63841": {
                      "tier": 5,
                      "tank_id": 63841,
                      "name": "Panzer IV Ankou Special",
                      "nation": "japan"
                    },
                    "65377": {
                      "tier": 5,
                      "tank_id": 65377,
                      "name": "Type 3 Chi-Nu Kai Shinobi",
                      "nation": "japan"
                    }
                  }
                }
                """;
    }

    public String franceTankData;
    {
        franceTankData = """
                {
                  "status": "ok",
                  "meta": {
                    "count": 44
                  },
                  "data": {
                    "321": {
                      "tier": 3,
                      "tank_id": 321,
                      "name": "D2",
                      "nation": "france"
                    },
                    "1089": {
                      "tier": 4,
                      "tank_id": 1089,
                      "name": "B1",
                      "nation": "france"
                    },
                    "1601": {
                      "tier": 2,
                      "tank_id": 1601,
                      "name": "D1",
                      "nation": "france"
                    },
                    "1857": {
                      "tier": 9,
                      "tank_id": 1857,
                      "name": "Bat.-Châtillon 25 t AP",
                      "nation": "france"
                    },
                    "2369": {
                      "tier": 3,
                      "tank_id": 2369,
                      "name": "FCM 36 Pak 40",
                      "nation": "france"
                    },
                    "2625": {
                      "tier": 6,
                      "tank_id": 2625,
                      "name": "ARL 44",
                      "nation": "france"
                    },
                    "2881": {
                      "tier": 4,
                      "tank_id": 2881,
                      "name": "AMX 40",
                      "nation": "france"
                    },
                    "3137": {
                      "tier": 8,
                      "tank_id": 3137,
                      "name": "AMX 50 100",
                      "nation": "france"
                    },
                    "3649": {
                      "tier": 10,
                      "tank_id": 3649,
                      "name": "Bat.-Châtillon 25 t",
                      "nation": "france"
                    },
                    "3905": {
                      "tier": 9,
                      "tank_id": 3905,
                      "name": "AMX 50 120",
                      "nation": "france"
                    },
                    "4417": {
                      "tier": 10,
                      "tank_id": 4417,
                      "name": "AMX M4 mle. 54",
                      "nation": "france"
                    },
                    "4929": {
                      "tier": 8,
                      "tank_id": 4929,
                      "name": "AMX 13 90",
                      "nation": "france"
                    },
                    "5185": {
                      "tier": 7,
                      "tank_id": 5185,
                      "name": "AMX 13 75",
                      "nation": "france"
                    },
                    "5441": {
                      "tier": 9,
                      "tank_id": 5441,
                      "name": "AMX 30 1er prototype",
                      "nation": "france"
                    },
                    "5953": {
                      "tier": 2,
                      "tank_id": 5953,
                      "name": "AMX 38",
                      "nation": "france"
                    },
                    "6209": {
                      "tier": 10,
                      "tank_id": 6209,
                      "name": "AMX 50 B",
                      "nation": "france"
                    },
                    "6465": {
                      "tier": 6,
                      "tank_id": 6465,
                      "name": "AMX 12 t",
                      "nation": "france"
                    },
                    "6721": {
                      "tier": 5,
                      "tank_id": 6721,
                      "name": "BDR G1 B",
                      "nation": "france"
                    },
                    "6977": {
                      "tier": 7,
                      "tank_id": 6977,
                      "name": "AMX M4 mle. 45",
                      "nation": "france"
                    },
                    "7745": {
                      "tier": 2,
                      "tank_id": 7745,
                      "name": "Renault FT AC",
                      "nation": "france"
                    },
                    "8001": {
                      "tier": 8,
                      "tank_id": 8001,
                      "name": "Lorraine 40 t",
                      "nation": "france"
                    },
                    "8257": {
                      "tier": 3,
                      "tank_id": 8257,
                      "name": "Renault UE 57",
                      "nation": "france"
                    },
                    "8513": {
                      "tier": 10,
                      "tank_id": 8513,
                      "name": "AMX 30 B",
                      "nation": "france"
                    },
                    "9793": {
                      "tier": 4,
                      "tank_id": 9793,
                      "name": "Somua SAu 40",
                      "nation": "france"
                    },
                    "10049": {
                      "tier": 5,
                      "tank_id": 10049,
                      "name": "S35 CA",
                      "nation": "france"
                    },
                    "10817": {
                      "tier": 7,
                      "tank_id": 10817,
                      "name": "AMX AC mle. 46",
                      "nation": "france"
                    },
                    "11073": {
                      "tier": 9,
                      "tank_id": 11073,
                      "name": "AMX 50 Foch",
                      "nation": "france"
                    },
                    "11585": {
                      "tier": 6,
                      "tank_id": 11585,
                      "name": "ARL V39",
                      "nation": "france"
                    },
                    "12097": {
                      "tier": 8,
                      "tank_id": 12097,
                      "name": "AMX AC mle. 48",
                      "nation": "france"
                    },
                    "13889": {
                      "tier": 10,
                      "tank_id": 13889,
                      "name": "AMX 50 Foch (155)",
                      "nation": "france"
                    },
                    "14145": {
                      "tier": 5,
                      "tank_id": 14145,
                      "name": "AMX ELC bis",
                      "nation": "france"
                    },
                    "15937": {
                      "tier": 1,
                      "tank_id": 15937,
                      "name": "Renault R35",
                      "nation": "france"
                    },
                    "16193": {
                      "tier": 8,
                      "tank_id": 16193,
                      "name": "M4A1 Revalorisé",
                      "nation": "france"
                    },
                    "16449": {
                      "tier": 7,
                      "tank_id": 16449,
                      "name": "AMX 13 57",
                      "nation": "france"
                    },
                    "16705": {
                      "tier": 8,
                      "tank_id": 16705,
                      "name": "AMX M4 mle. 49",
                      "nation": "france"
                    },
                    "17217": {
                      "tier": 6,
                      "tank_id": 17217,
                      "name": "Eraser BP44",
                      "nation": "france"
                    },
                    "17473": {
                      "tier": 8,
                      "tank_id": 17473,
                      "name": "AMX Defender",
                      "nation": "france"
                    },
                    "17729": {
                      "tier": 8,
                      "tank_id": 17729,
                      "name": "Somua SM",
                      "nation": "france"
                    },
                    "18497": {
                      "tier": 8,
                      "tank_id": 18497,
                      "name": "Lorraine 40 t Fearless",
                      "nation": "france"
                    },
                    "18753": {
                      "tier": 8,
                      "tank_id": 18753,
                      "name": "AMX Canon d'assaut 105",
                      "nation": "france"
                    },
                    "19009": {
                      "tier": 6,
                      "tank_id": 19009,
                      "name": "AMXmas",
                      "nation": "france"
                    },
                    "19265": {
                      "tier": 6,
                      "tank_id": 19265,
                      "name": "Charles",
                      "nation": "france"
                    },
                    "63553": {
                      "tier": 8,
                      "tank_id": 63553,
                      "name": "AMX Chasseur de chars",
                      "nation": "france"
                    },
                    "64065": {
                      "tier": 8,
                      "tank_id": 64065,
                      "name": "FCM 50 t",
                      "nation": "france"
                    }
                  }
                }
                """;
    }

    public String chinaTankData;
    {
        chinaTankData = """
                {
                  "status": "ok",
                  "meta": {
                    "count": 32
                  },
                  "data": {
                    "49": {
                      "tier": 8,
                      "tank_id": 49,
                      "name": "Type 59",
                      "nation": "china"
                    },
                    "817": {
                      "tier": 8,
                      "tank_id": 817,
                      "name": "WZ-111",
                      "nation": "china"
                    },
                    "1073": {
                      "tier": 7,
                      "tank_id": 1073,
                      "name": "T-34-1",
                      "nation": "china"
                    },
                    "1585": {
                      "tier": 8,
                      "tank_id": 1585,
                      "name": "T-34-2",
                      "nation": "china"
                    },
                    "1841": {
                      "tier": 9,
                      "tank_id": 1841,
                      "name": "WZ-120",
                      "nation": "china"
                    },
                    "2097": {
                      "tier": 9,
                      "tank_id": 2097,
                      "name": "WZ-111 model 1-4",
                      "nation": "china"
                    },
                    "2353": {
                      "tier": 1,
                      "tank_id": 2353,
                      "name": "Vickers Mk. E Type B",
                      "nation": "china"
                    },
                    "2609": {
                      "tier": 6,
                      "tank_id": 2609,
                      "name": "Type 64",
                      "nation": "china"
                    },
                    "2865": {
                      "tier": 8,
                      "tank_id": 2865,
                      "name": "WZ-110",
                      "nation": "china"
                    },
                    "3121": {
                      "tier": 4,
                      "tank_id": 3121,
                      "name": "M5A1 Stuart",
                      "nation": "china"
                    },
                    "3633": {
                      "tier": 7,
                      "tank_id": 3633,
                      "name": "IS-2",
                      "nation": "china"
                    },
                    "4145": {
                      "tier": 10,
                      "tank_id": 4145,
                      "name": "WZ-121",
                      "nation": "china"
                    },
                    "4401": {
                      "tier": 3,
                      "tank_id": 4401,
                      "name": "Type 2597 Chi-Ha",
                      "nation": "china"
                    },
                    "4657": {
                      "tier": 5,
                      "tank_id": 4657,
                      "name": "Type T-34",
                      "nation": "china"
                    },
                    "5169": {
                      "tier": 6,
                      "tank_id": 5169,
                      "name": "Type 58",
                      "nation": "china"
                    },
                    "5681": {
                      "tier": 10,
                      "tank_id": 5681,
                      "name": "121B",
                      "nation": "china"
                    },
                    "5937": {
                      "tier": 8,
                      "tank_id": 5937,
                      "name": "59-Patton",
                      "nation": "china"
                    },
                    "6193": {
                      "tier": 8,
                      "tank_id": 6193,
                      "name": "T-34-3",
                      "nation": "china"
                    },
                    "6449": {
                      "tier": 10,
                      "tank_id": 6449,
                      "name": "WZ-113G FT",
                      "nation": "china"
                    },
                    "6705": {
                      "tier": 2,
                      "tank_id": 6705,
                      "name": "LT vz. 38",
                      "nation": "china"
                    },
                    "6961": {
                      "tier": 8,
                      "tank_id": 6961,
                      "name": "WZ-120-1G FT",
                      "nation": "china"
                    },
                    "7217": {
                      "tier": 8,
                      "tank_id": 7217,
                      "name": "WZ-112-2",
                      "nation": "china"
                    },
                    "7473": {
                      "tier": 7,
                      "tank_id": 7473,
                      "name": "T-34-2G FT",
                      "nation": "china"
                    },
                    "7729": {
                      "tier": 6,
                      "tank_id": 7729,
                      "name": "WZ-131G FT",
                      "nation": "china"
                    },
                    "7985": {
                      "tier": 8,
                      "tank_id": 7985,
                      "name": "WZ-111-1G FT",
                      "nation": "china"
                    },
                    "8241": {
                      "tier": 9,
                      "tank_id": 8241,
                      "name": "WZ-111G FT",
                      "nation": "china"
                    },
                    "8497": {
                      "tier": 10,
                      "tank_id": 8497,
                      "name": "WZ-111 model 5A",
                      "nation": "china"
                    },
                    "8753": {
                      "tier": 7,
                      "tank_id": 8753,
                      "name": "M41D",
                      "nation": "china"
                    },
                    "9009": {
                      "tier": 6,
                      "tank_id": 9009,
                      "name": "Ox",
                      "nation": "china"
                    },
                    "9521": {
                      "tier": 8,
                      "tank_id": 9521,
                      "name": "WZ-122 TM",
                      "nation": "china"
                    },
                    "64561": {
                      "tier": 8,
                      "tank_id": 64561,
                      "name": "112 Glacial",
                      "nation": "china"
                    },
                    "65329": {
                      "tier": 7,
                      "tank_id": 65329,
                      "name": "Type 62",
                      "nation": "china"
                    }
                  }
                }
                """;
    }

    public String europeanTankData;
    {
        europeanTankData = """
                {
                  "status": "ok",
                  "meta": {
                    "count": 37
                  },
                  "data": {
                    "385": {
                      "tier": 10,
                      "tank_id": 385,
                      "name": "Progetto M40 mod. 65",
                      "nation": "european"
                    },
                    "641": {
                      "tier": 9,
                      "tank_id": 641,
                      "name": "Prototipo Standard B",
                      "nation": "european"
                    },
                    "897": {
                      "tier": 8,
                      "tank_id": 897,
                      "name": "P.44 Pantera",
                      "nation": "european"
                    },
                    "1153": {
                      "tier": 7,
                      "tank_id": 1153,
                      "name": "P.43 ter",
                      "nation": "european"
                    },
                    "1409": {
                      "tier": 6,
                      "tank_id": 1409,
                      "name": "P.43 bis",
                      "nation": "european"
                    },
                    "1665": {
                      "tier": 4,
                      "tank_id": 1665,
                      "name": "Lago",
                      "nation": "european"
                    },
                    "1921": {
                      "tier": 5,
                      "tank_id": 1921,
                      "name": "Strv m/42",
                      "nation": "european"
                    },
                    "2177": {
                      "tier": 3,
                      "tank_id": 2177,
                      "name": "14TP",
                      "nation": "european"
                    },
                    "2433": {
                      "tier": 2,
                      "tank_id": 2433,
                      "name": "10TP",
                      "nation": "european"
                    },
                    "2689": {
                      "tier": 1,
                      "tank_id": 2689,
                      "name": "Vickers Mk. F",
                      "nation": "european"
                    },
                    "2945": {
                      "tier": 8,
                      "tank_id": 2945,
                      "name": "Progetto M35 mod. 46",
                      "nation": "european"
                    },
                    "3201": {
                      "tier": 9,
                      "tank_id": 3201,
                      "name": "50TP prototyp",
                      "nation": "european"
                    },
                    "3457": {
                      "tier": 8,
                      "tank_id": 3457,
                      "name": "Emil I",
                      "nation": "european"
                    },
                    "3713": {
                      "tier": 6,
                      "tank_id": 3713,
                      "name": "Strv 74",
                      "nation": "european"
                    },
                    "3969": {
                      "tier": 7,
                      "tank_id": 3969,
                      "name": "Leo",
                      "nation": "european"
                    },
                    "4225": {
                      "tier": 9,
                      "tank_id": 4225,
                      "name": "Emil II",
                      "nation": "european"
                    },
                    "4481": {
                      "tier": 10,
                      "tank_id": 4481,
                      "name": "Kranvagn",
                      "nation": "european"
                    },
                    "4737": {
                      "tier": 8,
                      "tank_id": 4737,
                      "name": "EMIL 1951",
                      "nation": "european"
                    },
                    "4993": {
                      "tier": 6,
                      "tank_id": 4993,
                      "name": "P.43/06 anniversario",
                      "nation": "european"
                    },
                    "5249": {
                      "tier": 6,
                      "tank_id": 5249,
                      "name": "Pudel",
                      "nation": "european"
                    },
                    "5505": {
                      "tier": 10,
                      "tank_id": 5505,
                      "name": "TVP T 50/51",
                      "nation": "european"
                    },
                    "5761": {
                      "tier": 9,
                      "tank_id": 5761,
                      "name": "Škoda T 50",
                      "nation": "european"
                    },
                    "6017": {
                      "tier": 8,
                      "tank_id": 6017,
                      "name": "TVP VTU Koncept",
                      "nation": "european"
                    },
                    "6273": {
                      "tier": 7,
                      "tank_id": 6273,
                      "name": "Konštrukta T-34/100",
                      "nation": "european"
                    },
                    "6529": {
                      "tier": 6,
                      "tank_id": 6529,
                      "name": "Škoda T 25",
                      "nation": "european"
                    },
                    "6785": {
                      "tier": 8,
                      "tank_id": 6785,
                      "name": "Škoda T 27",
                      "nation": "european"
                    },
                    "7041": {
                      "tier": 6,
                      "tank_id": 7041,
                      "name": "Turbo",
                      "nation": "european"
                    },
                    "7553": {
                      "tier": 9,
                      "tank_id": 7553,
                      "name": "50TP Tyszkiewicza",
                      "nation": "european"
                    },
                    "7809": {
                      "tier": 8,
                      "tank_id": 7809,
                      "name": "53TP Markowskiego",
                      "nation": "european"
                    },
                    "8065": {
                      "tier": 6,
                      "tank_id": 8065,
                      "name": "40TP Habicha",
                      "nation": "european"
                    },
                    "8321": {
                      "tier": 7,
                      "tank_id": 8321,
                      "name": "45TP Habicha",
                      "nation": "european"
                    },
                    "8577": {
                      "tier": 8,
                      "tank_id": 8577,
                      "name": "Lansen C",
                      "nation": "european"
                    },
                    "8833": {
                      "tier": 6,
                      "tank_id": 8833,
                      "name": "Spark",
                      "nation": "european"
                    },
                    "9089": {
                      "tier": 8,
                      "tank_id": 9089,
                      "name": "Škoda T 56",
                      "nation": "european"
                    },
                    "9345": {
                      "tier": 7,
                      "tank_id": 9345,
                      "name": "Svear",
                      "nation": "european"
                    },
                    "9601": {
                      "tier": 7,
                      "tank_id": 9601,
                      "name": "CS-52 LIS",
                      "nation": "european"
                    },
                    "9857": {
                      "tier": 7,
                      "tank_id": 9857,
                      "name": "Škoda T 45",
                      "nation": "european"
                    }
                  }
                }
                """;
    }

    public String otherTankData;
    {
        otherTankData = """
                {
                  "status": "ok",
                  "meta": {
                    "count": 29
                  },
                  "data": {
                    "113": {
                      "tier": 7,
                      "tank_id": 113,
                      "name": "Vindicator Ultramarines",
                      "nation": "other"
                    },
                    "625": {
                      "tier": 6,
                      "tank_id": 625,
                      "name": "Stridsvagn 74A2",
                      "nation": "other"
                    },
                    "881": {
                      "tier": 7,
                      "tank_id": 881,
                      "name": "Edelweiss",
                      "nation": "other"
                    },
                    "1137": {
                      "tier": 7,
                      "tank_id": 1137,
                      "name": "Predator Ultramarines",
                      "nation": "other"
                    },
                    "1393": {
                      "tier": 7,
                      "tank_id": 1393,
                      "name": "Nameless",
                      "nation": "other"
                    },
                    "1649": {
                      "tier": 7,
                      "tank_id": 1649,
                      "name": "Helsing",
                      "nation": "other"
                    },
                    "1905": {
                      "tier": 8,
                      "tank_id": 1905,
                      "name": "O-47",
                      "nation": "other"
                    },
                    "2161": {
                      "tier": 7,
                      "tank_id": 2161,
                      "name": "WZ 135G FT Blaze",
                      "nation": "other"
                    },
                    "3697": {
                      "tier": 7,
                      "tank_id": 3697,
                      "name": "Lupus",
                      "nation": "other"
                    },
                    "3953": {
                      "tier": 9,
                      "tank_id": 3953,
                      "name": "T 55A",
                      "nation": "other"
                    },
                    "4465": {
                      "tier": 7,
                      "tank_id": 4465,
                      "name": "Hafen",
                      "nation": "other"
                    },
                    "4721": {
                      "tier": 7,
                      "tank_id": 4721,
                      "name": "Gravedigger",
                      "nation": "other"
                    },
                    "4977": {
                      "tier": 5,
                      "tank_id": 4977,
                      "name": "Scavenger",
                      "nation": "other"
                    },
                    "5233": {
                      "tier": 7,
                      "tank_id": 5233,
                      "name": "Smasher",
                      "nation": "other"
                    },
                    "5489": {
                      "tier": 5,
                      "tank_id": 5489,
                      "name": "Y5 T-34",
                      "nation": "other"
                    },
                    "5745": {
                      "tier": 6,
                      "tank_id": 5745,
                      "name": "Y5 Firefly",
                      "nation": "other"
                    },
                    "6001": {
                      "tier": 7,
                      "tank_id": 6001,
                      "name": "Y5 ELC bis",
                      "nation": "other"
                    },
                    "6257": {
                      "tier": 7,
                      "tank_id": 6257,
                      "name": "M4/FL10",
                      "nation": "other"
                    },
                    "7025": {
                      "tier": 7,
                      "tank_id": 7025,
                      "name": "Vulcan",
                      "nation": "other"
                    },
                    "7281": {
                      "tier": 7,
                      "tank_id": 7281,
                      "name": "Lycan",
                      "nation": "other"
                    },
                    "7537": {
                      "tier": 5,
                      "tank_id": 7537,
                      "name": "Nightmare",
                      "nation": "other"
                    },
                    "7793": {
                      "tier": 7,
                      "tank_id": 7793,
                      "name": "Annihilator",
                      "nation": "other"
                    },
                    "8049": {
                      "tier": 5,
                      "tank_id": 8049,
                      "name": "Spike",
                      "nation": "other"
                    },
                    "8305": {
                      "tier": 7,
                      "tank_id": 8305,
                      "name": "Titan H-Nd",
                      "nation": "other"
                    },
                    "8561": {
                      "tier": 6,
                      "tank_id": 8561,
                      "name": "Titan T24 57",
                      "nation": "other"
                    },
                    "8817": {
                      "tier": 5,
                      "tank_id": 8817,
                      "name": "Titan Mk. I",
                      "nation": "other"
                    },
                    "9329": {
                      "tier": 6,
                      "tank_id": 9329,
                      "name": "Titan-150",
                      "nation": "other"
                    },
                    "9841": {
                      "tier": 6,
                      "tank_id": 9841,
                      "name": "Rover",
                      "nation": "other"
                    },
                    "63601": {
                      "tier": 7,
                      "tank_id": 63601,
                      "name": "Dracula",
                      "nation": "other"
                    }
                  }
                }
                """;
    }
}
