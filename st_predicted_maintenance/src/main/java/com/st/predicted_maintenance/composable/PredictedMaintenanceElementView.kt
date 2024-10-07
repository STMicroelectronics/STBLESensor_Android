package com.st.predicted_maintenance.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.st.blue_sdk.features.extended.predictive.Status
import com.st.predicted_maintenance.utilities.Point
import com.st.predicted_maintenance.utilities.ViewStatus
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes

@Composable
fun PredictedMaintenanceElementView(
    modifier: Modifier = Modifier,
    title: String,
    value: ViewStatus,
    format: String = "Peak: %.2f"
) {


    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingMedium)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = title
            )

            PredictedMaintenanceAxeElementView(
                axeName = "X",
                statusValue = value.xStatus,
                point = value.x,
                format = format
            )

            PredictedMaintenanceAxeElementView(
                axeName = "Y",
                statusValue = value.yStatus,
                point = value.y,
                format = format
            )

            PredictedMaintenanceAxeElementView(
                axeName = "Z",
                statusValue = value.zStatus,
                point = value.z,
                format = format
            )

        }
    }
}


@Composable
@Preview
fun PredictedMaintenanceElementExample() {
    PredictedMaintenanceElementView(
        title = "Example",
        value = ViewStatus(
            xStatus = Status.GOOD,
            yStatus = Status.WARNING,
            zStatus = Status.BAD,
            x = Point(
                freq = 1.0f,
                value = 2.0f
            ),
            y = Point(
                freq = 3.0f
            ),
            z = Point(
                freq = 5.0f,
                value = 6.0F
            )
        ),
        format = "Peak: %.2f"
    )
}