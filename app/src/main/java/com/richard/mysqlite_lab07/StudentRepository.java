package com.richard.mysqlite_lab07;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

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

    private void submitSafe(Runnable task) {
        try {
            executor.execute(task);
        } catch (RejectedExecutionException e) {
            // Executor is shutting down; run task synchronously to ensure onComplete runs.
            try {
                task.run();
            } catch (Exception ignored) {
            }
        }
    }

    private void loadStudentsAsync() {
        submitSafe(() -> {
            List<Sinhvien> list = dbHelper.getAllSv();
            studentsLive.postValue(list);
        });
    }

    public void addStudent(Sinhvien sv, Runnable onComplete) {
        submitSafe(() -> {
            dbHelper.addSinhvien(sv);
            loadStudentsAsync();
            if (onComplete != null) onComplete.run();
        });
    }

    public void updateStudent(Sinhvien sv, Runnable onComplete) {
        submitSafe(() -> {
            dbHelper.updateSinhvien(sv);
            loadStudentsAsync();
            if (onComplete != null) onComplete.run();
        });
    }

    public void deleteStudent(int id, Runnable onComplete) {
        submitSafe(() -> {
            dbHelper.deleteSinhvien(id);
            loadStudentsAsync();
            if (onComplete != null) onComplete.run();
        });
    }

    public void deleteAll(Runnable onComplete) {
        submitSafe(() -> {
            List<Sinhvien> all = dbHelper.getAllSv();
            for (Sinhvien s : all) {
                dbHelper.deleteSinhvien(s.getId());
            }
            loadStudentsAsync();
            if (onComplete != null) onComplete.run();
        });
    }

    public void close() {
        // First stop accepting new tasks and wait for running tasks to finish
        executor.shutdown();
        try {
            // Wait up to 5 seconds for tasks to finish
            if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                // If still not finished, attempt to cancel running tasks
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Now safe to close DB helper
        try {
            dbHelper.close();
        } catch (Exception ignored) {
            // ignore close errors
        }
    }
}
