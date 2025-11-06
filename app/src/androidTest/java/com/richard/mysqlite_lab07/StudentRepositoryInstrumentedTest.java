package com.richard.mysqlite_lab07;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class StudentRepositoryInstrumentedTest {
    private StudentRepository repository;

    @Before
    public void setup() {
        Context ctx = ApplicationProvider.getApplicationContext();
        repository = new StudentRepository(ctx);
    }

    @After
    public void tearDown() {
        repository.deleteAll(null);
        repository.close();
    }

    @Test
    public void addAndGetStudent() throws InterruptedException {
        Sinhvien s = new Sinhvien("TestHo","TestTen","TEST123");
        repository.addStudent(s, null);

        // small wait for background executor to finish
        Thread.sleep(200);

        List<Sinhvien> all = repository.getStudentsLive().getValue();
        Assert.assertNotNull(all);
        Assert.assertTrue(all.size() > 0);

        boolean found = false;
        for (Sinhvien x : all) {
            if ("TestHo".equals(x.getHo()) && "TestTen".equals(x.getTen())) {
                found = true; break;
            }
        }
        Assert.assertTrue(found);
    }
}

