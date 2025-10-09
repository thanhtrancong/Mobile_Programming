package com.richard.mytopten;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Quản lý danh sách Top 3 người chơi.
 * File gốc (khởi tạo) đặt trong assets/high_scores.txt.
 * Khi chạy lần đầu sẽ copy sang internal storage rồi ghi đè về sau.
 */
public class PlayerList {
    private static final String INTERNAL_FILE_NAME = "high_scores.txt"; // internal storage
    private static final String ASSET_FILE_PATH = "high_scores.txt";   // assets root
    private final List<Player> players = new ArrayList<>();

    public List<Player> getPlayers() {
        return players;
    }

    public void load(Context context) {
        players.clear();
        File internal = new File(context.getFilesDir(), INTERNAL_FILE_NAME);
        if (!internal.exists()) {
            // Try copy from assets first time
            try (InputStream is = context.getAssets().open(ASSET_FILE_PATH)) {
                copyStreamToFile(is, internal);
            } catch (IOException e) {
                // If asset missing, create empty file
                try {
                    if (internal.createNewFile()) {
                        // ignore
                    }
                } catch (IOException ignored) {}
            }
        }
        // Read from internal file
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(internal), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    try {
                        int score = Integer.parseInt(parts[1].trim());
                        players.add(new Player(name, score));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException ignored) {}
        sortAndTrim();
    }

    public boolean tryInsert(String name, int score) {
        if (name == null || name.isEmpty()) return false;
        if (score < 0) return false;
        Player existing = null;
        for (Player p : players) {
            if (p.getName().equalsIgnoreCase(name)) {
                existing = p;
                break;
            }
        }
        if (existing != null) {
            if (score > existing.getScore()) {
                existing.setScore(score);
            } else {
                // No improvement; still considered inside top 3, but no need to save unless ordering changes
            }
        } else {
            players.add(new Player(name, score));
        }
        sortAndTrim();
        // If player not in top 3 after update and was a new insert attempt -> fail
        boolean withinTop3 = false;
        int limit = Math.min(3, players.size());
        for (int i = 0; i < limit; i++) {
            if (players.get(i).getName().equalsIgnoreCase(name)) {
                withinTop3 = true;
                break;
            }
        }
        if (!withinTop3) {
            // Remove candidate if it was newly added but not qualified
            if (existing == null) {
                // remove the newly added one (at tail after trimming logic maybe)
                for (int i = players.size() - 1; i >= 0; i--) {
                    if (players.get(i).getName().equalsIgnoreCase(name) && players.get(i).getScore() == score) {
                        players.remove(i);
                        break;
                    }
                }
            }
            return false;
        }
        return true;
    }

    public void save(Context context) {
        File internal = new File(context.getFilesDir(), INTERNAL_FILE_NAME);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(internal, false), StandardCharsets.UTF_8))) {
            for (Player p : players) {
                bw.write(p.getName() + "," + p.getScore());
                bw.newLine();
            }
        } catch (IOException ignored) {}
    }

    private void sortAndTrim() {
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                return Integer.compare(o2.getScore(), o1.getScore());
            }
        });
        if (players.size() > 3) {
            while (players.size() > 3) players.remove(players.size() - 1);
        }
    }

    private void copyStreamToFile(InputStream is, File out) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buf = new byte[4096];
            int r;
            while ((r = is.read(buf)) != -1) {
                fos.write(buf, 0, r);
            }
        }
    }
}

