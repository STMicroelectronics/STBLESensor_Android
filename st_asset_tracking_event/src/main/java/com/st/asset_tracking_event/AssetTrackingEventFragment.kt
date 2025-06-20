package com.st.asset_tracking_event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.st.core.ARG_NODE_ID
import com.st.asset_tracking_event.composable.AssetTrackingEventContent
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.LocalDimensions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssetTrackingEventFragment : Fragment() {

    private val viewModel: AssetTrackingEventFViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    AssetTrackingEventFDemoScreen(
                        modifier =  Modifier
                            .fillMaxSize()
                            .padding(top = LocalDimensions.current.paddingNormal,
                                start = LocalDimensions.current.paddingNormal,
                                end = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        nodeId = nodeId
                    )
                }
            }

        }
    }
}

@Composable
fun AssetTrackingEventFDemoScreen(
    modifier: Modifier,
    viewModel: AssetTrackingEventFViewModel,
    nodeId: String
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                viewModel.startDemo(nodeId = nodeId)
            }

            Lifecycle.Event.ON_STOP -> viewModel.stopDemo(nodeId = nodeId)
            else -> Unit
        }
    }

    AssetTrackingEventContent(
        modifier = modifier,
        viewModel = viewModel
    )
}