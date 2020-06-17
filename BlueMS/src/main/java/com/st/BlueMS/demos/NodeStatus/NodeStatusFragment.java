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

package com.st.BlueMS.demos.NodeStatus;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.math.MathUtils;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureBattery;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.NodeGui;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import org.jetbrains.annotations.NotNull;

/**
 * Display the battery status and the Rssi value
 */
@DemoDescriptionAnnotation(name="Rssi & Battery",iconRes=R.drawable.demo_battery)
public class NodeStatusFragment extends BaseDemoFragment implements Node.BleConnectionParamUpdateListener {

    private static final String BATTERY_CAPACITY = NodeStatusFragment.class.getName()+".BATTERY_CAPACITY";

    private static long RSSI_UPDATE_PERIOD_MS=1000;

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

    private FeatureBattery mBatteryFeature;
    private TextView mBatteryStatusText;
    private TextView mBatteryPercentageText;
    private TextView mBatteryVoltageText;
    private ImageView mBatteryIcon;
    private static int[] BATTERY_CHARGING_IMAGES = new int[]{
            R.drawable.battery_00c,
            R.drawable.battery_20c,
            R.drawable.battery_40c,
            R.drawable.battery_60c,
            R.drawable.battery_80c,
            R.drawable.battery_100c
    };

    private static int[] BATTERY_DISCHARGE_IMAGES = new int[]{
            R.drawable.battery_00,
            R.drawable.battery_20,
            R.drawable.battery_40,
            R.drawable.battery_60,
            R.drawable.battery_80,
            R.drawable.battery_100
    };
    private TextView mBatteryCurrentText;

    private float mBatteryCapacity;

    private TextView mRemainingTime;

    private TextView mNodeName;
    private TextView mNodeAddress;
    private ImageView mNodeIcon;

    /**
     * compute the remaing time in seconds
     * @param batteryCapacity battery capacity in mA/h
     * @param current current used by the system in mA
     * @return remaining time in seconds
     */
    private static float getRemainingTimeMinutes(float batteryCapacity, float current){
        if(current<0)
            return (batteryCapacity/(-current))*(60);
        return Float.NaN;
    }

    private FeatureBattery.FeatureBatteryListener mBatteryListener =
            new FeatureBattery.FeatureBatteryListener() {
                @Override
                public void onCapacityRead(FeatureBattery featureBattery, int batteryCapacity) {
                    mBatteryCapacity=batteryCapacity;
                }

                @Override
                public void onMaxAssorbedCurrentRead(FeatureBattery featureBattery, float current) {
                }

                private int getIconIndex(float percentage, int nIcons){
                    int iconIndex = (((int) percentage) * nIcons) / 100;
                    return MathUtils.clamp(iconIndex,0,nIcons-1);
                }

                private @DrawableRes int getBatteryIcon(float percentage, FeatureBattery.BatteryStatus status){
                    int index;
                    switch (status){
                        case LowBattery:
                        case Discharging:
                        case PluggedNotCharging:
                            index = getIconIndex(percentage, BATTERY_DISCHARGE_IMAGES.length);
                            return BATTERY_DISCHARGE_IMAGES[index];
                        case Charging:
                            index = getIconIndex(percentage, BATTERY_CHARGING_IMAGES.length);
                            return BATTERY_CHARGING_IMAGES[index];
                        case Unknown:
                        case Error:
                            return R.drawable.battery_missing;
                    }
                    return R.drawable.battery_missing;
                }

                @Override
                public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample data) {
                    final Field[] fieldsDesc = f.getFieldsDesc();
                    final Resources res = NodeStatusFragment.this.getResources();
                    final float percentage = FeatureBattery.getBatteryLevel(data);
                    final FeatureBattery.BatteryStatus status = FeatureBattery.getBatteryStatus(data);
                    float voltage = FeatureBattery.getVoltage(data);
                    float current = FeatureBattery.getCurrent(data);

                    final @DrawableRes int batteryIcon = getBatteryIcon(percentage, status);
                    final Drawable icon = ContextCompat.getDrawable(requireContext(),batteryIcon);

                    final String batteryStatus = "Status: "+status;

                    final String batteryPercentage = res.getString(R.string.nodeStatus_battery_percentage,
                            percentage,fieldsDesc[FeatureBattery.PERCENTAGE_INDEX].getUnit());
                    final String batteryVoltage = res.getString(R.string.nodeStatus_battery_voltage,
                            voltage, fieldsDesc[FeatureBattery.VOLTAGE_INDEX].getUnit());
                    final String batteryCurrent;
                    if(Float.isNaN(current))
                        batteryCurrent = res.getString(R.string.nodeStatus_battery_current_unknown);
                    else
                        batteryCurrent = res.getString(R.string.nodeStatus_battery_current,
                            current, fieldsDesc[FeatureBattery.CURRENT_INDEX].getUnit());

                    float remainingBattery = mBatteryCapacity * (percentage/100.0f);
                    float remainingTime = getRemainingTimeMinutes(remainingBattery,current);
                    final String remainingTimeStr = Float.isNaN(remainingTime) ? "" :
                            res.getString(R.string.nodeStatus_battery_remainingTime,
                            remainingTime);

                    updateGui(() -> {
                        try {

                            mBatteryStatusText.setText(batteryStatus);
                            mBatteryPercentageText.setText(batteryPercentage);

                            mBatteryIcon.setImageDrawable(icon);
                            mBatteryVoltageText.setText(batteryVoltage);
                            mBatteryCurrentText.setText(batteryCurrent);
                            if(displayRemainingTime(status)) {
                                mRemainingTime.setText(remainingTimeStr);
                            }else{
                                mRemainingTime.setText("");
                            }
                        }catch (NullPointerException e){
                            //this exception can happen when the task is run after the fragment is
                            // destroyed
                        }
                    });
                }//onUpdate
    };

    private boolean displayRemainingTime(FeatureBattery.BatteryStatus status) {
        return status!= FeatureBattery.BatteryStatus.Charging;
    }

    public NodeStatusFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        HandlerThread looper = new HandlerThread(NodeStatusFragment.class.getName()+".UpdateRssi");
        looper.start();
        mUpdateRssiRequestQueue = new Handler(looper.getLooper());

        loadBatteryCapacity(savedInstanceState);
    }

    private void loadStdConsumedCurrent() {
        mBatteryFeature.readMaxAbsorbedCurrent();

    }

    private void loadBatteryCapacity(@Nullable Bundle savedInstanceState) {
        if(savedInstanceState==null) {
            mBatteryCapacity = Float.NaN;
        }else{
            mBatteryCapacity = savedInstanceState.getFloat(BATTERY_CAPACITY,Float.NaN);
        }
    }

    private void loadBatteryCapacity() {
        if(!Float.isNaN(mBatteryCapacity))
            return;
        mBatteryFeature.readBatteryCapacity();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_node_status, container, false);

        mRssiText = root.findViewById(R.id.status_rssiText);

        mBatteryPercentageText = root.findViewById(R.id.status_batteryPercentageText);
        mBatteryStatusText = root.findViewById(R.id.status_batteryStatusText);
        mBatteryVoltageText = root.findViewById(R.id.status_batteryVoltageText);
        mBatteryCurrentText = root.findViewById(R.id.status_batteryCurrentText);
        mBatteryIcon = root.findViewById(R.id.status_batteryImage);
        mRemainingTime  = root.findViewById(R.id.status_batteryRemainingTimeText);

        mNodeAddress = root.findViewById(R.id.status_boardAddress);
        mNodeName = root.findViewById(R.id.status_boardName);
        mNodeIcon = root.findViewById(R.id.status_boardTypeIcon);

        //ask to add our option to the menu
        setHasOptionsMenu(true);

        return root;
    }

    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_node_status_demo, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.batteryInfo_showInfo) {
            showBatteryInfoDialog();
            return true;
        }//else
        return super.onOptionsItemSelected(item);
    }

    private void showBatteryInfoDialog(){
        Node node = getNode();
        if(node!=null) {
            final BatteryInfoDialogFragment dialog = BatteryInfoDialogFragment.newInstance(node);
            dialog.show(getChildFragmentManager(), "batteryInfoDialog");
        }
    }


    private void setUpNodeInfo(@NonNull Node node){
        mNodeName.setText(node.getName());
        mNodeAddress.setText(node.getTag());
        mNodeIcon.setImageResource(NodeGui.getBoardTypeImage(node.getType()));
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        setUpNodeInfo(node);
        mBatteryFeature = node.getFeature(FeatureBattery.class);
        node.addBleConnectionParamListener(this);
        mUpdateRssiRequestQueue.postDelayed(mAskNewRssi, RSSI_UPDATE_PERIOD_MS);
        if(mBatteryFeature!=null){
            mBatteryFeature.addFeatureListener(mBatteryListener);
            node.enableNotification(mBatteryFeature);
            loadBatteryCapacity();
            loadStdConsumedCurrent();
        }else
            mBatteryVoltageText.setText(R.string.nodeStatus_battery_notFound);
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
    public void onRSSIChanged(@NotNull Node node, final int newRSSIValue) {
        updateGui(() -> mRssiText.setText(getString(R.string.nodeStatus_rssi_format,newRSSIValue)));
    }//onRSSIChanged

    @Override
    public void onMtuChange(@NotNull Node node, int newMtu) { }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(BATTERY_CAPACITY,mBatteryCapacity);
    }
}
