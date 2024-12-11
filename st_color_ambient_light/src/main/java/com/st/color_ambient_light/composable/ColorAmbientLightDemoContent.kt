package com.st.color_ambient_light.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.color_ambient_light.ColorAmbientLightViewModel
import com.st.ui.theme.LocalDimensions


@Composable
fun ColorAmbientLightDemoContent(
    modifier: Modifier,
    viewModel: ColorAmbientLightViewModel
) {
    val colorData by viewModel.colorData.collectAsStateWithLifecycle()

    if (colorData != null) {
        Column(
            modifier = modifier.fillMaxWidth().padding(start = LocalDimensions.current.paddingNormal,
                end = LocalDimensions.current.paddingNormal,
                top = LocalDimensions.current.paddingNormal,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingLarge)
        ) {

            ColorElementView(
                modifier = Modifier.padding(top = LocalDimensions.current.paddingLarge),
                name = colorData!!.lux.name,
                value = colorData!!.lux.value,
                min = colorData!!.lux.min!!.toInt(),
                max = colorData!!.lux.max!!.toInt(),
                unit = colorData!!.lux.unit!!
            )

            ColorElementView(
                name = colorData!!.cct.name,
                value = colorData!!.cct.value.toInt(),
                min = colorData!!.cct.min!!.toInt(),
                max = colorData!!.cct.max!!.toInt(),
                unit = colorData!!.cct.unit!!
            )

            ColorElementView(
                name = colorData!!.uvIndex.name,
                value = colorData!!.uvIndex.value.toInt(),
                min = colorData!!.uvIndex.min!!.toInt(),
                max = colorData!!.uvIndex.max!!.toInt(),
                unit = ""
            )
        }
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                text = "Waiting data…"
            )
        }
    }
}