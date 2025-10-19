package com.example.autotapper

import android.provider.Settings
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testServiceStatusToggle() {
        val contentResolver = InstrumentationRegistry.getInstrumentation().context.contentResolver

        // Initial state: service is disabled
        onView(withId(R.id.service_status_textview)).check(matches(withText("Service is disabled")))

        // Enable the service
        val serviceName = "com.example.autotapper/com.example.autotapper.MyAccessibilityService"
        Settings.Secure.putString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, serviceName)

        // Re-check the status
        onView(withId(R.id.service_status_textview)).check(matches(withText("Service is enabled")))
    }
}