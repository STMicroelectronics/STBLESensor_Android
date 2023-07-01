/* Copyright 2023 Google LLC.
   SPDX-License-Identifier: Apache-2.0 */
package com.st.ui.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.st.ui.theme.Grey10

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.fadedEdgeMarquee(edgeWidth: Dp = 0.dp, velocity: Dp = 40.dp): Modifier = composed {
    // In a real app this would probably be a top-level, private function.
    fun ContentDrawScope.drawFadedEdge(leftEdge: Boolean) {
        val edgeWidthPx = edgeWidth.toPx()
        drawRect(
            topLeft = Offset(x = if (leftEdge) 0f else size.width - edgeWidthPx, y = 0f),
            size = Size(width = edgeWidthPx, height = size.height),
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Grey10),
                startX = if (leftEdge) 0f else size.width,
                endX = if (leftEdge) edgeWidthPx else size.width - edgeWidthPx
            ),
            blendMode = BlendMode.DstIn
        )
    }

    var innerWidth by remember { mutableStateOf(value = -1) }

    return@composed this
        // Rendering to an offscreen buffer is required to get the faded edges' alpha to be
        // applied only to the text, and not whatever is drawn below this composable (e.g. the
        // window).
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            if (innerWidth > size.width) {
                drawFadedEdge(leftEdge = true)
                drawFadedEdge(leftEdge = false)
            }
        }
        .basicMarquee(
            // Animate forever.
            iterations = Int.MAX_VALUE,
            spacing = MarqueeSpacing(spacing = 0.dp),
            velocity = velocity
        )
        .onSizeChanged { innerWidth = it.width }
        .padding(start = edgeWidth)
}
