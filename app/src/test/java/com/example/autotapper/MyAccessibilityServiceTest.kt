package com.example.autotapper

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowAccessibilityService

@RunWith(RobolectricTestRunner::class)
class MyAccessibilityServiceTest {

    private lateinit var service: MyAccessibilityService
    private lateinit var shadowService: ShadowAccessibilityService

    @Before
    fun setUp() {
        service = Robolectric.setupService(MyAccessibilityService::class.java)
        shadowService = org.robolectric.Shadows.shadowOf(service)
    }

    @Test
    fun `onAccessibilityEvent clicks approve button`() {
        val approveButtonNode: AccessibilityNodeInfo = mock()
        whenever(approveButtonNode.isClickable).thenReturn(true)

        val rootNode: AccessibilityNodeInfo = mock()
        val nodes = mutableListOf(approveButtonNode)
        whenever(rootNode.findAccessibilityNodeInfosByText("Approve")).thenReturn(nodes)

        service.setRootInActiveWindowForTest(rootNode)

        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        service.onAccessibilityEvent(event)

        verify(approveButtonNode).performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
}