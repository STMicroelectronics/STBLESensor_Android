/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/

package com.st.BlueSTSDK.Log;

import androidx.annotation.NonNull;
import android.util.Log;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;

import java.util.Date;

/**
 * Log the feature data using the Android logcat console
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureLogLogCat implements Feature.FeatureLoggerListener {

    public static final int DEFAULT_LOG_PRIORITY = Log.DEBUG;

    /**
     * Start log date time
     */
    private  Date mStartLog;

    /**
     * check that the priority value is valid (one of the priority defined in the Log class or
     * return the default one
     * @param logPriority priority log passed by the user
     * @return logPriority if is a valid value or the default one
     * @see android.util.Log
     */
    private static int checkLogPriority(int logPriority){
        if(logPriority == Log.DEBUG || logPriority == Log.ERROR ||
           logPriority == Log.INFO  || logPriority == Log.VERBOSE ||
           logPriority == Log.WARN){
            return logPriority;
        }else
            return DEFAULT_LOG_PRIORITY;
        //if-else
    }//checkLogPriority

    /** tag to use for the message send by this class */
    private String mTag;
    /** priority to use for send message by this class */
    private int mPriority;

    /** build a class using the class name as tag and the default message priority */
    public FeatureLogLogCat(){
        this(FeatureLogLogCat.class.getCanonicalName());
        mStartLog = new Date();
    }

    /**
     * build a class with a specific log and using the default message priority
     * @param tag tag to use when a message is send;
     */
    public FeatureLogLogCat(String tag){
        this(tag, Log.DEBUG);
    }

    /**
     * build a class with a specific log tag and priority
     * @param tag tag to use when a message is send;
     * @param logPriority priority log passed by the user
     */
    public FeatureLogLogCat(String tag,int logPriority){
        mPriority=checkLogPriority(logPriority);
        mTag=tag;
    }

    @Override
    public void logFeatureUpdate(@NonNull Feature feature, @NonNull byte[] rawData, Feature.Sample sample){
        StringBuilder sb = new StringBuilder();
        sb.append(Long.toString(System.currentTimeMillis() - mStartLog.getTime())).append(' ')
          .append(feature.getParentNode().getFriendlyName().replace(" @", "_")).append(' ')
          .append(Long.toString(sample.timestamp)).append(' ')
          .append(feature.getName()).append(' ');
        if(rawData!=null) {
            for (byte b : rawData) {
                sb.append(String.format("%02X", b));
            }//for
            //Log.println(mPriority,mTag,sb.toString());
        }


        //sb.delete(0,sb.length());
        //sb.append(feature.getName()).append('\n');
        Field f[] = feature.getFieldsDesc();
        for(int i =0;i<f.length;i++){
            sb.append('\t').append(f[i].getName()).append(' ');
            sb.append(sample.data[i]).append('\n');
        }//for i
        Log.println(mPriority,mTag,sb.toString());
    }

}//FeatureLogLogCat
