package com.seyone22.cook

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.seyone22.cook.data.AppContainer
import com.seyone22.cook.data.AppDataContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

private const val BASECURRENCYIDT_PREFERENCE_NAME = "baseCurrencyId"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = BASECURRENCYIDT_PREFERENCE_NAME
)

class CookApplication : Application() {
    lateinit var container: AppContainer
    private val scope = CoroutineScope(SupervisorJob())
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        container = AppDataContainer(this, scope)
    }

    private fun createNotificationChannel() {
        val name = "Channel Name"
        val descriptionText = "Channel Description"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("channel_id", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}