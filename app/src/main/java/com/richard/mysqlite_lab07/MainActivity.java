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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
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

    // ViewModel
    private StudentViewModel viewModel;

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

        // Create ViewModel
        viewModel = new ViewModelProvider(this).get(StudentViewModel.class);

        // Observe students LiveData
        viewModel.getStudents().observe(this, students -> {
            // Update UI when data changes
            studentDataList.clear();
            studentObjectList.clear();
            if (students != null) {
                for (Sinhvien s : students) {
                    studentDataList.add(s.toString());
                    studentObjectList.add(s);
                }
            }
            adapter = new ArrayAdapter<>(
                    MainActivity.this,
                    android.R.layout.simple_list_item_1,
                    studentDataList
            );
            lvStudentList.setAdapter(adapter);

            tvInfo.setText("Tổng số: " + (students == null ? 0 : students.size()) + " sinh viên | Nhấn để chọn • Giữ lâu để xóa • Nhấn đúp để sửa");
        });

        // Insert sample students into the database via ViewModel (first run only)
        viewModel.addStudent(new Sinhvien("Nguyen", "An", "C21CNTT"), null);
        viewModel.addStudent(new Sinhvien("Le", "Binh", "C21CNTT"), null);
        viewModel.addStudent(new Sinhvien("Tran", "Cuong", "C21CNTT"), null);
        viewModel.addStudent(new Sinhvien("Pham", "Dung", "C21CNTT"), null);

        // Set up button click listeners
        setupButtonListeners();

        // Set up ListView event handlers
        setupListViewListeners();
    }

    /**
     * Initialize all view components from the layout
     */
    private void initializeViews() {
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
        // No longer directly load; trigger repository to refresh by accessing LiveData
        // The LiveData observer will update UI. We can force a refresh by re-requesting LiveData.
        viewModel.getStudents();
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

        // Refresh button - reload data
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStudentData();
                Toast.makeText(MainActivity.this, "Đã làm mới danh sách! / List refreshed!", Toast.LENGTH_SHORT).show();
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
            String info = "Đã chọn: " + student.getHo() + " " + student.getTen() +
                    " (ID: " + student.getId() + ", Lớp: " + student.getLop() + ")";
            tvInfo.setText(info);
            Toast.makeText(this, "Đã chọn sinh viên. Nhấn đúp để sửa.", Toast.LENGTH_SHORT).show();
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
        builder.setTitle("Thêm Sinh Viên Mới / Add New Student");

        // Create input fields with Vietnamese language support
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // EditText for Last Name with Vietnamese input support
        final EditText etHo = new EditText(this);
        etHo.setHint("Họ (Last Name)");
        etHo.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        // Enable Vietnamese IME (Input Method Editor)
        etHo.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        layout.addView(etHo);

        // EditText for First Name with Vietnamese input support
        final EditText etTen = new EditText(this);
        etTen.setHint("Tên (First Name)");
        etTen.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etTen.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        layout.addView(etTen);

        // EditText for Class with Vietnamese input support
        final EditText etLop = new EditText(this);
        etLop.setHint("Lớp (Class)");
        etLop.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        etLop.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        layout.addView(etLop);

        builder.setView(layout);

        // Add button with Vietnamese text
        builder.setPositiveButton("Thêm / Add", (dialog, which) -> {
            String ho = etHo.getText().toString().trim();
            String ten = etTen.getText().toString().trim();
            String lop = etLop.getText().toString().trim();

            // Validate input
            if (ho.isEmpty() || ten.isEmpty() || lop.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin! / All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add student to database
            Sinhvien newStudent = new Sinhvien(ho, ten, lop);
            viewModel.addStudent(newStudent, null);

            // Reload the list
            loadStudentData();

            Toast.makeText(this, "Đã thêm sinh viên thành công! / Student added successfully!", Toast.LENGTH_SHORT).show();
        });

        // Cancel button with Vietnamese text
        builder.setNegativeButton("Hủy / Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Show dialog to edit an existing student
     * @param student The student to edit
     * @param position The position in the list
     */
    private void showEditStudentDialog(Sinhvien student, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa Thông Tin Sinh Viên / Edit Student");

        // Create input fields with current values and Vietnamese input support
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // EditText for Last Name with Vietnamese input support
        final EditText etHo = new EditText(this);
        etHo.setHint("Họ (Last Name)");
        etHo.setText(student.getHo());
        etHo.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        // Enable Vietnamese IME (Input Method Editor)
        etHo.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        layout.addView(etHo);

        // EditText for First Name with Vietnamese input support
        final EditText etTen = new EditText(this);
        etTen.setHint("Tên (First Name)");
        etTen.setText(student.getTen());
        etTen.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etTen.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        layout.addView(etTen);

        // EditText for Class with Vietnamese input support
        final EditText etLop = new EditText(this);
        etLop.setHint("Lớp (Class)");
        etLop.setText(student.getLop());
        etLop.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        etLop.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        layout.addView(etLop);

        builder.setView(layout);

        // Update button with Vietnamese text
        builder.setPositiveButton("Cập Nhật / Update", (dialog, which) -> {
            String ho = etHo.getText().toString().trim();
            String ten = etTen.getText().toString().trim();
            String lop = etLop.getText().toString().trim();

            // Validate input
            if (ho.isEmpty() || ten.isEmpty() || lop.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin! / All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update student object
            student.setHo(ho);
            student.setTen(ten);
            student.setLop(lop);

            // Update in database
            viewModel.updateStudent(student, null);

            // Reload the list
            loadStudentData();

            Toast.makeText(this, "Đã cập nhật thành công! / Student updated successfully!", Toast.LENGTH_SHORT).show();
        });

        // Cancel button with Vietnamese text
        builder.setNegativeButton("Hủy / Cancel", (dialog, which) -> dialog.cancel());

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
            builder.setTitle("Xóa Sinh Viên / Delete Student");
            builder.setMessage("Bạn có chắc muốn xóa sinh viên " +
                    student.getHo() + " " + student.getTen() + " không?\n\nAre you sure you want to delete this student?");

            builder.setPositiveButton("Xóa / Delete", (dialog, which) -> {
                viewModel.deleteStudent(student.getId(), null);
            });

            builder.setNegativeButton("Hủy / Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        }
    }

    /**
     * Show confirmation dialog to delete all students
     */
    private void showDeleteAllConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa Tất Cả Sinh Viên / Delete All Students");
        builder.setMessage("Bạn có chắc muốn xóa TẤT CẢ sinh viên không? Hành động này không thể hoàn tác!\n\nAre you sure you want to delete ALL students? This action cannot be undone!");

        builder.setPositiveButton("Xóa Tất Cả / Delete All", (dialog, which) -> {
            viewModel.deleteAll(() -> runOnUiThread(() -> Toast.makeText(MainActivity.this, "Đã xóa tất cả sinh viên! / All students deleted!", Toast.LENGTH_SHORT).show()));
        });

        builder.setNegativeButton("Hủy / Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // No direct DB close in Activity anymore; repository will be closed in ViewModel.onCleared
    }
}