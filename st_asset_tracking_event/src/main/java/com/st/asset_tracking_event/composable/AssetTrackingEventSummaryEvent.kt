package com.st.asset_tracking_event.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.st.ui.theme.Grey3
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.NotActiveColor
import com.st.ui.theme.PrimaryBlue3
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.Shapes
import kotlinx.coroutines.delay

@Composable
fun AssetTrackingEventSummaryEvent(
    modifier: Modifier=Modifier,
    description: String,
    numEvents: Int,
    painter: Painter,
) {

    var isAnimated by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = numEvents) {
        if (numEvents!=0) {
            isAnimated = true
            delay(500)
            isAnimated = false
        }
    }

    val animatedColor by animateColorAsState(
        if (isAnimated) {
            PrimaryYellow
        } else {
            NotActiveColor
        },
        label = "color"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
    ) {
        Surface(
            modifier = Modifier.padding(all = LocalDimensions.current.paddingSmall),
            shape = Shapes.small,
            color = animatedColor
        ) {
            Icon(
                modifier = Modifier
                    .padding(LocalDimensions.current.paddingNormal)
                    .size(64.dp),
                tint = PrimaryBlue3,
                painter = painter,
                contentDescription = null
            )
        }
        Text(
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            text = description
        )
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = "$numEvents Events"
        )
    }
}