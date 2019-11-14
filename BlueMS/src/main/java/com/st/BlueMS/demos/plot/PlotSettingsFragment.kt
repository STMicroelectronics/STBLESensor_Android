package com.st.BlueMS.demos.plot

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.activityViewModels
import androidx.preference.*
import com.st.BlueMS.R
import kotlin.time.ExperimentalTime
import kotlin.time.minutes
import kotlin.time.seconds

@ExperimentalTime
private class DataStorageMapper(private val viewModel: PlotSettingsViewModel) : PreferenceDataStore(){

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        if(key == AUTOSCALE_KEY){
            return viewModel.plotBoundary.value?.enableAutoScale ?: defValue
        }//else
        return super.getBoolean(key, defValue)
    }


    override fun getString(key: String?, defValue: String?): String? {
        return when(key){
            MINIMUM_Y_KEY -> viewModel.plotBoundary.value?.min?.toFormatString()
            MAXIMUM_Y_KEY -> viewModel.plotBoundary.value?.max?.toFormatString()
            TIME_TO_PLOT_KEY -> viewModel.plotDuration.value?.inSeconds?.toInt().toString()
            else -> null
        }
    }

    override fun putString(key: String?, value: String?) {
        when(key){
            MINIMUM_Y_KEY -> viewModel.changeMimimumYValue(value?.toFloatOrNull())
            MAXIMUM_Y_KEY -> viewModel.changeMaximumYValue(value?.toFloatOrNull())
            TIME_TO_PLOT_KEY-> viewModel.changePlotDuration(value?.toInt()?.seconds)
        }
    }

    override fun putBoolean(key: String?, value: Boolean) {
        if(key == AUTOSCALE_KEY && value){
            viewModel.enableAutoScale()
        }//else
    }

    override fun putInt(key: String?, value: Int) {
        if(key == TIME_TO_PLOT_KEY){
            viewModel.changePlotDuration(value.minutes)
        }
    }

    override fun getInt(key: String?, defValue: Int): Int {
        if(key == TIME_TO_PLOT_KEY){
            return viewModel.plotDuration.value?.inSeconds?.toInt() ?: defValue
        }
        return super.getInt(key, defValue)
    }

    companion object{
        const val AUTOSCALE_KEY = "plotFeatureSettings_autoscale"
        const val MINIMUM_Y_KEY = "plotFeatureSettings_minumumY"
        const val MAXIMUM_Y_KEY = "plotFeatureSettings_maximumY"
        const val TIME_TO_PLOT_KEY = "plotFeatureSettings_timeStampRange"
    }

}

private fun Float.toFormatString():String{
    return String.format("%.2f",this)
}

@ExperimentalTime
internal class PlotPreferenceFragment : PreferenceFragmentCompat(){

    private val settingsViewModel by activityViewModels<PlotSettingsViewModel>()


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_plot_feature, rootKey)

        preferenceManager.preferenceDataStore = DataStorageMapper(settingsViewModel)
        val minPreference = findPreference<EditTextPreference>(DataStorageMapper.MINIMUM_Y_KEY)
        val maxPreference = findPreference<EditTextPreference>(DataStorageMapper.MAXIMUM_Y_KEY)
        val autoscaleIsEnabled = settingsViewModel.plotBoundary.value?.enableAutoScale ?: true
        findPreference<CheckBoxPreference>(DataStorageMapper.AUTOSCALE_KEY)?.let { autoScalePreference ->
            autoScalePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener{
                preference, value ->
                val hasAutoscale = value as Boolean

                minPreference?.isEnabled = !hasAutoscale
                maxPreference?.isEnabled = !hasAutoscale

                return@OnPreferenceChangeListener true
            }
            autoScalePreference.isChecked = autoscaleIsEnabled
        }


        minPreference?.let {
            setupTextEditPreference(it)
            it.isEnabled = !autoscaleIsEnabled
            it.text = settingsViewModel.plotBoundary.value?.min?.toFormatString()
        }
        maxPreference?.let {
            setupTextEditPreference(it)
            it.isEnabled = !autoscaleIsEnabled
            it.text = settingsViewModel.plotBoundary.value?.max?.toFormatString()
        }

        findPreference<ListPreference>(DataStorageMapper.TIME_TO_PLOT_KEY)?.let {
            it.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            if(it.value == null){
                it.setValueIndex(selectedIndex())
            }
        }

    }

    private fun selectedIndex() : Int {
        val value = settingsViewModel.plotDuration.value?.inSeconds?.toInt().toString()
        val values = resources.getStringArray(R.array.plotFeatureSettings_secondsToPlotValue)
        return values.indexOf(value)
    }

    private fun setupTextEditPreference(preference: EditTextPreference){
        preference.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        preference.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
    }

}