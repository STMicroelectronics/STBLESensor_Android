package com.st.flow_demo.composable.more_info

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.st.blue_sdk.models.Boards
import com.st.flow_demo.FlowDemoViewModel
import com.st.flow_demo.R
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.utils.getBlueStBoardImages

@Composable
fun FlowDemoMoreInfoScreen(
    viewModel: FlowDemoViewModel,
    paddingValues: PaddingValues
) {
    val boardType = viewModel.node?.boardType

    boardType?.let {
        val context = LocalContext.current

        Column(modifier = Modifier.padding(paddingValues)) {

            if (it != Boards.Model.SENSOR_TILE_BOX) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

                    Image(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.iconMedium)
                            .padding(all = LocalDimensions.current.paddingSmall),
                        painter = painterResource(id = getBlueStBoardImages(boardType = boardType.name)),
                        contentDescription = null
                    )


                    Text(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        text = "STEVAL-MKBOXPRO"
                    )

                    Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))


                }

                FlowDemoMoreInfoRow(
                    iconId = R.drawable.ic_book,
                    content = stringResource(id = R.string.technical_documentation),
                    onClick = {
                        openLinkFromUrl(
                            context = context,
                            link = "https://www.st.com/en/evaluation-tools/steval-mkboxpro.html#documentation"
                        )
                    }
                )

                FlowDemoMoreInfoRow(
                    iconId = R.drawable.ic_help,
                    content = stringResource(id = R.string.help_support),
                    onClick = {
                        openLinkFromUrl(
                            context = context,
                            link = "https://www.st.com/en/evaluation-tools/steval-mkboxpro.html"
                        )
                    }
                )

                FlowDemoMoreInfoRow(
                    iconId = R.drawable.ic_web,
                    content = stringResource(id = R.string.about_sensortile_box_pro),
                    onClick = {
                        openLinkFromUrl(
                            context = context,
                            link = "https://www.st.com/en/evaluation-tools/steval-mkboxpro.html"
                        )
                    }
                )

                FlowDemoMoreInfoRow(
                    iconId = R.drawable.ic_language,
                    content = stringResource(id = R.string.stm_website),
                    onClick = {
                        openLinkFromUrl(
                            context = context,
                            link = "https://www.st.com/"
                        )
                    }
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

                    Image(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.iconMedium)
                            .padding(all = LocalDimensions.current.paddingSmall),
                        painter = painterResource(id = getBlueStBoardImages(boardType = boardType.name)),
                        contentDescription = null
                    )


                    Text(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        text = "TEVAL-MKSBOX1V1"
                    )

                    Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingLarge))


                }

                FlowDemoMoreInfoRow(
                    iconId = R.drawable.ic_book,
                    content = stringResource(id = R.string.technical_documentation),
                    onClick = {
                        openLinkFromUrl(
                            context = context,
                            link = "https://www.st.com/SensorTilebox#documentation"
                        )
                    }
                )

                FlowDemoMoreInfoRow(
                    iconId = R.drawable.ic_help,
                    content = stringResource(id = R.string.help_support),
                    onClick = {
                        openLinkFromUrl(
                            context = context,
                            link = "https://www.st.com/SensorTilebox"
                        )
                    }
                )

                FlowDemoMoreInfoRow(
                    iconId = R.drawable.ic_web,
                    content = stringResource(id = R.string.about_sensortile_box),
                    onClick = {
                        openLinkFromUrl(
                            context = context,
                            link = "https://www.st.com/SensorTilebox"
                        )
                    }
                )

                FlowDemoMoreInfoRow(
                    iconId = R.drawable.ic_language,
                    content = stringResource(id = R.string.stm_website),
                    onClick = {
                        openLinkFromUrl(
                            context = context,
                            link = "https://www.st.com/"
                        )
                    }
                )
            }

            Spacer(
                Modifier.windowInsetsBottomHeight(
                    WindowInsets.navigationBars
                )
            )
        }
    }
}

@Composable
fun FlowDemoMoreInfoRow(
    @DrawableRes iconId: Int,
    content: String,
    onClick: () -> Unit = { /** NOOP **/ }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingMedium)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

        Icon(
            modifier = Modifier
                .size(size = LocalDimensions.current.iconNormal)
                .padding(all = LocalDimensions.current.paddingSmall),
            painter = painterResource(id = iconId),
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

        Text(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            text = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.weight(1.0f))

        Icon(
            modifier = Modifier.size(size = LocalDimensions.current.iconNormal),
            painter = painterResource(id = R.drawable.ic_right_arrow),
            tint = PrimaryBlue,
            contentDescription = null
        )
    }

    Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))
}

private fun openLinkFromUrl(context: Context, link: String) {
    Intent(Intent.ACTION_VIEW).also { intent ->
        intent.data = Uri.parse(link)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}


