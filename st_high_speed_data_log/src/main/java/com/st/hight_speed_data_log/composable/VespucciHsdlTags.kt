package com.st.hight_speed_data_log.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.st.high_speed_data_log.R
import com.st.ui.theme.Grey3
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme

@Composable
fun VespucciHsdlTags(
    modifier: Modifier = Modifier,
    acquisitionInfo: String,
    isLogging: Boolean,
    vespucciTags: Map<String, Boolean>,
    onTagChangeState: (String, Boolean) -> Unit = { _, _ -> /**NOOP**/ }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = LocalDimensions.current.paddingNormal)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        Description()

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

        AcquisitionInfo(acquisitionInfo = acquisitionInfo)

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

        TagsInfo(
            isLogging = isLogging,
            vespucciTags = vespucciTags,
            onTagChangeState = onTagChangeState
        )
    }
}

@Composable
fun Description(
    modifier: Modifier = Modifier
) {
    val style = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    )
    val emphasisStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.25.sp
    )
    val annotatedString = buildAnnotatedString {
        withStyle(style = style) {
            append(stringResource(id = R.string.st_hsdl_tags_description1))
            append(" ")
        }
        withStyle(style = emphasisStyle) {
            append(stringResource(id = R.string.st_hsdl_tags_description2))
            append(" ")
        }
        withStyle(style = style) {
            append(stringResource(id = R.string.st_hsdl_tags_description3))
        }
    }

    Text(
        color = Grey6,
        modifier = modifier.fillMaxWidth(),
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
        text = annotatedString
    )
}

@Composable
fun AcquisitionInfo(
    modifier: Modifier = Modifier,
    acquisitionInfo: String
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal),
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingMedium)
        ) {
            Icon(
                modifier = Modifier
                    .size(size = LocalDimensions.current.iconNormal),
                imageVector = Icons.Outlined.Info,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(LocalDimensions.current.paddingNormal))

            Column(
                modifier = Modifier.fillMaxWidth()

            ) {
                Text(
                    color = MaterialTheme.colorScheme.primary,
                    text = stringResource(id = R.string.st_hsdl_tags_acquisitionTitle),
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.68.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.14.sp,
                    fontSize = 16.sp
                )
                Text(
                    color = Grey6,
                    lineHeight = 24.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.15.sp,
                    text = acquisitionInfo,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TagsInfo(
    modifier: Modifier = Modifier,
    isLogging: Boolean,
    vespucciTags: Map<String, Boolean> = emptyMap(),
    onTagChangeState: (String, Boolean) -> Unit = { _, _ -> /**NOOP**/ }
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal),
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalDimensions.current.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.iconNormal),
                    imageVector = Icons.Outlined.Info,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(LocalDimensions.current.paddingNormal))

                Column(
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Text(
                        color = MaterialTheme.colorScheme.primary,
                        text = stringResource(id = R.string.st_hsdl_tags_tagsTitle),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.68.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.14.sp,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(LocalDimensions.current.paddingNormal))

            Divider()

            Spacer(modifier = Modifier.height(LocalDimensions.current.paddingNormal))

            vespucciTags.forEach { tag ->
                TagListItem(
                    tag = tag.key,
                    isEnabled = isLogging,
                    isChecked = tag.value,
                    onCheckChange = { checked ->
                        onTagChangeState(tag.key, checked)
                    }
                )
            }
        }
    }
}

@Composable
fun TagListItem(
    modifier: Modifier = Modifier,
    tag: String,
    isChecked: Boolean = true,
    isEnabled: Boolean = true,
    onCheckChange: (Boolean) -> Unit = { /** NOOP **/ }
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = LocalDimensions.current.paddingMedium)
    ) {
        Icon(
            modifier = Modifier
                .size(size = LocalDimensions.current.iconSmall)
                .scale(scaleX = -1f, scaleY = 1f),
            imageVector = Icons.Default.Sell,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingMedium))

        Text(
            color = Grey6,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = tag,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            enabled = isEnabled,
            checked = isChecked,
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedTrackColor = Grey6,
                disabledUncheckedTrackColor = Grey3
            ),
            onCheckedChange = onCheckChange
        )
    }
}


/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun VespucciHsdlTagsPreview() {
    PreviewBlueMSTheme {
        VespucciHsdlTags(
            acquisitionInfo = "Fri May 26 2023 13:47:27",
            isLogging = false,
            vespucciTags = mapOf("Prova" to true, "Test" to false, "Mock" to true)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VespucciHsdlTagsLoggingPreview() {
    PreviewBlueMSTheme {
        VespucciHsdlTags(
            acquisitionInfo = "Fri May 26 2023 13:47:27",
            isLogging = true,
            vespucciTags = mapOf("Prova" to true, "Test" to false, "Mock" to true)
        )
    }
}
