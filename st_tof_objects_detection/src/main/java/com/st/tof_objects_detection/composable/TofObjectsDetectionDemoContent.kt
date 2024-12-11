package com.st.tof_objects_detection.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.tof_objects_detection.R
import com.st.tof_objects_detection.TofObjectsDetectionViewModel
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import java.util.Locale


@Composable
fun TofObjectsDetectionDemoContent(
    modifier: Modifier,
    viewModel: TofObjectsDetectionViewModel,
    nodeId: String
) {

    val tofData by viewModel.tofData.collectAsStateWithLifecycle()

    var presenceDemo by remember { mutableStateOf(value = viewModel.mPresenceDemo) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start =LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
    ) {
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = "Use the switch to change detection type"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                style = MaterialTheme.typography.bodySmall,
                text = "Object"
            )

            Switch(
                modifier = Modifier.padding(
                    start = LocalDimensions.current.paddingNormal,
                    end = LocalDimensions.current.paddingNormal
                ),
                checked = presenceDemo, onCheckedChange = {
                    presenceDemo = it
                    viewModel.enableDisablePresence(enablePresence = it, nodeId = nodeId)
                },
                colors = SwitchDefaults.colors(
                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedTrackColor = Grey6,
                    disabledUncheckedTrackColor = Grey3
                )
            )

            Text(
                style = MaterialTheme.typography.bodySmall,
                text = "Presence"
            )
        }

        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            if (tofData != null) {
                if (presenceDemo) {
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                    ) {
                        AnimatedContent(
                            targetState = tofData!!.presenceFound.value.toInt(), label = "",
                            transitionSpec = {
                                // Compare the incoming number with the previous number.
                                if (targetState > initialState) {
                                    // If the target number is larger, it slides up and fades in
                                    // while the initial (smaller) number slides up and fades out.
                                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                        slideOutVertically { height -> -height } + fadeOut())
                                } else {
                                    // If the target number is smaller, it slides down and fades in
                                    // while the initial number slides down and fades out.
                                    (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                                        slideOutVertically { height -> height } + fadeOut())
                                }.using(
                                    // Disable clipping since the faded slide-in/out should
                                    // be displayed out of bounds.
                                    SizeTransform(clip = false)
                                )
                            }
                        ) { count ->
                            Text(
                                modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                text = getNumPresenceToString(count)
                            )
                        }

                        AnimatedContent(targetState = (tofData!!.presenceFound.value.toInt() != 0), label = "", transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }) { _ ->
                            Image(
                                painter = painterResource(
                                    if (tofData!!.presenceFound.value.toInt() != 0) {
                                        R.drawable.tof_presence
                                    } else {
                                        R.drawable.tof_not_presence
                                    }
                                ),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = LocalDimensions.current.paddingNormal),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(size = LocalDimensions.current.iconNormal)
                                    .padding(all = LocalDimensions.current.paddingSmall),
                                painter = painterResource(
                                    id = if (tofData!!.nObjsFound.value != 0.toShort()) {
                                        R.drawable.tof_obj_found
                                    } else {
                                        R.drawable.tof_obj_search
                                    }
                                ),
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = null
                            )

                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                text = getNumObjectsToString(tofData!!.nObjsFound.value.toInt())
                            )
                        }

                        AnimatedVisibility(
                            visible = tofData!!.distanceObjs.isNotEmpty(),
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            if (tofData!!.distanceObjs.isNotEmpty()) {
                                TofObjectElementView(
                                    modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                                    name = tofData!!.distanceObjs[0].name,
                                    value = tofData!!.distanceObjs[0].value,
                                    min = tofData!!.distanceObjs[0].min!!,
                                    max = tofData!!.distanceObjs[0].max!!,
                                    unit = tofData!!.distanceObjs[0].unit
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = tofData!!.distanceObjs.size > 1,
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            if (tofData!!.distanceObjs.size > 1) {
                                TofObjectElementView(
                                    modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                                    name = tofData!!.distanceObjs[1].name,
                                    value = tofData!!.distanceObjs[1].value,
                                    min = tofData!!.distanceObjs[1].min!!,
                                    max = tofData!!.distanceObjs[1].max!!,
                                    unit = tofData!!.distanceObjs[1].unit
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = tofData!!.distanceObjs.size > 2,
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            if (tofData!!.distanceObjs.size > 2) {
                                TofObjectElementView(
                                    modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                                    name = tofData!!.distanceObjs[2].name,
                                    value = tofData!!.distanceObjs[2].value,
                                    min = tofData!!.distanceObjs[2].min!!,
                                    max = tofData!!.distanceObjs[2].max!!,
                                    unit = tofData!!.distanceObjs[2].unit
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = tofData!!.distanceObjs.size > 3,
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            if (tofData!!.distanceObjs.size > 3) {
                                TofObjectElementView(
                                    modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                                    name = tofData!!.distanceObjs[3].name,
                                    value = tofData!!.distanceObjs[3].value,
                                    min = tofData!!.distanceObjs[3].min!!,
                                    max = tofData!!.distanceObjs[3].max!!,
                                    unit = tofData!!.distanceObjs[3].unit
                                )
                            }
                        }

                    }


                }
            } else {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    text = "Waiting data…"
                )
            }
        }
    }
}

private fun getNumObjectsToString(numObjs: Int): String {
    return if (numObjs == 1) {
        "1 Object Found"
    } else if (numObjs > 1) {
        String.format(Locale.getDefault(), "%d Objects Found", numObjs)
    } else {
        "No objects found"
    }
}

private fun getNumPresenceToString(numPresence: Int): String {
    return if (numPresence == 1) {
        "1 Person Found"
    } else if (numPresence > 1) {
        String.format(Locale.getDefault(), "%d People Found", numPresence)
    } else {
        "No Presence found"
    }
}