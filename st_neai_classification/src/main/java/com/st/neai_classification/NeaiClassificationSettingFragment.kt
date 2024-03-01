package com.st.neai_classification

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.st.core.ARG_NODE_ID

const val NEAI_CLASSIFICATION_SETTINGS = "Class Name"

class NeaiClassificationSettingFragment : PreferenceFragmentCompat() {

    private lateinit var nodeId: String

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        setPreferencesFromResource(R.xml.pref_neai_classification, rootKey)
        
        //val defaultName = findPreference<CheckBoxPreference>("neai_classification_default_names")
        val nameClass1 = findPreference<EditTextPreference>("neai_classification_custom_class_1")
        val nameClass2 = findPreference<EditTextPreference>("neai_classification_custom_class_2")
        val nameClass3 = findPreference<EditTextPreference>("neai_classification_custom_class_3")
        val nameClass4 = findPreference<EditTextPreference>("neai_classification_custom_class_4")
        val nameClass5 = findPreference<EditTextPreference>("neai_classification_custom_class_5")
        val nameClass6 = findPreference<EditTextPreference>("neai_classification_custom_class_6")
        val nameClass7 = findPreference<EditTextPreference>("neai_classification_custom_class_7")
        val nameClass8 = findPreference<EditTextPreference>("neai_classification_custom_class_8")

        nameClass1?.let {
            setEditTextPreference(it)
        }
        nameClass2?.let {
            setEditTextPreference(it)
        }
        nameClass3?.let {
            setEditTextPreference(it)
        }
        nameClass4?.let {
            setEditTextPreference(it)
        }
        nameClass5?.let {
            setEditTextPreference(it)
        }
        nameClass6?.let {
            setEditTextPreference(it)
        }
        nameClass7?.let {
            setEditTextPreference(it)
        }
        nameClass8?.let {
            setEditTextPreference(it)
        }
    }

    private fun setEditTextPreference( editText: EditTextPreference) {
        editText.summary = editText.text
        editText.setOnPreferenceChangeListener { preference, newValue ->
            preference.summary = newValue.toString()
            editText.text = newValue.toString()
            true
        }
    }
}