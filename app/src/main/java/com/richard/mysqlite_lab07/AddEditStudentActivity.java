package com.richard.mysqlite_lab07;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class AddEditStudentActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "mode"; // "add" or "edit"
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_HO = "ho";
    public static final String EXTRA_TEN = "ten";
    public static final String EXTRA_LOP = "lop";

    private EditText etHo, etTen, etLop;
    private Button btnSave, btnCancel;
    private StudentViewModel viewModel;
    private boolean isEdit = false;
    private int editId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_student);

        etHo = findViewById(R.id.etHo);
        etTen = findViewById(R.id.etTen);
        etLop = findViewById(R.id.etLop);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        viewModel = new ViewModelProvider(this).get(StudentViewModel.class);

        // Check incoming intent for edit mode
        if (getIntent() != null && "edit".equals(getIntent().getStringExtra(EXTRA_MODE))) {
            isEdit = true;
            editId = getIntent().getIntExtra(EXTRA_ID, -1);
            etHo.setText(getIntent().getStringExtra(EXTRA_HO));
            etTen.setText(getIntent().getStringExtra(EXTRA_TEN));
            etLop.setText(getIntent().getStringExtra(EXTRA_LOP));
        }

        btnSave.setOnClickListener(v -> onSave());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void onSave() {
        String ho = etHo.getText().toString().trim();
        String ten = etTen.getText().toString().trim();
        String lop = etLop.getText().toString().trim();

        if (ho.isEmpty() || ten.isEmpty() || lop.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEdit && editId != -1) {
            Sinhvien sv = new Sinhvien(editId, ho, ten, lop);
            viewModel.updateStudent(sv, () -> runOnUiThread(() -> {
                Toast.makeText(AddEditStudentActivity.this, "Đã cập nhật sinh viên", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }));
        } else {
            Sinhvien sv = new Sinhvien(ho, ten, lop);
            viewModel.addStudent(sv, () -> runOnUiThread(() -> {
                Toast.makeText(AddEditStudentActivity.this, "Đã thêm sinh viên", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }));
        }
    }
}

