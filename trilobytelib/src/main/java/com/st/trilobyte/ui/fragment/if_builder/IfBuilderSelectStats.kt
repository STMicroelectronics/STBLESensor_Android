package com.st.trilobyte.ui.fragment.if_builder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.st.BlueSTSDK.Node
import com.st.trilobyte.R
import com.st.trilobyte.databinding.FragmentIfBuilderSelectFlowBinding
import com.st.trilobyte.helper.*
import com.st.trilobyte.ui.adapter.FlowAdapter


class IfBuilderSelectStats : IfFragment() {

    private lateinit var flowsAdapter: FlowAdapter
    private lateinit var mBoard: Node.Type

    companion object {
        fun getInstance(board: Node.Type): IfBuilderSelectStats {
            val fragment = IfBuilderSelectStats()
            fragment.setBoardType(board)
            return fragment
        }
    }

    private fun setBoardType(board: Node.Type) {
        mBoard = board
    }

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

        binding.stActionbarIfBuilderSelect.actionbarText.text = getString(R.string.select_flows)
        binding.stActionbarIfBuilderSelect.leftAction.setOnClickListener { activity?.onBackPressed() }

        binding.userHintMessage.text = getString(R.string.expression_widget_empty_statements)

        val savedFlows = loadSavedFlows(mBoard)

        val availableFlows = savedFlows.filter { it.canBeUploaded() }.toMutableList()
        getCounterFlowList(context!!,mBoard)?.let {
            availableFlows.addAll(0, it)
        }

        flowsAdapter = FlowAdapter(availableFlows, null)
        binding.flowsRecyclerView.adapter = flowsAdapter

        getParentActivity()?.statements?.let {
            flowsAdapter.selectedItems = it.filter { flow -> availableFlows.contains(flow) }
        }

        flowsAdapter.notifyDataSetChanged()

        binding.saveButton.setOnClickListener { saveSelectedStatements() }
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
//        actionbar_text.text = getString(R.string.select_flows)
//        left_action.setOnClickListener { activity?.onBackPressed() }
//
//        user_hint_message.text = getString(R.string.expression_widget_empty_statements)
//
//        val savedFlows = loadSavedFlows()
//
//        val availableFlows = savedFlows.filter { it.canBeUploaded() }.toMutableList()
//        getCounterFlowList(context!!)?.let {
//            availableFlows.addAll(0, it)
//        }
//
//        flowsAdapter = FlowAdapter(availableFlows, null)
//        flows_recycler_view.adapter = flowsAdapter
//
//        getParentActivity()?.statements?.let {
//            flowsAdapter.selectedItems = it.filter { flow -> availableFlows.contains(flow) }
//        }
//
//        flowsAdapter.notifyDataSetChanged()
//
//        save_button.setOnClickListener { saveSelectedStatements() }
//    }

    private fun saveSelectedStatements() {
        getParentActivity()?.statements = flowsAdapter.selectedItems
        activity?.onBackPressed()
    }
}