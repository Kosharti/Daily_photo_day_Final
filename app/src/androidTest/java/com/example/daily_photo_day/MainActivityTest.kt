package com.example.daily_photo_day

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testEmptyStateDisplayedWhenNoPosts() {
        // Даем время для инициализации
        Thread.sleep(2000)

        // Проверяем empty state
        onView(withId(R.id.text_empty))
            .check(matches(isDisplayed()))
            .check(matches(withText("Нет фотографий\nДобавьте первую фотографию!")))
    }

    @Test
    fun testBottomNavigationIsDisplayed() {
        Thread.sleep(1000)
        onView(withId(R.id.bottom_navigation))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testToolbarIsDisplayed() {
        Thread.sleep(1000)
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testRecyclerViewIsDisplayed() {
        Thread.sleep(1000)
        onView(withId(R.id.recyclerView_posts))
            .check(matches(isDisplayed()))
    }
}