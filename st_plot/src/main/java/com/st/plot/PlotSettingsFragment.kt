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
package com.st.plot

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.activityViewModels
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@ExperimentalTime
private class DataStorageMapper(private val viewModel: PlotSettingsViewModel) :
    PreferenceDataStore() {

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        if (key == AUTOSCALE_KEY) {
            return viewModel.plotBoundary.value?.enableAutoScale ?: defValue
        }//else
        return super.getBoolean(key, defValue)
    }


    override fun getString(key: String?, defValue: String?): String? {
        return when (key) {
            MINIMUM_Y_KEY -> viewModel.plotBoundary.value?.min?.toFormatString()
            MAXIMUM_Y_KEY -> viewModel.plotBoundary.value?.max?.toFormatString()
            TIME_TO_PLOT_KEY -> viewModel.plotDuration.value?.toDouble(DurationUnit.SECONDS)
                ?.toInt().toString()

            else -> null
        }
    }

    override fun putString(key: String?, value: String?) {
        when (key) {
            MINIMUM_Y_KEY -> viewModel.changeMimimumYValue(value?.toFloatOrNull())
            MAXIMUM_Y_KEY -> viewModel.changeMaximumYValue(value?.toFloatOrNull())
            TIME_TO_PLOT_KEY -> viewModel.changePlotDuration(value?.toInt()
                ?.let { it.seconds })
        }
    }

    override fun putBoolean(key: String?, value: Boolean) {
        if (key == AUTOSCALE_KEY && value) {
            viewModel.enableAutoScale()
        }//else
    }

    override fun putInt(key: String?, value: Int) {
        if (key == TIME_TO_PLOT_KEY) {
            viewModel.changePlotDuration(value.minutes)
        }
    }

    override fun getInt(key: String?, defValue: Int): Int {
        if (key == TIME_TO_PLOT_KEY) {
            return viewModel.plotDuration.value?.toDouble(DurationUnit.SECONDS)?.toInt() ?: defValue
        }
        return super.getInt(key, defValue)
    }

    companion object {
        const val AUTOSCALE_KEY = "plotFeatureSettings_autoscale"
        const val MINIMUM_Y_KEY = "plotFeatureSettings_minumumY"
        const val MAXIMUM_Y_KEY = "plotFeatureSettings_maximumY"
        const val TIME_TO_PLOT_KEY = "plotFeatureSettings_timeStampRange"
    }

}

private fun Float.toFormatString(): String {
    return String.format("%.2f", this)
}

@ExperimentalTime
class PlotSettingsFragment : PreferenceFragmentCompat() {

    private val settingsViewModel by activityViewModels<PlotSettingsViewModel>()


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_plot_feature, rootKey)

        preferenceManager.preferenceDataStore = DataStorageMapper(settingsViewModel)
        val minPreference = findPreference<EditTextPreference>(DataStorageMapper.MINIMUM_Y_KEY)
        val maxPreference = findPreference<EditTextPreference>(DataStorageMapper.MAXIMUM_Y_KEY)
        val autoscaleIsEnabled = settingsViewModel.plotBoundary.value?.enableAutoScale ?: true
        findPreference<CheckBoxPreference>(DataStorageMapper.AUTOSCALE_KEY)?.let { autoScalePreference ->
            autoScalePreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, value ->
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
            if (it.value == null) {
                it.setValueIndex(selectedIndex())
            }
        }

    }

    private fun selectedIndex(): Int {
        val value =
            settingsViewModel.plotDuration.value?.toDouble(DurationUnit.SECONDS)?.toInt().toString()
        val values = resources.getStringArray(R.array.plotFeatureSettings_secondsToPlotValue)
        return values.indexOf(value)
    }

    private fun setupTextEditPreference(preference: EditTextPreference) {
        preference.setOnBindEditTextListener { editText ->
            editText.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        preference.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
    }

}