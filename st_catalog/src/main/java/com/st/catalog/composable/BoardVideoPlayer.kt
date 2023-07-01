/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog.composable

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.st.ui.theme.Grey1

@Composable
fun BoardVideoPlayer(
    modifier: Modifier = Modifier,
    url: String = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(url)
            this.setMediaItem(mediaItem)
            this.prepare()
            this.playWhenReady = false
        }
    }

    Box(modifier = Modifier.background(Grey1)) {
        DisposableEffect(
            key1 = AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 1f),
                factory = {
                    StyledPlayerView(context).apply {
                        player = exoPlayer
                        layoutParams =
                            LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                    }
                }
            )
        ) {
            onDispose {
                exoPlayer.release()
            }
        }
    }
}

@Composable
fun BoardYouTubePlayer(
    modifier: Modifier = Modifier,
    videoId: String
) {
    Box(modifier = Modifier.background(Grey1)) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 1f),
            factory = { context ->
                YouTubePlayerView(context).also { view ->
                    view.addYouTubePlayerListener(
                        object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                super.onReady(youTubePlayer)
                                youTubePlayer.cueVideo(
                                    videoId = videoId,
                                    startSeconds = 0f
                                )
                            }
                        }
                    )
                }
            }
        )
    }
}
