package com.st.color_ambient_light.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun ColorElementView(
    modifier: Modifier = Modifier,
    name: String,
    value: Int,
    min: Int,
    max: Int,
    unit: String
) {

    val progress = animateFloatAsState(targetValue =  (value - min) / (max - min).toFloat() , label = "")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = name
            )

            Text(
                modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                text = "Value: $value $unit"
            )

            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LocalDimensions.current.paddingNormal)
                    .height(16.dp),
                drawStopIndicator = {}
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LocalDimensions.current.paddingNormal)
            ) {
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    text = min.toString()
                )

                Spacer(modifier = Modifier.weight(2f))

                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    text = max.toString()
                )

            }
        }

    }
}

@Composable
@Preview
fun ColorElementExample() {
    ColorElementView(
        name = "Example",
        value = 10,
        min = 0,
        max = 20,
        unit = "Unit"
    )
}