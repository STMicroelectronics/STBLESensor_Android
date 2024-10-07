/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.neai_classification

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetectionInfo
import com.st.blue_sdk.features.extended.neai_class_classification.NeaiClassClassification
import com.st.blue_sdk.features.extended.neai_class_classification.NeaiClassClassificationInfo
import com.st.blue_sdk.features.extended.neai_class_classification.request.WriteStartClassificationCommand
import com.st.blue_sdk.features.extended.neai_class_classification.request.WriteStopClassificationCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeaiClassificationViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) :
    ViewModel() {

    private val features = mutableListOf<Feature<*>>()

    private val _classificationData =
        MutableStateFlow<NeaiClassClassificationInfo?>(
            null
        )
    val classificationData: StateFlow<NeaiClassClassificationInfo?>
        get() = _classificationData.asStateFlow()


    fun writeStopClassificationCommand(nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == NeaiClassClassification.NAME
        }?.let {
            val feature = it as NeaiClassClassification

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = WriteStopClassificationCommand(feature = feature)
                )
            }
        }
    }


    var customNames = ArrayList<String>()
    var useDefaultNames: Boolean=true

    fun writeStartClassificationCommand(nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == NeaiClassClassification.NAME
        }?.let {
            val feature = it as NeaiClassClassification

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = WriteStartClassificationCommand(feature = feature)
                )
            }
        }
    }

    fun readCustomNames(context: Context) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        customNames.clear()
        customNames.add(pref.getString("neai_classification_custom_class_1","CL 1") ?: "CL 1")
        customNames.add(pref.getString("neai_classification_custom_class_2","CL 2") ?: "CL 2")
        customNames.add(pref.getString("neai_classification_custom_class_3","CL 3") ?: "CL 3")
        customNames.add(pref.getString("neai_classification_custom_class_4","CL 4") ?: "CL 4")
        customNames.add(pref.getString("neai_classification_custom_class_5","CL 5") ?: "CL 5")
        customNames.add(pref.getString("neai_classification_custom_class_6","CL 6") ?: "CL 6")
        customNames.add(pref.getString("neai_classification_custom_class_7","CL 7") ?: "CL 7")
        customNames.add(pref.getString("neai_classification_custom_class_8","CL 8") ?: "CL 8")

        useDefaultNames = pref.getBoolean("neai_classification_default_names",true)

        if(useDefaultNames) {
            customNames.forEachIndexed { index, _ ->
                customNames[index] = "CL ${index+1}"
            }
        }
    }


    fun startDemo(nodeId: String) {

        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).firstOrNull { it.name == NeaiClassClassification.NAME }
                ?.also {
                    features.add(it)
                }
        }

        viewModelScope.launch {
            blueManager.getFeatureUpdates(nodeId, features).collect {
                val data = it.data
                if (data is NeaiClassClassificationInfo) {
                    _classificationData.emit(data)
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        coroutineScope.launch {
            blueManager.disableFeatures(nodeId, features)
        }
    }
}
