package com.example.autotapper

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

class ConfirmationOverlay(
    private val context: Context,
    private val listener: (Decision) -> Unit
) {

    enum class Decision { APPROVE, DECLINE }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null

    fun show(title: String) {
        if (overlayView != null) {
            return
        }

        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.overlay_confirmation, null)
        overlayView = view

        val messageText = view.findViewById<TextView>(R.id.confirmation_message)
        val approveButton = view.findViewById<Button>(R.id.approve_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        messageText.text = context.getString(R.string.confirm_message, title)
        approveButton.setOnClickListener {
            listener(Decision.APPROVE)
            dismiss()
        }
        cancelButton.setOnClickListener {
            listener(Decision.DECLINE)
            dismiss()
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = 120
        }

        windowManager.addView(view, params)
    }

    fun dismiss() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: IllegalArgumentException) {
                // View was not attached.
            }
        }
        overlayView = null
    }
}
