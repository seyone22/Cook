package com.seyone22.cook.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import com.seyone22.cook.CameraLaunchActivity
import com.seyone22.cook.MainActivity
import com.seyone22.cook.R

class CameraQuickTileService : TileService() {
    override fun onTileAdded() {
        qsTile?.apply {
            label = "Log Meal"
            icon = Icon.createWithResource(this@CameraQuickTileService, R.drawable.logo)
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onClick() {
        Log.d("TAG", "onClick: Clicked!")

        val intent = Intent(this, CameraLaunchActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        startActivityAndCollapse(pendingIntent)
    }
}
