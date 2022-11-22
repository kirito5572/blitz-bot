package me.kirito5572.objects;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleAPI {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAPI.class);
    private final String Key;
    /** @noinspection unused*/
    public GoogleAPI(String Key) {
        this.Key = Key;
    }
    @Nullable
    public String[] @Nullable[] Search(@NotNull String name) {
        try {
            String[][] returns = new String[10][2];
            String apiURL = "https://www.googleapis.com/youtube/v3/search";
            apiURL += "?key=" + Key;
            apiURL += "&part=snippet&type=video&maxResults=10&order=relevance&safeSearch=none";
            apiURL += "&q=" + URLEncoder.encode(name, StandardCharsets.UTF_8);

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            System.out.println(url.toString());
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            JsonElement element = JsonParser.parseString(response.toString());
            JsonArray items = element.getAsJsonObject().get("items").getAsJsonArray();
            try {
                for(int i = 0; i < 10; i++) {
                    returns[i][0] = items.get(i).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                    returns[i][1] = items.get(i).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
                }
            } catch (Exception e) {
                logger.error("youtube api error");
                logger.info(e.getMessage());
                e.printStackTrace();
            }
            return returns;

        } catch (IOException e) {
            logger.error("youtube api error");
            logger.info(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String googleTranslateModule(String inputString) {
        Translate translate = TranslateOptions.newBuilder().setApiKey(Key).build().getService();
        Translation translation = translate.translate(inputString, Translate.TranslateOption.sourceLanguage("en"), Translate.TranslateOption.targetLanguage("ko"));
        return translation.getTranslatedText();
    }
}
