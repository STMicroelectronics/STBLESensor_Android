package com.st.neai_classification.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.SuccessText


@Composable
fun NeaiClassElementView(
    modifier: Modifier = Modifier,
    name: String,
    value: Short,
    isTheMostProbable: Boolean
) {
    val progress =
        animateFloatAsState(targetValue = value/100f, label = "")

    Row(modifier = modifier.fillMaxWidth().padding(all = LocalDimensions.current.paddingSmall),
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
            style = MaterialTheme.typography.bodyMedium,
            text = "$name ($value%)",
            color = if(isTheMostProbable) SuccessText else Color.Unspecified
        )

        LinearProgressIndicator(
            progress = { progress.value },
            modifier = Modifier
                .fillMaxWidth().weight(2f)
                .padding(start = LocalDimensions.current.paddingNormal)
                .height(8.dp),
            color = if(isTheMostProbable) SuccessText else ProgressIndicatorDefaults.linearColor,
            drawStopIndicator = {}
        )
    }
}