package com.st.motion_intensity.composable

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.motion_intensity.MotionIntensityViewModel
import com.st.ui.composables.STGaugeMeter
import com.st.ui.composables.STGaugeMeterSize
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.SuccessText
import com.st.ui.theme.WarningText
import java.util.Locale


@Composable
fun MotionIntensityDemoContent(
    modifier: Modifier,
    viewModel: MotionIntensityViewModel
) {

    val motIntData by viewModel.motIntData.collectAsStateWithLifecycle()

    val intensity by animateIntAsState(targetValue = motIntData.intensity.value.toInt(), label = "intensity")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            LocalDimensions.current.paddingLarge)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = LocalDimensions.current.paddingLarge),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            text = "The intensity is proportional to the movement"
        )

        Spacer(modifier = Modifier.height(LocalDimensions.current.paddingLarge))


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = LocalDimensions.current.paddingLarge)
        ) {

            STGaugeMeter(
                modifier = Modifier.align(Alignment.Center),
                inputValue = intensity,
                listOfColor = listOf(
                    SuccessText,
                    PrimaryYellow,
                    WarningText,
                    ErrorText
                ),
                minValue = motIntData.intensity.min?.toInt() ?: 0,
                maxValue = motIntData.intensity.max?.toInt() ?: 10,
                gaugeSize = STGaugeMeterSize.BIG,
                displayNeedle = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Resting"
                )

                Text(
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Sprinting"
                )

            }
        }


        Spacer(modifier = Modifier.height(LocalDimensions.current.paddingLarge))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = LocalDimensions.current.paddingLarge,
                    bottom = LocalDimensions.current.paddingLarge
                ),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            text = String.format(
                Locale.getDefault(),
                "Motion intensity value: %d",
                motIntData.intensity.value
            )
        )
    }
}