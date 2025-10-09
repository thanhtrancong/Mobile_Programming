package com.richard.mytopten;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện
    private EditText etName, etScore;
    private ListView lvHighScores;
    private ArrayAdapter<String> adapter;
    private final List<String> displayItems = new ArrayList<>();

    // Quản lý danh sách Top 3 bằng file thông qua lớp PlayerList
    private final PlayerList playerList = new PlayerList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Thiết lập padding cho layout để tránh bị che bởi thanh trạng thái/hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ các view từ layout
        etName = findViewById(R.id.etName);
        etScore = findViewById(R.id.etScore);
        Button btnAdd = findViewById(R.id.btnAdd);
        lvHighScores = findViewById(R.id.lvHighScores);

        // Adapter để hiển thị danh sách high scores lên ListView
        adapter = new ArrayAdapter<>(this, R.layout.item_high_score, R.id.tvItemText, displayItems);
        lvHighScores.setAdapter(adapter);

        // Tải dữ liệu high scores từ bộ nhớ và hiển thị lên giao diện
        loadAndDisplayScores();

        // Xử lý sự kiện khi nhấn nút "Thêm / Cập nhật"
        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String scoreStr = etScore.getText().toString().trim();

            // Kiểm tra tên người chơi
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Nhập tên", Toast.LENGTH_SHORT).show();
                return;
            }
            // Kiểm tra điểm số
            if (TextUtils.isEmpty(scoreStr)) {
                Toast.makeText(this, "Nhập điểm", Toast.LENGTH_SHORT).show();
                return;
            }
            int score;
            try {
                score = Integer.parseInt(scoreStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Điểm không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            // Kiểm tra điểm số phải >= 0
            if (score < 0) {
                Toast.makeText(this, "Điểm phải >= 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thử thêm/cập nhật điểm vào danh sách Top 3
            boolean updated = playerList.tryInsert(name, score);
            if (updated) {
                // Nếu được cập nhật, lưu lại vào bộ nhớ và cập nhật giao diện
                playerList.save(this);
                updateScoreViews();
                Toast.makeText(this, "Đã cập nhật High Scores", Toast.LENGTH_SHORT).show();
                etName.setText("");
                etScore.setText("");
            } else {
                // Nếu không đủ điểm vào Top 3
                Toast.makeText(this, "Không đủ điểm vào Top 3", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm tải danh sách high scores từ bộ nhớ và hiển thị lên giao diện
    private void loadAndDisplayScores() {
        playerList.load(this);
        updateScoreViews();
    }

    // Hàm cập nhật danh sách hiển thị trên ListView
    private void updateScoreViews() {
        displayItems.clear();
        List<Player> players = playerList.getPlayers();
        for (int i = 0; i < 3; i++) {
            if (i < players.size()) {
                Player p = players.get(i);
                // Hiển thị thứ hạng, tên và điểm số
                displayItems.add((i + 1) + ". " + p.getName() + " - " + p.getScore());
            } else {
                // Nếu chưa đủ 3 người, hiển thị "--"
                displayItems.add((i + 1) + ". --");
            }
        }
        adapter.notifyDataSetChanged();
    }
}