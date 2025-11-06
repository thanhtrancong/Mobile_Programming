package com.richard.mysqlite_lab07;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

@RunWith(AndroidJUnit4.class)
public class AddEditStudentActivityTest {

    @Test
    public void formValidation_workflow() {
        try (ActivityScenario<AddEditStudentActivity> scenario = ActivityScenario.launch(AddEditStudentActivity.class)) {
            // Initially Save button disabled
            Espresso.onView(ViewMatchers.withId(R.id.btnSave)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
            Espresso.onView(ViewMatchers.withId(R.id.btnSave)).check(ViewAssertions.matches(ViewMatchers.withText("Lưu")));

            // Enter only Ho
            Espresso.onView(ViewMatchers.withId(R.id.etHo)).perform(ViewActions.typeText("A"));
            Espresso.closeSoftKeyboard();

            // Save still disabled
            Espresso.onView(ViewMatchers.withId(R.id.btnSave)).check(ViewAssertions.matches(ViewMatchers.isEnabled()));

            // Fill remaining
            Espresso.onView(ViewMatchers.withId(R.id.etTen)).perform(ViewActions.typeText("B"));
            Espresso.onView(ViewMatchers.withId(R.id.etLop)).perform(ViewActions.typeText("C"));
            Espresso.closeSoftKeyboard();

            // Save enabled now
            Espresso.onView(ViewMatchers.withId(R.id.btnSave)).check(ViewAssertions.matches(ViewMatchers.isEnabled()));

        }
    }
}

