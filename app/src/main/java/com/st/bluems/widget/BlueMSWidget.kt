package com.st.bluems.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.compose.runtime.rememberCoroutineScope
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.st.bluems.MainActivity
import com.st.preferences.BoardSetting
import com.st.preferences.StPreferences
import com.st.preferences.toBoardSetting
import com.st.ui.utils.getBlueStBoardImages
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


@EntryPoint
@InstallIn(SingletonComponent::class)
interface StPreferencesEntryPoint {
    fun stPreferences(): StPreferences
}

private val mockPinnedDevices = listOf(
    "AA:BB:CC:DD:EE:01",
    "AA:BB:CC:DD:EE:02",
    "AA:BB:CC:DD:EE:03",
    "AA:BB:CC:DD:EE:04"
)
private val mockBoardsSetting = mapOf(
    "AA:BB:CC:DD:EE:01" to BoardSetting("AA:BB:CC:DD:EE:01", "SENSOR_TILE_BOX", "My SensorTile"),
    "AA:BB:CC:DD:EE:02" to BoardSetting("AA:BB:CC:DD:EE:02", "STEVAL_STWINKT1B", "Living Room"),
    "AA:BB:CC:DD:EE:03" to BoardSetting("AA:BB:CC:DD:EE:03", "NUCLEO", "Office Hub"),
    "AA:BB:CC:DD:EE:04" to BoardSetting("AA:BB:CC:DD:EE:04", "BLUE_COIN", null)
)


val nodeIdKey = ActionParameters.Key<String>(MainActivity.EXTRA_NODE_ID)


class BlueMSWidget : GlanceAppWidget() {

    // Define how the state is stored (using DataStore Preferences)
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            GlanceTheme {
                BlueMSWidgetContent(
                    pinnedDevices = mockPinnedDevices,
                    boardsSetting = mockBoardsSetting
                )
            }
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val stPreferences = EntryPointAccessors.fromApplication(
            context.applicationContext,
            StPreferencesEntryPoint::class.java
        ).stPreferences()

        val pinnedDevicesInitial = stPreferences.getFavouriteDevices().first()
        val boardsSettingInitial = stPreferences.getBoardsSetting().first().toMap()

        provideContent {
            val prefs = currentState<Preferences>()
            val favoriteDeviceString = prefs[BlueMSWidgetStateKeys.favoriteDevicesKey]
            val boardsSettingString = prefs[BlueMSWidgetStateKeys.boardsSettingsKey]

            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            val pinnedDevices: List<String> =
                favoriteDeviceString?.split(", ") ?: pinnedDevicesInitial

            val boardsSetting: Map<String, BoardSetting> =
                boardsSettingString?.toBoardSetting()?.associate { Pair(it.nodeId, it) }
                    ?: boardsSettingInitial

            GlanceTheme {
                BlueMSWidgetContent(pinnedDevices, boardsSetting, onDelete = { nodeId ->
                    Log.i("delete", "nodeId=$nodeId")
                    stPreferences.unsetFavouriteDevice(nodeId)

                    scope.launch {
                        try {
                            val pinnedList = stPreferences.getFavouriteDevices().first()
                            val settingList =
                                stPreferences.getBoardsSetting().first().map { it.second }

                            val intent = Intent(context, BlueMSWidgetReceiver::class.java).apply {
                                action = BlueMSWidgetReceiver.ACTION_RELOAD
                                putExtra(
                                    BlueMSWidgetReceiver.EXTRA_FAVORITE_DEVICES,
                                    pinnedList.joinToString(", ")
                                )
                                putExtra(
                                    BlueMSWidgetReceiver.EXTRA_BOARDS_SETTING,
                                    Json.encodeToString(settingList)
                                )
                            }
                            context.sendBroadcast(intent)
                        } catch (e: Exception) {
                            Log.e("BlueMSWidget", "Error updating widget after delete", e)
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun BlueMSWidgetContent(
    pinnedDevices: List<String>,
    boardsSetting: Map<String, BoardSetting>,
    onDelete: (String) -> Unit = { /** NOOP **/ }
) {

    Scaffold(
        modifier = GlanceModifier.fillMaxWidth()
            .padding(4.dp).clickable(actionStartActivity<MainActivity>()),
        backgroundColor = GlanceTheme.colors.widgetBackground,
        titleBar = {
            TitleBar(
                startIcon = ImageProvider(com.st.bluems.R.drawable.ic_launcher_widget),
                title = "BlueMS",
                iconColor = GlanceTheme.colors.onSurface,
                textColor = GlanceTheme.colors.onSurface,
//                    actions = {
//                        CircleIconButton(
//                            imageProvider = ImageProvider(com.st.flow_demo.R.drawable.ic_reload),
//                            contentDescription = "Reload",
//                            onClick = actionRunCallback<ReloadAction>(),
//                            backgroundColor = null, // Transparent background as suggested in TitleBar docs
//                            contentColor = GlanceTheme.colors.onSurface
//                        )
//                    }
            )
        }) {

        if (pinnedDevices.isEmpty()) {
            Text(
                text = "No pinned devices",
                style = TextStyle(color = GlanceTheme.colors.onSurface),
                modifier = GlanceModifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                items(pinnedDevices) { nodeId ->
                    if (nodeId.isNotBlank()) {
                        PinnedItem(
                            displayName = boardsSetting[nodeId]?.customName,
                            nodeId = nodeId,
                            boardTypeName = boardsSetting[nodeId]?.boardTypeName ?: "",
                            onClick = actionStartActivity<MainActivity>(
                                actionParametersOf(nodeIdKey to nodeId)
                            ),
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PinnedItem(
    displayName: String?,
    nodeId: String,
    boardTypeName: String,
    onClick: Action,
    onDelete: (String) -> Unit = { /** NOOP **/ }
) {
    Column(modifier = GlanceModifier.background(GlanceTheme.colors.widgetBackground)) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .cornerRadius(8.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .padding(4.dp)
                .clickable(onClick),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
//            Box(
//                modifier = GlanceModifier.size(48.dp)
//                    .cornerRadius(8.dp)
//                    .background(GlanceTheme.colors.tertiaryContainer),
//                contentAlignment = Alignment.Center
//            ) {
            Image(
                provider = ImageProvider(getBlueStBoardImages(boardTypeName)),
                contentDescription = null,
                //modifier = GlanceModifier.size(32.dp)
                modifier = GlanceModifier.padding(start = 4.dp).size(48.dp)
            )
//            }
            Spacer(modifier = GlanceModifier.width(12.dp))
            if (displayName != null) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = displayName,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSecondaryContainer
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = nodeId,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = GlanceTheme.colors.onSecondaryContainer
                        )
                    )
                }
            } else {
                Text(
                    text = nodeId,
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSecondaryContainer
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = GlanceModifier.width(8.dp))
            Box(
                modifier = GlanceModifier.size(40.dp)
                    .cornerRadius(8.dp)
                    .background(GlanceTheme.colors.widgetBackground)
                    .clickable { onDelete(nodeId) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(com.st.bluems.R.drawable.ic_delete),
                    contentDescription = null,
                    modifier = GlanceModifier.size(24.dp),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer),
                )
            }
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
    }
}


/** ----------------------- PREVIEW --------------------------------------- **/
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
fun PreviewPinnedItem() {
    GlanceTheme {
        PinnedItem(
            displayName = "Luca Name",
            nodeId = "AA:BB:CC:DD:FF:GG",
            boardTypeName = "",
            onClick = actionStartActivity<MainActivity>()
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
fun BlueMSWidgetContentPreview() {
    GlanceTheme {
        BlueMSWidgetContent(
            pinnedDevices = mockPinnedDevices,
            boardsSetting = mockBoardsSetting
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
fun BlueMSWidgetContentDarkPreview() {
    GlanceTheme(colors = ColorProviders(darkColorScheme())) {
        BlueMSWidgetContent(
            pinnedDevices = mockPinnedDevices,
            boardsSetting = mockBoardsSetting
        )
    }
}
