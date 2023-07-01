/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.BoardDescription
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.utils.SHR_MASK
import com.st.blue_sdk.utils.getFeature
import com.st.blue_sdk.utils.getGPFeature
import com.st.blue_sdk.utils.isExtendedOrExternalFeatureCharacteristics
import com.st.blue_sdk.utils.isGeneralPurposeFeatureCharacteristics
import com.st.blue_sdk.utils.isStandardFeatureCharacteristics
import com.st.demo_showcase.models.Demo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
) : ViewModel() {

    private val _boards = MutableStateFlow(emptyList<BoardFirmware>())
    val boards = _boards.asStateFlow()

    private val _firmwareList = MutableStateFlow(emptyList<BoardFirmware>())
    val firmwareList = _firmwareList.asStateFlow()

    private val _board: MutableStateFlow<BoardFirmware?> = MutableStateFlow(null)
    val board: StateFlow<BoardFirmware?> = _board.asStateFlow()

    private val _boardsDescription = MutableStateFlow(emptyList<BoardDescription>())
    val boardsDescription = _boardsDescription.asStateFlow()

    private val _boardDescription: MutableStateFlow<BoardDescription?> = MutableStateFlow(null)
    val boardDescription = _boardDescription.asStateFlow()

    fun getBoard(boardId: String) {
        viewModelScope.launch {
            _board.value = blueManager.getBoardCatalog().findLast { it.bleDevId == boardId }
            _boardDescription.value =
                blueManager.getBoardsDescription().findLast { it.bleDevId == boardId }
        }
    }

    fun getFirmwareList(boardId: String) {
        viewModelScope.launch {
            _firmwareList.value = blueManager.getBoardCatalog().filter { it.bleDevId == boardId }
        }
    }


    init {
        viewModelScope.launch {
            _boards.value = blueManager.getBoardCatalog().filter {
                if (StCatalogConfig.boardModelFilter.isNotEmpty())
                    StCatalogConfig.boardModelFilter.contains(it.boardModel())
                else
                    true
            }
            _boardsDescription.value = blueManager.getBoardsDescription()
        }
    }
}

fun UUID.buildFeatures(
    boardModel: Boards.Model
): List<Feature<*>> {
    val featureMask = (mostSignificantBits shr SHR_MASK).toInt()
    val features = mutableListOf<Feature<*>>()

    var mask = 1L shl 31
    for (i in 0..31) {
        if ((featureMask and mask.toInt()) != 0) {
            features.add(
                Feature.createFeature(
                    boardModel = boardModel,
                    type = Feature.Type.STANDARD,
                    identifier = mask.toInt(),
                    isEnabled = true
                )
            )
        }
        mask = mask shr 1
    }

    return features
}

fun BoardFirmware.availableDemos(): List<Demo> {
    val features = mutableListOf<Feature<*>>()

    characteristics.map { UUID.fromString(it.uuid) }.forEach { characteristic ->
        if (characteristic.isExtendedOrExternalFeatureCharacteristics()) {
            features.add(characteristic.getFeature())
        }

        if (characteristic.isGeneralPurposeFeatureCharacteristics()) {
            features.add(characteristic.getGPFeature())
        }

        if (characteristic.isStandardFeatureCharacteristics()) {
            features.addAll(characteristic.buildFeatures(boardModel = boardModel()))
        }
    }

    return Demo.values().toList().filter { demo ->
        when (demo) {
            Demo.Flow -> boardModel() == Boards.Model.SENSOR_TILE_BOX || boardModel() == Boards.Model.SENSOR_TILE_BOX_PRO || boardModel() == Boards.Model.SENSOR_TILE_BOX_PROB
            Demo.BlueVoiceFullDuplex -> false
            Demo.BlueVoiceFullBand -> false
            else -> {

                if (demo.featuresNotAllowed == null) {
                    if (demo.requireAllFeatures) {
                        features.map { it.name }.containsAll(demo.features)
                    } else {
                        features.map { it.name }.any {
                            demo.features.contains(it)
                        }
                    }
                } else {

                    if (features.map { it.name }.containsAll(demo.featuresNotAllowed!!)) {
                        false
                    } else {
                        if (demo.requireAllFeatures) {
                            features.map { it.name }.containsAll(demo.features)
                        } else {
                            features.map { it.name }.any {
                                demo.features.contains(it)
                            }
                        }
                    }
                }
            }
        }
    }
}
