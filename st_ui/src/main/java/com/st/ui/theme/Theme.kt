package com.st.ui.theme

import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Grey0,
    secondary = PrimaryBlue,
    onSecondary = Grey0,
    tertiary = PrimaryBlue,
    onTertiary = Grey0,

    background = Grey0,
    surface = Grey0,
    onSurface = PrimaryBlue3,
    surfaceVariant = Grey1,
    outline = Grey1,
    outlineVariant = Grey1,

    inversePrimary = PrimaryYellow,
    inverseSurface = Grey10,
    inverseOnSurface = Grey0,

//    surfaceContainer = Grey0,
//    surfaceContainerHigh = Grey0,
//    surfaceContainerHighest = Grey0,
//    surfaceContainerLow = Grey0,
//    surfaceContainerLowest = Grey0
)

@Composable
fun PreviewBlueMSTheme(content: @Composable () -> Unit) {
    BlueMSTheme {
        Surface {
            content()
        }
    }
}

@Composable
fun BlueMSTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalDimensions provides regularDimension) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            shapes = Shapes,
            typography = Typography(),
            content = {
                ProvideTextStyle(
                    value = LocalTextStyle.current.copy(fontFamily = st_main_font),
                    content = content
                )
            }
        )
    }
}
