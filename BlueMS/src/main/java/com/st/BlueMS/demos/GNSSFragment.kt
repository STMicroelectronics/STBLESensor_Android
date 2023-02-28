package com.st.BlueMS.demos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.st.BlueMS.R
import com.st.BlueMS.demos.util.BaseDemoFragment
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureGNSS
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation
import kotlin.math.absoluteValue


@DemoDescriptionAnnotation(name = "GNSS",
    iconRes = R.drawable.ic_gnss_satellite_compact,
    demoCategory = ["Environmental Sensors"],
    requireAll = [FeatureGNSS::class])
class GNSSFragment : BaseDemoFragment() {

    private var mLatitudeValue: TextView? =null
    private var mLatitudeDirection: TextView? =null
    private var mLongitudeValue: TextView? =null
    private var mLongitudeDirection: TextView? =null
    private var mAltitudeValue: TextView? =null
    private var mNumSatellitesValue: TextView? =null
    private var mSigQualityValue: TextView? =null
    private var mLocateOnMapButton: FloatingActionButton?=null

    private var mFeature : FeatureGNSS? =null
    private lateinit var mWebView: WebView

    private var mCurrentPosition: LocationData?=null
    private var mDrawenPosition: LocationData?=null

    private var mViewModel: SupportViewModel? = null

    private val featureListener = Feature.FeatureListener { _, sample ->
        val latitude = FeatureGNSS.getLatitudeValue(sample)
        val longitude = FeatureGNSS.getLongitudeValue(sample)
        val altitude = FeatureGNSS.getAltitudeValue(sample)
        val numSatellites = FeatureGNSS.getNSatValue(sample)
        val sigQuality = FeatureGNSS.getSigQualityValue(sample)

        if((latitude!=null) && (longitude!=null)) {
            mCurrentPosition = LocationData(latitude,longitude)
            if(mLocateOnMapButton!=null) {
                // Enable the Button
                updateGui {
                    mLocateOnMapButton!!.isEnabled = true
                }
            }
        }

        updateGui {
            if(latitude!=null) {
                mLatitudeValue?.text = latitude.absoluteValue.toString()
                if(latitude>=0) {
                    mLatitudeDirection?.text = "N"
                } else {
                    mLatitudeDirection?.text = "S"
                }
            }

            if(longitude!=null) {
                mLongitudeValue?.text = longitude.absoluteValue.toString()
                if(longitude>=0) {
                    mLongitudeDirection?.text = "E"
                } else {
                    mLongitudeDirection?.text = "W"
                }
            }

            if(altitude!=null) {
                mAltitudeValue?.text = altitude.toString()
            }

            if(numSatellites!=null) {
                mNumSatellitesValue?.text = numSatellites.toString()
            }

            if(sigQuality!=null) {
                mSigQualityValue?.text = sigQuality.toString()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(SupportViewModel::class.java)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(mViewModel!= null) {
            mViewModel!!.set_PositionOnMap(mWebView.isVisible)
            if(mDrawenPosition!=null) {
                mViewModel!!.set_LocationData(mDrawenPosition!!)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(mViewModel!= null) {
            if(mViewModel!!.get_PositionOnMap()) {
                mDrawenPosition = mViewModel!!.get_LocationData()
                setPositionOnMaps(mDrawenPosition)
                mLocateOnMapButton!!.isEnabled=true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        val rootView = inflater.inflate(R.layout.fragment_demo_gnss, container, false)

        mLatitudeValue = rootView.findViewById(R.id.gnss_latitude_value)
        mLatitudeDirection = rootView.findViewById(R.id.gnss_latitude_direction)

        mLongitudeValue = rootView.findViewById(R.id.gnss_longitude_value)
        mLongitudeDirection = rootView.findViewById(R.id.gnss_longitude_direction)

        mAltitudeValue = rootView.findViewById(R.id.gnss_altitude_value)

        mNumSatellitesValue = rootView.findViewById(R.id.gnss_num_satellites_value)

        mSigQualityValue = rootView.findViewById(R.id.gnss_sig_quality_value)

        mLocateOnMapButton = rootView.findViewById(R.id.gnss_locate_on_map)

        mLocateOnMapButton?.setOnClickListener { setPositionOnMaps() }

        mWebView = rootView.findViewById(R.id.gnss_webview)
        mWebView.webChromeClient = WebChromeClient()
        mWebView.settings.javaScriptEnabled = true

        return rootView
    }


    private class SetLocation(data: LocationData) {

        private val jsonLocationStr:String = Gson().toJson(data)

        @JavascriptInterface
        fun setLocation(): String {
            return jsonLocationStr
        }
    }


//    private var fakeDataPosition =0
    private fun setPositionOnMaps(savedPosition: LocationData?=null) {

        //Fake data for Testing the Maps
//        val fakeData: Array<LocationData> = arrayOf(LocationData(45.57431f,9.34793f),
//                                                          LocationData(41.89193f, 12.51133f),
//                                                          LocationData(37.49223f, 15.07041f),
//                                                          LocationData(46.49067f, 11.33982f))
//        mWebView.visibility = View.VISIBLE
//        mWebView.loadUrl("file:///android_asset/gnss_leaflat.html")
//        mWebView.addJavascriptInterface(SetLocation(fakeData[fakeDataPosition]), "Android")
//        fakeDataPosition =  (fakeDataPosition +1 ).and(0x3)



        if(savedPosition!=null) {
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

    override fun enableNeededNotification(node: Node) {
        mFeature = node.getFeature(FeatureGNSS::class.java)
        mFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
        }
    }

    override fun disableNeedNotification(node: Node) {
        node.getFeature(FeatureGNSS::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }
        mFeature = null
    }
}