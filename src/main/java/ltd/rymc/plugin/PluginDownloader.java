package ltd.rymc.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PluginDownloader {

    private static final Logger LOGGER = LogManager.getLogger("PluginDownloader");

    private final int resourceID;

    public PluginDownloader(int resourceID) {
        this.resourceID = resourceID;
    }

    public String getDownloadURL() throws IOException {
        LOGGER.info("Getting download url from resource");
        String data = getFromUrl(getResourceDataURL(resourceID));
        if (data == null || data.isEmpty()) return null;

        JsonArray versions = JsonParser.parseString(data.trim()).getAsJsonObject().getAsJsonArray("versions");

        int versionID = versions.get(0).getAsJsonObject().get("id").getAsInt();
        LOGGER.info("Version: {}", versionID);
        return getDownloadURL(resourceID, versionID).toString();
    }


    private static String getFromUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) return null;

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private static URL getResourceDataURL(int resourceID) throws IOException {
        return new URL("https://api.spiget.org/v2/resources/" + resourceID);
    }

    private static URL getDownloadURL(int resourceID, int versionID) throws IOException {
        return new URL("https://www.spigotmc.org/resources/" + resourceID + "/download?version=" + versionID);
    }
}
