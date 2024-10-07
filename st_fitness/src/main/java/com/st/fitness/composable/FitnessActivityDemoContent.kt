package com.st.fitness.composable

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.extended.fitness_activity.FitnessActivityType
import com.st.fitness.FitnessActivityViewModel
import com.st.fitness.R
import com.st.ui.theme.LocalDimensions
import java.util.Locale

@Composable
fun FitnessActivityDemoContent(
    modifier: Modifier,
    viewModel: FitnessActivityViewModel,
    nodeId: String
) {


    val currentActivity by viewModel.currentActivity.collectAsStateWithLifecycle()
    val currentCounter by viewModel.currentCounter.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingLarge)
    ) {

        FitnessActivityDropDownMenu(
            title = "Activity:",
            initialValue = currentActivity.toString(),
            values = FitnessActivityType.entries.filter{ it != FitnessActivityType.Error }.map { it.toString() },
            onValueSelected = { eventSelected ->
                viewModel.setActivity(nodeId, FitnessActivityType.fromString(eventSelected))
            })

        AnimatedContent(targetState = currentActivity, label = "") { activity ->
            Icon(
                modifier = Modifier.size(size = LocalDimensions.current.imageExtraLarge),
                painter = painterResource(
                    getActivityIconResource(activity = activity)
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )
        }

        AnimatedContent(targetState = currentCounter, label = "",
            transitionSpec = {
                // Compare the incoming number with the previous number.
                if (targetState > initialState) {
                    // If the target number is larger, it slides up and fades in
                    // while the initial (smaller) number slides up and fades out.
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                } else {
                    // If the target number is smaller, it slides down and fades in
                    // while the initial number slides down and fades out.
                    (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> height } + fadeOut())
                }.using(
                    // Disable clipping since the faded slide-in/out should
                    // be displayed out of bounds.
                    SizeTransform(clip = false)
                )
            }) { counter ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = LocalDimensions.current.paddingLarge),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = String.format(
                    Locale.getDefault(),
                    "%d %s", counter, currentActivity.toString()
                )
            )
        }
    }
}

@DrawableRes
fun getActivityIconResource(activity: FitnessActivityType): Int {
    return when (activity) {
        FitnessActivityType.NoActivity -> R.drawable.fitness_unknown_activity_icon
        FitnessActivityType.BicepCurl -> R.drawable.fitness_bicipet_curl_icon
        FitnessActivityType.Squat -> R.drawable.fitness_squat_icon
        FitnessActivityType.PushUp -> R.drawable.fitness_push_up_icon
        FitnessActivityType.Error -> R.drawable.fitness_unknown_activity_icon
    }
}
