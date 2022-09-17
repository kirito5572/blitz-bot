package me.kirito5572.objects;

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

import static me.kirito5572.App.openFileData;

public class getYoutubeSearch {
    private static final Logger logger = LoggerFactory.getLogger(getYoutubeSearch.class);
    private static final String youtubeKey = openFileData("YOUTUBE_DATA_API_KEY");
    /** @noinspection unused*/
    public getYoutubeSearch() {
    }
    @Nullable
    public static String[][] Search(@NotNull String name) {
        try {
            String[][] returns = new String[10][2];
            String apiURL = "https://www.googleapis.com/youtube/v3/search";
            apiURL += "?key=" + youtubeKey;
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
                e.printStackTrace();
            }
            return returns;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
