package com.st.BlueMS;

import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.LeakCanary;

public class BlueMSApplication extends MultiDexApplication {

    private void initMemoryLeakDetector(){
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        initMemoryLeakDetector();
    }

}
