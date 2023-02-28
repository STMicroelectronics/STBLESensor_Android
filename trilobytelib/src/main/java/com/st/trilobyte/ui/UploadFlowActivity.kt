package com.st.trilobyte.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NodeScanActivity
import com.st.trilobyte.R
import com.st.trilobyte.adapter.BoardAdapter
import com.st.trilobyte.databinding.ActivityUploadFlowBinding
import com.st.trilobyte.helper.*
import com.st.trilobyte.models.Flow
import com.st.trilobyte.services.Session
import com.st.trilobyte.ui.fragment.dialog.UploadDialogFragment

class UploadFlowActivity : NodeScanActivity() {

    companion object {
        const val SEARCH_TIMEOUT = 10000
            val EXTRA_BOARD_TYPE = "extra-board_type"

            fun provideIntent(context: Context, board: Node.Type) : Intent {
                val intent = Intent(context,UploadFlowActivity::class.java)
                intent.putExtra(EXTRA_BOARD_TYPE,board)
                return intent
            }
    }

    private lateinit var mAdapter: BoardAdapter

    private var mSelectedNode: Node? = null

    private lateinit var mBoard: Node.Type

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_upload_flow)
//
//        supportActionBar?.let {
//            it.title = getString(R.string.board)
//            it.setDisplayHomeAsUpEnabled(true)
//        }
//
//        if (Session.selectedFlows.isNullOrEmpty()) {
//            finish()
//            return
//        }
//
//        mAdapter = BoardAdapter(BoardAdapter.BoardListener { node ->
//            DialogHelper.showDialog(this@UploadFlowActivity, getString(R.string.overwrite_board),
//                    getString(R.string.warn_overvrite_message), getString(R.string.ok), getString(R.string.cancel), null) { _, which ->
//                if (which == DialogInterface.BUTTON_POSITIVE) {
//                    mSelectedNode = node
//                    checkBoardType()
//                }
//            }
//        })
//
//
//        board_list.adapter = mAdapter
//
//        val rotate = RotateAnimation(360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//        rotate.duration = 3000
//        rotate.repeatCount = Animation.INFINITE
//        sync_imageview.animation = rotate
//    }

    private lateinit var binding: ActivityUploadFlowBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUploadFlowBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val startIntent = intent
        mBoard = startIntent.getSerializableExtra(SelectFlowsActivity.EXTRA_BOARD_TYPE) as Node.Type

        supportActionBar?.let {
            it.title = getString(R.string.board)
            it.setDisplayHomeAsUpEnabled(true)
        }

        if (Session.selectedFlows.isNullOrEmpty()) {
            finish()
            return
        }

        mAdapter = BoardAdapter(BoardAdapter.BoardListener { node ->

            binding.boardListSwypeRefresh.post(Runnable {
                binding.boardListSwypeRefresh.isRefreshing = false
            })

            DialogHelper.showDialog(this@UploadFlowActivity, getString(R.string.overwrite_board),
                    getString(R.string.warn_overvrite_message), getString(R.string.ok), getString(R.string.cancel), null) { _, which ->
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    mSelectedNode = node
                    checkBoardType()
                }
            }
        })


        binding.boardList.adapter = mAdapter

        binding.boardListSwypeRefresh.setOnRefreshListener {
            startDiscovery()
        }


        //set refreshing color
        binding.boardListSwypeRefresh.setProgressBackgroundColorSchemeColor(resources.getColor(com.st.BlueSTSDK.gui.R.color.swipeColor_background))
        binding.boardListSwypeRefresh.setColorSchemeResources(
            com.st.BlueSTSDK.gui.R.color.swipeColor_1, com.st.BlueSTSDK.gui.R.color.swipeColor_2,
            com.st.BlueSTSDK.gui.R.color.swipeColor_3, com.st.BlueSTSDK.gui.R.color.swipeColor_4
        )

        binding.boardListSwypeRefresh.setSize(SwipeRefreshLayout.DEFAULT)
        binding.boardListSwypeRefresh.setOnRefreshListener {
            startDiscovery()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        if (validateFlows()) {
            Manager.getSharedInstance().addListener(bleScanListener)
            startDiscovery()
        }
    }

    private fun validateFlows(): Boolean {

        val flows = mutableListOf<Flow>()

        Session.expression?.let {
            flows.add(it)
        }

        Session.selectedFlows?.let {
            flows.addAll(it)
        }

        val functions = getFunctionList(this,mBoard)
        if (!validateFlows(this, flows, functions!!,
                        DialogInterface.OnClickListener { _, _ -> finish() })) {
            return false
        }

        return true
    }

    /*
    private fun startDiscovery() {
        if (!Manager.getSharedInstance().isDiscovering) {
            Manager.getSharedInstance().resetDiscovery()
            mAdapter.clear()
            Manager.getSharedInstance().nodes.forEach {
                if(it.type!=Node.Type.SENSOR_TILE_BOX)
                    return@forEach
                mAdapter.addNode(it)
                Log.d("Flow","add: ${it.tag}")
            }
            runOnUiThread { mAdapter.notifyDataSetChanged() }
            startNodeDiscovery(SEARCH_TIMEOUT)
        }
    }
    */

    private fun startDiscovery() {
        if (!Manager.getSharedInstance().isDiscovering) {
            binding.boardListSwypeRefresh.post(Runnable {
                binding.boardListSwypeRefresh.isRefreshing = true
            })
            Manager.getSharedInstance().resetDiscovery()
            mAdapter.clear()
            Manager.getSharedInstance().nodes.forEach {
                //if((it.type!=Node.Type.SENSOR_TILE_BOX) && (it.type!=Node.Type.SENSOR_TILE_BOX_PRO))
                if(it.type!=mBoard) {
                    return@forEach
                }
                mAdapter.addNode(it)
            }
            runOnUiThread { mAdapter.notifyDataSetChanged() }
            startNodeDiscovery(SEARCH_TIMEOUT)
        }
    }


    override fun onPause() {
        super.onPause()
        Manager.getSharedInstance().removeListener(bleScanListener)
        stopNodeDiscovery()
    }

    private fun checkBoardType() {
        mSelectedNode?.let {
            //if ((it.type != Node.Type.SENSOR_TILE_BOX) && (it.type != Node.Type.SENSOR_TILE_BOX_PRO)){
            if(it.type!=mBoard) {
                DialogHelper.showDialog(this@UploadFlowActivity, getString(R.string.wrong_board_error), null)
                return
            }
        }

        showUploadDialog()
    }

    private fun showUploadDialog() {
        mSelectedNode?.let {
            Manager.getSharedInstance().stopDiscovery()
            val uploadDialog = UploadDialogFragment.getInstance(it, uploadDialogListener)
            uploadDialog.show(supportFragmentManager, null)
        }
    }

    // Listeners

    private val bleScanListener = object : Manager.ManagerListener {
        override fun onDiscoveryChange(m: Manager, enabled: Boolean) {
            runOnUiThread {
                if (!enabled) {
                    binding.boardListSwypeRefresh.post(Runnable {
                        binding.boardListSwypeRefresh.isRefreshing = false
                    })
                    if (mAdapter.isEmpty) {
                        val dialog = AlertDialog.Builder(this@UploadFlowActivity)
                            .setMessage(R.string.board_scan_timeout)
                            .setPositiveButton("Try Again"){ dialog, _ ->
                                startDiscovery()
                                dialog.dismiss()
                            }
                            .setNeutralButton("Cancel"){ dialog, _ ->
                                dialog.dismiss()
                            }
                            .setNegativeButton("Exit"){ _, _ ->
                               finish()
                            }
                            .create()
                        dialog.show()

                        //DialogHelper.showDialog(this@UploadFlowActivity, getString(R.string.board_scan_timeout)) { dialog, _ -> dialog.dismiss() }
                        //DialogHelper.showDialog(this@UploadFlowActivity, getString(R.string.board_scan_timeout)) { _, _ -> finish() }
                    }
                }
            }
        }

        override fun onNodeDiscovered(m: Manager, node: Node) {
            //if((node.type != Node.Type.SENSOR_TILE_BOX) && (node.type != Node.Type.SENSOR_TILE_BOX_PRO)){
            if(node.type!=mBoard) {
                return
            }
            runOnUiThread {
                Log.d("Flow","onNodeDiscovered: ${node.tag}")
                mAdapter.addNode(node)
                mAdapter.notifyDataSetChanged()
            }

        }
    }

    private val uploadDialogListener = object : UploadDialogFragment.UploadListener {
        override fun onSuccess() {
            DialogHelper.showDialog(this@UploadFlowActivity, getString(R.string.upload_uploaded_successfully),"Auto connect","Boards List") { _, which ->
                if (Session.selectedFlows?.hasBtStreamAsOutput() == true) {
                    val manager = LocalBroadcastManager.getInstance(this@UploadFlowActivity)
                    val intent = Intent(TrilobyteActivity.FINISH_ACTIVITY_ACTION)
                    if(which==DialogInterface.BUTTON_POSITIVE) {
                        mSelectedNode?.let {
                            //Save the Node Address for making the auto Connect to its
                            intent.putExtra(
                                DashboardActivity.FINISH_ACTIVITY_NODE_ADDRESS,
                                mSelectedNode!!.tag
                            )
                        }
                    }
                    manager.sendBroadcast(intent)
                    finish()
                }
                if (Session.selectedFlows?.hasSDStreamAsOutput() == true){
                    val message =
                        getString(R.string.sd_recording_info_message)
                    DialogHelper.showDialog(this@UploadFlowActivity,message){ _,_ ->
                        val manager = LocalBroadcastManager.getInstance(this@UploadFlowActivity)
                        val intent = Intent(TrilobyteActivity.FINISH_ACTIVITY_ACTION)
                        if(which==DialogInterface.BUTTON_POSITIVE) {
                            mSelectedNode?.let {
                                //Save the Node Address for making the auto Connect to it
                                intent.putExtra(
                                    DashboardActivity.FINISH_ACTIVITY_NODE_ADDRESS,
                                    mSelectedNode!!.tag
                                )
                            }
                        }
                        manager.sendBroadcast(intent)
                        finish()
                    }
                }else{
                    finish()
                }


            }
        }

        override fun onError(errorCode: Int) {
            val errors = resources.getStringArray(R.array.board_errors)
            DialogHelper.showDialog(this@UploadFlowActivity, errors[errorCode]) { _, _ -> startDiscovery() }
        }
    }
}