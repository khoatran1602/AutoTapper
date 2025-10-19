package com.example.autotapper

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    private val TAG = "AutoTapperService"
    private var inCooldown = false

    private var currentPendingIntent: PendingIntent? = null
    private var awaitingUserConfirmation: Boolean = false
    private var confirmationOverlay: ConfirmationOverlay? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "=== ACCESSIBILITY SERVICE CONNECTED AND RUNNING ===")
        Log.i(TAG, "Service info: ${serviceInfo}")
        Log.i(TAG, "Event types configured: ${serviceInfo?.eventTypes}")
    }

    override fun onDestroy() {
        dismissOverlay()
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            Log.w(TAG, "Received null accessibility event")
            return
        }

        // Debug: Log all events
        val eventTypeString = when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> "TYPE_NOTIFICATION_STATE_CHANGED"
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "TYPE_WINDOW_CONTENT_CHANGED"
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "TYPE_WINDOW_STATE_CHANGED"
            else -> "OTHER (${event.eventType})"
        }
        
        Log.d(TAG, "Received event: $eventTypeString from package: ${event.packageName}")

        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                Log.i(TAG, "Processing notification event")
                handleNotification(event)
            }
            
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                Log.d(TAG, "Processing window content change event")
                // Also check for notification-like content in window changes
                checkForNotificationContent(event)
                handleWindowEvent(event)
            }
            
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d(TAG, "Processing window state change event")
                checkForNotificationContent(event)
                handleWindowEvent(event)
            }
        }
    }

    private fun handleNotification(event: AccessibilityEvent) {
        Log.d(TAG, "handleNotification called with event from package: ${event.packageName}")
        
        if (awaitingUserConfirmation) {
            Log.d(TAG, "Already awaiting user confirmation; ignoring notification event.")
            return
        }

        Log.d(TAG, "Event parcelable data: ${event.parcelableData}")
        Log.d(TAG, "Event parcelable data class: ${event.parcelableData?.javaClass?.name}")
        Log.d(TAG, "Event text: ${event.text}")
        Log.d(TAG, "Event content description: ${event.contentDescription}")
        
        val notification = event.parcelableData as? Notification
        if (notification == null) {
            Log.w(TAG, "No notification data found in event")
            return
        }
        
        val notificationTitle = notification.extras.getString(Notification.EXTRA_TITLE) ?: ""
        val notificationText = notification.extras.getString(Notification.EXTRA_TEXT) ?: ""
        val notificationSubText = notification.extras.getString(Notification.EXTRA_SUB_TEXT) ?: ""

        Log.i(TAG, "Notification details:")
        Log.i(TAG, "  Title: \"$notificationTitle\"")
        Log.i(TAG, "  Text: \"$notificationText\"")
        Log.i(TAG, "  SubText: \"$notificationSubText\"")
        Log.i(TAG, "  Package: ${event.packageName}")

        // Check multiple fields for the sign-in text
        val containsSignIn = notificationTitle.contains("Approve sign-in?", ignoreCase = true) ||
                           notificationText.contains("Approve sign-in?", ignoreCase = true) ||
                           notificationTitle.contains("sign-in", ignoreCase = true) ||
                           notificationText.contains("sign-in", ignoreCase = true) ||
                           notificationTitle.contains("authenticator", ignoreCase = true) ||
                           notificationText.contains("authenticator", ignoreCase = true)
                           
        if (!containsSignIn) {
            Log.d(TAG, "Notification does not contain sign-in related text")
            return
        }
        
        Log.i(TAG, "Found potential sign-in notification!")

        val intentToFire = notification.fullScreenIntent ?: notification.contentIntent
        if (intentToFire == null) {
            Log.w(TAG, "Notification had no PendingIntent to fire.")
            return
        }

        currentPendingIntent = intentToFire
        
        // Try to launch authenticator immediately when sign-in notification appears
        Log.i(TAG, "Attempting immediate launch of authenticator app...")
        val launchIntent = packageManager.getLaunchIntentForPackage("com.azure.authenticator")
        if (launchIntent != null) {
            try {
                startActivity(launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                Log.i(TAG, "SUCCESS: Immediately launched authenticator app")
                startCooldown()
                resetPendingState()
                return // Don't show overlay if we successfully launched
            } catch (e: Exception) {
                Log.e(TAG, "Error immediately launching authenticator app", e)
            }
        }
        
        // Fallback to overlay if immediate launch failed
        awaitingUserConfirmation = true
        showOverlay(notificationTitle)
    }
    
    private fun checkForNotificationContent(event: AccessibilityEvent) {
        // Check if this might be a notification drawer or notification-related window
        val packageName = event.packageName?.toString() ?: ""
        val eventText = event.text?.toString() ?: ""
        
        Log.d(TAG, "Checking window content from package: $packageName")
        Log.d(TAG, "Window content text: $eventText")
        
        // Look for authenticator-related packages or text
        if (packageName.contains("authenticator", ignoreCase = true) ||
            packageName.contains("microsoft", ignoreCase = true) ||
            eventText.contains("sign-in", ignoreCase = true) ||
            eventText.contains("approve", ignoreCase = true) ||
            eventText.contains("authenticator", ignoreCase = true)) {
            
            Log.i(TAG, "Found potential authenticator-related content!")
            Log.i(TAG, "Package: $packageName")
            Log.i(TAG, "Text content: $eventText")
            
            // If this looks like an authenticator notification, try to handle it
            if (eventText.contains("sign-in", ignoreCase = true) ||
                eventText.contains("approve", ignoreCase = true)) {
                handlePotentialAuthenticatorNotification(event, eventText)
            }
        }
    }
    
    private fun handlePotentialAuthenticatorNotification(event: AccessibilityEvent, content: String) {
        if (awaitingUserConfirmation) {
            Log.d(TAG, "Already awaiting user confirmation; ignoring potential notification")
            return
        }
        
        Log.i(TAG, "Detected potential authenticator notification via window content!")
        Log.i(TAG, "Content: $content")
        
        // Since we don't have a PendingIntent from window events, we'll try to launch the authenticator directly
        val launchIntent = packageManager.getLaunchIntentForPackage("com.azure.authenticator")
        if (launchIntent != null) {
            try {
                startActivity(launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                Log.i(TAG, "SUCCESS: Launched Authenticator app from window content detection")
                startCooldown()
            } catch (e: Exception) {
                Log.e(TAG, "Error launching Authenticator app", e)
            }
        } else {
            Log.w(TAG, "Could not get launch intent for com.azure.authenticator")
        }
    }

    private fun handleWindowEvent(event: AccessibilityEvent) {
        if (inCooldown) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                Log.d(TAG, "In cooldown, ignoring window change event.")
            }
            return
        }

        val packageName = event.packageName?.toString() ?: ""
        if (packageName == "com.azure.authenticator") {
            val rootNode = rootInActiveWindow ?: return
            if (findAndClick(rootNode, "Approve")) {
                Log.i(TAG, "SUCCESS: Clicked final 'Approve' button in Authenticator.")
                startCooldown()
            }
            rootNode.recycle()
        }
    }

    private fun showOverlay(notificationTitle: String) {
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Overlay permission missing; cannot prompt user for approval.")
            resetPendingState()
            return
        }

        dismissOverlay()
        confirmationOverlay = ConfirmationOverlay(applicationContext) { decision ->
            when (decision) {
                ConfirmationOverlay.Decision.APPROVE -> handleApproval()
                ConfirmationOverlay.Decision.DECLINE -> {
                    Log.i(TAG, "User declined the approval request via overlay.")
                    resetPendingState()
                }
            }
        }.also {
            it.show(notificationTitle)
        }
    }

    private fun dismissOverlay() {
        confirmationOverlay?.dismiss()
        confirmationOverlay = null
    }

    private fun handleApproval() {
        val pendingIntent = currentPendingIntent
        if (pendingIntent == null) {
            resetPendingState()
            return
        }

        dismissOverlay()
        try {
            pendingIntent.send()
            Log.i(TAG, "User approved request; fired notification PendingIntent.")
            
            // Also try launching the authenticator app directly as backup
            Log.i(TAG, "Launching authenticator app as backup after PendingIntent")
            val launchIntent = packageManager.getLaunchIntentForPackage("com.azure.authenticator")
            if (launchIntent != null) {
                try {
                    startActivity(launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                    Log.i(TAG, "SUCCESS: Launched authenticator app as backup")
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching authenticator app as backup", e)
                }
            } else {
                Log.w(TAG, "Could not get launch intent for authenticator app")
            }
            
            startCooldown()
        } catch (e: PendingIntent.CanceledException) {
            Log.e(TAG, "Error firing the PendingIntent after user approval.", e)
        } finally {
            resetPendingState()
        }
    }

    private fun resetPendingState() {
        dismissOverlay()
        currentPendingIntent = null
        awaitingUserConfirmation = false
    }

    private fun findAndClick(rootNode: AccessibilityNodeInfo, text: String): Boolean {
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        if (nodes.isEmpty()) {
            return false
        }

        for (node in nodes) {
            var clickableParent: AccessibilityNodeInfo? = node
            while (clickableParent != null) {
                if (clickableParent.isClickable) {
                    if (clickableParent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        return true
                    }
                }
                clickableParent = clickableParent.parent
            }
        }
        return false
    }

    private fun startCooldown() {
        inCooldown = true
        Handler(Looper.getMainLooper()).postDelayed({
            inCooldown = false
            Log.d(TAG, "Cooldown finished.")
        }, 2000)
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service was interrupted.")
    }
}
