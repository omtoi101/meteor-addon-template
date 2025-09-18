package com.baseminer.basefinder.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {
    public static void sendMessage(String content) {
        if (Config.discordWebhookUrl.isEmpty()) {
            return;
        }


        try {
            URL url = new URI(Config.discordWebhookUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "BaseFinder");
            connection.setDoOutput(true);

            String json = "{\"content\":\"" + content + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            connection.getResponseCode();
            connection.disconnect();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
