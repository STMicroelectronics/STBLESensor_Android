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
package com.st.BlueMS.demos.plot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import kotlin.reflect.KClass
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds


internal class PlotSettingsViewModelFactory(private val node:Node) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlotSettingsViewModel(node) as T
    }
}

private const val SECONDS_TO_PLOT_DEFAULT =  5

internal class PlotSettingsViewModel(node: Node)  : ViewModel(){

    private var _plotDuration = MutableLiveData(SECONDS_TO_PLOT_DEFAULT.seconds)
    val plotDuration: LiveData<Duration>
        get() = _plotDuration

    private var _legendItems = MutableLiveData<Array<String>>()
    val legendItems: LiveData<Array<String>>
        get() = _legendItems

    private var _yAxisLabel = MutableLiveData<String?>()
    val yAxisLabel: LiveData<String?>
        get() = _yAxisLabel

    val supportedFeature:LiveData<List<String>>

    private var _selectedFeature = MutableLiveData<Feature>()
    val selectedFeature:LiveData<Feature>
        get() = _selectedFeature

    private var _plotBoundaries = MutableLiveData<PlotBoundary>()
    val plotBoundary:MutableLiveData<PlotBoundary>
        get() = _plotBoundaries

    private var _selectedFeatureIndex = MutableLiveData<Int>()
    val selectedFeatureIndex:LiveData<Int>
        get() = _selectedFeatureIndex

    fun startPlotSelectedFeature(){
        val f = _selectedFeature.value ?: return
        val fields = f.fieldsDesc.filterIndexed { _, it -> it.plotIt}

        val items = fields.map { it.name }.toTypedArray()
        _legendItems.postValue(items)
        val unit = fields.firstOrNull()?.unit
        if(unit == null){
            _yAxisLabel.postValue(f.name)
        }else{
            _yAxisLabel.postValue("${f.name} (${unit})")
        }
    }

    private val plottableFeature:List<Feature>

    init {
        plottableFeature = filterPlottableFeature(node.features)
        supportedFeature = MutableLiveData(plottableFeature.map { it.name })
    }


    fun onSelectedIndex(index:Int){
        if(_selectedFeature.value != plottableFeature[index]) {
            val feature = plottableFeature[index]
            _selectedFeature.postValue(feature)
            _selectedFeatureIndex.postValue(index)
            _plotBoundaries.postValue(PlotBoundary.getDefaultFor(feature))
        }

    }

    fun changeMimimumYValue(minimum:Float?){
        val currentValue = _plotBoundaries.value ?: PlotBoundary()
        val newValue = currentValue.copy(min = minimum)
        _plotBoundaries.postValue(newValue)
    }

    fun changeMaximumYValue(maximum:Float?){
        val currentValue = _plotBoundaries.value ?: PlotBoundary()
        val newValue = currentValue.copy(max = maximum)
        _plotBoundaries.postValue(newValue)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun getSupportedFeatures(): List<KClass<out Feature>> {
        val temp = PlotFeatureFragment::class.java.getAnnotation(DemoDescriptionAnnotation::class.java)?.requireOneOf!!
        return listOf(*temp)
    }

    private fun filterPlottableFeature(all: List<Feature>): List<Feature> {
        val supportedFeatures = getSupportedFeatures()
        return all.filter { supportedFeatures.contains(it::class) }
    }

    fun enableAutoScale() {
        _plotBoundaries.postValue(PlotBoundary())
    }

    fun changePlotDuration(newDuration:Duration?){
        _plotDuration.postValue(newDuration ?: SECONDS_TO_PLOT_DEFAULT.seconds)
    }

}