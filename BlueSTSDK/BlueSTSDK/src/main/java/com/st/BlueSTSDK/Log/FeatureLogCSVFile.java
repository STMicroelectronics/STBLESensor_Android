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
import androidx.annotation.Nullable;
import android.util.Log;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dump the feature change on a coma separated value file, the fist line will contain the data
 * name and the others the feature data.
 * <p>
 * The name file will be equal to the feature name.
 * </p>
 * <p>
 * This class can be used for dump more than a feature at time, each feature will be dumped in a
 * different file
 * </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureLogCSVFile extends FeatureLogBase {
    private final static String TAG = FeatureLogCSVFile.class.getCanonicalName();


    /**
     * map used for associate a file to each feature
     */
    private Map<String,Formatter> mFormatterCacheMap;


    /**
     * create a new logger
     * @param dumpDirectoryPath directory path used for save the file
     * @param nodes list of nodes connected
     */
    public FeatureLogCSVFile(String dumpDirectoryPath, List<Node> nodes){
        super(dumpDirectoryPath, nodes);
        mFormatterCacheMap = new HashMap<>();
    }//FeatureLogCSVFile


    /**
     * create a new file for the feature or return an already opened file,
     * <p>
     *   the file will be created in the directory passed to the constructor and with the feature
     *   name
     * </p>
     * @param f feature that you want dump
     * @return stream where write the feature data
     * @throws IOException if there is an error during the stream writing
     */
    private Formatter openDumpFile(Feature f) throws IOException {
        Formatter temp = mFormatterCacheMap.get(f.getName());
        if(temp!=null){
            return temp;
        }
        //else
        temp = new Formatter(new File(logFeatureFileName(f)));
        synchronized (temp) {
            printHeader(temp, f);
            mFormatterCacheMap.put(f.getName(), temp);
        }
        return temp;
    }

    @Override
    public void logFeatureUpdate(@NonNull Feature feature, @NonNull byte[] rawData, @Nullable Feature.Sample data) {
        try {
            Formatter out = openDumpFile(feature);
            Date notificationTime = data !=null ? new Date(data.notificationTime) : new Date();
            String receivedTime = DATE_FIELD_FORMAT_PREFIX.format(notificationTime);

            synchronized (out) { // be secure that only one call write on the file
                out.format(receivedTime); //date
                out.format(",");
                out.format(Long.toString(notificationTime.getTime() - mStartLog.getTime()));  //HostTimestamp
                out.format(",");
                out.format(feature.getParentNode().getFriendlyName()); //NodeName
                out.format(",");
                if(data!=null)
                    out.format(Long.toString(data.timestamp)); //NodeTimestamp
                out.format(",");
                storeBlobData(out, rawData);
                out.format(",");
                if(data!=null)
                    storeFeatureData(out, data.data);
                out.format("\n");
                out.flush();
            }//synchronized
        } catch (IOException e) {
            Log.e(TAG,"Error dumping data Feature: "+feature.getName()+"\n"+e.toString());
        } catch (FormatterClosedException e) {
            Log.w(TAG,"Skip dumping data Feature: "+feature.getName()+"\n"+e.toString());
        }
    }

    /**
     * close all the open file
     */
    public void closeFiles(){
            for (Formatter w : mFormatterCacheMap.values()) {
                synchronized (w) {
                    w.flush();
                    w.close();
                }
            }//for
        mFormatterCacheMap.clear();
    }



}
