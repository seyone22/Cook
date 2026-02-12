package com.seyone22.cook

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class CameraLaunchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collapseNotificationShade()

        val intent = Intent(this, MainActivity::class.java).apply {
            action = "OPEN_CAMERA_FROM_TILE"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        finish()
    }

    private fun collapseNotificationShade() {
        try {
            val statusBarService = getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val collapse = statusBarManager.getMethod("collapsePanels")
            collapse.isAccessible = true
            collapse.invoke(statusBarService)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
