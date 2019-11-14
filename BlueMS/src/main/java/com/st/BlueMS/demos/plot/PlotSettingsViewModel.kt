package com.st.BlueMS.demos.plot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
internal class PlotSettingsViewModelFactory(private val node:Node) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PlotSettingsViewModel(node) as T
    }

}
@ExperimentalTime
private val SECONDS_TO_PLOT_DEFAULT = 5.seconds

@ExperimentalTime
internal class PlotSettingsViewModel(node: Node)  : ViewModel(){

    private var _plotDuration = MutableLiveData<Duration>(SECONDS_TO_PLOT_DEFAULT)
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
        val fields = f.fieldsDesc
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

    private fun getSupportedFeatures(): List<KClass<out Feature>> {
        val temp = PlotFeatureFragment::class.java.getAnnotation(DemoDescriptionAnnotation::class.java)?.requareOneOf!!
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
        _plotDuration.postValue(newDuration ?: SECONDS_TO_PLOT_DEFAULT)
    }

}