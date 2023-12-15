/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.environmental

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.st.core.ARG_NODE_ID
import com.st.environmental.model.EnvironmentalValues
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnvironmentalDemoFragment : Fragment() {

    private val viewModel: EnvironmentalViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    EnvironmentalDemoScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(all = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        nodeId = nodeId
                    )
                }
            }
        }
    }
}

@Composable
fun EnvironmentalDemoScreen(
    modifier: Modifier,
    viewModel: EnvironmentalViewModel,
    nodeId: String
) {

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.startDemo(nodeId = nodeId)
            Lifecycle.Event.ON_STOP -> viewModel.stopDemo(nodeId = nodeId)
            else -> Unit
        }
    }

    EnvironmentalDemoScreen(
        modifier = modifier,
        viewModel = viewModel,
        nodeId = nodeId,
        environmentalValues = viewModel.environmentalValues
    )
}

@Composable
fun EnvironmentalDemoScreen(
    modifier: Modifier,
    viewModel: EnvironmentalViewModel,
    nodeId: String,
    environmentalValues: EnvironmentalValues
) {

    Column(modifier = modifier) {

        var useFahrenheit by remember { mutableStateOf(value = false) }
        var temperatureModifier by rememberSaveable { mutableStateOf(value = "0.0") }
        val temperaturesState by remember(key1 = environmentalValues.temperatures) {
            derivedStateOf {
                adjustEnvironmentalValues(
                    environmentalValues.temperatures.values.toList(),
                    temperatureModifier
                )
            }
        }
        val temperatures = temperaturesState.map {
            if (useFahrenheit.not()) it else (it * 9 / 5) + 32
        }

        var pressureModifier by rememberSaveable { mutableStateOf(value = "0.0") }
        val pressure by remember(key1 = environmentalValues.pressure) {
            derivedStateOf {
                adjustEnvironmentalValue(environmentalValues.pressure, pressureModifier)
            }
        }

        var humidityModifier by rememberSaveable { mutableStateOf(value = "0.0") }
        val humidity by remember(key1 = environmentalValues.humidity) {
            derivedStateOf {
                adjustEnvironmentalValue(environmentalValues.humidity, humidityModifier)
            }
        }

        if(viewModel.environmentalValues.hasTemperatures) {
            EnvironmentalWidget(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.readFeatures(nodeId = nodeId)
                    },
                drawableResId = R.drawable.temperature_icon,
                values = temperatures,
                modifierValue = temperatureModifier,
                onModifierChanged = {
                    temperatureModifier = it
                },
                topWidget = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "°C/°F")
                        Switch(
                            checked = useFahrenheit,
                            onCheckedChange = {
                                useFahrenheit = it
                            }
                        )
                    }
                }
            )
        }

        if(viewModel.environmentalValues.hasPressure) {
            Spacer(Modifier.height(height = 16.0.dp))

            EnvironmentalWidget(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.readFeatures(nodeId = nodeId)
                    },
                drawableResId = R.drawable.pressure_icon,
                values = listOf(pressure),
                modifierValue = pressureModifier,
                onModifierChanged = {
                    pressureModifier = it
                }
            )
        }

        if(viewModel.environmentalValues.hasHumidity) {
            Spacer(Modifier.height(height = 16.0.dp))

            EnvironmentalWidget(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.readFeatures(nodeId = nodeId)
                    },
                drawableResId = R.drawable.humidity_icon,
                values = listOf(humidity),
                modifierValue = humidityModifier,
                onModifierChanged = {
                    humidityModifier = it
                }
            )
        }
    }
}

private fun adjustEnvironmentalValue(baseValue: Float, modifier: String): Float {
    return baseValue + runCatching { modifier.toFloat() }.getOrDefault(0f)
}

private fun adjustEnvironmentalValues(baseValue: List<Float>, modifier: String): List<Float> {
    return baseValue.map { adjustEnvironmentalValue(it, modifier) }
}

@Composable
fun EnvironmentalWidget(
    modifier: Modifier,
    @DrawableRes drawableResId: Int,
    values: List<Float>,
    //modifierLabel: String = "+/-",
    modifierLabel: String = "Offset +/-",
    modifierValue: String = "0.0",
    topWidget: @Composable () -> Unit = { /** NOOP **/ },
    onModifierChanged: (String) -> Unit = { /** NOOP **/ }
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Min)
            .padding(all = LocalDimensions.current.paddingSmall),
        shape = Shapes.small
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                modifier = Modifier.size(size = 100.dp),
                painter = painterResource(id = drawableResId),
                contentDescription = null
            )

            Column(
                modifier = Modifier
                    .weight(weight = 0.1f)
                    .padding(start = LocalDimensions.current.paddingNormal),
            ) {
                values.forEach {
                    Text(
                        textAlign = TextAlign.Start,
                        text = it.toString()
                    )
                }
            }

            Column(
                modifier = Modifier.padding(all = LocalDimensions.current.paddingSmall),
                horizontalAlignment = Alignment.End
            ) {
                topWidget()
                Spacer(modifier = Modifier.weight(weight = 1f))
                Text(modifierLabel)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier.width(width = 80.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
//                    label = {
//                        Text(text = modifierLabel)
//                    },
                    value = modifierValue,
                    onValueChange = {
                        onModifierChanged(it)
                    }
                )
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

//@Preview(showBackground = true)
//@Composable
//private fun EnvironmentalDemoScreenPreview() {
//    BlueMSTheme {
//        EnvironmentalDemoScreen(
//            Modifier.fillMaxSize(),
//            null,
//            EnvironmentalValues(mapOf(Pair(0, 0f)), 0f, 0f)
//        )
//    }
//}