package com.richard.mysqlite_lab07;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentRepository {
    private final Quanlysinhvien dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StudentRepository(Context context) {
        dbHelper = new Quanlysinhvien(context.getApplicationContext());
    }

    // Expose LiveData for the list
    private final MutableLiveData<List<Sinhvien>> studentsLive = new MutableLiveData<>();

    public LiveData<List<Sinhvien>> getStudentsLive() {
        loadStudentsAsync();
        return studentsLive;
    }

    private void loadStudentsAsync() {
        executor.execute(() -> {
            List<Sinhvien> list = dbHelper.getAllSv();
            studentsLive.postValue(list);
        });
    }

    public void addStudent(Sinhvien sv, Runnable onComplete) {
        executor.execute(() -> {
            dbHelper.addSinhvien(sv);
            loadStudentsAsync();
            if (onComplete != null) onComplete.run();
        });
    }

    public void updateStudent(Sinhvien sv, Runnable onComplete) {
        executor.execute(() -> {
            dbHelper.updateSinhvien(sv);
            loadStudentsAsync();
            if (onComplete != null) onComplete.run();
        });
    }

    public void deleteStudent(int id, Runnable onComplete) {
        executor.execute(() -> {
            dbHelper.deleteSinhvien(id);
            loadStudentsAsync();
            if (onComplete != null) onComplete.run();
        });
    }

    public void deleteAll(Runnable onComplete) {
        executor.execute(() -> {
            List<Sinhvien> all = dbHelper.getAllSv();
            for (Sinhvien s : all) {
                dbHelper.deleteSinhvien(s.getId());
            }
            loadStudentsAsync();
            if (onComplete != null) onComplete.run();
        });
    }

    public void close() {
        dbHelper.close();
        executor.shutdownNow();
    }
}

