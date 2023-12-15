package com.st.catalog.composable

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Shapes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogBoardComponents(
    compList: List<String>,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = Shapes.medium
        ) {
            Column(modifier = Modifier.padding(all= LocalDimensions.current.paddingMedium)) {
                Text(
                    text = "On board components:",
                    modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    letterSpacing = 0.15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyColumn(
                    contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                    verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingSmall)
                ) {
                    items(compList) {
                        Text(
                            text = "\u2022 $it",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            letterSpacing = 0.25.sp,
                            color = Grey6,
                        )
                    }
                }
                BlueMsButton(
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.End)
                        .padding(top = LocalDimensions.current.paddingNormal),
                    text = stringResource(id = R.string.ok),
                    onClick = {
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun DialogBoardComponentsPreview() {
    PreviewBlueMSTheme {
        DialogBoardComponents(
            compList = listOf("Giuseppe", "Alberto", "Luca"),
            onDismissRequest = {}
        )
    }
}