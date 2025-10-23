package com.richard.mysqlite_lab07;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
// Old import for TextView (kept for reference)
// import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Old TextView declaration (kept for reference)
    // TextView stdlist;

    // New ListView to display the list of students
    ListView lvStudentList;

    // ArrayAdapter to bind student data to the ListView
    ArrayAdapter<String> adapter;

    // ArrayList to store student information as strings
    ArrayList<String> studentDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display for modern Android UI
        EdgeToEdge.enable(this);
        // Set the layout for this activity
        setContentView(R.layout.activity_main);

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Old code for TextView (kept for reference)
        // stdlist = findViewById(R.id.tvstudentlist);

        // Initialize the ListView from the layout
        lvStudentList = findViewById(R.id.lvStudentList);

        // Initialize the ArrayList to store student data
        studentDataList = new ArrayList<>();

        // Create and initialize the database manager class
        Quanlysinhvien db = new Quanlysinhvien(this);

        // Insert sample students into the database
        db.addSinhvien(new Sinhvien("Nguyen", "An", "C21CNTT"));
        db.addSinhvien(new Sinhvien("Le", "Binh", "C21CNTT"));
        db.addSinhvien(new Sinhvien("Tran", "Cuong", "C21CNTT"));
        db.addSinhvien(new Sinhvien("Pham", "Dung", "C21CNTT"));

        // Fetch all students from the database
        List<Sinhvien> students = db.getAllSv();

        // Old code using StringBuilder to concatenate strings (kept for reference)
        /*
        StringBuilder sb = new StringBuilder();
        for (Sinhvien s : students) {
            sb.append(s.toString()).append("\n");
        }
        stdlist.setText(sb.toString());
        */

        // New code: Convert each student object to string and add to ArrayList
        for (Sinhvien s : students) {
            // Add formatted student information to the list
            studentDataList.add(s.toString());
        }

        // Create an ArrayAdapter to bind the ArrayList to the ListView
        // Uses simple_list_item_1 which is a built-in Android layout for list items
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                studentDataList
        );

        // Set the adapter to the ListView to display the data
        lvStudentList.setAdapter(adapter);

        // Close the database connection when done
        db.close();
    }
}