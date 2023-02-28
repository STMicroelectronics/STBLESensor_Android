package com.st.trilobyte.ui.fragment.if_builder

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.st.BlueSTSDK.Node
import com.st.trilobyte.R
import com.st.trilobyte.databinding.FragmentIfBuilderBinding
import com.st.trilobyte.helper.DialogHelper.showDialog
import com.st.trilobyte.services.Session
import com.st.trilobyte.ui.UploadFlowActivity
import com.st.trilobyte.widget.IfBuilderWidget

class IfBuilderFragment : IfFragment() {

    private lateinit var mBoard: Node.Type
    companion object {
        fun getInstance(board: Node.Type): IfBuilderFragment {
            val instance = IfBuilderFragment()
            instance.setBoardType(board)
            return instance
        }
    }

    private fun setBoardType(board: Node.Type) {
        mBoard = board
    }

    private var _binding: FragmentIfBuilderBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIfBuilderBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.stActionbarIfBuilder.actionbarText.text = getString(R.string.build_if_header)
        view.findViewById<View>(R.id.left_action).setOnClickListener { activity?.onBackPressed() }

        binding.expressionWidget.setWidgetListener(object : IfBuilderWidget.IfWidgetClickListener {
            override fun onWidgetSelected() {
                switchFragment(IfBuilderSelectExp.getInstance(mBoard))
            }
        })

        binding.statementsWidget.setWidgetListener(object : IfBuilderWidget.IfWidgetClickListener {
            override fun onWidgetSelected() {
                switchFragment(IfBuilderSelectStats.getInstance(mBoard))
            }
        })

        binding.terminateButton.setOnClickListener { activity?.onBackPressed() }

        binding.uploadButton.setOnClickListener { uploadSelectedFlows() }

        updateView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.fragment_if_builder, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        actionbar_text.text = getString(R.string.build_if_header)
//        view.findViewById<View>(R.id.left_action).setOnClickListener { activity?.onBackPressed() }
//
//        expression_widget.setWidgetListener(object : IfBuilderWidget.IfWidgetClickListener {
//            override fun onWidgetSelected() {
//                switchFragment(IfBuilderSelectExp.getInstance())
//            }
//        })
//
//        statements_widget.setWidgetListener(object : IfBuilderWidget.IfWidgetClickListener {
//            override fun onWidgetSelected() {
//                switchFragment(IfBuilderSelectStats.getInstance())
//            }
//        })
//
//        terminate_button.setOnClickListener { activity?.onBackPressed() }
//
//        upload_button.setOnClickListener { uploadSelectedFlows() }
//
//        updateView()
//    }

    private fun updateView() {
        binding.expressionWidget.addExpression(getParentActivity()?.expression)
        binding.statementsWidget.addStatements(getParentActivity()?.statements)
    }

    private fun uploadSelectedFlows() {

        val exp = getParentActivity()?.expression
        if (exp == null) {
            showDialog(requireActivity(), getString(R.string.error_select_if_condition_before_play), null)
            return
        }

        val stats = getParentActivity()?.statements
        if (stats.isNullOrEmpty()) {
            showDialog(requireActivity(), getString(R.string.error_select_flows_before_play), null)
            return
        }

        Session.setSession(stats, exp)

        val intent = UploadFlowActivity.provideIntent(requireContext(),mBoard)
        startActivity(intent)
        activity?.finish()
    }
}