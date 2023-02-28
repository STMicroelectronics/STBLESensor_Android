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

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Features.*
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import kotlin.time.ExperimentalTime

@ExperimentalTime
@DemoDescriptionAnnotation(name = "Plot Data",
        iconRes = R.drawable.demo_charts,
        demoCategory = ["Graphs"],
        requireOneOf = [
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
            FeatureQVAR::class,
            FeatureToFMultiObject::class,
            FeatureEventCounter::class])
class PlotFeatureFragment : BaseDemoFragment(){

    private lateinit var viewModel: PlotDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_plot_feature2, container, false)

        // Get the ViewModel
        viewModel = ViewModelProvider(this).get(PlotDataViewModel::class.java)
        viewModel.node = node!!

        val fm = childFragmentManager
        if (fm.findFragmentByTag(PLOT_TAG) == null) {
            fm.beginTransaction()
                    //.add(R.id.plotFeature_contentView, PlotFragment(node!!), PLOT_TAG)
                    .add(R.id.plotFeature_contentView, PlotFragment(), PLOT_TAG)
                    .commit()
        }
        return root
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