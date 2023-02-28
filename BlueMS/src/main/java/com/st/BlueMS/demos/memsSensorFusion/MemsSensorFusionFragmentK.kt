package com.st.BlueMS.demos.memsSensorFusion

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.st.BlueMS.R
import com.st.BlueMS.databinding.FragmentMemsSensorFusionBinding
import com.st.BlueMS.demos.memsSensorFusion.calibration.CalibrationContract
import com.st.BlueMS.demos.memsSensorFusion.calibration.CalibrationPresenter
import com.st.BlueMS.demos.memsSensorFusion.calibration.CalibrationView
import com.st.BlueMS.demos.memsSensorFusion.feature_listener.ProximityData
import com.st.BlueMS.demos.memsSensorFusion.feature_listener.SensorFusionData
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueMS.demos.util.GLCubeRender
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusion
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusionCompact
import com.st.BlueSTSDK.Features.FeatureProximity
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Fragment that show the feature and proximity feature
 * <p>
 * will show a cube that moves according to the quaternion computed by the mems sensor fusion
 * engine inside the board
 * if present the data from the proximity sensor will be used for change the cube size
 * </p>
 */
@DemoDescriptionAnnotation(
    name = "Mems Sensor Fusion",
    iconRes = R.drawable.demo_sensors_fusion,
    demoCategory = ["Inertial Sensors"],
    requireOneOf = [FeatureMemsSensorFusion::class, FeatureMemsSensorFusionCompact::class]
)
class MemsSensorFusionFragmentK : BaseDemoFragment(), CalibrationContract.View {

    private val viewModel: MemsSensorFusionViewModel by viewModels()

    private var binding: FragmentMemsSensorFusionBinding? = null

    private var glRender: GLCubeRender? = null

    private val calibrationPresenter: CalibrationContract.Presenter = CalibrationPresenter()

    private var calibrationView: CalibrationContract.View? = null

    private var vibratorManager: Vibrator? = null

    companion object {
        /**
         * initial size of the cube
         */
        private const val INITIAL_CUBE_SCALE = 0.9f

        private const val PROXIMITY_MAX_DISTANCE = 200
        private const val PROXIMITY_SCALE_FACTOR = 1 / PROXIMITY_MAX_DISTANCE.toFloat()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemsSensorFusionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.calibrationImage?.setOnClickListener { calibrationPresenter.startCalibration() }
        binding?.memsSensorfusionResetButton?.setOnClickListener { onResetPositionButtonClicked() }
        binding?.memsSensorfusionProximityButton?.setOnClickListener { onProximityButtonClicked() }

        glRender = GLCubeRender(activity, getBgColor()).apply {
            setScaleCube(INITIAL_CUBE_SCALE)
        }
        binding?.memsSensorfusionGlSurface?.let {
            // Request an OpenGL ES 2.0 compatible context.
            it.setEGLContextClientVersion(2)
            it.setRenderer(glRender)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {

            launch {
                viewModel.sensorFusionEvents.collect {
                    updateCubeRotation(it)
                }
            }

            launch {
                viewModel.proximityEvents.collect {
                    updateCubeProximity(it)
                }
            }

            launch {
                viewModel.freeFallEvents.collect {
                    updateFreeFall()
                }
            }
        }
    }

    /**
     * get the fragment background color, to be used for fill the opengl background
     *
     * If the background is not a color we return white
     *
     * @return fragment background color
     */
    private fun getBgColor(): Int {
        val a = TypedValue()
        requireActivity().theme.resolveAttribute(android.R.attr.windowBackground, a, true)
        return if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            a.data
        } else {
            // windowBackground is not a color, probably a drawable
            Color.WHITE
        } //if else
    }

    override fun onResume() {
        super.onResume()
        binding?.memsSensorfusionGlSurface?.onResume()
        glRender?.resetCube()
    }

    private fun updateCubeRotation(sampleData: SensorFusionData?) {

        sampleData ?: return

        glRender?.let {
            updateGui {

                //update the cube rotation
                it.setRotation(sampleData.qi, sampleData.qj, sampleData.qk, sampleData.qs)

                binding?.memsSensorfusionQuaternionRateText?.text = getString(
                    R.string.memsSensorFusion_frameRate,
                    it.renderingRate
                )

                binding?.memsSensorfusionRenderingRateText?.text = getString(
                    R.string.memsSensorFusion_quaternionRate,
                    sampleData.avgQuaternionRate
                )
            }
        }
    }

    private fun updateCubeProximity(proximityData: ProximityData?) {

        proximityData ?: return

        val proximityStr = if (proximityData.proximity == FeatureProximity.OUT_OF_RANGE_VALUE) {
            glRender?.setScaleCube(INITIAL_CUBE_SCALE)
            getString(R.string.memsSensorFusion_proximityOutOfRange)
        } else {
            val proximity = min(proximityData.proximity, PROXIMITY_MAX_DISTANCE)
            glRender?.setScaleCube(proximity * PROXIMITY_SCALE_FACTOR)
            getString(R.string.memsSensorFusion_proximityFormat, proximityData.proximity)
        }

        updateGui { binding?.memsSensorfusionProximityText?.text = proximityStr }
    }

    private fun updateFreeFall() {

        if (vibratorManager == null) {
            vibratorManager = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val vibrationTime = context?.resources?.getInteger(R.integer.wesu_motion_fx_free_fall_vibration_duration) ?: 1000
        vibratorManager?.vibrate(vibrationTime.toLong())
        binding?.let {
            updateGui {
                Snackbar.make(it.root, R.string.wesu_motion_fx_freeFallDetected, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun onResetPositionButtonClicked() {
        val node = node ?: return
        buildResetInfoDialog(node.type)?.also {
            it.show()
        } ?: run { resetPosition() }
    }

    private fun resetPosition() {
        glRender?.resetCube()
    }

    private fun buildResetInfoDialog(
        @StringRes messageId: Int,
        @DrawableRes imageId: Int
    ): Dialog {
        val activity: Activity = requireActivity()
        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle(R.string.memsSensorFusionInfoTitle)
        val view = activity.layoutInflater.inflate(R.layout.dialog_reset_sensor_fusion, null)
        val message = view.findViewById<TextView>(R.id.dialog_reset_message)
        val image = view.findViewById<ImageView>(R.id.dialog_reset_image)
        message.setText(messageId)
        image.setImageResource(imageId)
        dialog.setView(view)
        dialog.setPositiveButton(
            android.R.string.ok
        ) { _: DialogInterface?, _: Int -> resetPosition() }
        return dialog.create()
    }

    private fun buildResetInfoDialog(type: Node.Type): Dialog? {
        return when (type) {
            Node.Type.STEVAL_WESU1 -> buildResetInfoDialog(
                R.string.memsSensorFusionDialogResetText_STEVAL_WESU1,
                R.drawable.steval_wesu1_reset_position
            )
            Node.Type.SENSOR_TILE -> buildResetInfoDialog(
                R.string.memsSensorFusionDialogResetText_nucleo,
                R.drawable.ic_board_sensortile_bg
            )
            //This must be changed for .box-Pro
            Node.Type.SENSOR_TILE_BOX, Node.Type.SENSOR_TILE_BOX_PRO -> buildResetInfoDialog(
                R.string.memsSensorFusionDialogResetText_nucleo,
                R.drawable.ic_sensortile_box
            )
            Node.Type.NUCLEO, Node.Type.NUCLEO_L476RG, Node.Type.NUCLEO_F446RE,Node.Type.NUCLEO_L053R8, Node.Type.NUCLEO_F401RE -> buildResetInfoDialog(
                R.string.memsSensorFusionDialogResetText_nucleo,
                R.drawable.ic_board_nucleo_bg
            )
            Node.Type.BLUE_COIN -> buildResetInfoDialog(
                R.string.memsSensorFusionDialogResetText_nucleo,
                R.drawable.ic_board_bluecoin_bg
            )
            Node.Type.STEVAL_BCN002V1 -> buildResetInfoDialog(
                R.string.memsSensorFusionDialogResetText_nucleo,
                R.drawable.ic_board_bluenrgtile
            )
            Node.Type.GENERIC -> null
            else -> null
        }
    }

    private fun onProximityButtonClicked() {
        glRender?.setScaleCube(INITIAL_CUBE_SCALE)
        val node = node ?: return
        if (viewModel.isProximityEnabled()) {
            if (node.isEnableNotification(viewModel.getProximityFeature())) viewModel.disableProximity(node)
               else viewModel.enableProximity(node)
        }
    }

    private fun enableSensorFusionCalibration() {
        viewModel.getSensorFusionFeature()?.let { feature ->
            binding?.calibrationImage?.let { calibrationButton ->
                calibrationView = CalibrationView(childFragmentManager, calibrationButton)
                calibrationPresenter.manage(this, feature)
            }
        }
    }

    private fun disableSensorFusionCalibration() {
        if (viewModel.isSensorFusionEnabled()) {
            calibrationPresenter.unManageFeature()
        }
    }

    override fun enableNeededNotification(node: Node) {

        val isSensorFusionEnabled = viewModel.enableSensorFusion(node)
        if (isSensorFusionEnabled.not()) {
            showActivityToast(R.string.memsSensorFusionNotFound)
        }

        val enableProximityNotifications = binding?.memsSensorfusionProximityButton?.isChecked ?: false
        val isProximityEnabled = viewModel.enableProximity(node, enableProximityNotifications)
        if (isProximityEnabled) {

            /*
             * proximity sensor is present, show the button and attach the listener for
             * enable/disable the sensor reading
             */
            updateGui {
                binding?.memsSensorfusionProximityButton?.visibility = View.VISIBLE
                binding?.memsSensorfusionProximityText?.visibility = View.VISIBLE
            }
        }

        viewModel.enableFreeFall(node)
        enableSensorFusionCalibration()
    }

    override fun disableNeedNotification(node: Node) {

        disableSensorFusionCalibration()

        viewModel.disableFreeFall(node)

        viewModel.disableProximity(node)
        updateGui {
            binding?.memsSensorfusionProximityButton?.isChecked = false
            binding?.memsSensorfusionProximityText?.visibility = View.GONE
        }

        viewModel.disableSensorFusion(node)
    }

    override fun showCalibrationDialog() {
        calibrationView?.let {
            it.showCalibrationDialog()
        }
    }

    override fun hideCalibrationDialog() {
        calibrationView?.let {
            it.hideCalibrationDialog()
        }
    }

    /**
     * when the calibration start, ai_log_stop the free fall and proximity notification
     * @param isCalibrated true if the system is calibrated
     */
    override fun setCalibrationButtonState(isCalibrated: Boolean) {

        val node = node ?: return
        if (isCalibrated) {
            viewModel.enableFreeFall(node)
            viewModel.enableProximity(node)
        } else {
            viewModel.disableFreeFall(node)
            viewModel.disableProximity(node)
        }

        calibrationView?.let {
            it.setCalibrationButtonState(isCalibrated)
        }
    }

    override fun onPause() {
        binding?.memsSensorfusionGlSurface?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}