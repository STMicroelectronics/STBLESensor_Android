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

package com.st.BlueMS.demos.AccEvent;


import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent.AccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent.DetectableEvent;
import com.st.BlueSTSDK.Node;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

/**
 * Demo showing a list of possible events and it shake the icon each time an event is detected
 */
@DemoDescriptionAnnotation(name = "Acc Event", requareAll = {FeatureAccelerationEvent.class},
        iconRes = R.drawable.demo_sensors_fusion)
public class AccEventFragment extends BaseDemoFragment implements AdapterView.OnItemSelectedListener {

    private static final String CURRENT_SELECTED_EVENT = AccEventFragment.class.getCanonicalName() +
            ".CURRENT_SELECTED_EVENT";

    /**
     * list of possible events supported by the wesu board
     */
    private static final DetectableEvent WESU_SUPPORTED_EVENT[] = {DetectableEvent.NONE,DetectableEvent.MULTIPLE};

    /**
     * list of possible events supported by the nucleo board
     */
    private static final DetectableEvent NUCLEO_SUPPORTED_EVENT[] = {
            DetectableEvent.NONE,
            DetectableEvent.MULTIPLE,
            DetectableEvent.ORIENTATION,
            DetectableEvent.DOUBLE_TAP,
            DetectableEvent.FREE_FALL,
            DetectableEvent.PEDOMETER,
            DetectableEvent.SINGLE_TAP,
            DetectableEvent.TILT,
            DetectableEvent.WAKE_UP
    };

    private static final DetectableEvent SENSORTILE101_SUPPORTED_EVENT[] = {
            DetectableEvent.NONE,
            DetectableEvent.ORIENTATION,
            DetectableEvent.DOUBLE_TAP,
            DetectableEvent.FREE_FALL,
            DetectableEvent.SINGLE_TAP,
            DetectableEvent.TILT,
            DetectableEvent.WAKE_UP
    };

    private static final DetectableEvent IDB008_SUPPORTED_EVENT[] = {
            DetectableEvent.NONE,
            DetectableEvent.FREE_FALL,
            DetectableEvent.SINGLE_TAP,
            DetectableEvent.WAKE_UP,
            DetectableEvent.TILT,
            DetectableEvent.PEDOMETER,
    };

    private static final DetectableEvent BCN002V1_SUPPORTED_EVENT[] = {
            DetectableEvent.NONE,
            DetectableEvent.WAKE_UP,
            DetectableEvent.SINGLE_TAP,
            DetectableEvent.PEDOMETER,
            DetectableEvent.TILT,
            DetectableEvent.FREE_FALL,
    };

    /**
     * get the list of supported event by the node
     * @param type type of node that we are using
     * @return supported events
     */
    private static DetectableEvent[] getDetectableEvent(Node.Type type){
        switch (type){
            case STEVAL_WESU1:
                return WESU_SUPPORTED_EVENT;
            case STEVAL_IDB008VX:
                return IDB008_SUPPORTED_EVENT;
            case STEVAL_BCN002V1:
                return BCN002V1_SUPPORTED_EVENT;
            case SENSOR_TILE:
            case BLUE_COIN:
            case NUCLEO:
                return NUCLEO_SUPPORTED_EVENT;
            case SENSOR_TILE_BOX:
                return SENSORTILE101_SUPPORTED_EVENT;
            default:
                return new DetectableEvent[0];
        }
    }

    /**
     * default event to show for a specific not type
     * @param type type of board
     * @return default event for the board
     */
    private static DetectableEvent getDefaultEvent(Node.Type type){
        switch (type){
            case STEVAL_WESU1:
                return DetectableEvent.MULTIPLE;
            case STEVAL_IDB008VX:
                return DetectableEvent.FREE_FALL;
            case STEVAL_BCN002V1:
                return DetectableEvent.WAKE_UP;
            case SENSOR_TILE:
            case BLUE_COIN:
            case NUCLEO:
            case SENSOR_TILE_BOX:
                return DetectableEvent.ORIENTATION;
            default:
                return DetectableEvent.NONE;
        }
    }

    /***
     * view to show when a single event type is selected
     */
    private SingleEventView mSingleEventView;

    /**
     * view to show when a multiple event type is selected
     */
    private MultipleEventView mMultipleEventView;

    /**
     * current selected event
     */
    private DetectableEvent mCurrentEvent=FeatureAccelerationEvent.DEFAULT_ENABLED_EVENT;

    /**
     * feature that will fire event for this demo
     */
    private FeatureAccelerationEvent mFeature;

    /**
     * adapter with the list of supported event
     */
    private ArrayAdapter<DetectableEvent> mDetectableEventArrayAdapter;

    /**
     * spinner where select the event
     */
    private Spinner mEventSelector;


    /**
     * enable the detection o a specific event
     *
     * @param eventType event that will be enabled in the node
     */
    public void enableEvent(Node.Type type, FeatureAccelerationEvent.DetectableEvent eventType) {

        if(eventType == mCurrentEvent)
            return;

        setGuiToDisplayEvent(type,eventType);

        //disable the current one
        mFeature.detectEvent(mCurrentEvent, false);
        //enable the new one
        mFeature.detectEvent(eventType, true);
        mCurrentEvent = eventType;
    }

    private void setGuiToDisplayEvent(Node.Type type, FeatureAccelerationEvent.DetectableEvent eventType){
        if(mSingleEventView.getVisibility()==View.VISIBLE)
            mSingleEventView.enableEvent(type,eventType);
        if(mMultipleEventView.getVisibility()==View.VISIBLE)
            mMultipleEventView.enableEvent(type,eventType);
    }

    /**
     * updat the view for display the last received event
     * @param event event to display
     * @param data aditional event data (nsteps if the event is a pedomenter event)
     */
    public void displayEvent(@AccelerationEvent int event, int data) {
        if(mSingleEventView.getVisibility()==View.VISIBLE)
            mSingleEventView.displayEvent(event,data);
        if(mMultipleEventView.getVisibility()==View.VISIBLE)
            mMultipleEventView.displayEvent(event, data);
    }


    /**
     * Event listener, will extract the data and update the view
     */
    private FeatureAccelerationEvent.FeatureAccelerationEventListener mFeatureListener =
            new FeatureAccelerationEvent.FeatureAccelerationEventListener() {

                @Override
                public void onDetectableEventChange(FeatureAccelerationEvent f,
                                                    DetectableEvent event, boolean newStatus) {
                }//onDetectableEventChange

                /**
                 *
                 * @param f feature that has received an update
                 * @param sample new data received from the feature
                 */
                @Override
                public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
                    final @AccelerationEvent int event =
                            FeatureAccelerationEvent.getAccelerationEvent(sample);

                    final int nSteps = FeatureAccelerationEvent.getPedometerSteps(sample);

                    updateGui(() -> displayEvent(event,nSteps));
                }//on update
            };//FeatureAccelerationEventListener


    public AccEventFragment() {
        // Required empty public constructor
    }

    /**
     * create the adapter and attach to the spinner,
     * @param type node type, it is used for choose the default event and the list of event
     */
    private void initializeEventSelector(Node.Type type){
        mDetectableEventArrayAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                getDetectableEvent(type));
        mEventSelector.setAdapter(mDetectableEventArrayAdapter);

        mEventSelector.setSelection(mDetectableEventArrayAdapter.getPosition(mCurrentEvent));

        mEventSelector.setOnItemSelectedListener(this);
        mEventSelector.setEnabled(true);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mFeature = node.getFeature(FeatureAccelerationEvent.class);

        if (mFeature != null) {

            if(mCurrentEvent==DetectableEvent.NONE) // first we enable the notification time
                mCurrentEvent = getDefaultEvent(node.getType());
            initializeEventSelector(node.getType());
            setGuiToDisplayEvent(node.getType(),mCurrentEvent);
            node.enableNotification(mFeature);
            if(mCurrentEvent != FeatureAccelerationEvent.DEFAULT_ENABLED_EVENT) {
                //force to enable the currentEvent
                DetectableEvent temp = mCurrentEvent;
                mCurrentEvent = FeatureAccelerationEvent.DEFAULT_ENABLED_EVENT;
                enableEvent(node.getType(), temp);
            }

            mFeature.addFeatureListener(mFeatureListener);

        }//if
    }//enableNeededNotification

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (mFeature != null) {
            mFeature.removeFeatureListener(mFeatureListener);
            node.disableNotification(mFeature);
        }//if
    }//disableNeedNotification



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_acc_event, container, false);

        mEventSelector = root.findViewById(R.id.selectEventType);
        mSingleEventView = root.findViewById(R.id.accEvent_singleEventView);
        mMultipleEventView = root.findViewById(R.id.accEvent_multipleEventView);

        if (savedInstanceState != null &&
                savedInstanceState.containsKey(CURRENT_SELECTED_EVENT)) {
            mCurrentEvent =(DetectableEvent) savedInstanceState.getSerializable(CURRENT_SELECTED_EVENT);
        }else{
            //force to use the demo default and not the board default
            if(mCurrentEvent == FeatureAccelerationEvent.DEFAULT_ENABLED_EVENT)
                mCurrentEvent = DetectableEvent.NONE;
        }

        return root;
    }//onCreateView


    private void enableMultipleEventView(){
        mSingleEventView.setVisibility(View.GONE);
        mMultipleEventView.setVisibility(View.VISIBLE);
    }

    private void disableMultipleEventView(){
        mSingleEventView.setVisibility(View.VISIBLE);
        mMultipleEventView.setVisibility(View.GONE);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (mFeature != null) {
            DetectableEvent selectedEvent = mDetectableEventArrayAdapter.getItem(i);
            if(selectedEvent==DetectableEvent.MULTIPLE){
                enableMultipleEventView();
            }else{
                disableMultipleEventView();
            }
            Node node = getNode();
            if(node!=null)
                enableEvent(node.getType(), selectedEvent);
            else
                enableEvent(Node.Type.GENERIC,selectedEvent);
        }//if null
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //we have an event to save
        savedInstanceState.putSerializable(CURRENT_SELECTED_EVENT,mCurrentEvent);
    }//onSaveInstanceState

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
