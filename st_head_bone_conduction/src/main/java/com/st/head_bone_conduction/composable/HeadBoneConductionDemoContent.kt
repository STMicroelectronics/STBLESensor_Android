package com.st.head_bone_conduction.composable

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.toByteArray
import com.st.head_bone_conduction.HeadBoneConductionViewModel
import com.st.head_bone_conduction.R
import com.st.head_bone_conduction.utility.AudioFileRecorder
import com.st.head_bone_conduction.utility.GLCubeRenderHead
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey0
import com.st.ui.theme.Grey2
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.SecondaryBlue2
import com.st.ui.theme.Shapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import java.io.BufferedInputStream
import java.io.File
import java.net.URL
import java.util.Locale
import java.util.zip.ZipInputStream
import java.io.FileOutputStream

private var mAudioTrack: AudioTrack? = null
private var audioManager: AudioManager? = null
private var voskModel: Model? = null
private var voskRecognizer: Recognizer? = null

enum class HeadBoneTab(val title: String, val iconResId: Int) {
    FUSION("Head Tracking", R.drawable.sensor_fusion_icon), // Using existing project icons
    AUDIO_STREAMING("Streaming", R.drawable.ic_volume_up_black_32dp),
    AUDIO_RECORDING("Recording", R.drawable.ic_record),
    STEPS("Pedometer", R.drawable.pedometer_icon),
    VAD("VAD", R.drawable.voice_recognition300_dpi),
    POSITION("Frame Sensing", R.drawable.eyeglasses_icon_300dp),
    DEBUG("Debug", R.drawable.head_bone_debug)
}

private suspend fun downloadAndUnzipModel(
    context: Context,
    urlString: String,
    onComplete: () -> Unit
) {
    withContext(Dispatchers.IO) {
    val modelDir = File(context.filesDir, "vosk-model")
    if (!modelDir.exists()) modelDir.mkdirs()

    try {
        // Show starting toast
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Downloading Model...", Toast.LENGTH_SHORT).show()
        }

        // Download and Unzip on the fly
        URL(urlString).openStream().use { inputStream ->
            ZipInputStream(BufferedInputStream(inputStream)).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    val file = File(modelDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { outputStream ->
                            zipInputStream.copyTo(outputStream)
                        }
                    }
                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
            }
        }

        // Create the uuid file inside the unzipped directory
        // We search for the actual model folder (the zip usually contains a subfolder)
        val extractedFolders = modelDir.listFiles { file -> file.isDirectory }
        val targetFolder = extractedFolders?.firstOrNull() ?: modelDir

        Log.i("Vosk","unzip target folder = $targetFolder")

        val uuidFile = File(targetFolder, "uuid")
        val randomUuid = java.util.UUID.randomUUID().toString()

        uuidFile.writeText(randomUuid)

        // Success Feedback
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Model Ready!", Toast.LENGTH_SHORT).show()

            onComplete()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadBoneConductionDemoContent(
    modifier: Modifier,
    viewModel: HeadBoneConductionViewModel,
    nodeId: String
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val coroutineScope = rememberCoroutineScope()

    // ViewModel States
    val fusionData by viewModel.fusionData.collectAsStateWithLifecycle()
    val registersData by viewModel.registersData.collectAsStateWithLifecycle()
    val fusionEnabled by viewModel.fusionEnabled.collectAsStateWithLifecycle()
    val audioEnabled by viewModel.audioEnabled.collectAsStateWithLifecycle()
    val mlcEnabled by viewModel.mlcEnabled.collectAsStateWithLifecycle()
    val stepData by viewModel.stepData.collectAsStateWithLifecycle()
    val pedometerEnabled by viewModel.pedometerEnabled.collectAsStateWithLifecycle()
    val isBetaRelease by viewModel.isBetaRelease.collectAsStateWithLifecycle()
    val vadEnableValue by viewModel.vadEnableValue.collectAsStateWithLifecycle()
    val streamingEnableValue by viewModel.streamingEnableValue.collectAsStateWithLifecycle()
    val recordingEnableValue by viewModel.recordingEnableValue.collectAsStateWithLifecycle()
    val positionEnableValue by viewModel.positionEnableValue.collectAsStateWithLifecycle()
    val debugMessages by viewModel.debugConsole.collectAsStateWithLifecycle()
    val boardId by viewModel.boardId.collectAsStateWithLifecycle()

    // Filter visible tabs based on current mode
    val visibleTabs = HeadBoneTab.entries.filter { tab ->
        when (tab) {
            HeadBoneTab.AUDIO_STREAMING -> streamingEnableValue
            HeadBoneTab.AUDIO_RECORDING -> recordingEnableValue
            HeadBoneTab.VAD -> vadEnableValue
            HeadBoneTab.POSITION -> positionEnableValue
            HeadBoneTab.DEBUG -> false // DEBUG TAB ALWAYS INVISIBLE
            else -> true
        }
    }

    // Navigation State
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedTab by remember(key1 = selectedIndex) { derivedStateOf { visibleTabs[selectedIndex] } }

    // Composable Logic States
    val shaderRenderer = remember {
        GLCubeRenderHead(activity, 0xFFFFFF).apply { setScaleCube(3.0f) }
    }

    // Centralized Tab Management: reset active features on tab change
    LaunchedEffect(key1 = selectedTab) {
        // Reset the stabilization gate in the renderer when switching tabs
        shaderRenderer.hideModel()

        if (fusionEnabled) {
            viewModel.startStopFusion(nodeId)
        }
        if (audioEnabled) {
            viewModel.startStopAudio()
        }
        if (mlcEnabled) {
            viewModel.startStopMLC(nodeId)
        }
        if (pedometerEnabled) {
            viewModel.startStopPedometer(nodeId)
        }

        // Send command when Position tab is selected
        if (selectedTab == HeadBoneTab.POSITION) {
            viewModel.sendDebugCommand(nodeId, "*posE\n")
        }

        // Send command when VAD tab is selected
        if (selectedTab == HeadBoneTab.VAD) {
            viewModel.sendDebugCommand(nodeId, "*vadE\n")
        }
    }

    // Resets the motion gate in the renderer when Sensor Fusion is toggled off
    LaunchedEffect(fusionEnabled) {
        if (!fusionEnabled) {
            shaderRenderer.hideModel()
        }
    }

    // Initialize demo on start
    LaunchedEffect(nodeId) {
        viewModel.startDemo(nodeId)
    }

    // Cleanup resources
    DisposableEffect(nodeId) {
        onDispose {
            viewModel.stopDemo(nodeId)
        }
    }

    val recorder = remember { AudioFileRecorder( "HeadBone") }
    var isMute by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var maxVolume by remember { mutableFloatStateOf(100f) }
    var currentVolume by remember { mutableFloatStateOf(50f) }

    var audioPacket by remember { mutableStateOf(shortArrayOf()) }

    var frequency by remember { mutableStateOf("") }
    var codecType by remember { mutableStateOf("") }
    var transcribedText by remember { mutableStateOf("Waiting for speech...") }
    var isSTTActive by remember { mutableStateOf(false) }
    var isModelLoaded by remember { mutableStateOf(false) }

    //We plot only the first sample of each chunk
    var sample by remember {
        mutableStateOf<Short>(0)
    }

    var viewSpectrogram by rememberSaveable { mutableStateOf(true) }

    val isActive by remember(key1 = selectedTab) {
        derivedStateOf {
            when (selectedTab) {
                HeadBoneTab.FUSION -> fusionEnabled
                HeadBoneTab.AUDIO_STREAMING -> audioEnabled
                HeadBoneTab.AUDIO_RECORDING -> audioEnabled
                HeadBoneTab.STEPS -> pedometerEnabled
                HeadBoneTab.VAD -> mlcEnabled
                HeadBoneTab.POSITION -> mlcEnabled
                HeadBoneTab.DEBUG -> false
            }
        }
    }

    fun loadVoskModel() {
        val modelPath = File(context.filesDir, "vosk-model/${viewModel.voskModelVersion}").absolutePath
        Log.i("Vosk","Model path: $modelPath")
        try {
            // Initialize the Model from the local storage path
            voskModel = Model(modelPath)
            isModelLoaded = true
        } catch (e: Exception) {
            Log.e("Vosk", "Failed to load model: ${e.message}")
        }
    }

    // 1. Initialize Vosk Model (Offline)
    LaunchedEffect(isBetaRelease) {
        if (isBetaRelease) {
            loadVoskModel()
        }
    }

    // 2. Setup Recognizer when Audio starts
    fun initVosk(sampleRate: Float) {
        voskModel?.let {
            voskRecognizer = Recognizer(it, sampleRate)
            voskRecognizer?.setWords(true)
        }
    }

    // Audio Flow Handling
    LaunchedEffect(key1 = audioEnabled) {
        if (audioEnabled) {
            viewModel.audioData(nodeId).onStart {
                val decodeParams = viewModel.getAudioDecodeParams(nodeId)
                frequency =
                    String.format(Locale.getDefault(), "%d kHz", decodeParams.samplingFreq / 1000)
                recorder.sampleRate = decodeParams.samplingFreq
                recorder.channels = decodeParams.channels.toShort()
                recorder.fileSuffix = viewModel.getFeatureName(nodeId).replace("Feature", "")
                codecType = viewModel.getAudioCodecType(nodeId).name
                audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.let {
                    maxVolume = it.toFloat()
                    currentVolume = maxVolume / 2
                }

                // Initialize Vosk with the correct sampling rate from BlueVoice
                if (isBetaRelease) {
                    initVosk(decodeParams.samplingFreq.toFloat())
                }

                initAudioTrack(decodeParams)
            }.filter { it.isNotEmpty() }.onEach {
                audioPacket = it.copyOf()
            }.onEach {
                sample = it[0]
            }.collect { pcmData ->
                val bytes = pcmData.toByteArray()
                playAudio(bytes)
                if (isRecording) recorder.writeSample(bytes)

                // 3. Feed the BlueVoice bytes to Vosk
                if (isSTTActive && voskRecognizer != null) {
                    if (voskRecognizer!!.acceptWaveForm(bytes, bytes.size)) {
                        // Partial result gives real-time feedback, Result gives final sentence
                        val result = voskRecognizer!!.result
                        Log.d("Vosk", "result=${result}")
                        if (result.contains("\"text\" : \"")) {
                            transcribedText =
                                result.substringAfter("\"text\" : \"").substringBefore("\"")
                        }
                    } else {
                        val partial = voskRecognizer!!.partialResult
                        if (partial.contains("\"partial\" : \"")) {
                            transcribedText =
                                partial.substringAfter("\"partial\" : \"").substringBefore("\"")
                        }
                    }
                }
            }
        } else {
            if (isRecording) {
                recorder.stop()
                    ?.let { Toast.makeText(context, "Audio saved", Toast.LENGTH_SHORT).show() }
                isRecording = false
            }
            viewModel.destroyAudioService(nodeId = nodeId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            // Main action button at the bottom, not overlapping with content
            if (selectedTab != HeadBoneTab.DEBUG) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LocalDimensions.current.paddingNormal)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExtendedFloatingActionButton(
                        containerColor = if (selectedTab == HeadBoneTab.AUDIO_RECORDING && audioEnabled) ErrorText else SecondaryBlue,
                        onClick = {
                            when (selectedTab) {
                                HeadBoneTab.FUSION -> viewModel.startStopFusion(nodeId)
                                HeadBoneTab.AUDIO_STREAMING -> viewModel.startStopAudio()
                                HeadBoneTab.AUDIO_RECORDING -> viewModel.startStopAudio()
                                HeadBoneTab.STEPS -> viewModel.startStopPedometer(nodeId)
                                HeadBoneTab.VAD -> viewModel.startStopMLC(nodeId)
                                HeadBoneTab.POSITION -> viewModel.startStopMLC(nodeId)
                                HeadBoneTab.DEBUG -> { /* No main action for debug */ }
                            }
                        },
                        expanded = !isActive,
                        icon = {
                            Icon(
                                tint = if (isActive && selectedTab == HeadBoneTab.AUDIO_RECORDING) Grey0 else PrimaryBlue,
                                imageVector = if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                        },
                        text = {
                            val label = when (selectedTab) {
                                HeadBoneTab.AUDIO_RECORDING -> if (audioEnabled) "Stop Rec" else "Start Rec"
                                else -> if (isTabActive(
                                        selectedTab,
                                        fusionEnabled,
                                        audioEnabled,
                                        pedometerEnabled,
                                        mlcEnabled
                                    )
                                ) "Stop" else "Start"
                            }
                            Text(
                                text = label,
                                color = if (audioEnabled && selectedTab == HeadBoneTab.AUDIO_RECORDING) Grey0 else PrimaryBlue
                            )
                        }
                    )

                    if (selectedTab == HeadBoneTab.FUSION) {
                        BlueMsButton(text = "Reset", onClick = { shaderRenderer.resetCube() })
                    }
                }
            }
        },
        topBar = {
            PrimaryScrollableTabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = selectedIndex,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                edgePadding = 0.dp,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            selectedTabIndex = selectedIndex,
                            matchContentSize = false
                        ),
                        width = 60.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        height = 4.dp,
                        shape = Shapes.medium
                    )
                },
                divider = {}
            ) {
                visibleTabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedIndex,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.iconResId),
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues)
                .padding(LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (selectedTab) {
                HeadBoneTab.FUSION -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(BorderStroke(2.dp, PrimaryBlue), Shapes.small)
                    ) {
                        AndroidView(factory = {
                            GLSurfaceView(it).also { glSurface ->
                                glSurface.layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                glSurface.setEGLContextClientVersion(2)
                                glSurface.setRenderer(shaderRenderer)
                            }
                        }, update = {
                            boardId?.let { shaderRenderer.setBoardId(it) }
                            fusionData?.let { fusion ->
                                shaderRenderer.setRotation(
                                    fusion.qi,
                                    fusion.qj,
                                    fusion.qk,
                                    fusion.qs
                                )
                            }
                        })
                    }
                }

                HeadBoneTab.AUDIO_STREAMING -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Shapes.small,
                        shadowElevation = LocalDimensions.current.elevationNormal
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            if (isBetaRelease) {
                                Column(
                                    modifier = Modifier
                                        .background(Grey2, shape = Shapes.small)
                                        .padding(LocalDimensions.current.paddingNormal)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Speech to Text (en-us)",
                                            style = MaterialTheme.typography.titleSmall
                                        )

                                        BlueMsButton(
                                            enabled = audioEnabled && isModelLoaded,
                                            text = if (isSTTActive) "Stop" else "Start",
                                            color = PrimaryBlue,
                                            textColor = Grey0,
                                            iconPainter = painterResource(
                                                R.drawable.speech_to_text
                                            ),
                                            onClick = { isSTTActive = !isSTTActive }
                                        )
                                    }

                                    if (isSTTActive) {
                                        Text(
                                            text = transcribedText,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PrimaryBlue,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    if(!isModelLoaded){
                                        BlueMsButton(
                                            text = "Download Model",
                                            color = PrimaryBlue,
                                            textColor = Grey0,
                                            iconPainter = painterResource(
                                                R.drawable.ic_download
                                            ),
                                            onClick = {
                                                coroutineScope.launch {
                                                    downloadAndUnzipModel(
                                                        context,
                                                        "https://alphacephei.com/vosk/models/${viewModel.voskModelVersion}.zip"
                                                    ) {
                                                        // This callback runs when finished
                                                        loadVoskModel()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Sampling Rate: $frequency",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Volume: ", style = MaterialTheme.typography.titleMedium)
                                Icon(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable {
                                            isMute = !isMute
                                            if (isMute) muteAudio() else unMuteAudio(
                                                currentVolume.toInt()
                                            )
                                        },
                                    painter = painterResource(if (isMute) R.drawable.ic_volume_off_black_32dp else R.drawable.ic_volume_up_black_32dp),
                                    tint = PrimaryBlue, contentDescription = null
                                )
                                Slider(
                                    value = currentVolume, enabled = !isMute,
                                    onValueChange = { currentVolume = it },
                                    valueRange = 0f..maxVolume,
                                    onValueChangeFinished = { unMuteAudio(currentVolume.toInt()) },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // New Record Button for Streaming Tab with frame and inverted logic
                                Spacer(modifier = Modifier.size(8.dp))
                                Box(
                                    modifier = Modifier
                                        .border(BorderStroke(1.dp, PrimaryBlue), Shapes.small)
                                        .clickable {
                                            if (audioEnabled) {
                                                if (!isRecording) {
                                                    isRecording = true
                                                    recorder.start()
                                                } else {
                                                    recorder.stop()
                                                        ?.let { Toast.makeText(context, "Audio saved", Toast.LENGTH_SHORT).show() }
                                                    isRecording = false
                                                }
                                            } else {
                                                Toast.makeText(context, "Start Audio first", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        modifier = Modifier.size(32.dp),
                                        painter = painterResource(id = R.drawable.ic_record),
                                        tint = if (!isRecording) ErrorText else PrimaryBlue,
                                        contentDescription = "Record"
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = Shapes.small,
                        shadowElevation = 2.dp
                    ) {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
                            horizontalAlignment = Alignment.End
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(
                                        top = LocalDimensions.current.paddingNormal,
                                        end = LocalDimensions.current.paddingNormal
                                    )
                                    .clip(Shapes.small)
                                    .clickable {
                                        viewSpectrogram = !viewSpectrogram
                                    }
                                    .background(color = PrimaryBlue)
                                    .padding(
                                        start = LocalDimensions.current.paddingSmall,
                                        top = LocalDimensions.current.paddingSmall,
                                        end = LocalDimensions.current.paddingSmall,
                                        bottom = LocalDimensions.current.paddingSmall
                                    ),
                                painter = painterResource(id = R.drawable.switch_home),
                                tint = Grey0,
                                contentDescription = "Change View"
                            )

                            if (viewSpectrogram) {
                                SpectrogramPlotView(audioData = audioPacket)
                            } else {
                                WaveFormPlotView(sample = sample)
                            }
                        }
                    }
                }

                HeadBoneTab.AUDIO_RECORDING -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (audioEnabled) {
                            Text(
                                text = "Recording Ongoing",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = ErrorText,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        } else {
                            Text(
                                text = "Ready to record",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                HeadBoneTab.STEPS -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = Shapes.small,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Pedometer",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Image(
                                painter = painterResource(id = R.drawable.pedometer_step_image),
                                contentDescription = "Pedometer Image",
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Steps: ${stepData.first.steps.value}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }

                HeadBoneTab.VAD -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = Shapes.small,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            Modifier.padding(16.dp),//16
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Voice Activity Detection (MLC)",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                                color = PrimaryBlue
                            )
                            if (registersData.isNotEmpty()) {
                                val firstValue = registersData[0].toInt()
                                val (label, faceResId) = when (firstValue) {
                                    4 -> "VAD Detected" to R.drawable.man_talk_azure300_pi
                                    0 -> "" to R.drawable.man_no_talk_grey_new_300_dpi //No VAD Detected
                                    else -> "Unknown" to null
                                }
                                faceResId?.let {
                                    Image(
                                        painter = painterResource(id = it),
                                        contentDescription = "Face status",
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.displaySmall,//headlineLarge,
                                    color = if (firstValue == 4) SecondaryBlue2 else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            } else {
                                Text(
                                    "No detection data",
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                        }
                    }
                }

                HeadBoneTab.POSITION -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = Shapes.small,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            //verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "Frame Sensing (MLC)",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                                color = PrimaryBlue
                            )
                            
                            Spacer(modifier = Modifier.height(1.dp)) // Aggiunge 8dp extra per arrivare a 16dp totali (8 di spacedBy + 8 di Spacer)

                            if (registersData.size >= 3) {
                                // Row 1: Idle / Active
                                val val1 = registersData[0].toInt()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(
                                        text = "    Idle / Active:",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PrimaryBlue
                                    )
                                    val res1 = if (val1 == 4) R.drawable.mlc0_out04 else R.drawable.mlc0_out00
                                    Image(
                                        painter = painterResource(id = res1),
                                        contentDescription = "Position 1",
                                        modifier = Modifier.size(150.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                // Row 2: Hinge Action
                                val val2 = registersData[1].toInt()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(
                                        text = "     Hinge Action:",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PrimaryBlue
                                    )
                                    val res2 = when (val2) {
                                        4 -> R.drawable.mlc1_out04
                                        8 -> R.drawable.mlc1_out08
                                        else -> R.drawable.mlc1_out00
                                    }
                                    Image(
                                        painter = painterResource(id = res2),
                                        contentDescription = "Position 2",
                                        modifier = Modifier.size(150.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                // Row 3: Frame Orientation
                                val val3 = registersData[2].toInt()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(
                                        text = "Frame Orientation:",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PrimaryBlue
                                    )
                                    val res3 = when (val3) {
                                        0 -> R.drawable.mlc2_out00
                                        4 -> R.drawable.mlc2_out04
                                        8 -> R.drawable.mlc2_out08
                                        12 -> R.drawable.mlc2_out12
                                        else -> R.drawable.mlc2_out14
                                    }
                                    Image(
                                        painter = painterResource(id = res3),
                                        contentDescription = "Position 3",
                                        modifier = Modifier.size(150.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else {
                                Text(
                                    "Insufficient data (need 3 registers)",
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                        }
                    }
                }
                HeadBoneTab.DEBUG -> {
                    var commandText by remember { mutableStateOf("") }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            color = Grey2,
                            shape = Shapes.small
                        ) {
                            val scrollState = rememberScrollState()
                            LaunchedEffect(debugMessages) {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                            Text(
                                text = debugMessages,
                                modifier = Modifier.padding(8.dp).verticalScroll(scrollState),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = commandText,
                                onValueChange = { commandText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Command...") }
                            )
                            BlueMsButton(
                                text = "Send",
                                onClick = {
                                    if (commandText.isNotEmpty()) {
                                        viewModel.sendDebugCommand(nodeId, commandText + "\n")
                                        commandText = ""
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isTabActive(
    tab: HeadBoneTab,
    fusion: Boolean,
    audio: Boolean,
    steps: Boolean,
    mlc: Boolean
): Boolean {
    return when (tab) {
        HeadBoneTab.FUSION -> fusion
        HeadBoneTab.AUDIO_STREAMING -> audio
        HeadBoneTab.AUDIO_RECORDING -> audio
        HeadBoneTab.STEPS -> steps
        HeadBoneTab.VAD -> mlc
        HeadBoneTab.POSITION -> mlc
        HeadBoneTab.DEBUG -> false
    }
}

private fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}

private fun initAudioTrack(decodeParams: DecodeParams) {
    val channelConfig =
        if (decodeParams.channels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
    val minBufSize = AudioTrack.getMinBufferSize(
        decodeParams.samplingFreq,
        channelConfig,
        AudioFormat.ENCODING_PCM_16BIT
    )
    mAudioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        )
        .setAudioFormat(
            AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(decodeParams.samplingFreq).setChannelMask(channelConfig).build()
        )
        .setBufferSizeInBytes(minBufSize).setTransferMode(AudioTrack.MODE_STREAM).build()
    mAudioTrack?.play()
}

private fun playAudio(sample: ByteArray) {
    mAudioTrack?.write(sample, 0, sample.size, AudioTrack.WRITE_NON_BLOCKING)
}

private fun muteAudio() {
    audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
}

private fun unMuteAudio(volume: Int) {
    audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
}
