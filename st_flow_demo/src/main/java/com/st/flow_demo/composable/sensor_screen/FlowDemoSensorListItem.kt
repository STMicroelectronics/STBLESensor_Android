package com.st.flow_demo.composable.sensor_screen

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.st.flow_demo.R
import com.st.flow_demo.helpers.getSensorIconResourceByName
import com.st.blue_sdk.board_catalog.models.Sensor
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.Shapes
import com.st.ui.theme.WarningText

@Composable
fun FlowDemoSensorListItem(
    sensor: Sensor,
    mounted: Boolean = true,
    onSensorSelected: () -> Unit = { /** NOOP**/ }
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal,
        onClick = onSensorSelected
    ) {
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
                painter = painterResource(id = getSensorIconResourceByName(sensor.icon)),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))

            Column(
                modifier = Modifier
                    .padding(start = LocalDimensions.current.paddingNormal)
                    .weight(1f), horizontalAlignment = Alignment.Start
            ) {

                if (!mounted) {
                    Text(
                        modifier = Modifier.padding(
                            bottom = LocalDimensions.current.paddingNormal
                        ),
                        fontSize = 12.sp,
                        color = WarningText,
                        text = "Not Mounted"
                    )
                }

                Text(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    text = sensor.description
                )

                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

                Text(
                    fontSize = 12.sp,
                    color = Grey6,
                    text = sensor.model
                )
            }

            Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))

            Icon(
                modifier = Modifier.size(size = LocalDimensions.current.iconNormal),
                painter = painterResource(id = R.drawable.ic_right_arrow),
                tint = PrimaryBlue,
                contentDescription = null
            )
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun SensorListItemMountedPreview() {
    FlowDemoSensorListItem(
        sensor = Sensor(
            id = "id",
            description = "description",
            icon = "ic_termometer",
            output = "output",
            model = "model",
            notes = "notes",
            dataType = "dataType",
            um = "um",
            fullScaleUm = "fullScaleUm",
            datasheetLink = "datasheetLink",
            board_compatibility = arrayListOf("board_compatibility")
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun SensorListItemNotMountedPreviewNotMounted() {
    FlowDemoSensorListItem(
        mounted = false,
        sensor = Sensor(
            id = "id",
            description = "description",
            icon = "ic_termometer",
            output = "output",
            model = "model",
            notes = "notes",
            dataType = "dataType",
            um = "um",
            fullScaleUm = "fullScaleUm",
            datasheetLink = "datasheetLink",
            board_compatibility = arrayListOf("board_compatibility")
        )
    )
}