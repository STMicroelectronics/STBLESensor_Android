package com.st.trilobyte.ui.fragment.if_builder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.st.BlueSTSDK.Node
import com.st.trilobyte.R
import com.st.trilobyte.databinding.FragmentIfBuilderBinding
import com.st.trilobyte.databinding.FragmentIfBuilderSelectFlowBinding
import com.st.trilobyte.helper.FlowHelper
import com.st.trilobyte.helper.loadSavedFlows
import com.st.trilobyte.ui.adapter.RadioAdapter

class IfBuilderSelectExp : IfFragment() {

    private lateinit var mBoard: Node.Type
    companion object {
        fun getInstance(board: Node.Type): IfBuilderSelectExp {
            val fragment = IfBuilderSelectExp()
            fragment.setBoardType(board)
            return fragment
        }
    }

    private fun setBoardType(board: Node.Type) {
        mBoard = board
    }

    private val expAdapter = RadioAdapter()

    private var _binding: FragmentIfBuilderSelectFlowBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIfBuilderSelectFlowBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.stActionbarIfBuilderSelect.actionbarText.text = getString(R.string.select_condition)
        binding.stActionbarIfBuilderSelect.leftAction.setOnClickListener { activity?.onBackPressed() }
        binding.userHintMessage.text = getString(R.string.expression_widget_empty_message)

        val availableFlows = loadSavedFlows(mBoard).toMutableList()
        expAdapter.setItems(availableFlows.filter { FlowHelper.canBeUsedAsExp(it) })
        binding.flowsRecyclerView.adapter = expAdapter

        getParentActivity()?.expression?.let {
            expAdapter.setSelectedItem(it)
        }

        expAdapter.notifyDataSetChanged()

        binding.saveButton.setOnClickListener { saveSelectedExpression() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.fragment_if_builder_select_flow, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        actionbar_text.text = getString(R.string.select_condition)
//        left_action.setOnClickListener { activity?.onBackPressed() }
//
//        user_hint_message.text = getString(R.string.expression_widget_empty_message)
//
//        val availableFlows = loadSavedFlows().toMutableList()
//        expAdapter.setItems(availableFlows.filter { FlowHelper.canBeUsedAsExp(it) })
//        flows_recycler_view.adapter = expAdapter
//
//        getParentActivity()?.expression?.let {
//            expAdapter.setSelectedItem(it)
//        }
//
//        expAdapter.notifyDataSetChanged()
//
//        save_button.setOnClickListener { saveSelectedExpression() }
//    }

    private fun saveSelectedExpression() {
        getParentActivity()?.expression = expAdapter.getSelectedItem()
        activity?.onBackPressed()
    }
}