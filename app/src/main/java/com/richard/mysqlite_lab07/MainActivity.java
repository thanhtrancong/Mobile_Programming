package com.richard.mysqlite_lab07;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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

    // UI Components
    // New ListView to display the list of students
    ListView lvStudentList;

    // Buttons for student management operations
    Button btnAddStudent, btnRefresh, btnDeleteAll;

    // TextView to show information and selected item
    TextView tvInfo;

    // ArrayAdapter to bind student data to the ListView
    ArrayAdapter<String> adapter;

    // ArrayList to store student information as strings
    ArrayList<String> studentDataList;

    // ArrayList to store actual Student objects for easy access
    ArrayList<Sinhvien> studentObjectList;

    // Database manager instance
    Quanlysinhvien db;

    // Variable to track the last clicked position for double-click detection
    private int lastClickedPosition = -1;
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // milliseconds

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

        // Initialize all UI components
        initializeViews();

        // Create and initialize the database manager class
        db = new Quanlysinhvien(this);

        // Insert sample students into the database (only on first run)
        // Comment this out after first run to avoid duplicates
        db.addSinhvien(new Sinhvien("Nguyen", "An", "C21CNTT"));
        db.addSinhvien(new Sinhvien("Le", "Binh", "C21CNTT"));
        db.addSinhvien(new Sinhvien("Tran", "Cuong", "C21CNTT"));
        db.addSinhvien(new Sinhvien("Pham", "Dung", "C21CNTT"));

        // Load student data from database
        loadStudentData();

        // Set up button click listeners
        setupButtonListeners();

        // Set up ListView event handlers
        setupListViewListeners();
    }

    /**
     * Initialize all view components from the layout
     */
    private void initializeViews() {
        // Old code for TextView (kept for reference)
        // stdlist = findViewById(R.id.tvstudentlist);

        // Initialize the ListView from the layout
        lvStudentList = findViewById(R.id.lvStudentList);

        // Initialize buttons
        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);

        // Initialize info TextView
        tvInfo = findViewById(R.id.tvInfo);

        // Initialize the ArrayLists to store student data
        studentDataList = new ArrayList<>();
        studentObjectList = new ArrayList<>();
    }

    /**
     * Load student data from database and display in ListView
     */
    private void loadStudentData() {
        // Clear existing data
        studentDataList.clear();
        studentObjectList.clear();

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
            // Add formatted student information to the display list
            studentDataList.add(s.toString());
            // Keep reference to the actual student object
            studentObjectList.add(s);
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

        // Update info text with student count
        tvInfo.setText("Total Students: " + students.size() + " | Tap to select • Long press to delete • Double tap to edit");
    }

    /**
     * Set up click listeners for all buttons
     */
    private void setupButtonListeners() {
        // Add Student button - Opens dialog to add new student
        btnAddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddStudentDialog();
            }
        });

        // Refresh button - Reload data from database
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStudentData();
                Toast.makeText(MainActivity.this, "List refreshed!", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete All button - Remove all students with confirmation
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAllConfirmationDialog();
            }
        });
    }

    /**
     * Set up event listeners for ListView interactions
     * - Single click: Select item and show info
     * - Double click: Edit student
     * - Long press: Delete student
     */
    private void setupListViewListeners() {
        // Single click listener - Select item and detect double click
        lvStudentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the current time
                long clickTime = System.currentTimeMillis();

                // Check if this is a double click (same position, within time threshold)
                if (position == lastClickedPosition &&
                    (clickTime - lastClickTime) < DOUBLE_CLICK_TIME_DELTA) {
                    // Double click detected - Edit student
                    onDoubleClick(position);
                    // Reset to prevent triple click
                    lastClickedPosition = -1;
                } else {
                    // Single click - Show selected student info
                    onSingleClick(position);
                    // Update last click info for double click detection
                    lastClickedPosition = position;
                    lastClickTime = clickTime;
                }
            }
        });

        // Long click listener - Delete student
        lvStudentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Show delete confirmation dialog
                showDeleteStudentDialog(position);
                return true; // Return true to indicate the event was handled
            }
        });
    }

    /**
     * Handle single click on ListView item - Display selected student info
     * @param position The position of clicked item
     */
    private void onSingleClick(int position) {
        if (position < studentObjectList.size()) {
            Sinhvien student = studentObjectList.get(position);
            String info = "Selected: " + student.getHo() + " " + student.getTen() +
                         " (ID: " + student.getId() + ", Class: " + student.getLop() + ")";
            tvInfo.setText(info);
            Toast.makeText(this, "Student selected. Double tap to edit.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle double click on ListView item - Edit student
     * @param position The position of clicked item
     */
    private void onDoubleClick(int position) {
        if (position < studentObjectList.size()) {
            Sinhvien student = studentObjectList.get(position);
            showEditStudentDialog(student, position);
        }
    }

    /**
     * Show dialog to add a new student
     */
    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Student");

        // Create input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etHo = new EditText(this);
        etHo.setHint("Last Name (Ho)");
        layout.addView(etHo);

        final EditText etTen = new EditText(this);
        etTen.setHint("First Name (Ten)");
        layout.addView(etTen);

        final EditText etLop = new EditText(this);
        etLop.setHint("Class (Lop)");
        layout.addView(etLop);

        builder.setView(layout);

        // Add button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String ho = etHo.getText().toString().trim();
            String ten = etTen.getText().toString().trim();
            String lop = etLop.getText().toString().trim();

            // Validate input
            if (ho.isEmpty() || ten.isEmpty() || lop.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add student to database
            Sinhvien newStudent = new Sinhvien(ho, ten, lop);
            db.addSinhvien(newStudent);

            // Reload the list
            loadStudentData();

            Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show();
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Show dialog to edit an existing student
     * @param student The student to edit
     * @param position The position in the list
     */
    private void showEditStudentDialog(Sinhvien student, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Student");

        // Create input fields with current values
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etHo = new EditText(this);
        etHo.setHint("Last Name (Ho)");
        etHo.setText(student.getHo());
        layout.addView(etHo);

        final EditText etTen = new EditText(this);
        etTen.setHint("First Name (Ten)");
        etTen.setText(student.getTen());
        layout.addView(etTen);

        final EditText etLop = new EditText(this);
        etLop.setHint("Class (Lop)");
        etLop.setText(student.getLop());
        layout.addView(etLop);

        builder.setView(layout);

        // Update button
        builder.setPositiveButton("Update", (dialog, which) -> {
            String ho = etHo.getText().toString().trim();
            String ten = etTen.getText().toString().trim();
            String lop = etLop.getText().toString().trim();

            // Validate input
            if (ho.isEmpty() || ten.isEmpty() || lop.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update student object
            student.setHo(ho);
            student.setTen(ten);
            student.setLop(lop);

            // Update in database
            db.updateSinhvien(student);

            // Reload the list
            loadStudentData();

            Toast.makeText(this, "Student updated successfully!", Toast.LENGTH_SHORT).show();
        });

        // Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Show confirmation dialog to delete a single student
     * @param position The position of student to delete
     */
    private void showDeleteStudentDialog(int position) {
        if (position < studentObjectList.size()) {
            Sinhvien student = studentObjectList.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Student");
            builder.setMessage("Are you sure you want to delete " +
                             student.getHo() + " " + student.getTen() + "?");

            builder.setPositiveButton("Delete", (dialog, which) -> {
                // Delete from database
                db.deleteSinhvien(student.getId());

                // Reload the list
                loadStudentData();

                Toast.makeText(this, "Student deleted!", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        }
    }

    /**
     * Show confirmation dialog to delete all students
     */
    private void showDeleteAllConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All Students");
        builder.setMessage("Are you sure you want to delete ALL students? This action cannot be undone!");

        builder.setPositiveButton("Delete All", (dialog, which) -> {
            // Delete all students from database
            for (Sinhvien student : studentObjectList) {
                db.deleteSinhvien(student.getId());
            }

            // Reload the list
            loadStudentData();

            Toast.makeText(this, "All students deleted!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database connection when activity is destroyed
        if (db != null) {
            db.close();
        }
    }
}