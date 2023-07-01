/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.compass

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalibrationDialogFragment : DialogFragment() {

    private val viewModel: CalibrationViewModel by viewModels()
    private val navArgs: CompassFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.st_compass_calibration_title)
            setView(layoutInflater.inflate(R.layout.compass_calibration_dialog_fragment, null))
            setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int -> }
        }

        viewModel.startCalibration(nodeId = navArgs.nodeId)

        viewModel.calibrationStatus.observe(this) { calibrationStatus ->
            if (calibrationStatus) {
                dismiss()
            }
        }
        return dialog.create()
    }
}
