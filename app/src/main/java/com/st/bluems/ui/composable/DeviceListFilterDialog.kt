/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.st.bluems.R
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Shapes
import kotlin.math.roundToInt

@Composable
fun DeviceListFilterDialog(
    modifier: Modifier = Modifier,
    filters: DeviceListFilter,
    onFilterChange: (DeviceListFilter) -> Unit = { /** NOOP**/ }
) {
    var internalFilters by remember(key1 = filters) { mutableStateOf(value = filters) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(id = R.string.st_home_deviceList_filterDialog_title)
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Divider()

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = stringResource(id = R.string.st_home_deviceList_filterDialog_rssiLabel)
                )
                Slider(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .padding(horizontal = LocalDimensions.current.paddingNormal),
                    value = internalFilters.rssi.toFloat() * -1,
                    onValueChange = {
                        internalFilters = internalFilters.copy(rssi = it.roundToInt() * -1)
                    },
                    valueRange = DeviceListFilter.rssiRange,
                    onValueChangeFinished = { /** NOOP **/ }
                )
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = stringResource(
                        id = R.string.st_home_deviceListItem_rssiFormatter,
                        internalFilters.rssi
                    )
                )
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                BlueMsButton(
                    text = stringResource(id = R.string.st_home_deviceList_filterDialog_okBtn),
                    onClick = { onFilterChange(internalFilters) }
                )
            }
        }
    }
}

data class DeviceListFilter(
    val rssi: Int = DEFAULT_RSSI
) {
    companion object {
        private const val DEFAULT_RSSI = -100
        val rssiRange = 4f..100f
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun DeviceListFilterDialogPreview() {
    PreviewBlueMSTheme {
        DeviceListFilterDialog(
            filters = DeviceListFilter(rssi = -22)
        )
    }
}
