/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.st.bluems.R
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6

@Composable
fun EmptyDeviceList(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.spacerLarge))

        Image(
            modifier = Modifier.size(size = LocalDimensions.current.imageLarge),
            painter = painterResource(R.drawable.empty_list),
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Text(
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            text = stringResource(id = R.string.st_home_deviceList_emptyTitle)
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Text(
            style = MaterialTheme.typography.titleSmall,
            color = Grey6,
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.st_home_deviceList_emptyDescription)
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun EmptyDeviceListPreview() {
    PreviewBlueMSTheme {
        EmptyDeviceList()
    }
}
