package com.richard.lab06_problem01;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
TextView show;
Button readfile;
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
        show = findViewById(R.id.tvshow);
        readfile = findViewById(R.id.btnreadfile);
        readfile.setOnClickListener(v -> {
           String data = "";
            try {
               InputStream iput = getAssets().open("thongtin.txt");
               int size = iput.available();
               byte[] buffer = new byte[size];
                iput.read(buffer);
                data=new String(buffer);
            } catch (IOException e) {
                show.setText(e.getMessage());
            }
            show.setText(data);
        });
    }
}