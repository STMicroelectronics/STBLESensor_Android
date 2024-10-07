package com.st.audio_classification_demo.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.audio_classification_demo.AudioClassificationDemoViewModel
import com.st.blue_sdk.features.extended.audio_classification.AudioClassificationInfo.Companion.ALGORITHM_NOT_DEFINED


@Composable
fun AudioClassificationDemoContent(
    modifier: Modifier,
    viewModel: AudioClassificationDemoViewModel
) {

    val audioClassificationData by viewModel.audioClassificationData.collectAsStateWithLifecycle()

    if (audioClassificationData.second != null) {
        val algorithmType by remember(key1 = audioClassificationData.second) {
            derivedStateOf {
                if (audioClassificationData.first.algorithm.value == ALGORITHM_NOT_DEFINED) {
                    0
                } else {
                    audioClassificationData.first.algorithm.value.toInt()
                }
            }
        }

        when (algorithmType) {
            1 -> {
                //mBabyCryingView
                AudioClassificationBabyCryingContent(modifier,audioClassificationData)
            }
            else -> {
                //mSceneClassificationView
                AudioClassificationContent(modifier,audioClassificationData)
            }
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