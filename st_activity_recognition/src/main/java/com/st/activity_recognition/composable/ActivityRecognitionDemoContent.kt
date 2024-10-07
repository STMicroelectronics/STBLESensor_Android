package com.st.activity_recognition.composable

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
import com.st.activity_recognition.ActivityRecognitionViewModel
import com.st.blue_sdk.features.activity.ActivityInfo

@Composable
fun ActivityRecognitionDemoContent(
    modifier: Modifier,
    viewModel: ActivityRecognitionViewModel
) {

    val activityData by viewModel.activityData.collectAsStateWithLifecycle()


    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val algorithmType by remember(key1 = activityData.second) {
            derivedStateOf {
                if (activityData.first.algorithm.value == ActivityInfo.ALGORITHM_NOT_DEFINED) {
                    0
                } else {
                    activityData.first.algorithm.value.toInt()
                }
            }
        }

        when (algorithmType) {
            1 -> {
                // GMP activity recognition
                ActivityRecognitionMotionARContent(modifier, showFastWalking = false, activityData)
            }

            2 -> {
                // IGN activity recognition
                ActivityRecognitionMotionIGNContent(modifier, activityData)
            }

            3 -> {
                //activity recognition from mlc
                ActivityRecognitionMotionARContent(modifier, showFastWalking = false, activityData)
            }

            4 -> {
                //adult presence recognition from mlc
                ActivityRecognitionMotionAPD_MLCContent(modifier, activityData)
            }

            else -> {
                //default view
                ActivityRecognitionMotionARContent(modifier, showFastWalking = true, activityData)
            }
        }

        if (activityData.second == null) {
            Text(
                style = MaterialTheme.typography.displayMedium,
                text = "Waiting data…"
            )
        }
    }
}