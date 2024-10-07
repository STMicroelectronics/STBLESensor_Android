package com.st.medical_signal.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.medical_signal.MedicalSignalViewModel
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes


@Composable
fun MedicalSignalDemoContent(
    modifier: Modifier,
    viewModel: MedicalSignalViewModel,
    nodeId: String
) {

    val dataFeature16 by viewModel.dataFeature16.collectAsStateWithLifecycle()

    val dataFeature24 by viewModel.dataFeature24.collectAsStateWithLifecycle()

    val feature16Visible by remember(key1 = dataFeature16) {
        derivedStateOf { dataFeature16 != null }
    }

    val feature24Visible by remember(key1 = dataFeature24) {
        derivedStateOf { dataFeature24 != null }
    }

    val isMed16Streaming by viewModel.isMed16Streaming.collectAsStateWithLifecycle()

    val isMed24Streaming by viewModel.isMed24Streaming.collectAsStateWithLifecycle()

    val syntheticData by viewModel.syntheticData.collectAsStateWithLifecycle()

    var resetZoomTime16 by remember {
        mutableLongStateOf(0)
    }

    var resetZoomTime24 by remember {
        mutableLongStateOf(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LocalDimensions.current.paddingNormal)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingSmall),
            horizontalArrangement = Arrangement.spacedBy(
                LocalDimensions.current.paddingSmall
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (feature16Visible) {
                BlueMsButton(
                    text = "Med16", onClick = {
                        if (isMed16Streaming) {
                            viewModel.stopMed16(nodeId)
                        } else {
                            resetZoomTime16 = System.currentTimeMillis()
                            viewModel.startMed16(nodeId)
                        }
                    }, iconPainter = painterResource(
                        if (isMed16Streaming) {
                            com.st.ui.R.drawable.ic_stop
                        } else {
                            com.st.ui.R.drawable.ic_play
                        }
                    )
                )
            }

            if (feature24Visible) {
                BlueMsButton(
                    text = "Med24", onClick = {
                        if (isMed24Streaming) {
                            viewModel.stopMed24(nodeId)
                        } else {
                            resetZoomTime24 = System.currentTimeMillis()
                            viewModel.startMed24(nodeId)
                        }
                    }, iconPainter = painterResource(
                        if (isMed24Streaming) {
                            com.st.ui.R.drawable.ic_stop
                        } else {
                            com.st.ui.R.drawable.ic_play
                        }
                    )
                )
            }

            if (feature24Visible || feature16Visible) {
                BlueMsButton(text = "Reset", onClick = {
                    resetZoomTime16 = System.currentTimeMillis()
                    resetZoomTime24 = System.currentTimeMillis()
                })
            }
        }

        if (feature16Visible) {
            MedicalSignalPlotView(
                modifier = Modifier.weight(2f),
                type = "Med16",
                featureUpdate = dataFeature16,
                resetZoomTime = resetZoomTime16
            )
        }

        if (feature24Visible) {
            MedicalSignalPlotView(
                modifier = Modifier.weight(2f),
                type = "Med24",
                featureUpdate = dataFeature24,
                resetZoomTime = resetZoomTime24
            )
        }

        syntheticData?.let { data ->
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = LocalDimensions.current.paddingNormal),
                shape = Shapes.small,
                shadowElevation = LocalDimensions.current.elevationNormal
            ) {
                Text(
                    modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
                    text = data,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}