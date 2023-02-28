package com.st.BlueMS.demos


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueMS.R
import com.st.BlueMS.demos.PnPL.PnPLComponentAdapter
import com.st.BlueMS.demos.PnPL.PnPLComponentViewData
import com.st.BlueMS.demos.PnPL.PnPLConfigViewModel
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureBinaryContent
import com.st.BlueSTSDK.Features.PnPL.FeaturePnPL
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import java.io.IOError


@DemoDescriptionAnnotation(name = "Binary Content",
    iconRes = R.drawable.sd_log_start,
    demoCategory = ["BinaryContent"],
    requireAll = [FeatureBinaryContent::class, FeaturePnPL::class])
class BinaryContentDemoFragment : BaseDemoFragment() {

    private lateinit var mSaveButton: Button
    private lateinit var mLoadButton: Button

    private val WRITE_PERMISSION_REQ_CODE = 7776
    private val PICKFILE_REQUEST_TYPE = "*/*"//"application/json"
    private val CREATE_FILE_REQUEST_CODE = 7775
    private val READ_FILE_REQUEST_CODE = 7774

    private lateinit var viewModel: PnPLConfigViewModel
    private lateinit var viewModelSupported : SupportViewModel

    private var byteArrayContent: ByteArray?=null

    private val adapterPnPLComponents = PnPLComponentAdapter(
        object : PnPLComponentAdapter.ComponentInteractionCallback {
            override fun onComponentCollapsed(selected: PnPLComponentViewData) {
                viewModel.collapseComponent(selected)
            }

            override fun onComponentExpanded(selected: PnPLComponentViewData) {
                viewModel.expandComponent(selected)
            }
        },
        contChangedListener = { component, content, value ->
            Log.i("PnPLConfFragment", "component: $component, content: $content,value: $value")
            viewModel.sendPnPLSetProperty(component.comp_name, content.cont_name, value)
        },
        subContChangedListener = { component, content, subContent, value ->
            Log.i("PnPLConfFragment", "component: $component, content: $content,value: $value")
            viewModel.sendPnPLSetProperty(
                component.comp_name,
                content.cont_name,
                subContent.cont_name,
                value
            )
        },
        commandSentListener = { component, content, command_list ->
            Log.i(
                "PnPLConfFragment",
                "component: $component, content: $content,value: $command_list"
            )
            val fieldMap = emptyMap<String, Any>().toMutableMap()
            if (content.sub_cont_list != null) {
                for (c in content.sub_cont_list!!) {
                    if (c.cont_enum_pos != null) {
                        fieldMap[c.cont_name] = c.cont_enum_pos!!
                    } else {
                        fieldMap[c.cont_name] = c.cont_info!!
                    }
                }
                if (content.request_name != null) {
                    viewModel.sendPnPLCommandCmd(
                        component.comp_name,
                        content.cont_name,
                        content.request_name!!,
                        fieldMap
                    )
                } else {
                    viewModel.sendPnPLCommandCmd(component.comp_name, content.cont_name, fieldMap)
                }
            } else {
                viewModel.sendPnPLCommandCmd(component.comp_name, content.cont_name)
            }
        },
        loadfileListener = { component, content ->
            Log.i("PnPLConfFragment", "component: $component, content: $content")
            viewModel.setComponentToLoadFile(
                component.comp_name,
                content.cont_name,
                content.request_name!!
            )
            //requestOpenFile()
            TODO("Not Yet Supported")
        })

    private lateinit var recyclerViewPnPLComponents: RecyclerView

    private var mBinaryContentFeature: FeatureBinaryContent? = null

    private val featureListenerBinaryContent = Feature.FeatureListener { _: Feature, sample: Feature.Sample? ->
//        if (sample != null) {
//            Log.i("BinaryContent","Data Received size=${sample.data.size}")
//        } else {
//            Log.i("BinaryContent","Data Received null")
//        }
        if(sample!=null) {
            byteArrayContent = FeatureBinaryContent.getBinaryContent(sample)
            activity?.runOnUiThread {
                mSaveButton.isEnabled = byteArrayContent != null
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* Inflate the layout */
        val rootView = inflater.inflate(R.layout.frament_binary_content, container, false)

        mSaveButton = rootView.findViewById(R.id.binary_content_save)
        mSaveButton.setOnClickListener{
            saveBinaryContentToFile()
        }
        mLoadButton = rootView.findViewById(R.id.binary_content_load)
        mLoadButton.setOnClickListener{
            loadBinaryContentFromFile()
            //Toast.makeText(context, "Not Yet Implemented!!!!", Toast.LENGTH_SHORT).show()
        }

        //Get the ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(PnPLConfigViewModel::class.java)
        viewModel.context = context;

        viewModelSupported = ViewModelProvider(requireActivity()).get(SupportViewModel::class.java)

        //Set the Recycler View
        recyclerViewPnPLComponents = rootView.findViewById(R.id.binary_content_demo_recycler)
        recyclerViewPnPLComponents.adapter = adapterPnPLComponents
        return rootView
    }

    private fun saveBinaryContentToFile() {

       if(checkStoragePermission()) {
           requestFileCreation("Binary.bin")
       }
    }

    private fun loadBinaryContentFromFile() {
        if(checkStoragePermission()) {
            requestFileOpen("Binary.bin")
        }
    }

    private fun checkStoragePermission() : Boolean {
        //Check the permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_PERMISSION_REQ_CODE
                )
                return false
            }
        }
        return true
    }

    private fun requestFileCreation(fileName: String){
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = PICKFILE_REQUEST_TYPE
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    private fun requestFileOpen(fileName: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = PICKFILE_REQUEST_TYPE
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        startActivityForResult(intent, READ_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CREATE_FILE_REQUEST_CODE -> {
                val fileUri: Uri?= data?.data
                if (resultCode == Activity.RESULT_OK) {
                    if ((byteArrayContent != null) && (fileUri != null)) {
                        val contentResolver = requireContext().contentResolver
                        val scope = CoroutineScope(Job() + Dispatchers.IO)
                        scope.launch {
                            try {
                                val stream =
                                    contentResolver.openOutputStream(fileUri) ?: return@launch
                                Log.i("BinaryContent","need to write ${byteArrayContent!!.size} Bytes")
                                withContext(Dispatchers.IO) {
                                    stream.write(byteArrayContent!!)
                                }
                                stream.close()
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            } catch (e: IOError) {
                                e.printStackTrace()
                            }
                        }

                    }
                }
            }
            READ_FILE_REQUEST_CODE -> {
                val fileUri: Uri?= data?.data
                if (resultCode == Activity.RESULT_OK) {
                    if (fileUri != null) {
                        val contentResolver = requireContext().contentResolver
                        val scope = CoroutineScope(Job() + Dispatchers.IO)
                        scope.launch {
                            try {
                                val stream =
                                    contentResolver.openInputStream(fileUri) ?: return@launch
                                withContext(Dispatchers.IO) {
                                    byteArrayContent = stream.readBytes()
                                }
                                stream.close()
                                Log.i("BinaryContent","Read from file ${byteArrayContent!!.size} Bytes")
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            } catch (e: IOError) {
                                e.printStackTrace()
                            }
                            if(byteArrayContent!=null) {
                                mBinaryContentFeature?.sendBinaryContent(byteArrayContent!!,
                                    viewModelSupported.get_MaxPayLoadSize())
                            }
                        }

                    }
                }
            }
        }
    }

    override fun enableNeededNotification(node: Node) {

        val server = node.nodeServer
        if(server!=null) {
            viewModelSupported.set_MaxPayLoadSize(server.maxPayloadSize)
        }
        mBinaryContentFeature = node.getFeature(FeatureBinaryContent::class.java)?.apply {
            addFeatureListener(featureListenerBinaryContent)
            enableNotification()
            node.nodeServer?.let {
                mBinaryContentFeature?.setMaxPayLoadSize(it.maxPayloadSize)
            }
        }

        viewModel.compList.observe(viewLifecycleOwner, Observer {
            adapterPnPLComponents.updatePnPLCompList(it)
        })

        if(node.dtdlModel !=null) {
            viewModel.parseDeviceModelControl(node.dtdlModel, listOf("control"), false)
        }

        if(node.dtdlModel !=null) {
            viewModel.enableNotificationFromNode(node)
            viewModel.sendPnPLGetDeviceStatus()
        }
    }

    override fun disableNeedNotification(node: Node) {
        node.getFeature(FeatureBinaryContent::class.java)?.apply {
            removeFeatureListener(featureListenerBinaryContent)
            disableNotification()
        }
        viewModel.disableNotificationFromNode(node)
    }
}