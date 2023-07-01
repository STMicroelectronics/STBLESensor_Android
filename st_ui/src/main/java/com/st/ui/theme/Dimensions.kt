/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val regularDimension = AppDimension(
    btnMinHeight = 40.dp,
    btnMinWidth = 120.dp,
    topAppBarHeight = 64.dp,
    catalogCardHeight = 180.dp,

    spacerSmall = 24.dp,
    spacerNormal = 48.dp,
    spacerMedium = 96.dp,
    spacerLarge = 192.dp,

    imageSmall = 30.dp,
    imageNormal = 60.dp,
    imageMedium = 120.dp,
    imageLarge = 240.dp,

    iconSmall = 24.dp,
    iconNormal = 48.dp,
    iconMedium = 96.dp,
    iconLarge = 192.dp,

    cornerSmall = 4.dp,
    cornerNormal = 8.dp,
    cornerMedium = 16.dp,
    cornerLarge = 32.dp,

    elevationSmall = 4.dp,
    elevationNormal = 8.dp,
    elevationMedium = 16.dp,
    elevationLarge = 32.dp,

    paddingSmall = 4.dp,
    paddingNormal = 8.dp,
    paddingMedium = 16.dp,
    paddingLarge = 32.dp
)

val LocalDimensions = staticCompositionLocalOf { regularDimension }

@Immutable
data class AppDimension(
    val btnMinWidth: Dp,
    val btnMinHeight: Dp,
    val topAppBarHeight: Dp,
    val catalogCardHeight: Dp,

    val spacerSmall: Dp,
    val spacerNormal: Dp,
    val spacerMedium: Dp,
    val spacerLarge: Dp,

    val imageSmall: Dp,
    val imageNormal: Dp,
    val imageMedium: Dp,
    val imageLarge: Dp,

    val iconSmall: Dp,
    val iconNormal: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,

    val cornerSmall: Dp,
    val cornerNormal: Dp,
    val cornerMedium: Dp,
    val cornerLarge: Dp,

    val elevationSmall: Dp,
    val elevationNormal: Dp,
    val elevationMedium: Dp,
    val elevationLarge: Dp,

    val paddingSmall: Dp,
    val paddingNormal: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp
)
