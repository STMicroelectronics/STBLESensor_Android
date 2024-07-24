package com.st.flow_demo.composable.custom_flow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.flow_demo.composable.custom_flow.entry.FlowDemoBooleanEntry
import com.st.flow_demo.composable.custom_flow.entry.FlowDemoDecimalEntry
import com.st.flow_demo.composable.custom_flow.entry.FlowDemoIntegerEntry
import com.st.flow_demo.composable.custom_flow.entry.FlowDemoRadioButtonGroupEntry
import com.st.flow_demo.composable.custom_flow.entry.FlowDemoStringEntry
import com.st.flow_demo.models.PropertyType
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoFunctionConfigurationScreen(
    paddingValues: PaddingValues,
    viewModel: FlowDemoViewModel,
    navController: NavHostController
) {

    BackHandler {
        navController.popBackStack()
    }

    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            text = "Function Configuration"
        )

        if (viewModel.functionOnConfig != null) {
            val functionOnConfig by remember { mutableStateOf(value = viewModel.functionOnConfig!!.copy()) }

            Text(
                modifier = Modifier.padding(
                    start = LocalDimensions.current.paddingNormal,
                    bottom = LocalDimensions.current.paddingMedium
                ),
                fontSize = 18.sp,
                text = functionOnConfig.description
            )

            functionOnConfig.properties.forEach { property ->
                when (property.type) {
                    PropertyType.FLOAT -> FlowDemoDecimalEntry(
                        title = property.label,
                        defaultValue = property.value as Float,
                        onModified = {
                            property.value = it
                        }
                    )

                    PropertyType.INT -> {
                        FlowDemoIntegerEntry(
                            title = property.label,
                            defaultValue = property.value as Int,
                            onModified = {
                                property.value = it
                            }
                        )
                    }

                    PropertyType.ENUM -> {
                        property.enumValues?.let { listPropertyValues ->
                            val valuesMap: List<Pair<Int, String>> =
                                listPropertyValues.map { single -> single.value to single.label }
                            FlowDemoRadioButtonGroupEntry(
                                title = property.label,
                                values = valuesMap,
                                defaultValue = property.value as Int,
                                onValueSelected = {
                                    property.value = it
                                }
                            )
                        }
                    }

                    PropertyType.STRING -> {
                        FlowDemoStringEntry(
                            title = property.label,
                            defaultValue = property.value as String,
                            onModified = {
                                property.value = it
                            }
                        )
                    }

                    PropertyType.BOOL -> {
                        FlowDemoBooleanEntry(
                            title = property.label,
                            defaultValue = property.value as Boolean,
                            onModified = {
                                property.value = it
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = LocalDimensions.current.paddingNormal)
            ) {

                Spacer(modifier = Modifier.weight(1f))

                BlueMsButton(
                    text = stringResource(id = R.string.done_message),
                    iconPainter = painterResource(id = R.drawable.ic_done),
                    enabled = true,
                    onClick = {
                        //We need to save the new Function Configuration
                        val flow = viewModel.flowOnCreation
                        flow?.let {
                            var function =
                                flow.functions.firstOrNull { it.id == functionOnConfig.id }
                            function?.let {
                                function = functionOnConfig.copy()
                            }
                            viewModel.functionOnConfig = functionOnConfig.copy()
                        }

                        //Come back to previous screen
                        navController.popBackStack()
                    }
                )
            }

        } else {
            Text(
                modifier = Modifier.padding(
                    start = LocalDimensions.current.paddingNormal,
                    bottom = LocalDimensions.current.paddingNormal
                ),
                color = ErrorText,
                fontSize = 18.sp,
                text = "Something wrong.."
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = LocalDimensions.current.paddingNormal)
            ) {
                BlueMsButton(
                    text = "Cancel",
                    iconPainter = painterResource(id = R.drawable.ic_close),
                    onClick = {
                        //Don't save the new Function Configuration and come back to previous screen
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}