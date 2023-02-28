package com.st.BlueMS.demos.HighSpeedDatalog2.tagging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueMS.R
import com.st.BlueMS.demos.HighSpeedDataLog.tagging.AnnotationViewData
import com.st.BlueMS.demos.HighSpeedDataLog.tagging.HSDAnnotationListAdapter
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node

/**
 *
 */
class HSD2TaggingFragment : Fragment() {

    private lateinit var viewModel : HSD2TaggingViewModel

    private lateinit var mAnnotationListView: RecyclerView
    private lateinit var mAcquisitionName:EditText
    private lateinit var mAcquisitionDesc:EditText
    private lateinit var mStartStopButton:Button

    private lateinit var mCheckAllTags: CheckBox
    private var mCheckAll = false
    private var mMaxTagListSize = 0
    private var isLogging = false


    private val mTagClassesAdapter: HSDAnnotationListAdapter = HSDAnnotationListAdapter(object :
        HSDAnnotationListAdapter.AnnotationInteractionCallback {
        override fun requestLabelEditing(annotation: AnnotationViewData) {
            buildNewLabelDialog(annotation)
        }

        override fun onAnnotationSelected(selected: AnnotationViewData) {
            viewModel.selectAnnotation(selected)
        }

        override fun onAnnotationDeselected(deselected: AnnotationViewData) {
            viewModel.deSelectAnnotation(deselected)
            mCheckAllTags.isChecked=false
        }
    })

    private fun buildNewLabelDialog(annotation: AnnotationViewData) {
        val context = requireContext()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_change_label,null,false)
        val textView = view.findViewById<EditText>(R.id.tagLog_changeLabel_value)
        textView.setText(annotation.label)
        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.tagLog_changeLabel_title)
                .setView(view)
                .setPositiveButton(R.string.tagLog_changeLabel_ok){ dialog, _ ->
                    viewModel.changeTagLabel(annotation,textView.text.toString())
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.tagLog_changeLabel_cancel){ dialog,_ ->
                    dialog.dismiss()
                }
                .create()
        dialog.show()
    }

    private val mSwapToDelete = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(recyclerView: RecyclerView,
                                    viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return true // true if moved, false otherwise
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.absoluteAdapterPosition
                    val selectedAnnotation = mTagClassesAdapter.currentList[position]
                    viewModel.removeAnnotation(selectedAnnotation)
                }
            })

    override fun onResume() {
        super.onResume()
        val node = arguments?.getString(NODE_TAG_EXTRA)?.let {
            Manager.getSharedInstance().getNodeWithTag(it)
        }
        if(node!=null){
            viewModel.enableNotification(node)
            viewModel.sendPnPLGetComponentStatus("log_controller")
            viewModel.sendPnPLGetComponentStatus("tags_info")
        }

    }

    override fun onPause() {
        super.onPause()
        val node = arguments?.getString(NODE_TAG_EXTRA)?.let {
            Manager.getSharedInstance().getNodeWithTag(it)
        }
        if(node!=null){
            viewModel.disableNotification(node)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_log_annotation, container, false)
        mAnnotationListView = view.findViewById(R.id.tagLog_annotation_list)
        mSwapToDelete.attachToRecyclerView(mAnnotationListView)
        mAnnotationListView.adapter = mTagClassesAdapter

        mAcquisitionName = view.findViewById(R.id.tagLog_acquisitionName_value)
        mAcquisitionDesc = view.findViewById(R.id.tagLog_acquisitionDescription_value)

        viewModel = ViewModelProvider(requireActivity()).get(HSD2TaggingViewModel::class.java)

        mStartStopButton = view.findViewById(R.id.tagLog_annotation_startButton)
        mStartStopButton.setOnClickListener {
            viewModel.onStartStopLogPressed(mAcquisitionName.text.toString(),mAcquisitionDesc.text.toString())
        }

        //Check button for checking all the tags
        mCheckAllTags = view.findViewById(R.id.tagLog_annotation_check_all)
        mCheckAllTags.setOnClickListener {
            checkUncheckAll()
        }

        //Set the default value
        mCheckAllTags.isChecked=mCheckAll

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.annotations.observe(viewLifecycleOwner, Observer {
            //mTagClassesAdapter.notifyDataSetChanged()
            mTagClassesAdapter.submitList(it)
        })

        viewModel.isSDCardInserted.observe(viewLifecycleOwner, Observer { isSDCardInserted ->
            mStartStopButton.isEnabled = isSDCardInserted != false
            if(viewModel.isLogging.value == true){
                mStartStopButton.setText(R.string.tagLog_stopLog)
            }else{
                if(mStartStopButton.isEnabled)
                    mStartStopButton.setText(R.string.tagLog_startLog)
                else
                    mStartStopButton.setText(R.string.tagLog_sdcardmissing)
            }
        })

        viewModel.isLogging.observe(viewLifecycleOwner, Observer { isLogging ->
            enableEditing(!isLogging)
            setupStartStopButtonView(isLogging)
            this.isLogging = isLogging
        })

        viewModel.areAllSelected.observe(viewLifecycleOwner, Observer { areAllSelected ->
            if (areAllSelected && viewModel.isLogging.value == false) {
                mCheckAllTags.isChecked = true
                checkUncheckAll()
                adaptSelectAllFlag()
            } else {
                mCheckAllTags.isChecked = false
            }
        })
    }

    private fun checkUncheckAll() {
        mCheckAll=mCheckAllTags.isChecked

        //Set all the Items inside the Recycler View
        mTagClassesAdapter.currentList.forEach {
            if(mCheckAll) {
                viewModel.selectAnnotation(it)
            } else {
                viewModel.deSelectAnnotation(it)
            }
        }
    }

    private fun adaptSelectAllFlag(){
        if (mTagClassesAdapter.itemCount > mMaxTagListSize) {
            mMaxTagListSize = mTagClassesAdapter.itemCount
        }
        if (mTagClassesAdapter.itemCount < mMaxTagListSize ){
            mCheckAllTags.isChecked=false
        }
    }

    private fun setupStartStopButtonView(logging: Boolean) {
        //todo add the icon
        if(logging){
            mStartStopButton.setText(R.string.tagLog_stopLog)
        }else{
            if(mStartStopButton.isEnabled)
                mStartStopButton.setText(R.string.tagLog_startLog)
            else
                mStartStopButton.setText(R.string.tagLog_sdcardmissing)
        }
    }

    private fun enableEditing(enable: Boolean) {
        mAcquisitionName.isEnabled = enable
        mAcquisitionDesc.isEnabled = enable
    }

    fun isLogging():Boolean{
        return isLogging
    }

    companion object {
        val NODE_TAG_EXTRA = HSD2TaggingFragment::class.java.name + ".NodeTag"

        fun newInstance(node: Node): Fragment {
            return HSD2TaggingFragment().apply {
                arguments = Bundle().apply {
                    putString(NODE_TAG_EXTRA,node.tag)
                }
            }
        }
    }
}