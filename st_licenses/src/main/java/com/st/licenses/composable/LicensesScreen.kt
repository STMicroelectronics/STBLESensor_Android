package com.st.licenses.composable

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.st.ui.composables.StTopBar

@Composable
fun LicensesScreen(onBack: () -> Unit = { /** NOOP**/ }) {
    Scaffold(modifier = Modifier
        .fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = { StTopBar(title = "Third Parties Licenses", onBack = onBack) }) { paddingValues ->

        Column(modifier = Modifier
            .consumeWindowInsets(paddingValues)
            .padding(paddingValues)) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        //settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        //settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                    }
                },
                update = { webView ->
                    webView.loadUrl("file:///android_asset/open_source_licenses.html")
                }
            )
        }
    }
}