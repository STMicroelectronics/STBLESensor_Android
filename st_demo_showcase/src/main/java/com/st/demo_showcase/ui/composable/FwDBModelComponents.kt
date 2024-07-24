package com.st.demo_showcase.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Shapes
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowFwDBModel(
    catalogInfo: BoardFirmware,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        val format = Json { prettyPrint = true }
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState()),
            shape = Shapes.medium
        ) {
            Column(modifier = Modifier.padding(all = LocalDimensions.current.paddingMedium)) {
                Text(
                    text = "Fw DB Entry:",
                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    letterSpacing = 0.15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                val model: String = format.encodeToString(catalogInfo)
                Text(
                    text = model,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.25.sp,
                    color = Grey6
                    )
            }

        }
    }
}