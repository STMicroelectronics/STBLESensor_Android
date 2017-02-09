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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYLegendWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureActivity;
import com.st.BlueSTSDK.Features.FeatureAudioADPCM;
import com.st.BlueSTSDK.Features.FeatureAudioADPCMSync;
import com.st.BlueSTSDK.Features.FeatureBattery;
import com.st.BlueSTSDK.Features.FeatureCarryPosition;
import com.st.BlueSTSDK.Features.FeatureDirectionOfArrival;
import com.st.BlueSTSDK.Features.FeatureFreeFall;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureLuminosity;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeatureMemsGesture;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusion;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusionCompact;
import com.st.BlueSTSDK.Features.FeatureMotionIntensity;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureProximity;
import com.st.BlueSTSDK.Features.FeatureProximityGesture;
import com.st.BlueSTSDK.Features.FeatureSwitch;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * fragment that plot the feature data in an xy plot
 */
@DemoDescriptionAnnotation(name="Plot Data",iconRes=R.drawable.demo_charts)
public class PlotFeatureFragment extends DemoFragment implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    @SuppressWarnings("unchecked")
    private static final Class<? extends Feature> NO_PLOT_FEATURE[] = new Class[]{
            FeatureFreeFall.class,
            FeatureBattery.class,
            FeatureActivity.class,
            FeatureCarryPosition.class,
            FeatureMemsGesture.class,
            FeatureProximityGesture.class,
            FeatureAccelerationEvent.class,
            FeatureSwitch.class,
            FeatureAudioADPCM.class,
            FeatureAudioADPCMSync.class
    };

    //on the fw side the timestamp is incremented each 10ms
    private static final int TIMESTAMP_TO_MS = 10;
    /**
     * simple class that contains the max min value to plot for a feature
     */
    private static class PlotBoundary {
        Number max;
        Number min;
        int nLabel;

        PlotBoundary(Number max, Number min, int nLabel){
            this.max = max;
            this.min = min;
            this.nLabel =nLabel;
        }
    }

    /**
     * map that associate a boundary for each know feature
     */
    private static final Map<Class<? extends  Feature>,PlotBoundary> sKnowFeatureBoundary = new HashMap<>();
    static {

        sKnowFeatureBoundary.put(FeatureAcceleration.class,
                new PlotBoundary(FeatureAcceleration.DATA_MAX, FeatureAcceleration.DATA_MIN, 21));
        sKnowFeatureBoundary.put(FeatureMagnetometer.class,
                new PlotBoundary(FeatureMagnetometer.DATA_MAX, FeatureMagnetometer.DATA_MIN, 21));
        sKnowFeatureBoundary.put(FeatureGyroscope.class,
                new PlotBoundary(FeatureGyroscope.DATA_MAX, FeatureGyroscope.DATA_MIN, 11));
        sKnowFeatureBoundary.put(FeatureHumidity.class,
                new PlotBoundary(FeatureHumidity.DATA_MAX, FeatureHumidity.DATA_MIN, 11));
        sKnowFeatureBoundary.put(FeatureLuminosity.class,
                new PlotBoundary(FeatureLuminosity.DATA_MAX, FeatureLuminosity.DATA_MIN, 11));
        sKnowFeatureBoundary.put(FeatureMemsSensorFusion.class,
                new PlotBoundary(FeatureMemsSensorFusion.DATA_MAX,
                        FeatureMemsSensorFusion.DATA_MIN, 21));
        sKnowFeatureBoundary.put(FeatureMemsSensorFusionCompact.class,
                new PlotBoundary(FeatureMemsSensorFusion.DATA_MAX,
                        FeatureMemsSensorFusion.DATA_MIN, 21));
        sKnowFeatureBoundary.put(FeatureProximity.class,
                new PlotBoundary(FeatureProximity.DATA_MAX,
                        FeatureProximity.DATA_MIN, 11));
        sKnowFeatureBoundary.put(FeatureDirectionOfArrival.class,
                new PlotBoundary(FeatureDirectionOfArrival.DATA_MAX,
                        FeatureDirectionOfArrival.DATA_MIN, 37));
        sKnowFeatureBoundary.put(FeaturePressure.class, new PlotBoundary(1060.0f, 960, 11));
        sKnowFeatureBoundary.put(FeatureTemperature.class, new PlotBoundary(50.0f, 0.0f, 11));
        sKnowFeatureBoundary.put(FeatureFreeFall.class, new PlotBoundary(2,0,3));
        sKnowFeatureBoundary.put(FeatureActivity.class, new PlotBoundary(FeatureActivity.DATA_MAX,
                FeatureActivity.DATA_MIN,
                (int)(FeatureActivity.DATA_MAX-FeatureActivity.DATA_MIN+1)));

    }

    /**the plot will show only the last {@code mMaxPlotLengthMs} milliseconds of data */
    private long mMaxPlotLengthMs = 5*1000; //default 5s

    /** color used for plot the different lines */
    private int[] mDataSetColors;
    /** button for start/stopping to plot the data */
    private ImageButton mStartPlotButton;
    /** list of available feature */
    private Spinner mFeatureSelector;

    /** index of the last selected index */
    private int mFeatureSelectorSelectedIndex=0;

    /** label where show the last feature value, is null if the layout is horizontal */
    private TextView mFeatureText;
    /** feature that we are plotting */
    private Feature mPlotFeature;
    /**
     * feature listener that will update the plot with the last data received by the feature
     */
    private ChartUpdater mPlotFeatureValue;
    /**
     * plot where plot the feature data
     */
    private XYPlot mChart;

    /**
     * lock to use for access to the xy series
     */
    private ReentrantLock mPlotDataLock;

    /**
     * series that we will plot
     */
    private SimpleXYSeries[] mPlottedData;

    /** true if we are plotting some data */
    private boolean mIsPlotting;

    /** domain axis label */
    private String mXAxisLabel;

    /**BoundaryMode*/
    private BoundaryMode mBoundaryMode;

    /**
     * class that will insert a new data in the plot.
     * <p> we will show only the last {@code maxDisplaySample} the other samples are deleted</p>
     */
    private class ChartUpdater implements Feature.FeatureListener {
        /**
         * we force the feature update every {@code FORCE_UPDATE_PERIOD_MS}, if new data aren't
         * present we use the last received data
         */
        private static final long FORCE_UPDATE_PERIOD_MS = 500;

        /**
         * each time that we start a new plot we increment this value, in this way we can discard
         * message from the previous plot that can update the plot in the past/feature with a
         * strange graphical effect
         */
        private AtomicInteger mPlotId=  new AtomicInteger(0);

        /**
         * timestamp of the first sample, is used for show the sample in a range 0.. n instead of
         * k.. k+n
         */
        private long mFirstTimestamp=-1;
        private long mLastTimestamp=-1;

        /**
         * Number of item that the feature will plot, if it change we restart the plot
         * */
        private int mNItem=-1;

        /**
         * loop that will handle the message send each {@code FORCE_UPDATE_PERIOD_MS} that will
         * update the plot if we didn't receive new data
         */
        private Handler mAddNewData = new Handler(new Handler.Callback() {

            /**
             * number of times that the handler is wake up without find a recent feature update
             */
            private int mMissValue=1;

            @Override
            public boolean handleMessage(Message message) {
                if(mPlotFeature!=null && message.what==mPlotId.get()) {
                    Feature.Sample sample = mPlotFeature.getSample();
                    if(sample==null) //we don't have noting to dos
                        return true;
                    long timestamp =  sample.timestamp*TIMESTAMP_TO_MS;
                    if (timestamp==(Long)message.obj) {
                        mPlotFeatureValue.addValueToPlot(timestamp + mMissValue *
                                FORCE_UPDATE_PERIOD_MS,mPlotFeature,sample);
                        mMissValue++;
                        Message m = mAddNewData.obtainMessage(mPlotId.get(),timestamp);
                        mAddNewData.sendMessageDelayed(m, FORCE_UPDATE_PERIOD_MS);
                    }else{ //there was an update
                        mMissValue=1;
                    }//if-else
                }//if
                return true;
            }//handleMessage
        }//CallBack
    ); // handler

        /**
         * when the plot is reset we have to reset also the sample index
         */
        void resetSampleIndex() {
            mAddNewData.removeMessages(mPlotId.get());
            mFirstTimestamp=-1;
            mLastTimestamp=-1;
            mPlotId.incrementAndGet();
        }

        /**
         * add a new value in the plot into a specific x position
         * @param dataTimeMs x coordinate used for plot the data
         * @param f data that we  have to insert in the plot
         */
        public void addValueToPlot(long dataTimeMs,Feature f,Feature.Sample sample){
            if(!isRunning()) //avoid to update if the fragment isn't show
                return;

            final String dataString = f.toString();

            mPlotDataLock.lock();
                //we plot only if it came after the last update
                if(dataTimeMs>mLastTimestamp){
                    mLastTimestamp=dataTimeMs;
                }else{
                    mPlotDataLock.unlock();
                    return;
                }//if-else

                // if is the fist time that we insert a new data, we fix the x axis range
                if(mFirstTimestamp<0) {
                    mBoundaryMode = BoundaryMode.FIXED;
                    mChart.setDomainBoundaries(0, BoundaryMode.AUTO,
                            mMaxPlotLengthMs, mBoundaryMode);
                    mFirstTimestamp = dataTimeMs;
                }//mFirstTimestamp

                //if we already finish the space in the x axis we move the x range
                if (mBoundaryMode == BoundaryMode.FIXED &&
                        (dataTimeMs-mFirstTimestamp) >= mMaxPlotLengthMs) {
                    mBoundaryMode = BoundaryMode.AUTO;
                    mChart.setDomainBoundaries(mChart.getDomainOrigin(), BoundaryMode.AUTO,
                            dataTimeMs, mBoundaryMode);
                }

                // add the data and remove the all sample
                final int dataLength = Math.min(sample.data.length,mPlottedData.length);
                for (int i = 0; i < dataLength; i++) {
                    SimpleXYSeries serie = mPlottedData[i];
                    final long currentRelativeTime = dataTimeMs-mFirstTimestamp;
                    serie.addLast(currentRelativeTime, sample.data[i]);
                    //we we insert more data than the maximum we remove the oldest
                    //we use a wile since the maxDiplaySample can change during the execution
                    while ((serie.size()!=0) &&
                            ((currentRelativeTime- serie.getX(0).longValue())>mMaxPlotLengthMs)) {
                        serie.removeFirst();
                    }//while
                }//for

            mPlotDataLock.unlock();

            updateGui(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(mFeatureText!=null) mFeatureText.setText(dataString);
                        //replot the plot with the new data
                        mPlotDataLock.lock();
                        mChart.redraw();
                    } catch (NullPointerException e) {
                        //this exception can happen when the task is run after the fragment is
                        // destroyed
                    } finally {
                        mPlotDataLock.unlock();
                    }
                }
            });
        }

        /**
         * change the of items inside the feature
         * @param nItem number of data plotted
         */
        public void setFeatureNItems(int nItem){
            mNItem = nItem;
        }

        @Override
        public void onUpdate(final Feature f,Feature.Sample sample) {
            long timestamp = sample.timestamp;
            if(mNItem <0 || f.getFieldsDesc().length!=mNItem){
                setFeatureNItems(f.getFieldsDesc().length);
                updateGui(new Runnable() {
                    @Override
                    public void run() {
                        prepareChartForFeature(f);
                    }//run
                });
            }//if
            addValueToPlot(timestamp*TIMESTAMP_TO_MS, f,sample);
            Message m = mAddNewData.obtainMessage(mPlotId.get(),timestamp*TIMESTAMP_TO_MS);
            mAddNewData.sendMessageDelayed(m, FORCE_UPDATE_PERIOD_MS);
        }//onUpdate
    }


    public PlotFeatureFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataSetColors = getResources().getIntArray(R.array.dataSetColor);
        mPlotDataLock = new ReentrantLock();

        mPlotFeatureValue = new ChartUpdater();

        //save the fragment state when the activity is destroyed
        setRetainInstance(true);
        //ask to add our option to the menu
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_plot_feature, container, false);

        mChart = (XYPlot) root.findViewById(R.id.chart);
        mFeatureSelector = (Spinner) root.findViewById(R.id.featureSelector);
        mFeatureText = (TextView) root.findViewById(R.id.featureValue);
        mStartPlotButton = (ImageButton) root.findViewById(R.id.startPlotButton);
        mStartPlotButton.setOnClickListener(this);
        mStartPlotButton.setEnabled(false);

        Paint legendBg = new Paint();
        legendBg.setColor(Color.BLACK);
        legendBg.setStyle(Paint.Style.FILL);
        legendBg.setAlpha(40);

        //set the legend size/position
        XYLegendWidget legend = mChart.getLegendWidget();
        Resources res = getResources();

        legend.setBackgroundPaint(legendBg);
        legend.setSize(new SizeMetrics(
                res.getDimension(R.dimen.legend_box_height),
                SizeLayoutType.ABSOLUTE,
                res.getDimension(R.dimen.legend_box_width),
                SizeLayoutType.ABSOLUTE)
        );

        //it will show when necessary
        legend.setVisible(false);

        mXAxisLabel = getResources().getString(R.string.xAxisLabel);

        mChart.setDomainValueFormat(new DecimalFormat("#"));
        return root;
    }


    /*
    * free the element gui for permit to the gc to free it before recreate the fragment
    */
    @Override
    public void onDestroyView (){
        if(mIsPlotting) {
            for(XYSeries s : mPlottedData){
                mChart.removeSeries(s);
            }
        }
        mChart=null;
        mStartPlotButton=null;
        mFeatureSelector=null;
        mFeatureText=null;
        super.onDestroyView();
    }

    /**
     * set the plot for receive the feature data and enable the feature
     * @param f feature to plot
     */
    public void startPlotFeature(Feature f) {
        Node node = getNode();
        if(node==null)
            return;
        prepareChartForFeature(f);
        mPlotFeature = f;
        mPlotFeature.addFeatureListener(mPlotFeatureValue);
        node.enableNotification(mPlotFeature);
        mIsPlotting = true;
        if(mFeatureText!=null) mFeatureText.setVisibility(View.VISIBLE);
    }

    /**
     * stop receiving data from the feature and avoid to update the plot
     */
    public void stopPlotting() {
        mIsPlotting = false;
        Node node = getNode();
        if(node==null)
            return;
        mPlotFeature.removeFeatureListener(mPlotFeatureValue);
        node.disableNotification(mPlotFeature);
        mPlotFeatureValue.resetSampleIndex();
        mPlotFeature=null;
        if(mFeatureText!=null){
            mFeatureText.setText("");
            mFeatureText.setVisibility(View.GONE);
        }
    }


    private void setButtonStopStatus(){
        mStartPlotButton.setImageResource(R.drawable.ic_stop);
        mStartPlotButton.setContentDescription(getResources().getString(R.string.startPlotButton));
    }

    private void setButtonStartStatus(){
        mStartPlotButton.setImageResource(R.drawable.ic_play_arrow);
        mStartPlotButton.setContentDescription(getResources().getString(R.string.startPlotButton));
    }

    /**
     * call when the user click on the button, will start/stop plotting the data for the selected
     * feature
     * @param v clicked item (not used)
     */
    @Override
    public void onClick(View v) {
        if (mIsPlotting) {
            stopPlotting();
            setButtonStartStatus();
        } else {
            Feature f = (Feature) mFeatureSelector.getSelectedItem();
            if(f==null) // if nothing is selected, do nothing
                return;
            startPlotFeature(f);
            setButtonStopStatus();
        }//if-else
    }

    /**
     * call when the user select a feature from the list
     * <p>if we are plotting another feature we stop that feature and start ploting the new one</p>
     * @param parent adapter that contains the display feature
     * @param view not used
     * @param position not used
     * @param id not used
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Feature currentFeature = (Feature) parent.getSelectedItem();
        mFeatureSelectorSelectedIndex=position;
        if(currentFeature != mPlotFeature && mPlotFeature!=null) {
            boolean wasPlotting = mIsPlotting;
            if(mIsPlotting)
                stopPlotting();
            prepareChartForFeature(currentFeature);
            if (wasPlotting) {
                startPlotFeature(currentFeature);
            }//if
        }//if
    }


    /**
     * prapare the plot for contains the data from a feature
     * @param f feature that we wont plot
     */
    @UiThread
    private void setChartForFeature(final Feature f){

        Field fields[] = f.getFieldsDesc();

//        //set the plot title
//        mChart.setTitle(f.getName());
        //set the x axis label
        mChart.setDomainLabel(mXAxisLabel);
        mChart.setDomainValueFormat(new DecimalFormat("#"));
        //se the y axis  with the unit of the first data
        mChart.setRangeLabel(fields[0].getUnit());
        //if we have move than one series show the legend otherwise hide it
        mChart.getLegendWidget().setVisible(fields.length!=1);

        //if we know the feature we set the min manx y, otherwise is automatic

        PlotBoundary featureBoundary = sKnowFeatureBoundary.get(f.getClass());

        if(featureBoundary!=null){
            mChart.setRangeBoundaries(featureBoundary.min, BoundaryMode.FIXED,
                    featureBoundary.max, BoundaryMode.FIXED);
            mChart.setRangeStep(XYStepMode.SUBDIVIDE, featureBoundary.nLabel);
        }else{
            mChart.setRangeBoundaries(null, BoundaryMode.GROW,
                    null, BoundaryMode.GROW);
        }//if-else

    }

    /**
     * reset the plot and create create a new one that will contain the feature data
     * @param f feature to plot
     */
    @UiThread
    private void prepareChartForFeature(final Feature f) {
        Field fields[] = f.getFieldsDesc();
        //set the plot for the feature

        setChartForFeature(f);
        //remove the previous series
        mChart.clear();
        ////////////////////
        //this call is a work around for avoid a null pointer exception fire since we have
        // a grow rangeBoundaries
        mChart.calculateMinMaxVals();
        //////////////////
        mPlotDataLock.lock();

            //create the series that we will contains the data
            mPlottedData = new SimpleXYSeries[fields.length];
            int nDataColor = mDataSetColors.length;
            for (int i = 0; i < fields.length; i++) {
                mPlottedData[i] = new SimpleXYSeries(fields[i].getName());
                // no marker, not fill, no text
                mChart.addSeries(mPlottedData[i],
                        new LineAndPointFormatter(mDataSetColors[i%nDataColor],null, null, null));
            }//for
            mChart.redraw();
        mPlotDataLock.unlock();

    }//prepareChart

    /**
     * after that the screen is rotate, the plot is recreated -> we have to restore the previous
     * data and plot status
     */
    private void restoreChart(){
        setChartForFeature(mPlotFeature);

        int nDataColor = mDataSetColors.length;
        for (int i = 0; i < mPlottedData.length; i++) {
            mChart.addSeries(mPlottedData[i],
                    new LineAndPointFormatter(mDataSetColors[i%nDataColor], null, null, null));
        }//for
        Number origin;
        if(mPlottedData[0].size()!=0)
            origin = mPlottedData[0].getX(0);
        else
            origin=0L;

        mChart.setDomainBoundaries(origin, BoundaryMode.AUTO,
                origin.longValue() + mMaxPlotLengthMs,
                mBoundaryMode);
    }

    /**
     * after a screen rotation the gui item are recreated so we have to restore the status
     * as before the the rotation, this method is called only if we rotate the screen when we are
     * plotting something
     */
    private void restoreGui(){
        //restore the plot
        restoreChart();
        //we are plotting something -> change the button label
        setButtonStopStatus();
        if(mFeatureText!=null)
            mFeatureText.setVisibility(View.VISIBLE);
    }


    private List<Feature> filterPlottableFeature(List<Feature> all){
        List<Feature> plottableFeature = new ArrayList<>(all.size());

        for(Feature f : all){
            if(f.isEnabled()) {
                Class<? extends Feature> searchMe = f.getClass();
                boolean plotMe=true;
                for(Class<? extends Feature> noPlotMe : NO_PLOT_FEATURE){
                    if(searchMe.equals(noPlotMe)) {
                        plotMe = false;
                        break;
                    }
                }
                if(plotMe)
                    plottableFeature.add(f);
            }//if isEnabled
        }//for

        return plottableFeature;
    }

    /**
     * we enable the button for start plotting the data
     * @param node node where the notification will be enabled
     */
    @Override
    protected void enableNeededNotification(@NonNull final Node node) {

        final List<Feature> plottableFeature = filterPlottableFeature(node.getFeatures());

        updateGui(new Runnable() {
            @Override
            public void run() {
                mFeatureSelector.setAdapter(new FeatureArrayAdapter(getActivity(), plottableFeature));
                mFeatureSelector.setSelection(mFeatureSelectorSelectedIndex);
                mFeatureSelector.setOnItemSelectedListener(PlotFeatureFragment.this);
                mStartPlotButton.setEnabled(true);
                //if this method is called after that the activity is recreated, we check if we were
                // plotting something, we restore the previous state
                if (mIsPlotting) {
                    restoreGui();
                    mPlotFeature.addFeatureListener(mPlotFeatureValue);
                    node.enableNotification(mPlotFeature);
                }
            }//run
        });
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node){
        //if we are plotting that feature
        if (node.isEnableNotification(mPlotFeature)) {
            mPlotFeature.removeFeatureListener(mPlotFeatureValue);
            node.disableNotification(mPlotFeature);
        }
    }



    /**
     * Adapter that for each feature will create a label with the feature name
     */
    private class FeatureArrayAdapter extends ArrayAdapter<Feature> {

        public FeatureArrayAdapter(Context c, List<Feature> data) {
            super(c,android.R.layout.simple_list_item_1, data);
        }

        //the spinner will use this value for show the possible value that can be selected
        @Override
        public View getDropDownView(int position, View v, ViewGroup parent) {
            return getView(position, v, parent);
        }

        /** create a view for a specific feature
         * @param position feature position
         * @param v if not null we can use this view instead of create create a new one
         * @param parent view where the class will insert the view that we create
         * @return view that represent the feature
         */
        @Override
        public View getView(int position, View v, ViewGroup parent) {

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }//else

            ((TextView) v).setText(getItem(position).getName());

            return v;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_plot_feature_demo, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private AlertDialog buildMaxSampleDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.selectMaxSamples);
        dialog.setItems(R.array.availableMaxSamplesString,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int[] maxSample = getResources().getIntArray(R.array
                                .availableMaxSamplesValues);

                        //if we increase the number of sample, or the boundary is not initialize
                        //use the fixed size for the x axis
                        if (maxSample[which] > mMaxPlotLengthMs || mBoundaryMode==null) {
                            mBoundaryMode = BoundaryMode.FIXED;
                        }
                        mMaxPlotLengthMs = maxSample[which];
                        Number origin =mChart.getDomainOrigin();
                        mChart.setDomainBoundaries(origin, BoundaryMode.AUTO,
                                origin.longValue() + mMaxPlotLengthMs,
                                mBoundaryMode);
                    }//onClick
                }//DialogInterface
        );
        return dialog.create();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_plot_length) {

            buildMaxSampleDialog().show();
            return true;
        }//else
        return super.onOptionsItemSelected(item);
    }



}
