package com.example.autotapper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.enable_service_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.enable_overlay_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
        updateOverlayStatus()
    }

    private fun updateOverlayStatus() {
        val overlayEnabled = Settings.canDrawOverlays(this)
        val overlayStatusTextView: TextView = findViewById(R.id.overlay_status_textview)
        val overlayStatusIcon: ImageView = findViewById(R.id.overlay_status_icon)

        if (overlayEnabled) {
            overlayStatusTextView.text = getString(R.string.status_overlay_enabled)
            overlayStatusIcon.setImageResource(R.drawable.ic_status_ok)
            overlayStatusIcon.contentDescription = getString(R.string.status_overlay_enabled)
        } else {
            overlayStatusTextView.text = getString(R.string.status_overlay_disabled)
            overlayStatusIcon.setImageResource(R.drawable.ic_status_warning)
            overlayStatusIcon.contentDescription = getString(R.string.status_overlay_disabled)
        }
    }

    private fun updateServiceStatus() {
        val serviceEnabled = isAccessibilityServiceEnabled()
        val serviceStatusTextView: TextView = findViewById(R.id.service_status_textview)
        val serviceStatusIcon: ImageView = findViewById(R.id.service_status_icon)

        if (serviceEnabled) {
            serviceStatusTextView.text = getString(R.string.status_accessibility_enabled)
            serviceStatusIcon.setImageResource(R.drawable.ic_status_ok)
            serviceStatusIcon.contentDescription = getString(R.string.status_accessibility_enabled)
        } else {
            serviceStatusTextView.text = getString(R.string.status_accessibility_disabled)
            serviceStatusIcon.setImageResource(R.drawable.ic_status_warning)
            serviceStatusIcon.contentDescription = getString(R.string.status_accessibility_disabled)
        }
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${packageName}/${MyAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service) == true
    }
}
