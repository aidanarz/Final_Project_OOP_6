package com.NetRoyale.network;

import com.badlogic.gdx.Gdx;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScoreClient {
    private static final String SERVER_URL = "http://localhost:8081/scores";

    public static void sendScoreAsync(final String player, final int score) {
        new Thread(() -> sendScore(player, score)).start();
    }

    public static void sendScore(String player, int score) {
        try {
            URL url = new URL(SERVER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);

            String json = String.format("{\"player\":\"%s\",\"score\":%d}", escape(player), score);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
            }

            int rc = conn.getResponseCode();
            if (rc >= 200 && rc < 300) {
                Gdx.app.log("ScoreClient", "Posted score: " + json);
            } else {
                Gdx.app.log("ScoreClient", "Failed to post score (code=" + rc + ")");
            }
            conn.disconnect();
        } catch (Exception e) {
            Gdx.app.error("ScoreClient", "Error sending score", e);
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
