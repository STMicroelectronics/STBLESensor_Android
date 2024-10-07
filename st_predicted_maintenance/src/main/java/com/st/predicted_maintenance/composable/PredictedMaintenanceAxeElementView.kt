package com.st.predicted_maintenance.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.st.blue_sdk.features.extended.predictive.Status
import com.st.predicted_maintenance.R
import com.st.predicted_maintenance.utilities.Point
import com.st.ui.theme.LocalDimensions
import java.util.Locale

@Composable
fun PredictedMaintenanceAxeElementView(
    modifier: Modifier = Modifier,
    axeName: String,
    statusValue: Status?,
    point: Point?,
    format: String = "Peak: %.2f"
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
    ) {
        statusValue?.let { status ->
            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.iconNormal),
                painter = painterResource(
                    getStatusImage(status)
                ),
                tint = Color.Unspecified,
                contentDescription = null
            )
        }

        Column(
            modifier
                .weight(2f)
                .padding(start = LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
        ) {

            statusValue?.let { status ->
                Text(
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    text = "$axeName: ${getStatusString(status)}"
                )
            }

            point?.let { point ->

                point.freq?.let { freq ->
                    if(!freq.isNaN()) {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            text = String.format(
                                Locale.getDefault(),
                                "Frequency (Hz): %.2f",
                                freq
                            )
                        )
                    }
                }

                point.value?.let { value ->
                    if(!value.isNaN()) {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            text = String.format(Locale.getDefault(), format, value)
                        )
                    }
                }
            }
        }
    }
}

private fun getStatusString(s: Status): String {
    return when (s) {
        Status.GOOD -> "Good"
        Status.WARNING -> "Warning"
        Status.BAD -> "Alarm"
        else -> "Unknown"
    }
}

@DrawableRes
private fun getStatusImage(s: Status): Int {
    return when (s) {
        Status.GOOD -> R.drawable.predictive_status_good
        Status.WARNING -> R.drawable.predictive_status_warnings
        Status.BAD -> R.drawable.predictive_status_bad
        else -> R.drawable.predictive_status_unknown
    }
}