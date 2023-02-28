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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Common code between the csv and db log class
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public abstract class FeatureLogBase implements Feature.FeatureLoggerListener{
    private final static String TAG = FeatureLogBase.class.getCanonicalName();
    final static SimpleDateFormat DATE_FIELD_FORMAT_PREFIX = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS",
            Locale.getDefault());

    private final static SimpleDateFormat FILE_DATE_FORMAT_PREFIX = new SimpleDateFormat("yyyyMMdd_HHmmss",
            Locale.getDefault());
    private final static SimpleDateFormat DATE_FORMAT_TO_HEADER = new SimpleDateFormat("yyyy-MM-dd " +
            "HH:mm:ss",Locale.getDefault());

    protected static final String HOST_DATE_COLUMN ="Date";
    protected static final String HOST_TIMESTAMP_COLUMN ="HostTimestamp";
    protected static final String NODE_NAME_COLUMN ="NodeName";
    protected static final String NODE_TIMESTAMP_COLUMN ="NodeTimestamp";
    protected static final String NODE_RAW_DATA_COLUMN ="RawData";

    /**
     * directory where save the dump file
     * */
    protected String mDirectoryPath;

    /**
     * Start log date time
     */
    protected Date mStartLog;

    /**
     * Node list connected, current logged
     * */
    protected List<Node> mNodeList;

    /**
     * print the file header, with the node name the raw data and the feature field
     * @param out stream where write the feature data
     * @param f feature that this close will dump
     */
    protected void printHeader(Formatter out, Feature f) {
        Field[] fields = f.getFieldsDesc();
        out.format("Log start on," + DATE_FORMAT_TO_HEADER.format(mStartLog) + "\n");
        out.format("Feature," + f.getName() + "\n");
        out.format("Nodes,");
        if (mNodeList != null)
            for(Node n: mNodeList)
                out.format(n.getFriendlyName() + ", ");
        out.format("\n");

        out.format(HOST_DATE_COLUMN+","+HOST_TIMESTAMP_COLUMN + " (ms)," + NODE_NAME_COLUMN + "," + NODE_TIMESTAMP_COLUMN + "," +
                "" + NODE_RAW_DATA_COLUMN + ",");
        for(Field field:fields){
            out.format(field.getName());
            String unit =field.getUnit();
            if (unit!=null && !unit.isEmpty() )
                out.format(" (%s)", field.getUnit());
            out.format(",");

        }//for
        out.format("\n");
        out.flush();
    }//printHeader

    /**
     * dump an array of byte as a string of hexadecimal value
     * <p> No coma will be added at the end of the string</p>
     * @param out stream where write the feature data
     * @param data byte data to dump
     */
    protected void storeBlobData(Formatter out, byte[] data) {
        if (data == null || data.length == 0){
            return;
        }
        for(byte b: data) {
            out.format("%02X", b);
        }//for
    }//storeBlobData

    /**
     * dump the feature data
     * @param out stream where write the feature data
     * @param data feature data
     */
    protected void storeFeatureData(Formatter out, Number[] data)  {
        if (data == null || data.length == 0){
            return;
        }
        for(Number n: data){
            out.format("%s,",n.toString());
        }//for
    }

    /**
     * create a logger
     * @param dumpDirectoryPath path where store the log data
     * @param nodes list of node to log
     */
    public FeatureLogBase(String dumpDirectoryPath, List<Node> nodes){
        mDirectoryPath = dumpDirectoryPath;
        mStartLog = new Date();
        mNodeList = nodes;
        File f = new File(mDirectoryPath);
        if(!f.exists())
            f.mkdirs();
    }//FeatureLogCSVFile

    /**
     * create a string with the path where store the log. the name will have a timestamp for be
     * unique
     * @param f feature to log
     * @return file path where store the log for that feature
     */
    protected String logFeatureFileName(Feature f) {
        return String.format("%s/%s_%s.csv",
                mDirectoryPath, logSessionPrefix(), f.getName());
    }

    /**
     * Get the file prefix of current session
     * @return the file prefix of current session
     */
    public String logSessionPrefix(){
        String logPrefixName = "";
        synchronized (mStartLog)
        {
            logPrefixName = FILE_DATE_FORMAT_PREFIX.format(mStartLog);
        }
        return logPrefixName;
    }

    /**
     * get all the log file in the directory
     * @param directoryPath path where search the file
     * @return all file in the directory with an extension .csv
     */
    static public File[] getLogFiles(String directoryPath){
        File directory = new File(directoryPath);
        //find all the csv files
        final FileFilter csvFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String fileName =pathname.getName();
                return fileName.endsWith(".csv") || fileName.endsWith(".wav");
            }//accept
        };
        return directory.listFiles(csvFilter);
    }//getLogFiles

    /**
     * remove all the csv file in the directory
     * @param c context where the file were created
     * @param directoryPath directory where this class dumped the feature data
     */
    static public void clean(Context c, String directoryPath){
        File files[] =getLogFiles(directoryPath);
        if(files==null || files.length==0) //nothing to do
            return;

        for(File f: getLogFiles(directoryPath) ){
            if(!f.delete())
                Log.e(TAG, "Error deleting the file " + f.getAbsolutePath());
            c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
        }//for
    }//clean
}
