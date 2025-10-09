package com.richard.mytopten;

// Lớp Player dùng để lưu thông tin tên và điểm số của người chơi
public class Player {
    private final String name; // Tên người chơi
    private int score;         // Điểm số

    // Hàm khởi tạo
    public Player(String name, int score) {
        this.name = name;
        this.score = score;
    }

    // Lấy tên người chơi
    public String getName() { return name; }

    // Lấy điểm số
    public int getScore() { return score; }

    // Cập nhật điểm số
    public void setScore(int score) { this.score = score; }
}
