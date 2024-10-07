package com.st.motion_intensity.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.motion_intensity.MotionIntensityViewModel
import com.st.motion_intensity.R
import com.st.ui.theme.LocalDimensions
import java.util.Locale


@Composable
fun MotionIntensityDemoContent(
    modifier: Modifier,
    viewModel: MotionIntensityViewModel
) {

    val motIntData by viewModel.motIntData.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = LocalDimensions.current.paddingNormal),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
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

        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.imageExtraLarge),
                painter = painterResource(
                    R.drawable.motion_intensity_background
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )

            val direction by animateFloatAsState(
                targetValue = viewModel.getAngle(motIntData.intensity.value),
                label = "direction"
            )

            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.imageExtraLarge)
                    .rotate(direction),
                painter = painterResource(
                    R.drawable.motion_intensity_needle
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = LocalDimensions.current.paddingLarge),
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