package com.st.BlueMS.demos.plot

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProviders
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Features.*
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import kotlin.time.ExperimentalTime

@ExperimentalTime
@DemoDescriptionAnnotation(name = "Plot Data",
        iconRes = R.drawable.demo_charts,
        requareOneOf = [
            FeatureAcceleration::class,
            FeatureCompass::class,
            FeatureDirectionOfArrival::class,
            FeatureGyroscope::class,
            FeatureHumidity::class,
            FeatureLuminosity::class,
            FeatureMagnetometer::class,
            FeatureMemsSensorFusionCompact::class,
            FeatureMemsSensorFusion::class,
            FeatureMicLevel::class,
            FeatureMotionIntensity::class,
            FeatureProximity::class,
            FeaturePressure::class,
            FeatureTemperature::class,
            FeatureCOSensor::class,
            FeatureEulerAngle::class,
            FeatureMemsNorm::class,
            FeatureEventCounter::class])
class PlotFeatureFragment : BaseDemoFragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_plot_feature2, container, false)
        val fm = childFragmentManager
        if (fm.findFragmentByTag(PLOT_TAG) == null) {
            fm.beginTransaction()
                    .add(R.id.plotFeature_contentView, PlotFragment(), PLOT_TAG)
                    .commit()
        }
        return root
    }

    @ExperimentalTime
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ViewModelProviders.of(requireActivity(),PlotSettingsViewModelFactory(node!!))
                .get(PlotSettingsViewModel::class.java)
    }

    override fun enableNeededNotification(node: Node) {
/*        node.getFeature(FeatureAcceleration::class.java)?.let {
            dataViewModel.startPlotSelectedFeature(it)
        }
*/
    }

    override fun disableNeedNotification(node: Node) {
        val currentFragment = childFragmentManager.findFragmentByTag(PLOT_TAG)
        if(currentFragment is PlotFragment)
            currentFragment.stopPlotting()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_plot_feature_demo, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.menu_plot_length) {
            showPlotsettings()

            return true
        }//else
        return super.onOptionsItemSelected(item)
    }

    private fun showPlotsettings() {
        val settings = PlotPreferenceFragment()
        childFragmentManager.beginTransaction()
                .replace(R.id.plotFeature_contentView,settings)
                .addToBackStack(null)
                .commit()
    }

    companion object{
        private const val PLOT_TAG = "PlotTAg"
    }

}