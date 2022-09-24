package me.kirito5572.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.HashMap;

public class WargamingAPI {
    private final static Logger logger = LoggerFactory.getLogger(WargamingAPI.class);
    private final String token;
    private final SQLITEConnector sqliteConnector;
    private HashMap<String, AchievementData> achievementMap = new HashMap<>();

    public WargamingAPI(SQLITEConnector sqliteConnector) {
        token = "54634622f8be6ecf13cf721dfe133f32";     //App.openFileData("wargaming");
        this.sqliteConnector = sqliteConnector;

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
    }

    public static void main(String[] args) {
        MySQLConnector mySQLConnector;
        SQLITEConnector sqliteConnector = null;
        try {
            mySQLConnector = new MySQLConnector();
            sqliteConnector = new SQLITEConnector(mySQLConnector);
        } catch (ClassNotFoundException | SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
        WargamingAPI wargamingAPI = new WargamingAPI(sqliteConnector);
    }

    public String getWargamingPlayer(String nickname) throws SQLException {
        String id;
        ResultSet resultSet = sqliteConnector.Select_Query("SELECT * FROM wargamingUserId WHERE nickname = ?",
                new int[]{sqliteConnector.STRING}, new String[]{nickname});
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
            ResultSet resultSet1 = sqliteConnector.Select_Query("SELECT * FROM wargamingUserId WHERE userId = ?",
                    new int[]{sqliteConnector.STRING}, new String[]{id});
            if (resultSet1.next()) {
                sqliteConnector.Insert_Query("UPDATE wargamingUserId SET nickname = ? WHERE userId = ?",
                        new int[]{sqliteConnector.STRING, sqliteConnector.STRING}, new String[]{nickname, id});
            } else {
                sqliteConnector.Insert_Query("INSERT INTO wargamingUserId (nickname, userId) VALUES (?, ?)",
                        new int[]{sqliteConnector.STRING, sqliteConnector.STRING}, new String[]{nickname, id});
            }
        }
        return id;
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
            ratingDataObject.frags = rating.get("frags").getAsInt();
            ratingDataObject.spotted = rating.get("spotted").getAsInt();
            ratingDataObject.accuracy = rating.get("hits").getAsFloat() / rating.get("shots").getAsFloat();
            ratingDataObject.current_season = rating.get("current_season").getAsInt();
            ratingDataObject.reCalibration = rating.get("is_recalibration").getAsBoolean();
            ratingDataObject.reCalibrationTime = rating.get("recalibration_start_time").getAsLong();
            ratingDataObject.reCalibrationBattleLeft = rating.get("calibration_battles_left").getAsInt();
            ratingDataObject.rating = rating.get("mm_rating").getAsInt();
            dataObject.ratingDataObject = ratingDataObject;
            JsonObject all = statistics.get("all").getAsJsonObject();
            AllDataObject allDataObject = new AllDataObject();
            allDataObject.battles = all.get("battles").getAsInt();
            allDataObject.wins = all.get("wins").getAsInt();
            allDataObject.losses = all.get("losses").getAsInt();
            allDataObject.survived = all.get("survived_battles").getAsInt();
            allDataObject.frags = all.get("frags").getAsInt();
            allDataObject.spotted = all.get("spotted").getAsInt();
            allDataObject.accuracy = all.get("hits").getAsFloat() / all.get("shots").getAsFloat();
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
        public int frags;                   //격파
        public int spotted;                 //스팟
        public float accuracy;              //정확도(hits / shots)
        public int current_season;          //현재 시즌
        public boolean reCalibration;       //검증전투 여부
        public long reCalibrationTime;      //검증전투 시작시간
        public int reCalibrationBattleLeft; //검증전투 남은횟수
        public int rating;                  //현재 MMR
    }

    public static class AllDataObject {
        public int battles;                 //전투수
        public int wins;                    //승리
        public int losses;                  //패배
        public int survived;                //생존
        public int frags;                   //격파
        public int spotted;                 //스팟
        public float accuracy;              //정확도(hits / shots)
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
}
