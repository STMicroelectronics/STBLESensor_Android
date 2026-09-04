package com.st.ui.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.st.ui.theme.LocalDimensions

@Composable
fun BlueMSAnimatedIconsRow(
    modifier: Modifier = Modifier,
    tint: Color,
    numIcons: Int =3,
    imageVector: ImageVector,
    durationMillis:Int = 1000,
    isToLeft: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SdCardMovement")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = numIcons.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(numIcons) { index ->
            val activeIndex = if (isToLeft) numIcons -1  - (phase.toInt() % numIcons) else (phase.toInt() % numIcons)
            val alpha = if (index == activeIndex) 1f else 0.3f
            Icon(
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { this.alpha = alpha },
                tint = tint,
                imageVector = imageVector,
                contentDescription = null
            )
        }
    }
}