package com.richard.mysqlite_lab07;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView stdlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        stdlist = findViewById(R.id.tvstudentlist);

        // Use the new manager class
        Quanlysinhvien db = new Quanlysinhvien(this);
        // Insert sample students
        db.addSinhvien(new Sinhvien("Nguyen", "An", "C21CNTT"));
        db.addSinhvien(new Sinhvien("Le", "Binh", "C21CNTT"));

        // Fetch and display
        List<Sinhvien> students = db.getAllSv();
        StringBuilder sb = new StringBuilder();
        for (Sinhvien s : students) {
            sb.append(s.toString()).append("\n");
        }
        stdlist.setText(sb.toString());
    }
}