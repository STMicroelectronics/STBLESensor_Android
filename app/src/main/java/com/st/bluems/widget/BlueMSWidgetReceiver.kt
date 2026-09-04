package com.st.bluems.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlueMSWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BlueMSWidget()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_RELOAD) {
            val favoriteDevicesString = intent.getStringExtra(EXTRA_FAVORITE_DEVICES)
            val boardsSettingString = intent.getStringExtra(EXTRA_BOARDS_SETTING)

            val pendingResult = goAsync()
            if((favoriteDevicesString!=null) && (boardsSettingString!=null)) {
                coroutineScope.launch {
                    try {
                        val manager = GlanceAppWidgetManager(context)
                        val glanceIds = manager.getGlanceIds(BlueMSWidget::class.java)
                        glanceIds.forEach { glanceId ->
                            updateAppWidgetState(context, glanceId) { prefs ->
                                prefs[BlueMSWidgetStateKeys.favoriteDevicesKey] =
                                    favoriteDevicesString
                                prefs[BlueMSWidgetStateKeys.boardsSettingsKey] = boardsSettingString
                            }
                            glanceAppWidget.update(context, glanceId)
                        }
                    } catch (e: Exception) {
                        Log.e("BlueMSWidgetReceiver", "Error updating widget state", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        } else {
            super.onReceive(context, intent)
        }
    }

    companion object {
        const val ACTION_RELOAD = "com.st.bluems.widget.ACTION_RELOAD"
        const val EXTRA_FAVORITE_DEVICES = "com.st.bluems.widget.EXTRA_FAVORITE_DEVICES"
        const val EXTRA_BOARDS_SETTING = "com.st.bluems.widget.EXTRA_BOARDS_SETTING"
    }
}