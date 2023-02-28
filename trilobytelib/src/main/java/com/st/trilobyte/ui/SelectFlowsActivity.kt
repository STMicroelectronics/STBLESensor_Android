package com.st.trilobyte.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.st.BlueSTSDK.Node
import com.st.trilobyte.R
import com.st.trilobyte.databinding.ActivitySelectFlowsBinding
import com.st.trilobyte.helper.DialogHelper
import com.st.trilobyte.helper.getFunctionList
import com.st.trilobyte.helper.loadSavedFlows
import com.st.trilobyte.models.Flow
import com.st.trilobyte.services.Session
import com.st.trilobyte.ui.adapter.FlowAdapter

class SelectFlowsActivity : AppCompatActivity() {

    private lateinit var flowsAdapter: FlowAdapter

    private var selectedFlows: List<Flow> = listOf()

    private lateinit var mBoard: Node.Type

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_select_flows)
//
//        supportActionBar?.let {
//            title = getString(R.string.select_flow_to_play)
//            it.setDisplayHomeAsUpEnabled(true)
//        }
//
//        val functionList = getFunctionList(this)
//
//        val availableFlows = loadSavedFlows()
//        flowsAdapter = FlowAdapter(availableFlows.filter { it.canBeUploaded() }, null)
//        flows_recycler_view.adapter = flowsAdapter
//        flowsAdapter.notifyDataSetChanged()
//
//        upload_button.setOnClickListener(View.OnClickListener {
//            selectedFlows = flowsAdapter.selectedItems
    private lateinit var binding: ActivitySelectFlowsBinding

    companion object {
        val EXTRA_BOARD_TYPE = "extra-board_type"

        fun provideIntent(context: Context, board: Node.Type) : Intent {
            val intent = Intent(context,SelectFlowsActivity::class.java)
            intent.putExtra(EXTRA_BOARD_TYPE,board)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectFlowsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.let {
            title = getString(R.string.select_flow_to_play)
            it.setDisplayHomeAsUpEnabled(true)
        }

        val startIntent = intent
        mBoard = startIntent.getSerializableExtra(EXTRA_BOARD_TYPE) as Node.Type


        val functionList = getFunctionList(this,mBoard)

        val availableFlows = loadSavedFlows(mBoard)
        flowsAdapter = FlowAdapter(availableFlows.filter { it.canBeUploaded() }, null)
        binding.flowsRecyclerView.adapter = flowsAdapter
        flowsAdapter.notifyDataSetChanged()

        binding.uploadButton.setOnClickListener(View.OnClickListener {
        selectedFlows = flowsAdapter.selectedItems

            if (selectedFlows.isEmpty()) {
                DialogHelper.showDialog(this, getString(R.string.error_select_flows_before_play), null)
                return@OnClickListener
            }

            uploadSelectedFlows(selectedFlows)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun uploadSelectedFlows(selectedFlows: List<Flow>) {
        Session.setSession(selectedFlows)
        val intent = UploadFlowActivity.provideIntent(this, mBoard)
        startActivity(intent)
        finish()
    }
}