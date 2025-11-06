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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditStudentActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "mode"; // "add" or "edit"
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_HO = "ho";
    public static final String EXTRA_TEN = "ten";
    public static final String EXTRA_LOP = "lop";

    private TextInputLayout layoutHo, layoutTen, layoutLop;
    private EditText etHo, etTen, etLop;
    private Button btnSave, btnCancel;
    private StudentViewModel viewModel;
    private boolean isEdit = false;
    private int editId = -1;
    private boolean saving = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_student);

        layoutHo = findViewById(R.id.layoutHo);
        layoutTen = findViewById(R.id.layoutTen);
        layoutLop = findViewById(R.id.layoutLop);

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

        // Add text watchers for live validation
        SimpleTextWatcher watcher = new SimpleTextWatcher(() -> validateFields());
        etHo.addTextChangedListener(watcher);
        etTen.addTextChangedListener(watcher);
        etLop.addTextChangedListener(watcher);

        // Initial validation
        validateFields();

        btnSave.setOnClickListener(v -> onSave());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void validateFields() {
        String ho = etHo.getText() == null ? "" : etHo.getText().toString().trim();
        String ten = etTen.getText() == null ? "" : etTen.getText().toString().trim();
        String lop = etLop.getText() == null ? "" : etLop.getText().toString().trim();

        boolean hoOk = !ho.isEmpty();
        boolean tenOk = !ten.isEmpty();
        boolean lopOk = !lop.isEmpty();

        layoutHo.setError(hoOk ? null : "Họ không được để trống");
        layoutTen.setError(tenOk ? null : "Tên không được để trống");
        layoutLop.setError(lopOk ? null : "Lớp không được để trống");

        btnSave.setEnabled(hoOk && tenOk && lopOk && !saving);
    }

    private void onSave() {
        if (saving) return;
        validateFields();
        if (!btnSave.isEnabled()) return;

        saving = true;
        btnSave.setEnabled(false);

        String ho = etHo.getText().toString().trim();
        String ten = etTen.getText().toString().trim();
        String lop = etLop.getText().toString().trim();

        if (isEdit && editId != -1) {
            Sinhvien sv = new Sinhvien(editId, ho, ten, lop);
            viewModel.updateStudent(sv, () -> runOnUiThread(() -> {
                saving = false;
                setResult(Activity.RESULT_OK);
                finish();
            }));
        } else {
            Sinhvien sv = new Sinhvien(ho, ten, lop);
            viewModel.addStudent(sv, () -> runOnUiThread(() -> {
                saving = false;
                setResult(Activity.RESULT_OK);
                finish();
            }));
        }
    }

    // SimpleTextWatcher helper (inline class)
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable onChange;
        SimpleTextWatcher(Runnable onChange) { this.onChange = onChange; }
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
        @Override public void afterTextChanged(android.text.Editable s) { onChange.run(); }
    }
}
