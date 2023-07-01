/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.gnss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.st.blue_sdk.features.extended.gnss.GNSSInfo
import com.st.core.ARG_NODE_ID
import com.st.gnss.databinding.GnssFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.absoluteValue

@AndroidEntryPoint
class GnssFragment : Fragment() {

    private val viewModel: GnssViewModel by viewModels()
    private lateinit var binding: GnssFragmentBinding
    private lateinit var nodeId: String


    private lateinit var mLatitudeValue: TextView
    private lateinit var mLatitudeDirection: TextView
    private lateinit var mLongitudeValue: TextView
    private lateinit var mLongitudeDirection: TextView
    private lateinit var mAltitudeValue: TextView
    private lateinit var mNumSatellitesValue: TextView
    private lateinit var mSigQualityValue: TextView
    private lateinit var mLocateOnMapButton: FloatingActionButton

    private lateinit var mWebView: WebView

    private var mCurrentPosition: LocationData? = null
    private var mDrawenPosition: LocationData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        binding = GnssFragmentBinding.inflate(inflater, container, false)


        mLatitudeValue = binding.gnssLatitudeValue
        mLatitudeDirection = binding.gnssLatitudeDirection

        mLongitudeValue = binding.gnssLongitudeValue
        mLongitudeDirection = binding.gnssLongitudeDirection

        mAltitudeValue = binding.gnssAltitudeValue

        mNumSatellitesValue = binding.gnssNumSatellitesValue

        mSigQualityValue = binding.gnssSigQualityValue

        mLocateOnMapButton = binding.gnssLocateOnMap

        mLocateOnMapButton.setOnClickListener { setPositionOnMaps() }

        mWebView = binding.gnssWebview
        mWebView.webChromeClient = WebChromeClient()
        mWebView.settings.javaScriptEnabled = true

        return binding.root
    }

    private class SetLocation(data: LocationData) {

        private val jsonLocationStr: String = Json.encodeToString(data)

        @JavascriptInterface
        fun setLocation(): String {
            return jsonLocationStr
        }
    }

    //    private var fakeDataPosition =0
    private fun setPositionOnMaps(savedPosition: LocationData? = null) {

        //Fake data for Testing the Maps
//        val fakeData: Array<LocationData> = arrayOf(LocationData(45.57431f,9.34793f),
//                                                          LocationData(41.89193f, 12.51133f),
//                                                          LocationData(37.49223f, 15.07041f),
//                                                          LocationData(46.49067f, 11.33982f))
//        mWebView.visibility = View.VISIBLE
//        mWebView.loadUrl("file:///android_asset/gnss_leaflat.html")
//        mWebView.addJavascriptInterface(SetLocation(fakeData[fakeDataPosition]), "Android")
//        fakeDataPosition =  (fakeDataPosition +1 ).and(0x3)


        if (savedPosition != null) {
            mWebView.visibility = View.VISIBLE
            mWebView.loadUrl("file:///android_asset/gnss_leaflat.html")
            mWebView.addJavascriptInterface(SetLocation(savedPosition), "Android")
            mDrawenPosition = savedPosition
        } else {
            if (mCurrentPosition != null) {
                mWebView.visibility = View.VISIBLE
                mWebView.loadUrl("file:///android_asset/gnss_leaflat.html")
                mWebView.addJavascriptInterface(SetLocation(mCurrentPosition!!), "Android")
                mDrawenPosition = mCurrentPosition
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gnssData.collect {
                    updateGui(it)
                }
            }
        }
    }

    private fun updateGui(it: GNSSInfo) {
        val latitude = it.latitude.value
        val longitude = it.longitude.value
        val altitude = it.altitude.value
        val numSatellites = it.numSatellites.value
        val sigQuality = it.signalQuality.value

        mCurrentPosition = LocationData(latitude, longitude)
        mLocateOnMapButton.isEnabled = true

        mLatitudeValue.text = latitude.absoluteValue.toString()
        if (latitude >= 0) {
            mLatitudeDirection.text = "N"
        } else {
            mLatitudeDirection.text = "S"
        }

        mLongitudeValue.text = longitude.absoluteValue.toString()
        if (longitude >= 0) {
            mLongitudeDirection.text = "E"
        } else {
            mLongitudeDirection.text = "W"
        }

        mAltitudeValue.text = altitude.toString()

        mNumSatellitesValue.text = numSatellites.toString()

        mSigQualityValue.text = sigQuality.toString()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startDemo(nodeId = nodeId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopDemo(nodeId = nodeId)
    }
}
