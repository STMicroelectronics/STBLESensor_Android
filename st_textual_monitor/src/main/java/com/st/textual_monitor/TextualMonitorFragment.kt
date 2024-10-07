package com.st.textual_monitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled
import com.st.core.ARG_NODE_ID
import com.st.textual_monitor.composable.TextualMonitorDemoContent
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.BlueMSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TextualMonitorFragment : Fragment() {

    private val viewModel: TextualMonitorViewModel by viewModels()

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
                    TextualMonitorDemoScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        viewModel = viewModel,
                        nodeId = nodeId
                    )
                }
            }
        }
    }
}

@Composable
fun TextualMonitorDemoScreen(
    modifier: Modifier,
    viewModel: TextualMonitorViewModel,
    nodeId: String
) {
    ComposableLifecycle { _, event ->
        when (event) {

            Lifecycle.Event.ON_STOP -> {
                if (viewModel.feature != null) {
                    if (viewModel.feature!!.name == RawControlled.NAME) {
                        viewModel.stopRawPnPLDemo(nodeId)
                    } else {
                        viewModel.stopDemo(nodeId)
                    }
                }
            }
            else -> Unit
        }
    }

    TextualMonitorDemoContent(
        modifier = modifier,
        viewModel = viewModel,
        nodeId = nodeId
    )
}