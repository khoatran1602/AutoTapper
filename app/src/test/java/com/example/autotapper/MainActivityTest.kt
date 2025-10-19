package com.example.autotapper

import android.app.Application
import android.content.ContentResolver
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    private lateinit var contentResolver: ContentResolver
    private lateinit var mainActivity: MainActivity

    @Before
    fun setUp() {
        contentResolver = ApplicationProvider.getApplicationContext<Application>().contentResolver
        mainActivity = Robolectric.buildActivity(MainActivity::class.java).create().get()
    }

    @Test
    fun `isAccessibilityServiceEnabled returns true when service is enabled`() {
        val serviceName = "com.example.autotapper/com.example.autotapper.MyAccessibilityService"
        Settings.Secure.putString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, serviceName)

        assertTrue(mainActivity.isAccessibilityServiceEnabled())
    }

    @Test
    fun `isAccessibilityServiceEnabled returns false when service is disabled`() {
        Settings.Secure.putString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "")

        assertFalse(mainActivity.isAccessibilityServiceEnabled())
    }
}
