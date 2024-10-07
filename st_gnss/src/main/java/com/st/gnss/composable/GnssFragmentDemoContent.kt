package com.st.gnss.composable

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.features.extended.gnss.GNSSInfo
import com.st.gnss.GnssViewModel
import com.st.gnss.LocationData
import com.st.gnss.R
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.Shapes

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GnssFragmentDemoContent(
    modifier: Modifier,
    viewModel: GnssViewModel
) {

    val gnssData by viewModel.gnssData.collectAsStateWithLifecycle()

    var locationData by remember { mutableStateOf<Pair<LocationData, Long>?>(value = null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .weight(0.25f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium),
                    painter = painterResource(R.drawable.ic_gnss_gps_coordinates),
                    tint = Color.Unspecified,
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                        .padding(start = LocalDimensions.current.paddingLarge),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        text = "Lat: ${gnssData?.latitude?.value ?: 0.0} ${
                            getLatitudeDirection(
                                gnssData
                            )
                        }"
                    )

                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        text = "Lon: ${gnssData?.longitude?.value ?: 0.0}  ${
                            getLongitudeDirection(
                                gnssData
                            )
                        }"
                    )

                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        text = "Alt: ${gnssData?.altitude?.value ?: 0.0} [m]"
                    )
                }

                if (gnssData != null) {
                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.iconNormal)
                            .align(Alignment.Bottom)
                            .clickable {
                                locationData = Pair(
                                    LocationData(
                                        latitude = gnssData!!.latitude.value,
                                        longitude = gnssData!!.longitude.value
                                    ), System.currentTimeMillis()
                                )
                            }
                            .padding(
                                bottom = LocalDimensions.current.paddingNormal,
                                end = LocalDimensions.current.paddingNormal
                            ),
                        painter = painterResource(R.drawable.ic_gps_fixed_24),
                        tint = PrimaryBlue,
                        contentDescription = null
                    )
                } else {
                    Icon(
                        modifier = Modifier
                            .size(size = LocalDimensions.current.iconNormal)
                            .align(Alignment.Bottom)
                            .padding(
                                bottom = LocalDimensions.current.paddingNormal,
                                end = LocalDimensions.current.paddingNormal
                            ),
                        painter =
                        painterResource(R.drawable.ic_gps_not_fixed_24),
                        tint = Grey6,
                        contentDescription = null
                    )
                }
            }
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .weight(0.20f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(size = LocalDimensions.current.imageMedium),
                    painter = painterResource(R.drawable.ic_gnss_satellite),
                    tint = Color.Unspecified,
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                        .padding(start = LocalDimensions.current.paddingLarge),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        text = "${gnssData?.numSatellites?.value ?: 0.0} Num"
                    )

                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        text = "${gnssData?.signalQuality?.value ?: 0.0} [dB-Hz]"
                    )
                }
            }
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .weight(0.55f),
            shape = Shapes.small,
            shadowElevation = LocalDimensions.current.elevationNormal
        ) {
            locationData?.let { location ->
                AnimatedContent(targetState = location.second, label = "",
                    transitionSpec = {
                        (fadeIn() + slideInVertically(animationSpec = tween(400),
                            initialOffsetY = { fullHeight -> fullHeight })).togetherWith(
                            fadeOut(
                                animationSpec = tween(200)
                            )
                        )
                    }) { _ ->
                    AndroidView(factory = {
                        WebView(it).apply {
                            this.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            this.webChromeClient = WebChromeClient()
                            this.settings.javaScriptEnabled = true
                            this.loadUrl("file:///android_asset/gnss_leaflat.html")
                            this.addJavascriptInterface(
                                com.st.gnss.utility.SetLocation(location.first),
                                "Android"
                            )
                        }
                    })
                }
            }
        }
    }
}

fun getLatitudeDirection(gnss: GNSSInfo?): String {
    return if (gnss != null) {
        if (gnss.latitude.value >= 0) {
            "N"
        } else {
            "S"
        }
    } else {
        ""
    }
}

fun getLongitudeDirection(gnss: GNSSInfo?): String {
    return if (gnss != null) {
        if (gnss.longitude.value >= 0) {
            "E"
        } else {
            "W"
        }
    } else {
        ""
    }
}
