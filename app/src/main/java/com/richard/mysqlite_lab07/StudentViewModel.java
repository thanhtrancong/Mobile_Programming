package com.richard.mysqlite_lab07;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class StudentViewModel extends AndroidViewModel {
    private final StudentRepository repository;
    private final LiveData<List<Sinhvien>> students;

    public StudentViewModel(@NonNull Application application) {
        super(application);
        repository = new StudentRepository(application);
        students = repository.getStudentsLive();
    }

    public LiveData<List<Sinhvien>> getStudents() {
        return students;
    }

    public void addStudent(Sinhvien sv, Runnable onComplete) {
        repository.addStudent(sv, onComplete);
    }

    public void updateStudent(Sinhvien sv, Runnable onComplete) {
        repository.updateStudent(sv, onComplete);
    }

    public void deleteStudent(int id, Runnable onComplete) {
        repository.deleteStudent(id, onComplete);
    }

    public void deleteAll(Runnable onComplete) {
        repository.deleteAll(onComplete);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.close();
    }
}

