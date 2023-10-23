package com.st.flow_demo.composable.sensor_screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.helpers.getSensorIconResourceByName
import com.st.flow_demo.helpers.getSensorPropertiesDescription
import com.st.ui.theme.LocalDimensions

@Composable
fun FlowDemoSensorDetailScreen(
    viewModel: FlowDemoViewModel,
    sensorId: String,
    navController: NavHostController,
    paddingValues: PaddingValues
) {

    BackHandler {
        navController.popBackStack()
    }

    //val format = Json { prettyPrint = true }
    val sensorsList by viewModel.sensorsList.collectAsStateWithLifecycle()
    val expansionSensorsList by viewModel.expansionSensorsList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (sensorsList.isNotEmpty()) {
        var sensorSelected = sensorsList.firstOrNull { it.id == sensorId }

        if(sensorSelected==null) {
            sensorSelected = expansionSensorsList.firstOrNull { it.id == sensorId }
        }

        if (sensorSelected != null) {
            //in theory it should be always !=null

            //val details: String = format.encodeToString(sensorSelected)
            Column(modifier = Modifier.padding(paddingValues)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.iconNormal)
                            .padding(all = LocalDimensions.current.paddingSmall),
                        painter = painterResource(id = getSensorIconResourceByName(sensorSelected.icon)),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))

                    Text(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        text = sensorSelected.description
                    )
                }

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))

                FlowDemoSensorDetailsRow(title = "Output", content = sensorSelected.output)

                FlowDemoSensorDetailsRow(title = "Unit", content = sensorSelected.um)

                if (sensorSelected.configuration != null) {
                    FlowDemoSensorDetailsRow(
                        title = "Properties", content = getSensorPropertiesDescription(
                            context = context,
                            sensor = sensorSelected, board = viewModel.getBoardType()
                        )
                    )
                }

                FlowDemoSensorDetailsRow(
                    title = "Description",
                    content = sensorSelected.notes
                )

                FlowDemoSensorDetailsRow(title = "Model", content = sensorSelected.model)


                if (sensorSelected.datasheetLink.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = LocalDimensions.current.paddingLarge
                            ),
                    ) {
                        Text(
                            text = "DataSheet",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(0.3f)
                                .padding(end = LocalDimensions.current.paddingNormal)
                        )

                        Text(
                            buildAnnotatedString {
                                append("open from ")

                                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                    append("www.st.com")
                                }
                            },
                            modifier = Modifier
                                .weight(0.7f)
                                .padding(end = LocalDimensions.current.paddingNormal)
                                .clickable {
                                    openDataSheetFromUrl(
                                        context = context,
                                        dataSheetLink = sensorSelected.datasheetLink
                                    )
                                }
                        )
                    }
                }

//                Text(
//                    modifier = Modifier.padding(paddingValues),
//                    text = details
//                )
            }
        }
    }
}

private fun openDataSheetFromUrl(context: Context, dataSheetLink: String) {
    Intent(Intent.ACTION_VIEW).also { intent ->
        intent.data = Uri.parse(dataSheetLink)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}


@Composable
fun FlowDemoSensorDetailsRow(title: String, content: String) {
    if (content.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = LocalDimensions.current.paddingLarge
                )
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(0.3f)
                    .padding(end = LocalDimensions.current.paddingNormal)
            )

            Text(
                text = content,
                modifier = Modifier
                    .weight(0.7f)
                    .padding(end = LocalDimensions.current.paddingNormal)
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))
    }
}