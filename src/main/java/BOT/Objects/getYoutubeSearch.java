package BOT.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class getYoutubeSearch {
    private static final Logger logger = LoggerFactory.getLogger(getYoutubeSearch.class);
    private static final StringBuilder youtube_Key = new StringBuilder();
    public getYoutubeSearch() {
        try {
            File file = new File("C:\\DiscordServerBotSecrets\\rito-bot\\YOUTUBE_DATA_API_KEY.txt");
            FileReader fileReader = new FileReader(file);
            int singalCh;
            while((singalCh = fileReader.read()) != -1) {
                youtube_Key.append((char) singalCh);
            }
        } catch (Exception e) {

            StackTraceElement[] eStackTrace = e.getStackTrace();
            StringBuilder a = new StringBuilder();
            for (StackTraceElement stackTraceElement : eStackTrace) {
                a.append(stackTraceElement).append("\n");
            }
            logger.warn(a.toString());
        }
    }
    @Nullable
    public static String[][] Search(@NotNull String name) {
        try {
            String[][] returns = new String[10][2];
            String apiurl = "https://www.googleapis.com/youtube/v3/search";
            apiurl += "?key=" + youtube_Key.toString();
            apiurl += "&part=snippet&type=video&maxResults=10&videoEmbeddable=true";
            apiurl += "&q=" + URLEncoder.encode(name, "UTF-8");

            URL url = new URL(apiurl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(response.toString());
            try {
                returns[0][0] = element.getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                returns[0][1] = element.getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
                returns[1][0] = element.getAsJsonObject().get("items").getAsJsonArray().get(1).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                returns[1][1] = element.getAsJsonObject().get("items").getAsJsonArray().get(1).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
                returns[2][0] = element.getAsJsonObject().get("items").getAsJsonArray().get(2).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                returns[2][1] = element.getAsJsonObject().get("items").getAsJsonArray().get(2).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
                returns[3][0] = element.getAsJsonObject().get("items").getAsJsonArray().get(3).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                returns[3][1] = element.getAsJsonObject().get("items").getAsJsonArray().get(3).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
                returns[4][0] = element.getAsJsonObject().get("items").getAsJsonArray().get(4).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                returns[4][1] = element.getAsJsonObject().get("items").getAsJsonArray().get(4).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
                returns[5][0] = element.getAsJsonObject().get("items").getAsJsonArray().get(5).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                returns[5][1] = element.getAsJsonObject().get("items").getAsJsonArray().get(5).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
                returns[6][0] = element.getAsJsonObject().get("items").getAsJsonArray().get(6).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                returns[6][1] = element.getAsJsonObject().get("items").getAsJsonArray().get(6).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
                returns[7][0] = element.getAsJsonObject().get("items").getAsJsonArray().get(7).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                returns[7][1] = element.getAsJsonObject().get("items").getAsJsonArray().get(7).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
                returns[8][0] = element.getAsJsonObject().get("items").getAsJsonArray().get(8).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                returns[8][1] = element.getAsJsonObject().get("items").getAsJsonArray().get(8).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
                returns[9][0] = element.getAsJsonObject().get("items").getAsJsonArray().get(9).getAsJsonObject().get("snippet").getAsJsonObject().get("title").getAsString();
                returns[9][1] = element.getAsJsonObject().get("items").getAsJsonArray().get(9).getAsJsonObject().get("id").getAsJsonObject().get("videoId").getAsString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return returns;

        } catch (IOException e) {
            StackTraceElement[] eStackTrace = e.getStackTrace();
            StringBuilder a = new StringBuilder();
            for (StackTraceElement stackTraceElement : eStackTrace) {
                a.append(stackTraceElement).append("\n");
            }
            logger.warn(a.toString());
        }
        return null;
    }
}
