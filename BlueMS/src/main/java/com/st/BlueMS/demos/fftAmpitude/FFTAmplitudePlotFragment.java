package com.st.BlueMS.demos.fftAmpitude;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.st.BlueMS.R;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.Features.FeatureFFTAmplitude;
import com.st.BlueSTSDK.Features.FeatureMotorTimeParameter;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.util.FragmentUtil;

import java.util.ArrayList;
import java.util.List;

@DemoDescriptionAnnotation(name = "FFTPlot",requareAll = {
})
public class FFTAmplitudePlotFragment extends Fragment {

    private static final String DETAILS_DIALOG_TAG = "DETAILS_DIALOG";


    private static LineDataSet buildDataSet(FFTComponentsConfig.LineConf conf, float[] yData , float deltaX){
        List<Entry> data = new ArrayList<>(yData.length);
        float x = 0;
        for (float y : yData){
            data.add(new Entry(x,y));
            x+=deltaX;
        }
        LineDataSet dataSet = new LineDataSet(data,conf.name);
        dataSet.setDrawCircles(false);
        dataSet.setColor(conf.color);
        return dataSet;
    }

    public static Fragment newInstance() {
        return new FFTAmplitudePlotFragment();
    }

    private void updatePlot(List<float[]> data, float frequencySteps) {

        int nComponents = Math.min(data.size(),FFTComponentsConfig.LINES.length);
        List<ILineDataSet> dataSets = new ArrayList<>(nComponents);
        for (int i = 0; i<nComponents ; i++){
            LineDataSet line = buildDataSet(FFTComponentsConfig.LINES[i],data.get(i),frequencySteps);
            dataSets.add(line);
        }
        LineData lineData = new LineData(dataSets);
        FragmentUtil.runOnUiThread(this,() -> {
            mRefreshProgress.setVisibility(View.INVISIBLE);
            mFFTChart.setData(lineData);
            mFFTChart.invalidate();
        });
    }


    private LineChart mFFTChart;

    private ProgressBar mRefreshProgress;

    private FFTDataViewModel mFFTViewModel;
    private TimeDomainDataViewModel mTimeDomainViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fft_plot,container,false);

        mRefreshProgress = root.findViewById(R.id.fftAmpl_refreshingProgres);
        mRefreshProgress.setMax(100);

        mFFTChart = root.findViewById(R.id.fftAmpl_chart);
        setUpChart(mFFTChart);

        root.findViewById(R.id.fftAmpl_show_details).setOnClickListener(view -> {
            DialogFragment details = new FFTAmplitudeDataStatsFragment();
            details.show(requireFragmentManager(),DETAILS_DIALOG_TAG);
        });

        return root;
    }

    private static void setUpChart(LineChart chart){
        Context ctx = chart.getContext();
        //hide right axis
        chart.getAxisRight().setEnabled(false);
        //move x axis on the bottom
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        //hide plot description
        chart.getDescription().setEnabled(false);
        chart.setNoDataText(ctx.getString(R.string.fftAmpl_noDataText));
        chart.setTouchEnabled(false);

        Legend legend = chart.getLegend();
        legend.setDrawInside(true);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
    }

    private void registerFFTDataListener(){
        mFFTViewModel.getFftData().observe(this, data -> {
            Float freqStep = mFFTViewModel.getFrequencyStep().getValue();
            if(freqStep!=null && data!=null)
                updatePlot(data,freqStep);
        });
    }

    private void registerFFTUpdateListener(){
        mFFTViewModel.getLoadingStatus().observe(this, percentage -> {
            if(percentage==null)
                return;
            if(percentage >= 100) { // complete
                mRefreshProgress.setVisibility(View.INVISIBLE);
            }else{
                mRefreshProgress.setVisibility(View.VISIBLE);
                mRefreshProgress.setProgress(percentage);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFFTViewModel = ViewModelProviders.of(requireActivity()).get(FFTDataViewModel.class);
        mTimeDomainViewModel = ViewModelProviders.of(requireActivity()).get(TimeDomainDataViewModel.class);
        registerFFTUpdateListener();
        registerFFTDataListener();
    }

}
