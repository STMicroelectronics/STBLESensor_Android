package com.st.event_counter

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
import com.st.event_counter.composable.EventCounterDemoContent
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.BlueMSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventCounterFragment : Fragment() {

    private val viewModel: EventCounterViewModel by viewModels()

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
                    EventCounterDemoScreen(
                        modifier = Modifier
                            .fillMaxSize().padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                        viewModel = viewModel,
                        nodeId = nodeId
                    )
                }
            }

        }
    }
}

@Composable
fun EventCounterDemoScreen(
    modifier: Modifier,
    viewModel: EventCounterViewModel,
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

    EventCounterDemoContent(
        modifier = modifier,
        viewModel = viewModel
    )
}