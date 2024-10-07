package com.st.blue_voice.full_duplex

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
import com.st.blue_voice.BlueVoiceViewModel
import com.st.blue_voice.composable.BlueVoiceFullDuplexDemoContent
import com.st.core.ARG_NODE_ID
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.BlueMSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlueVoiceFullDuplexFragment : Fragment() {
    private val viewModel: BlueVoiceViewModel by viewModels()

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
                    BlueVoiceFullDuplexDemoScreen(
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
fun BlueVoiceFullDuplexDemoScreen(
    modifier: Modifier,
    viewModel: BlueVoiceViewModel,
    nodeId: String
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                viewModel.startDemo(nodeId = nodeId)
            }

            Lifecycle.Event.ON_STOP ->  {
                viewModel.stopDemo(nodeId = nodeId)
            }
            else -> Unit
        }
    }

    BlueVoiceFullDuplexDemoContent(
        modifier = modifier,
        viewModel = viewModel,
        nodeId = nodeId)
}
