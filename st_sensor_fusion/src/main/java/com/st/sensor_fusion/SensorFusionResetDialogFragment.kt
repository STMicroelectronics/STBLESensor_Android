/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.sensor_fusion

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.navArgs
import com.st.blue_sdk.models.Boards
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SensorFusionResetDialogFragment : DialogFragment() {

    //private val viewModel: SensorFusionViewModel by activityViewModels()
    private val viewModel: SensorFusionViewModel by hiltNavGraphViewModels(R.id.sensor_fusion_nav_graph)
    private val navArgs: SensorFusionFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.sensor_fusion_reset_dialog_fragment, null)
        val image = view.findViewById<ImageView>(R.id.dialog_reset_image)

        image.setImageResource(findBoardImage(viewModel.getNode(nodeId = navArgs.nodeId)))

        val dialog = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.st_sensor_fusion_reset_title)
            setView(view)
            setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                viewModel.resetCubePosition()
                dialog?.dismiss()
            }
            setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                dialog?.dismiss()
            }
        }

        return dialog.create()
    }

    private fun findBoardImage(model: Boards.Model): Int {
        return when (model) {
            Boards.Model.SENSOR_TILE -> R.drawable.ic_board_sensortile_bg
            Boards.Model.BLUE_COIN -> R.drawable.ic_board_bluecoin_bg
            Boards.Model.STEVAL_BCN002V1 -> R.drawable.ic_board_bluenrgtile
            Boards.Model.SENSOR_TILE_BOX -> R.drawable.ic_sensortile_box
            Boards.Model.SENSOR_TILE_BOX_PRO -> R.drawable.box_pro_case_top
            Boards.Model.SENSOR_TILE_BOX_PROB -> R.drawable.box_pro_case_top
            Boards.Model.NUCLEO -> R.drawable.ic_board_nucleo_bg
            Boards.Model.NUCLEO_F401RE -> R.drawable.ic_board_nucleo_bg
            Boards.Model.NUCLEO_L476RG -> R.drawable.ic_board_nucleo_bg
            Boards.Model.NUCLEO_L053R8 -> R.drawable.ic_board_nucleo_bg
            Boards.Model.NUCLEO_F446RE -> R.drawable.ic_board_nucleo_bg
            else -> R.drawable.baseline_device_unknown_24
        }
    }
}
