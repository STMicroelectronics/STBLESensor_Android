/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.BlueMS.demos.multiNN

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Features.FeatureActivity
import com.st.BlueSTSDK.Features.FeatureAudioClassification
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import java.text.SimpleDateFormat
import java.util.*

@DemoDescriptionAnnotation(name = "Multi Neural Network",
        iconRes = R.drawable.neural_network_demo_icon,
        demoCategory = ["AI","Inertial Sensors","Audio"],
        requireAll = [FeatureActivity::class, FeatureAudioClassification::class])
class MultiNeuralNetworkFragment : BaseDemoFragment() {

    companion object{
        private val DATE_FORMAT = SimpleDateFormat("HH:mm:ss",Locale.getDefault())
    }

    private lateinit var mHumanActivityView:View
    private lateinit var mHumanActivityImage:ImageView
    private lateinit var mHumanActivityDesc:TextView

    private lateinit var mAudioSceneView:View
    private lateinit var mAudioSceneImage:ImageView
    private lateinit var mAudioSceneStatusText:TextView
    private lateinit var mAudioSceneDesc:TextView

    private lateinit var mComboView:View
    private lateinit var mComboImage:ImageView
    private lateinit var mComboDesc:TextView

    private lateinit var mAlgoSelectorGroup:View
    private lateinit var mAlgoSelector:Spinner

    private var mHumanActivityViewModel:ActivityRecognitionViewModel? = null
    private var mAudioSceneViewModel:AudioSceneViewModel? = null
    private var mMultiNNViewModel:MultiNNViewModel? = null
    private var mComboViewModel:ComboViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_multi_neural_network, container, false)

        mAlgoSelector = view.findViewById(R.id.multiNN_algoSelectSpinner)

        mHumanActivityImage = view.findViewById(R.id.multiNN_humanActivityImage)
        mHumanActivityView = view.findViewById(R.id.multiNN_humanActivityCardView)
        mHumanActivityDesc = view.findViewById(R.id.multiNN_humanActivityDesc)

        mAudioSceneImage = view.findViewById(R.id.multiNN_audioSceneImage)
        mAudioSceneView = view.findViewById(R.id.multiNN_audioSceneCardView)
        mAudioSceneDesc = view.findViewById(R.id.multiNN_audioSceneImageDesc)
        mAudioSceneStatusText = view.findViewById(R.id.multiNN_audioSceneStatusText)

        mComboView = view.findViewById(R.id.multiNN_comboCardView)
        mComboImage = view.findViewById(R.id.multiNN_comboImage)
        mComboDesc = view.findViewById(R.id.multiNN_comboDesc)

        mAlgoSelectorGroup = view.findViewById(R.id.multiNN_algoSelectGroup)
        mAlgoSelector = view.findViewById(R.id.multiNN_algoSelectSpinner)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mHumanActivityViewModel = ViewModelProvider(this).get(ActivityRecognitionViewModel::class.java)
        mAudioSceneViewModel = ViewModelProvider(this).get(AudioSceneViewModel::class.java)
        mComboViewModel = ViewModelProvider(this).get(ComboViewModel::class.java)
        node?.let { node ->
            val factory = MultiNNViewModelFactory(node)
            mMultiNNViewModel = ViewModelProvider(this,factory).get(MultiNNViewModel::class.java)
        }

        mMultiNNViewModel?.let { attachMultiNNViewModel(it) }
        mAudioSceneViewModel?.let { attachAudioSceneViewModel(it) }
        mHumanActivityViewModel?.let { attachHumanActivityViewModel(it) }
        mComboViewModel?.let { attachComboViewModel(it) }

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun attachAudioSceneViewModel(vm: AudioSceneViewModel) {

        vm.viewIsVisible.observe(viewLifecycleOwner, Observer { viewIsVisible ->
            mAudioSceneView.visibility =
                    if(viewIsVisible == true){
                        View.VISIBLE
                    }else{
                        View.GONE
                    }
        })

        vm.currentImageId.observe(viewLifecycleOwner, Observer { newImage ->
            if(newImage == null)
                return@Observer
            mAudioSceneImage.setImageResource(newImage)

        })

        vm.currentDescriptionStrId.observe(viewLifecycleOwner, Observer { newDescription ->
            val deteString = DATE_FORMAT.format(Date())
            val sceneString = getString(newDescription)
            mAudioSceneDesc.text=String.format("%s: %s",deteString,sceneString)
        })

        vm.ascStatusStringId.observe(viewLifecycleOwner, Observer { newStringId ->
            mAudioSceneStatusText.setText(newStringId)
        })
    }

    private fun attachHumanActivityViewModel(vm: ActivityRecognitionViewModel) {

        vm.viewIsVisible.observe(viewLifecycleOwner, Observer { viewIsVisible ->
            mHumanActivityView.visibility =
                    if(viewIsVisible == true){
                        View.VISIBLE
                    }else{
                        View.GONE
                    }
        })

        vm.currentState.observe(viewLifecycleOwner, Observer { newImage ->
            if(newImage == null)
                return@Observer
            mHumanActivityImage.setImageResource(newImage)

        })

        vm.currentDescriptionStrId.observe(viewLifecycleOwner, Observer { newDescription ->
            val deteString = DATE_FORMAT.format(Date())
            val activityString = getString(newDescription)
            mHumanActivityDesc.text=String.format("%s: %s",deteString,activityString)
        })
    }

    private fun attachComboViewModel(vm: ComboViewModel) {

        vm.viewIsVisible.observe(viewLifecycleOwner, Observer { viewIsVisible ->
            mComboView.visibility =
                    if(viewIsVisible == true){
                        View.VISIBLE
                    }else{
                        View.GONE
                    }
        })

        vm.currentState.observe(viewLifecycleOwner, Observer { newImage ->
            if(newImage == null)
                return@Observer
            mComboImage.setImageResource(newImage)

        })

        vm.currentDescriptionStrId.observe(viewLifecycleOwner, Observer { newDescription ->
            val deteString = DATE_FORMAT.format(Date())
            val activityString = getString(newDescription)
            mComboDesc.text=String.format("%s: %s",deteString,activityString)
        })

    }

    private fun attachMultiNNViewModel(vm:MultiNNViewModel){
        vm.availableAlgorithm.observe(viewLifecycleOwner, Observer { algorithms ->
            if(algorithms == null) {
                return@Observer
            }//else
            val names = algorithms.map { it.name }.toTypedArray()
            mAlgoSelector.setSelection(0,false)
            mAlgoSelector.adapter = android.widget.ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_item,names).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            mAlgoSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) { }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selected = algorithms[position]
                    vm.selectAlgorithm(selected)
                }

            }
        })

        vm.currentAlgorithm.observe(getViewLifecycleOwner(), Observer { running ->
            if(running == null)
                return@Observer
            val selectedIndex = vm.availableAlgorithm.value?.indexOf(running)
            if(selectedIndex != null){
                mAlgoSelector.setSelection(selectedIndex)
            }
        })

        vm.showAlgorithmList.observe(getViewLifecycleOwner(), Observer { showList ->
            if(showList == null){
                return@Observer
            }
            mAlgoSelectorGroup.visibility = if (showList) View.VISIBLE else View.GONE
        })


    }

    private fun registerListener(){
        node?.let{
            mHumanActivityViewModel?.registerListener(it)
            mAudioSceneViewModel?.registerListener(it)
            mComboViewModel?.registerListener(it)
        }
    }

    private fun removeListener(){
        node?.let{
            mHumanActivityViewModel?.removeListener(it)
            mAudioSceneViewModel?.removeListener(it)
            mComboViewModel?.removeListener(it)
        }
    }

    override fun enableNeededNotification(node: Node) {
        registerListener()
        mMultiNNViewModel?.startMultiNeuralNetwork()
    }

    override fun disableNeedNotification(node: Node) {
        mMultiNNViewModel?.stopMultiNeuralNetwork()
        removeListener()
    }

}
