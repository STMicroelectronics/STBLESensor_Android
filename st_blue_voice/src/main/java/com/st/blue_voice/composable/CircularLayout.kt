package com.st.blue_voice.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.roundToIntRect
import kotlin.math.cos
import kotlin.math.sin


//Written by Rafsanjani Abdul-Aziz
//  in "Advance Layout Techniques in Jetpack Compose" article on medium.com:
//  https://medium.com/@abdulazizrafsanjani/advance-layout-techniques-in-jetpack-compose-12107091fe47

@Composable
fun CircularLayout(
    modifier: Modifier = Modifier,
    radius: Float = 200f,
    content: @Composable () -> Unit
) {
    Layout(modifier = modifier, content = content) { measurable, constraints ->
        val placeable = measurable.map { it.measure(constraints) }

        // Calculate angular spacing between individual items
        val angularSeparation = 360 / placeable.size


        // Represent the available working area by a rectangle
        val boundedRectangle = Rect(
            center = Offset(
                x = 0f,
                y = 0f,
            ),
            radius = radius + placeable.first().height,
        ).roundToIntRect()

        // Calculate the center of the working area and use it later for trig calculations
        val center = IntOffset(boundedRectangle.width / 2, boundedRectangle.height / 2)

        // Constrain our layout to the working area
        layout(boundedRectangle.width, boundedRectangle.height) {
            var requiredAngle = 0.0

            placeable.forEach { placeable ->
                // Calculate x,y coordinates where the layout will be placed on the
                // circumference of the circle using the required angle

//                val x = center.x + (radius * sin(Math.toRadians(requiredAngle))).toInt()
//                val y = center.y + (radius * cos(Math.toRadians(requiredAngle))).toInt()

                val x = center.x + (radius * sin(Math.toRadians(requiredAngle))).toInt()
                val y = center.y - (radius * cos(Math.toRadians(requiredAngle))).toInt()

                placeable.placeRelative(x - placeable.width / 2, y - placeable.height / 2)

                requiredAngle += angularSeparation
            }
        }
    }
}