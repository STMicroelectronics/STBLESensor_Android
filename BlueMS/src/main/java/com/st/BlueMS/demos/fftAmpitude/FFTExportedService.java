package com.st.BlueMS.demos.fftAmpitude;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;


public class FFTExportedService extends IntentService {
    private static final String ACTION_EXPORT_DATA = FFTExportedService.class.getCanonicalName()+".ACTION_EXPORT_DATA";

    private static final String EXTRA_FILE_NAME = FFTExportedService.class.getCanonicalName()+".EXTRA_FILE_NAME";
    private static final String EXTRA_NODE_NAME = FFTExportedService.class.getCanonicalName()+".EXTRA_NODE_NAME";
    private static final String EXTRA_FFT_AMPLITUDE = FFTExportedService.class.getCanonicalName()+".EXTRA_FFT_AMPLITUDE";
    private static final String EXTRA_FFT_FREQUENCY_STEP = FFTExportedService.class.getCanonicalName()+".EXTRA_FFT_FREQUENCY_STEP";

    public FFTExportedService() {
        super("FFTExportedService");
    }

    public static void startExport(Context context, String fileName,String nodeName, List<float[]> fftData, float freqStep) {
        Intent intent = new Intent(context, FFTExportedService.class);
        intent.setAction(ACTION_EXPORT_DATA);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        intent.putExtra(EXTRA_FFT_FREQUENCY_STEP, freqStep);
        ArrayList<float[]> temp = new ArrayList<>(fftData); //made it serializable
        intent.putExtra(EXTRA_FFT_AMPLITUDE,temp);
        intent.putExtra(EXTRA_NODE_NAME,nodeName);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_EXPORT_DATA.equals(action)) {
                final String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                final String nodeName = intent.getStringExtra(EXTRA_NODE_NAME);
                final ArrayList<float[]> data = (ArrayList<float[]>) intent.getSerializableExtra(EXTRA_FFT_AMPLITUDE);
                final float frequencyStep = intent.getFloatExtra(EXTRA_FFT_FREQUENCY_STEP,0);
                handleExportData(fileName,nodeName,data,frequencyStep);
            }
        }
    }

    private static final String[] COMPONENTS_NAME = {"AmplitudeX","AmplitudeY","AmplitudeZ"};

    final static SimpleDateFormat DATE_FORMAT_TO_HEADER = new SimpleDateFormat("yyyy-MM-dd " +
            "HH:mm:ss", Locale.getDefault());

    private void addHeader(File f,String nodeName,List<float[]> data, float deltaFreq) throws FileNotFoundException {
        int supportedComponents = Math.min(data.size(), COMPONENTS_NAME.length);
        Formatter formatter = new Formatter(f);
        formatter.format("Node:, %s\n",nodeName);
        formatter.format("Start:, %s\n",DATE_FORMAT_TO_HEADER.format(new Date()));
        formatter.format("# Components:, %d\n",data.size());
        formatter.format("# Sample:, %d\n",data.get(0).length);
        formatter.format("Frequency Step:, %f\n\n",deltaFreq);
        formatter.format("Frequency");
        for(int i = 0 ; i<supportedComponents ; i++){
            formatter.format(", ");
            formatter.format(COMPONENTS_NAME[i]);
        }
        formatter.format("\n");
        formatter.close();
    }

    private void appendFFTData(File f,List<float[]> data, float deltaFreq) throws FileNotFoundException {
        Formatter formatter = new Formatter(new FileOutputStream(f,true));
        int nSample = data.get(0).length;
        for(int i = 0 ; i<nSample ; i++){
            formatter.format("%f,",i*deltaFreq);
            for(float[] component: data){
                formatter.format("%f,",component[i]);
            }
            formatter.format("\n");
        }
        formatter.close();
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleExportData(String fileName,String nodeName,ArrayList<float[]> fftData,float frequencyStep) {
        File f = new File(fileName);
        try {
            if(!f.exists()){
                addHeader(f,nodeName,fftData,frequencyStep);
            }
            appendFFTData(f,fftData,frequencyStep);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
