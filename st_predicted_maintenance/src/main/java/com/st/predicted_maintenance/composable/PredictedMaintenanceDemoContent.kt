package com.st.predicted_maintenance.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.predicted_maintenance.PredictedMaintenanceViewModel
import com.st.ui.theme.LocalDimensions


@Composable
fun PredictedMaintenanceDemoContent(
    modifier: Modifier,
    viewModel: PredictedMaintenanceViewModel
) {
    val speedData by viewModel.speedData.collectAsStateWithLifecycle()
    val freqData by viewModel.freqData.collectAsStateWithLifecycle()
    val accData by viewModel.accData.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingLarge),
        horizontalAlignment = Alignment.Start
    ) {
        speedData?.let { speed ->
            PredictedMaintenanceElementView(
                title = "RMS Speed Status",
                value = speed,
                format = "RMS Speed (mm/s): %.2f"
            )
        }

        accData?.let { acc ->
            PredictedMaintenanceElementView(
                title = "Acceleration Peak Status",
                value = acc,
                format = "Acc Peak (m/s^2): %.2f"
            )
        }

        freqData?.let { freq ->
            PredictedMaintenanceElementView(
                title = "Frequency Domain Status",
                value = freq,
                format = "Max amplitude (m/s^2): %.2f"
            )
        }

        Spacer(Modifier.padding(bottom = LocalDimensions.current.paddingNormal))
    }
}