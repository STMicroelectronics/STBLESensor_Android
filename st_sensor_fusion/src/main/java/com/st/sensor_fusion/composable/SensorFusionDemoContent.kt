package com.st.sensor_fusion.composable

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.opengl.GLSurfaceView
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.proximity.ProximityInfo
import com.st.blue_sdk.models.Boards
import com.st.sensor_fusion.R
import com.st.sensor_fusion.SensorFusionViewModel
import com.st.sensor_fusion.utility.GLCubeRender
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey0
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min


@Composable
fun SensorFusionDemoContent(
    modifier: Modifier,
    viewModel: SensorFusionViewModel,
    nodeId: String
) {

    val context = LocalContext.current
    val activity = context.findActivity()

    val initialCubeScale = 1.0f
    val maxDistance = 200
    val scaleFactor = initialCubeScale / maxDistance

    var alfaTextQuaternionRate by remember { mutableFloatStateOf(value = 0f) }
    var alfaTextFrameRate by remember { mutableFloatStateOf(value = 0f) }

    val calibrationStatus by viewModel.calibrationStatus.collectAsStateWithLifecycle()

    var proximityChecked by remember { mutableStateOf(value = false) }

    val proximity by viewModel.proximity.collectAsStateWithLifecycle()

    val freeFall by viewModel.freeFall.collectAsStateWithLifecycle()

    val fusionData by viewModel.fusionData.collectAsStateWithLifecycle()

    var viewGLSurface by remember { mutableStateOf<GLSurfaceView?>(value = null) }

    var mFistQuaternionTime by remember {
        mutableLongStateOf(-1)
    }

    val mNQuaternion by remember {
        mutableStateOf(AtomicLong(0))
    }

    var averageQuaternionRate by remember {
        mutableLongStateOf(0)
    }

    val shaderRenderer = remember {
        //0xFFFFFF == White Color for the background
        GLCubeRender(activity, 0xFFFFFF).apply {
            setScaleCube(initialCubeScale)
        }
    }

    var showResetDialog by remember {
        mutableStateOf(false)
    }

    var showCalibrationDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = LocalDimensions.current.paddingNormal,
                start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                modifier = Modifier
                    .alpha(alfaTextQuaternionRate)
                    .clickable {
                        alfaTextQuaternionRate =
                            if (alfaTextQuaternionRate == 0f) {
                                1f
                            } else {
                                0f
                            }
                    },
                style = MaterialTheme.typography.bodySmall,
                text = "Quaternion Rate: $averageQuaternionRate"
            )

            proximity?.let {
                Text(
                    text =
                    if (it == ProximityInfo.OUT_OF_RANGE_VALUE) {
                        shaderRenderer.setScaleCube(initialCubeScale)
                        "Distance: Out of range"
                    } else {
                        shaderRenderer.setScaleCube(min(it, maxDistance) * scaleFactor)
                        String.format(
                            Locale.getDefault(), "Distance: %4d mm", it
                        )
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                modifier = Modifier
                    .alpha(alfaTextFrameRate)
                    .clickable {
                        alfaTextFrameRate =
                            if (alfaTextFrameRate == 0f) {
                                1f
                            } else {
                                0f
                            }
                    },
                text = "Frame Rate: ${shaderRenderer.getRenderingRate()} fps",
                style = MaterialTheme.typography.bodySmall
            )
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        ) {
            AndroidView(factory = {
                GLSurfaceView(it).also { glSurface ->
                    viewGLSurface = glSurface
                    glSurface.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    glSurface.setEGLContextClientVersion(2)
                    glSurface.setRenderer(shaderRenderer)
                }
            }, update = {
                fusionData?.let { fusion ->
                    if (mFistQuaternionTime < 0) mFistQuaternionTime = System.currentTimeMillis()

                    averageQuaternionRate = mNQuaternion.incrementAndGet() * 1000 /
                            (System.currentTimeMillis() - mFistQuaternionTime + 1)

                    shaderRenderer.setRotation(fusion.qi, fusion.qj, fusion.qk, fusion.qs)
                }
            })
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BlueMsButton(text = "Reset", onClick = { showResetDialog = true})
//            Text(
//                modifier = Modifier.clickable {
//                    shaderRenderer.resetCube()
//                    showResetDialog = true
//                },
//                color = PrimaryBlue,
//                style = MaterialTheme.typography.titleLarge,
//                text = "Reset"
//            )

            if (viewModel.nodeHaveProximityFeature()) {
                Column(
                    Modifier
                        .selectable(
                            selected = proximityChecked,
                            onClick = {
                                proximityChecked = !proximityChecked
                                if (proximityChecked) {
                                    viewModel.enableProximityNotification(nodeId)
                                } else {
                                    viewModel.disableProximityNotification(nodeId)
                                }
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Proximity",
                        style = MaterialTheme.typography.bodySmall,
                    )

                    RadioButton(
                        selected = proximityChecked,
                        onClick = {
                            proximityChecked = !proximityChecked
                            if (proximityChecked) {
                                viewModel.enableProximityNotification(nodeId)
                            } else {
                                viewModel.disableProximityNotification(nodeId)
                            }
                        }
                    )
                }
            }

            Surface(
                modifier = Modifier.padding(all = LocalDimensions.current.paddingSmall),
                shape = Shapes.small,
                color = if (calibrationStatus) {
                    SuccessText
                } else {
                    PrimaryBlue
                }
            ) {
                Icon(
                modifier = Modifier
                    .width(100.dp)
                        .height(42.dp).clickable {
                        viewModel.resetCubeCalibration(nodeId)
                        showCalibrationDialog = true
                    },
                    painter = painterResource(R.drawable.calibration),
                    tint =  Grey0,
                contentDescription = null
            )
        }
    }
    }

    if (showResetDialog) {
        AlertDialog(
            modifier = Modifier.alpha(0.95f),
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                BlueMsButton(
                    onClick = {
                        showResetDialog = false
                        shaderRenderer.resetCube()
                    },
                    text = "Reset"
                )
            },
            title = {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Reset Board Position"
                )
            },
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .size(size = 150.dp),
                        painter = painterResource(
                            findBoardImage(viewModel.getNode(nodeId = nodeId))
                        ),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )

                    Text(
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Grey6,
                        text = "Keep the board as shown in the image"
                    )
                }
            }
        )
    }

    if (showCalibrationDialog && !calibrationStatus) {
        AlertDialog(
            modifier = Modifier.alpha(0.90f),
            onDismissRequest = { showCalibrationDialog = false },
            confirmButton = {
                BlueMsButton(
                    onClick = {
                        showCalibrationDialog = false
                    },
                    text = "OK"
                )
            },
            title = {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Calibration on Going"
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingNormal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                ) {


                    Icon(
                        modifier = Modifier
                            .size(LocalDimensions.current.imageLarge),
                        painter = painterResource(
                            R.drawable.calibration
                        ),
                        tint = PrimaryBlue,
                        contentDescription = null
                    )

                    Text(
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Grey6,
                        text = "Usage: move the board as shown in the image"
                    )
                }
            }
        )
    }

    LaunchedEffect(freeFall.second) {
        if (freeFall.first) {
            Toast.makeText(
                context,
                "Free fall detected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

private fun findBoardImage(model: Boards.Model): Int {
    return when (model) {
        Boards.Model.SENSOR_TILE -> R.drawable.ic_board_sensortile_bg
        Boards.Model.BLUE_COIN -> R.drawable.ic_board_bluecoin_bg
        Boards.Model.STEVAL_BCN002V1 -> R.drawable.ic_board_bluenrgtile
        Boards.Model.SENSOR_TILE_BOX -> R.drawable.ic_sensortile_box
        Boards.Model.SENSOR_TILE_BOX_PRO -> R.drawable.box_pro_case_top
        Boards.Model.SENSOR_TILE_BOX_PROB -> R.drawable.box_pro_case_top
        Boards.Model.SENSOR_TILE_BOX_PROC -> R.drawable.box_pro_case_top
        Boards.Model.NUCLEO -> R.drawable.ic_board_nucleo_bg
        Boards.Model.NUCLEO_U575ZIQ -> R.drawable.ic_board_nucleo_bg
        Boards.Model.NUCLEO_U5A5ZJQ -> R.drawable.ic_board_nucleo_bg
        Boards.Model.NUCLEO_F401RE -> R.drawable.ic_board_nucleo_bg
        Boards.Model.NUCLEO_L476RG -> R.drawable.ic_board_nucleo_bg
        Boards.Model.NUCLEO_L053R8 -> R.drawable.ic_board_nucleo_bg
        Boards.Model.NUCLEO_F446RE -> R.drawable.ic_board_nucleo_bg
        else -> R.drawable.baseline_device_unknown_24
    }
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}