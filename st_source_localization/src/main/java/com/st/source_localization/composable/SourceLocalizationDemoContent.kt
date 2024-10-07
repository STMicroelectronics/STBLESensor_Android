package com.st.source_localization.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.models.Boards
import com.st.source_localization.R
import com.st.source_localization.SourceLocalizationViewModel
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import java.util.Locale

@Composable
fun SourceLocalizationDemoContent(
    modifier: Modifier,
    viewModel: SourceLocalizationViewModel,
    nodeId: String
) {

    var lowSensibility by remember {
        mutableStateOf(viewModel.lowSensitivity)
    }

    val directionData by viewModel.directionData.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingLarge)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = LocalDimensions.current.paddingNormal),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    minLines = 1,
                    text = "Sensitivity"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = "High"
                    )

                    Switch(
                        modifier = Modifier.padding(
                            start = LocalDimensions.current.paddingNormal,
                            end = LocalDimensions.current.paddingNormal
                        ),
                        checked = lowSensibility, onCheckedChange = {
                            lowSensibility = it
                            viewModel.enableLowSensitivity(nodeId = nodeId, sensitivityLow = it)
                        },
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            uncheckedTrackColor = Grey6,
                            disabledUncheckedTrackColor = Grey3
                        )
                    )

                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Low"
                    )
                }
            }

        }

        if (directionData != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = LocalDimensions.current.paddingNormal),
                shape = Shapes.small,
                shadowElevation = LocalDimensions.current.elevationNormal
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingNormal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                ) {

                    Text(
                        modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        text = String.format(
                            Locale.getDefault(), "Angle: %d°", directionData!!.angle.value
                        ),
                    )

                    Box(
                        modifier = modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {

                        Image(
                            modifier = Modifier
                                .rotate(getImageBoardRotation(viewModel, nodeId))
                                .padding(
                                    LocalDimensions.current.paddingLarge
                                ),
                            painter = painterResource(getImageBoard(viewModel, nodeId)),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth
                        )

                        val direction by animateFloatAsState(
                            targetValue = directionData!!.angle.value.toFloat(),
                            label = "direction"
                        )
                        Image(
                            modifier = Modifier.rotate(direction),
                            painter = painterResource(R.drawable.source_loc_needle),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(2f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    text = "Waiting data…"
                )
            }
        }
    }
}

private fun getImageBoardRotation(viewModel: SourceLocalizationViewModel, nodeId: String): Float {
    return when (viewModel.getNode(nodeId)) {
        Boards.Model.BLUE_COIN -> 0f
        Boards.Model.NUCLEO,
        Boards.Model.NUCLEO_F401RE,
        Boards.Model.NUCLEO_L476RG,
        Boards.Model.NUCLEO_L053R8,
        Boards.Model.NUCLEO_U575ZIQ,
        Boards.Model.NUCLEO_U5A5ZJQ,
        Boards.Model.NUCLEO_F446RE -> {
            90f
        }

        else -> 0f
    }
}

private fun getImageBoard(viewModel: SourceLocalizationViewModel, nodeId: String): Int {
    return when (viewModel.getNode(nodeId)) {
        Boards.Model.BLUE_COIN -> R.drawable.ic_board_bluecoin_bg
        Boards.Model.NUCLEO,
        Boards.Model.NUCLEO_F401RE,
        Boards.Model.NUCLEO_L476RG,
        Boards.Model.NUCLEO_L053R8,
        Boards.Model.NUCLEO_U575ZIQ,
        Boards.Model.NUCLEO_U5A5ZJQ,
        Boards.Model.NUCLEO_F446RE ->
            R.drawable.ic_board_nucleo_bg

        else -> R.drawable.mic_on
    }
}