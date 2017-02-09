/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
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

package com.st.BlueMS.demos;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.util.HidableTextView;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureBattery;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

@DemoDescriptionAnnotation(name="Rssi & Battery",iconRes=R.drawable.demo_battery)
public class NodeStatusFragment extends DemoFragment implements Node.BleConnectionParamUpdateListener {
    private static long RSSI_UPDATE_PERIOD_MS=500;

    private Handler mUpdateRssiRequestQueue;
    private TextView mRssiText;
    private Runnable mAskNewRssi = new Runnable() {
        @Override
        public void run() {
            Node n = getNode();
            if(n!=null){
                n.readRssi();
                mUpdateRssiRequestQueue.postDelayed(mAskNewRssi,RSSI_UPDATE_PERIOD_MS);
            }//if
        }//run
    };

    private Feature mBatteryFeature;
    private TextView mBatteryStatusText;
    private TextView mBatteryPercentageText;
    private TextView mBatteryVoltageText;
    private ImageView mBatteryIcon;
    private TypedArray mBatteryChargingImagesArray;
    private TypedArray mBatteryChargeImagesArray;
    private HidableTextView mBatteryCurrentText;

    private Feature.FeatureListener mBatteryListener = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f,Feature.Sample data) {
            final Field[] fieldsDesc = f.getFieldsDesc();
            final Resources res = NodeStatusFragment.this.getResources();
            float percentage = FeatureBattery.getBatteryLevel(data);
            FeatureBattery.BatteryStatus status = FeatureBattery.getBatteryStatus(data);
            float voltage = FeatureBattery.getVoltage(data);
            float current = FeatureBattery.getCurrent(data);
            int iconIndex;
            if(current >0)
                iconIndex = (((int)percentage)*mBatteryChargingImagesArray.length())/100;
            else
                iconIndex = (((int)percentage)*mBatteryChargeImagesArray.length())/100;

            final Drawable icon = current>0 ? mBatteryChargingImagesArray.getDrawable(iconIndex) :
                    mBatteryChargeImagesArray.getDrawable(iconIndex);
            final String batteryStatus = "Status: "+status;

            final String batteryPercentage = res.getString(R.string.nodeStatus_battery_percentage,
                    percentage,fieldsDesc[FeatureBattery.PERCENTAGE_INDEX].getUnit());
            final String batteryVoltage = res.getString(R.string.nodeStatus_battery_voltage,
                    voltage, fieldsDesc[FeatureBattery.VOLTAGE_INDEX].getUnit());
            final String batteryCurrent = res.getString(R.string.nodeStatus_battery_current,
                    current, fieldsDesc[FeatureBattery.CURRENT_INDEX].getUnit());

            updateGui(new Runnable() {
                @Override
                public void run() {
                    try {
                        mBatteryStatusText.setText(batteryStatus);
                        mBatteryPercentageText.setText(batteryPercentage);
                        mBatteryIcon.setImageDrawable(icon);
                        mBatteryVoltageText.setText(batteryVoltage);
                        mBatteryCurrentText.setText(batteryCurrent);
                    }catch (NullPointerException e){
                        //this exception can happen when the task is run after the fragment is
                        // destroyed
                    }
                }
            });
        }//onUpdate
    };

    public NodeStatusFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        HandlerThread looper = new HandlerThread(NodeStatusFragment.class.getName()+".UpdateRssi");
        looper.start();
        mUpdateRssiRequestQueue = new Handler(looper.getLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Resources res = getResources();
        View root = inflater.inflate(R.layout.fragment_node_status, container, false);

        mRssiText = (TextView) root.findViewById(R.id.rssiText);

        mBatteryPercentageText = (TextView) root.findViewById(R.id.batteryPercentageText);
        mBatteryStatusText = (TextView) root.findViewById(R.id.batteryStatus);
        mBatteryVoltageText = (TextView) root.findViewById(R.id.batteryVoltageText);
        mBatteryCurrentText = (HidableTextView) root.findViewById(R.id.batteryCurrentText);
        mBatteryIcon = (ImageView) root.findViewById(R.id.batteryImage);
        mBatteryChargingImagesArray = res.obtainTypedArray(R.array.batteryChargingIcon);
        mBatteryChargeImagesArray = res.obtainTypedArray(R.array.batteryChargeIcon);

        return root;
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mBatteryFeature = node.getFeature(FeatureBattery.class);
        node.addBleConnectionParamListener(this);
        mUpdateRssiRequestQueue.postDelayed(mAskNewRssi, RSSI_UPDATE_PERIOD_MS);
        if(mBatteryFeature!=null){
            mBatteryFeature.addFeatureListener(mBatteryListener);
            node.enableNotification(mBatteryFeature);
        }else {
        //    showActivityToast(R.string.batteryNotFound);
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        node.removeBleConnectionParamListener(this);
        mUpdateRssiRequestQueue.removeCallbacks(mAskNewRssi);
        if(mBatteryFeature!=null){
            mBatteryFeature.removeFeatureListener(mBatteryListener);
            node.disableNotification(mBatteryFeature);
        }
    }

    @Override
    public void onRSSIChanged(Node node, final int newRSSIValue) {
        updateGui(new Runnable() {
            @Override
            public void run() {
                mRssiText.setText("Rssi: "+newRSSIValue+" [dbm]");
            }//run
        });
    }//onRSSIChanged

    @Override
    public void onTxPowerChange(Node node, int newPower) {}
}
