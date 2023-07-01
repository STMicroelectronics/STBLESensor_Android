package com.st.demo_showcase.ui.serial_console
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.st.core.ARG_NODE_ID
import com.st.demo_showcase.R
import com.st.demo_showcase.databinding.FragmentSerialConsoleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SerialConsole : BottomSheetDialogFragment() {
    private lateinit var binding : FragmentSerialConsoleBinding
    private val viewModel: SerialConsoleViewModel by activityViewModels()
    private lateinit var nodeId: String

    private lateinit var textView: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var imageView: ImageView

    private var isPlaying=true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = FragmentSerialConsoleBinding.inflate(inflater, container, false)

        textView = binding.serialConsoleText
        scrollView = binding.serialConsoleScrollview

        imageView = binding.serialConsolePausePlay
        imageView.setOnClickListener { changePausePlay() }

        return binding.root
    }

    private fun changePausePlay() {
        if(isPlaying) {
            isPlaying = false
            imageView.setImageResource(R.drawable.baseline_play_arrow_24)
        } else {
            isPlaying = true
            imageView.setImageResource(R.drawable.baseline_pause_24)
        }
    }

    override fun getTheme(): Int {
        return com.st.ui.R.style.BottomSheetDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            maxHeight = getScreenHeight()/8
            state = BottomSheetBehavior.STATE_COLLAPSED
            isHideable = true //the user could hide it with a swipe down
            isFitToContents = false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.debugMessages.collect {
                    if(isPlaying) {
                        if (!it.isNullOrEmpty()) {
                            val string = textView.text.toString() + it
                            textView.text = string
                            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                        }
                    }
                }
            }
        }
    }

    private fun getScreenHeight() : Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = requireActivity().windowManager.currentWindowMetrics
            val rect = windowMetrics.bounds
            rect.bottom
        } else {
            resources.displayMetrics.heightPixels
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startReceiveDebugMessage(nodeId = nodeId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopReceiveDebugMessage()
    }

}