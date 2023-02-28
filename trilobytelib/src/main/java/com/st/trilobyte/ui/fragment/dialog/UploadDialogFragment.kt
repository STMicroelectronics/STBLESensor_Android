package com.st.trilobyte.ui.fragment.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.NodeConnectionService
import com.st.trilobyte.R
import com.st.trilobyte.communication.TrilobyteFlowUploader
import com.st.trilobyte.databinding.DialogUploadFlowBinding
import com.st.trilobyte.models.board.DeviceFlow
import com.st.trilobyte.models.board.DeviceIfStatement
import com.st.trilobyte.services.Session
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class UploadDialogFragment : DialogFragment() {

    private var node: Node? = null

    private var uploadListener: UploadListener? = null

    private val flowUploader = TrilobyteFlowUploader()

    companion object {
        fun getInstance(node: Node?, uploadListener: UploadListener): DialogFragment {
            val df = UploadDialogFragment()
            df.isCancelable = false
            df.node = node
            df.uploadListener = uploadListener
            return df
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.STBoxAppThemeTranslucent)
    }


    private var _binding: DialogUploadFlowBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = DialogUploadFlowBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rotate = RotateAnimation(360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 3000
        rotate.repeatCount = Animation.INFINITE
        binding.uploadImageview.animation = rotate

        Manager.getSharedInstance().stopDiscovery()

        val n = node
        if(n!=null){
            n.addNodeStateListener(nodeStateListener)
            NodeConnectionService.connect(requireContext(), n)
        } else {
            notifyError(TrilobyteFlowUploader.CommunicationError.GENERIC_ERROR.code)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.dialog_upload_flow, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val rotate = RotateAnimation(360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//        rotate.duration = 3000
//        rotate.repeatCount = Animation.INFINITE
//        upload_imageview.animation = rotate
//
//        Manager.getSharedInstance().stopDiscovery()
//
//        val n = node
//        if(n!=null){
//            n.addNodeStateListener(nodeStateListener)
//            NodeConnectionService.connect(requireContext(), n)
//        } else {
//            notifyError(TrilobyteFlowUploader.CommunicationError.GENERIC_ERROR.code)
//        }
//    }


    private fun sendFlow() {

        val data = when (Session.expression) {
            null -> DeviceFlow.getBoardStream(Session.selectedFlows!!)
            else -> DeviceIfStatement.getBoardStream(Session.expression!!, Session.selectedFlows!!)
        }

        // compress data
        val data_gzip = gzip(data)

        flowUploader.uploadFlow(node, data_gzip, object : TrilobyteFlowUploader.FlowUploadListener {
            override fun onSuccess() {
                node?.removeNodeStateListener(nodeStateListener)
                context?.let { NodeConnectionService.disconnect(it.applicationContext, node) }
                activity?.runOnUiThread {
                    uploadListener?.onSuccess()
                }
            }

            override fun onError(errorCode: Int) {
                notifyError(errorCode)
            }
        })
    }

    private fun gzip(content: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(content) }
        return bos.toByteArray()
    }

    private fun ungzip(content: ByteArray): String =
            GZIPInputStream(content.inputStream()).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun notifyError(errorCode: Int) {
        activity?.runOnUiThread {
            node?.removeNodeStateListener(nodeStateListener)
            NodeConnectionService.disconnect(requireContext(), node)
            uploadListener?.onError(errorCode)
            dismiss()
        }
    }

    private val nodeStateListener: Node.NodeStateListener = Node.NodeStateListener { _, newState, _ ->
        when (newState) {
            Node.State.Connected -> sendFlow()
            Node.State.Lost, Node.State.Dead, Node.State.Unreachable, Node.State.Disconnecting ->
                notifyError(TrilobyteFlowUploader.CommunicationError.GENERIC_ERROR.code)
            else -> {}
        }
    }

    interface UploadListener {
        fun onSuccess()

        fun onError(errorCode: Int)
    }
}