package com.st.pedometer.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.pedometer.PedometerViewModel
import com.st.pedometer.R
import com.st.ui.theme.LocalDimensions
import java.util.Locale


@Composable
fun PedometerDemoContent(
    modifier: Modifier,
    viewModel: PedometerViewModel,
    nodeId: String
) {
    val stepData by viewModel.stepData.collectAsStateWithLifecycle()
    var flipped by remember { mutableStateOf(value = false) }

    val flip by remember(key1 = stepData.second) {
        derivedStateOf {
            if ((stepData.second != null) && (stepData.first.steps.value != 0.toLong())) {
                flipped = !flipped
                flipped
            } else {
                flipped = false
                flipped
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AnimatedContent(targetState = flip, label = "") { rotate ->
            Icon(
                modifier = if (rotate)
                    Modifier
                        .size(size = LocalDimensions.current.imageExtraLarge)
                        .graphicsLayer(rotationY = 180f)
                        .clickable { viewModel.readFeature(nodeId) }
                else
                    Modifier
                        .size(size = LocalDimensions.current.imageExtraLarge)
                        .clickable { viewModel.readFeature(nodeId) },
                painter = painterResource(
                    R.drawable.pedometer_step_image
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
                "%d steps",
                stepData.first.steps.value
            )
        )


        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = LocalDimensions.current.paddingLarge),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            text = String.format(
                Locale.getDefault(),
                "%d %s",
                stepData.first.frequency.value, stepData.first.frequency.unit
            )
        )


    }
}