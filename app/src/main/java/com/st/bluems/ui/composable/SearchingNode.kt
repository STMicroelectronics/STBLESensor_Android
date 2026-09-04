package com.st.bluems.ui.composable

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.st.ui.composables.BlueMSPullToRefreshBox
import com.st.ui.composables.StTopBar
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchingNode(
    modifier: Modifier = Modifier,
    isBetaRelease: Boolean = false,
    nodeId: String
) {

    var isRefreshing by remember { mutableStateOf(true) }

    val context = LocalContext.current

    BackHandler {
        if(isRefreshing.not()) {
            (context as? Activity)?.finishAndRemoveTask()
        }
    }
    LaunchedEffect(key1 = Unit) {
        delay(15000.milliseconds)
        isRefreshing = false
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            StTopBar(
                title = "Searching Node"
            )
        }
    ) { paddingValues ->
        val pullRefreshState = rememberPullToRefreshState()
        BlueMSPullToRefreshBox(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize(),
            state = pullRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {},
            isBetaRelease = isBetaRelease
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(LocalDimensions.current.paddingMedium),
                contentAlignment = Alignment.Center
            ) {
                if (isRefreshing) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Searching Node\n$nodeId",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Column {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = "$nodeId\nNot Found!",
                            style = MaterialTheme.typography.displaySmall,
                            color = ErrorText
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = "Check the board\n or try again",
                            style = MaterialTheme.typography.displaySmall,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}