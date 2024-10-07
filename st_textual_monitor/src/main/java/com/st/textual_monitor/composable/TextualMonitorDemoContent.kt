package com.st.textual_monitor.composable

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled
import com.st.textual_monitor.R
import com.st.textual_monitor.TextualMonitorViewModel
import com.st.ui.theme.LocalDimensions


@Composable
fun TextualMonitorDemoContent(
    modifier: Modifier,
    viewModel: TextualMonitorViewModel,
    nodeId: String
) {
    //retrieve the List of Features
    val features = viewModel.getNodeFeatureList(nodeId)

    //Current Feature
    var currentFeature by remember { mutableStateOf(features.first()) }

    var isPlaying by remember { mutableStateOf(false) }

    val dataFeature by viewModel.featureTime.collectAsStateWithLifecycle()

    val dataValues by remember(key1 = dataFeature) {
        derivedStateOf {
            if (dataFeature != null) {
                viewModel.dataValues.joinToString("\n")
            } else {
                "Waiting samples"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingLarge)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextualMonitorDropDownMenu(
                modifier = Modifier.weight(0.85f),
                title = "Feature",
                initialValue = currentFeature.name,
                values = features.map { it.name },
                onValueSelected = { eventSelected ->
                    currentFeature = features.first { it.name == eventSelected }

                    //Check if the PrevSelected feature is already notifying something or not
                    val prevFeature = viewModel.feature
                    if (prevFeature != null) {
                        //Disable the notification of previous selected Feature
                        if (prevFeature.name == RawControlled.NAME) {
                            viewModel.stopRawPnPLDemo(nodeId)
                        } else {
                            viewModel.stopDemo(nodeId)
                        }

                        // Change the icon for starting button
                        isPlaying = false
                    }
                    //viewModel.setSelectedFeature(currentFeature.feature, currentFeature.bleCharDesc)
                })

            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.iconNormal)
                    .clickable {
                        //isPlaying = !isPlaying

                        if (viewModel.feature != null) {
                            //the feature is already notifying
                            if (viewModel.feature!!.name == RawControlled.NAME) {
                                viewModel.stopRawPnPLDemo(nodeId)
                            } else {
                                viewModel.stopDemo(nodeId)
                            }

                            isPlaying = false
                        } else {
                            if (currentFeature.name != RawControlled.NAME) {

                                //set the current feature
                                viewModel.setSelectedFeature(
                                    currentFeature.feature,
                                    currentFeature.bleCharDesc
                                )

                                //Start the notification
                                viewModel.startDemo(nodeId)
                            } else {
                                //set the current feature
                                viewModel.setSelectedFeature(
                                    currentFeature.feature,
                                    currentFeature.bleCharDesc
                                )
                                viewModel.startRawPnPLDemo(nodeId)
                            }
                            isPlaying = true
                        }
                    }
                    .weight(0.15f)
                    .padding(end = LocalDimensions.current.paddingSmall),
                painter = if (isPlaying) {
                    painterResource(
                        R.drawable.ic_stop
                    )
                } else {
                    painterResource(
                        R.drawable.ic_play_arrow
                    )
                },
                tint = Color.Unspecified,
                contentDescription = null
            )
        }

//        Text(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(2f)
//                .padding(
//                    start = LocalDimensions.current.paddingNormal,
//                    end = LocalDimensions.current.paddingNormal,
//                    bottom = LocalDimensions.current.paddingLarge
//                )
//                .verticalScroll(rememberScrollState()),
//            style = MaterialTheme.typography.bodySmall,
//            text = dataValues
//        )

        DrawScrollableView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .padding(
                    start = LocalDimensions.current.paddingNormal,
                    end = LocalDimensions.current.paddingNormal,
                    bottom = LocalDimensions.current.paddingLarge
                ),
            content = {
                Text(
                    style = MaterialTheme.typography.bodySmall,
                    text = dataValues
                )
            }
        )
    }
}

//View done for showing the scrollable indicator on Text, otherwise they are not visible
@Composable
fun DrawScrollableView(content: @Composable () -> Unit, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = {
            val scrollView = ScrollView(it)
            val layout = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scrollView.layoutParams = layout
            scrollView.isVerticalFadingEdgeEnabled = true
            scrollView.isScrollbarFadingEnabled = false
            scrollView.addView(ComposeView(it).apply {
                setContent {
                    content()
                }
            })
            val linearLayout = LinearLayout(it)
            linearLayout.orientation = LinearLayout.VERTICAL
            linearLayout.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            linearLayout.addView(scrollView)
            linearLayout
        }
    )
}
