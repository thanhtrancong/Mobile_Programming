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
    // Tên file nội bộ (internal storage) nơi dữ liệu High Scores sẽ được lưu/ghi mỗi lần thay đổi
    private static final String INTERNAL_FILE_NAME = "high_scores.txt"; // internal storage
    // Đường dẫn file mẫu ban đầu đặt trong thư mục assets (chỉ đọc)
    private static final String ASSET_FILE_PATH = "high_scores.txt";   // assets root
    // Danh sách người chơi đang ở trạng thái bộ nhớ (RAM). Chỉ giữ tối đa 3 phần tử sau khi sort.
    private final List<Player> players = new ArrayList<>();

    /**
     * Trả về danh sách hiện tại (đã được sắp xếp giảm dần sau mỗi thao tác).
     * Lưu ý: Không trả về bản sao để giảm chi phí; đừng sửa trực tiếp từ bên ngoài nếu không thông qua API lớp này.
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Tải dữ liệu High Scores:
     * 1. Nếu file nội bộ chưa tồn tại: cố gắng copy từ assets (lần chạy đầu tiên) hoặc tạo file rỗng.
     * 2. Đọc từng dòng: định dạng "Tên,Điểm".
     * 3. Parse và đưa vào danh sách.
     * 4. Sắp xếp giảm dần & cắt còn 3.
     */
    public void load(Context context) {
        players.clear();
        File internal = new File(context.getFilesDir(), INTERNAL_FILE_NAME);
        if (!internal.exists()) {
            // Thử copy file mẫu từ assets nếu có
            try (InputStream is = context.getAssets().open(ASSET_FILE_PATH)) {
                copyStreamToFile(is, internal);
            } catch (IOException e) {
                // Nếu assets không có hoặc lỗi đọc -> tạo file trống để tránh lỗi về sau
                try {
                    if (internal.createNewFile()) {
                        // Created empty file
                    }
                } catch (IOException ignored) {}
            }
        }
        // Đọc dữ liệu từ file nội bộ (UTF-8)
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(internal), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;              // Bỏ qua dòng rỗng
                String[] parts = line.split(",", 2);  // Chỉ tách làm 2 phần đầu tiên
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    try {
                        int score = Integer.parseInt(parts[1].trim());
                        players.add(new Player(name, score));
                    } catch (NumberFormatException ignored) { /* Bỏ qua dòng sai định dạng điểm */ }
                }
            }
        } catch (IOException ignored) { /* Có thể file đang bị khóa hoặc IO lỗi tạm thời */ }
        sortAndTrim();
    }

    /**
     * Cố gắng chèn/cập nhật một người chơi mới vào Top 3.
     * Quy tắc:
     *  - Nếu tên đã tồn tại: chỉ cập nhật khi điểm mới cao hơn.
     *  - Nếu là người mới: thêm vào danh sách tạm, sort, rồi kiểm tra có nằm trong Top 3 không.
     *  - Nếu không lọt Top 3 thì loại bỏ (và trả về false).
     *
     * @param name  Tên người chơi (không rỗng)
     * @param score Điểm >= 0
     * @return true nếu lọt Top 3 sau thao tác; false nếu không đủ điểm.
     */
    public boolean tryInsert(String name, int score) {
        if (name == null || name.isEmpty()) return false;  // Tên không hợp lệ
        if (score < 0) return false;                      // Điểm âm không chấp nhận

        // Tìm xem người chơi đã tồn tại chưa (so sánh không phân biệt hoa thường)
        Player existing = null;
        for (Player p : players) {
            if (p.getName().equalsIgnoreCase(name)) {
                existing = p;
                break;
            }
        }

        if (existing != null) {
            // Cập nhật điểm nếu cao hơn
            if (score > existing.getScore()) {
                existing.setScore(score);
            } else {
                // Không tăng điểm -> vẫn giữ nguyên (nhưng vẫn cần sort để đảm bảo thứ tự nếu có thay đổi nơi khác)
            }
        } else {
            // Người chơi mới
            players.add(new Player(name, score));
        }

        // Sắp xếp & cắt Top 3 tạm thời
        sortAndTrim();

        // Kiểm tra xem sau khi sort người này có nằm trong Top 3 không
        boolean withinTop3 = false;
        int limit = Math.min(3, players.size());
        for (int i = 0; i < limit; i++) {
            if (players.get(i).getName().equalsIgnoreCase(name)) {
                withinTop3 = true;
                break;
            }
        }

        if (!withinTop3) {
            // Nếu là người mới (existing == null) và không lọt top -> loại bỏ để tránh "rác" trong danh sách
            if (existing == null) {
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

    /**
     * Ghi danh sách hiện tại (tối đa 3) xuống file nội bộ.
     * Mỗi dòng: name,score
     */
    public void save(Context context) {
        File internal = new File(context.getFilesDir(), INTERNAL_FILE_NAME);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(internal, false), StandardCharsets.UTF_8))) {
            for (Player p : players) {
                bw.write(p.getName() + "," + p.getScore());
                bw.newLine();
            }
        } catch (IOException ignored) { /* Ghi lỗi -> có thể báo log nếu cần */ }
    }

    /**
     * Sắp xếp danh sách giảm dần theo điểm và giữ lại tối đa 3 phần tử đầu.
     */
    private void sortAndTrim() {
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                return Integer.compare(o2.getScore(), o1.getScore()); // Descending
            }
        });
        if (players.size() > 3) {
            while (players.size() > 3) players.remove(players.size() - 1); // Xóa phần tử cuối đến khi còn 3
        }
    }

    /**
     * Copy nội dung một InputStream (file trong assets) sang file nội bộ.
     */
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
