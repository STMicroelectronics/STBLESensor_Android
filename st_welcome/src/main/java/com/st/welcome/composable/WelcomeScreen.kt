/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.welcome.composable

import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.st.ui.composables.BlueMsButton
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.utils.asString
import com.st.welcome.R
import com.st.welcome.StWelcomeConfig
import com.st.welcome.model.WelcomePage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    welcomePages: List<WelcomePage> = emptyList(),
    onSkip: () -> Unit = { /** NOOP **/ }
) {
    val pagerState = rememberPagerState { welcomePages.size }


    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {

                    Text(
                        text = "Welcome",
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.15.sp,
                        fontWeight = FontWeight.Bold
                    )
                })
        }
    ) { paddingValues ->

        Column(
            modifier = modifier
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues)
                .padding(LocalDimensions.current.paddingNormal)
        ) {

            HorizontalPager(
                modifier = Modifier.weight(weight = 0.1f),
                verticalAlignment = Alignment.Top,
                state = pagerState
            ) { pageIndex ->
                val imageId = welcomePages[pageIndex].drawableRes
                val videoAssetsId = welcomePages[pageIndex].videoAssetsId
                val title = welcomePages[pageIndex].title
                val description = welcomePages[pageIndex].description

                WelcomePageContent(
                    modifier = Modifier.fillMaxSize(),
                    imageId = imageId,
                    videoAssetsId = videoAssetsId,
                    title = title,
                    description = description
                )
            }

            HorizontalPagerIndicator(
                modifier = Modifier
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(all = LocalDimensions.current.paddingNormal),
                indicatorCount = welcomePages.size,
                pagerState = pagerState
            )

            BlueMsButton(
                modifier = Modifier
                    .align(alignment = Alignment.End),
                text =
                    if (pagerState.currentPage == pagerState.pageCount - 1)
                        stringResource(id = R.string.st_welcome_closeButtonLabel)
                    else
                        stringResource(id = R.string.st_welcome_skipButtonLabel),
                onClick = onSkip
            )

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun WelcomePageContent(
    modifier: Modifier,
    imageId: Int?,
    videoAssetsId: String?,
    title: String,
    description: String
) {
    Column(
        modifier = modifier.padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(height = LocalDimensions.current.spacerSmall))

        if(imageId!=null) {
            Image(
                //modifier = Modifier.fillMaxWidth(),
                modifier = Modifier.fillMaxHeight(0.75f).fillMaxWidth(),
                contentScale = ContentScale.Fit,
                painter = painterResource(id = imageId),
                contentDescription = null
            )
        } else if(videoAssetsId!=null) {
            LoopMutedVideo(
                assetPath = videoAssetsId,
                modifier = Modifier.fillMaxHeight(0.75f).fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.spacerSmall))

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            maxLines = StWelcomeConfig.maxLinesTitle,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.primary,
            text = title
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.spacerSmall))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            maxLines = StWelcomeConfig.maxLinesDescription,
            overflow = TextOverflow.Ellipsis,
            color = Grey6,
            text = description
        )
    }
}

@Composable
fun LoopMutedVideo(assetPath: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val exoPlayer = remember(context, assetPath) {
        ExoPlayer.Builder(context).build().apply {
            val uri = "asset:///$assetPath".toUri()
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                player = exoPlayer
                useController = false // Hide player controls
                //resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        modifier = modifier.aspectRatio(ratio = 1f)
    )
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    PreviewBlueMSTheme {
        WelcomeScreen(
            modifier = Modifier,
            welcomePages = listOf(
                WelcomePage(
                    title = LoremIpsum(words = 3).asString(),
                    description = LoremIpsum(words = 20).asString(),
                    drawableRes = R.drawable.placeholder
                )
            )
        )
    }
}
