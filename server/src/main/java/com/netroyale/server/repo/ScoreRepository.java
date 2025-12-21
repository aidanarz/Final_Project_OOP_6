package com.netroyale.server.repo;

import com.netroyale.server.model.Score;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class ScoreRepository {
    private final List<Score> scores = new CopyOnWriteArrayList<>();
    private final Path persistenceFile = Path.of("scores.json");

    public ScoreRepository() {
        load();
    }

    public List<Score> all() {
        List<Score> copy = new ArrayList<>(scores);
        copy.sort(Comparator.comparingInt(Score::getScore).reversed());
        return copy;
    }

    public Score save(Score s) {
        scores.add(s);
        persist();
        return s;
    }

    private void persist() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < scores.size(); i++) {
                Score sc = scores.get(i);
                sb.append(String.format("{\"player\":\"%s\",\"score\":%d}", escape(sc.getPlayer()), sc.getScore()));
                if (i < scores.size() - 1) sb.append(",");
            }
            sb.append("]");
            Files.writeString(persistenceFile, sb.toString());
        } catch (IOException e) {
            // best-effort persistence: ignore errors
        }
    }

    private void load() {
        try {
            if (!Files.exists(persistenceFile)) return;
            String content = Files.readString(persistenceFile).trim();
            if (content.isEmpty()) return;
            // Very small, tolerant JSON parsing (not production-grade)
            content = content.replaceAll("\\\n", "");
            content = content.trim();
            if (!content.startsWith("[")) return;
            content = content.substring(1, content.length() - 1).trim();
            if (content.isEmpty()) return;
            String[] items = content.split("},\\s*\\{");
            for (String item : items) {
                item = item.replaceAll("^\\{", "").replaceAll("}$", "");
                String player = "";
                int score = 0;
                for (String kv : item.split(",")) {
                    String[] pair = kv.split(":", 2);
                    if (pair.length < 2) continue;
                    String key = pair[0].replaceAll("\"", "").trim();
                    String val = pair[1].trim();
                    if (val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length()-1);
                    if (key.equals("player")) player = val;
                    if (key.equals("score")) {
                        try { score = Integer.parseInt(val); } catch (NumberFormatException ignored) {}
                    }
                }
                scores.add(new Score(player, score));
            }
        } catch (IOException e) {
            // ignore
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
