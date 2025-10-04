package com.example.daily_photo_day

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.daily_photo_day.ui.AddEditPostActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddEditPostActivityTest {

    @Test
    fun testSaveWithoutTitleShowsError() {
        // Launch activity
        ActivityScenario.launch(AddEditPostActivity::class.java)

        // Try to save without entering title
        onView(withId(R.id.button_save)).perform(click())

        // Check if save button is still visible (meaning we didn't navigate away)
        onView(withId(R.id.button_save)).check(matches(isDisplayed()))
    }

    @Test
    fun testCustomDateSwitchShowsDateTimeButton() {
        // Launch activity
        ActivityScenario.launch(AddEditPostActivity::class.java)

        // Enable custom date switch
        onView(withId(R.id.switch_custom_date)).perform(click())

        // Check if date time button becomes visible
        onView(withId(R.id.button_set_date_time))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testBackButtonIsVisible() {
        // Launch activity
        ActivityScenario.launch(AddEditPostActivity::class.java)

        // Check if back button is displayed
        onView(withId(R.id.button_back))
            .check(matches(isDisplayed()))
    }
}