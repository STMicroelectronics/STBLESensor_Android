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
package com.st.BlueSTSDK.Utils;

import android.Manifest;
import android.app.Activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Log.FeatureLogBase;
import com.st.BlueSTSDK.Log.FeatureLogCSVFile;
import com.st.BlueSTSDK.Log.FeatureLogDB;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for an activity that have to log the feature data, this code manage the permission
 * required for store file on the disk and send the collected data by mail
 * the activity will instantiate a menu item for start/stop the log activity
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public abstract class LogFeatureActivity extends AppCompatActivity {
    private final static String TAG = LogFeatureActivity.class.getCanonicalName();

    /**
     * id used for request the write permission
     */
    private static final int REQUEST_WRITE_ACCESS=1;

    /**
     * object used for log the data
     */
    private static Feature.FeatureLoggerListener mCurrentLogger;

    /**
     * return the list of nodes to log
     * @return node that we want to log
     */
    protected abstract List<Node> getNodesToLog();

    /**
     * return the directory path where we will store the log file if generated
     * @return directory where store the log file
     */
    public static String getLogDirectory(){
        //return Environment.getExternalStorageDirectory()+"/STMicroelectronics/logs";
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/STMicroelectronics/logs";
    }

    /**
     * return the logger that we will use for store the data
     * @return logger used for log the feature data
     */
    protected abstract Feature.FeatureLoggerListener getLogger();

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if (isLogging()) {
            stopLogging(true);
        }
        else {
            super.onBackPressed();
        }
    }

    /**
     * create a menu item for start/stop the logging
     * @param menu menu where add out item
     * @return true if the menu is created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_feature, menu);

        if (mCurrentLogger==null) {
            menu.findItem(R.id.startLog).setVisible(true);
            menu.findItem(R.id.stopLog).setVisible(false);
        } else {
            menu.findItem(R.id.startLog).setVisible(false);
            menu.findItem(R.id.stopLog).setVisible(true);
        }//if-else

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * check it we have the permission to write data on the sd
     * @return true if we have it, false if we ask for it
     */
    public boolean checkWriteSDPermission(final int requestCode){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            return true;
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    final View viewRoot = ((ViewGroup) this
                            .findViewById(android.R.id.content)).getChildAt(0);
                    Snackbar.make(viewRoot, R.string.WriteSDRationale,
                                    Snackbar.LENGTH_INDEFINITE)
                            .setAction(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(LogFeatureActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            requestCode);
                                }//onClick
                            }).show();
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            requestCode);
                }//if-else
                return false;
            } else
                return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //we have the permission try start logging
                    for(Node n : getNodesToLog()) {
                        registerLoggerListener(n);
                    }
                } else {
                    View rootView = getWindow().getDecorView().getRootView();
                    if(rootView!=null)
                        Snackbar.make(rootView, R.string.WriteSDNotGranted,
                                Snackbar.LENGTH_SHORT).show();
                    mCurrentLogger=null;
                    invalidateOptionsMenu();
                }//if-else
                return;
            }//REQUEST_LOCATION_ACCESS
        }//switch
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

    }//onRequestPermissionsResult

    /**
     * set the logger for all the feature in the node
     * @param n node that will be logged
     */
    private void registerLoggerListener(Node n){
        List<Feature> features = n.getFeatures();
        for (Feature f : features) {
            f.addFeatureLoggerListener(mCurrentLogger);
        }//for
    }//registerLoggerListener


    public void startLogging(){
        if (mCurrentLogger!=null)
            stopLogging();
        else {
            mCurrentLogger = getLogger();
            View rootView = getWindow().getDecorView().getRootView();
            if(rootView!=null) {
                Snackbar.make(rootView, R.string.saving_csv_log_message,
                        Snackbar.LENGTH_SHORT).show();
            }
        }
        for (Node n : getNodesToLog()) {
            startLogging(n);
        }
        invalidateOptionsMenu();
    }

    /**
     * stop the previous logger and star a new one
     * @param n node where add the logger
     */
    protected void startLogging(Node n) {

        //if api >23 and we will store on disk
        if(mCurrentLogger instanceof FeatureLogCSVFile || mCurrentLogger instanceof FeatureLogDB) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                registerLoggerListener(n);
            } else {
                if (checkWriteSDPermission(REQUEST_WRITE_ACCESS)) {
                    registerLoggerListener(n);
                }//if
            }
        }else{
            registerLoggerListener(n);
        }
    }

    public void stopLogging(){stopLogging(false);}

    /**
     * remove the logging listener from all the nodes and ask to send a mail with the data
     * @param forceClose close the activity when the mail is send
     */
    protected void stopLogging(boolean forceClose)
    {
        if (mCurrentLogger==null)
            return;
        for(Node n : getNodesToLog()) {
            stopLogging(n);
        }

        final String directoryPath =getLogDirectory();
        File exportFiles[] = null;
        if (mCurrentLogger instanceof FeatureLogCSVFile) {
            ((FeatureLogCSVFile) mCurrentLogger).closeFiles();
            exportFiles = FeatureLogCSVFile.getLogFiles(directoryPath);
        }//if

        if (mCurrentLogger instanceof FeatureLogDB) {
            FeatureLogDB db = (FeatureLogDB) mCurrentLogger;
            exportFiles = db.dumpToFile(directoryPath);
        }

        //if we have something to export
        if(exportFiles!=null && exportFiles.length>0) {
            exportFiles = filterFileEmptyAndSession(exportFiles, ((FeatureLogBase)mCurrentLogger).logSessionPrefix());
            if(exportFiles.length>0) //if we have a non empty

            exportDataByMail(this,getLogDirectory(),exportFiles,forceClose);
            for(File f : exportFiles){
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(f)));
            }//for
        }//if !=null

        mCurrentLogger=null;
        invalidateOptionsMenu();

    }

    /**
     * true if the user is logging some data
     * @return * true if the user is logging some data
     */
    public boolean isLogging(){
        return mCurrentLogger!=null;
    }

    public @Nullable String getLoggingSessionPrefix(){
        if(mCurrentLogger instanceof  FeatureLogBase){
            return ((FeatureLogBase) mCurrentLogger).logSessionPrefix();
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.startLog) {
            startLogging();
            return true;
        }
        if (id == R.id.stopLog) {
            stopLogging();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * remove empty file from a list of file
     * @param allFiles array of file to filter
     * @return files with size >0
     */
    private @NonNull File[] filterFileEmptyAndSession(File[] allFiles, String prefixSession){
        ArrayList<File> temp = new ArrayList<>();
        for(File f: allFiles){
            if(f.length()>0)
                if (prefixSession != null) {
                    if (f.getName().startsWith(prefixSession))
                        temp.add(f);
                }else
                    temp.add(f);
        }
        return temp.toArray(new File[temp.size()]);
    }

    /**
     * stop a logger
     * @param n node where stop the logger
     * <p>
     * in case we are running the {@link FeatureLogDB} logger the data will be
     * dumped on a csv files
     * </p>
     */
    protected void stopLogging(Node n) {
        List<Feature> features = n.getFeatures();
        for (Feature f : features) {
            f.removeFeatureLoggerListener(mCurrentLogger);
        }//for
    }

    private final static String EMAIL_TITLE = "BlueSTSDK Log Data";


    /**
     * Compose and send a mail with all the log files as attached
     * @param ctx activity used for send the mail intent
     * @param folder folder where the log file are stored
     * @param logFiles log files to send as attached
     */
    private static void sendLogByMail(Context ctx, String folder, File[] logFiles) {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("message/rfc822");

        String strAppName = "BlueSTSDK";
        String strAppPackage = "com.st.BlueSTSDK";
        try {
            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            strAppName = ctx.getPackageManager().getApplicationLabel(pInfo.applicationInfo).toString();
            strAppPackage = pInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }//try-catch

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[" +strAppName + "] "+EMAIL_TITLE);

        Log.d(TAG, strAppName);
        try {
            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            strAppName += " " + pInfo.versionName + " (" +pInfo.versionCode+")";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }//try-catch

        String strEmail = "\nThis is an auto generated message from the " + strAppName +" application running on:";
        strEmail += "\n\tDevice manufacturer: " + Build.MANUFACTURER.toUpperCase();
        strEmail += "\n\tDevice model: "+ Build.MODEL;
        strEmail += "\n\tAndroid version: " + Build.VERSION.RELEASE + " ("+Build.VERSION.SDK_INT + ")";
        strEmail += ".\n\nIn attach the log data files available from " + folder + ".\n";

        emailIntent.putExtra(Intent.EXTRA_TEXT, strEmail);

        ArrayList<Uri> uris = new ArrayList<>();
         //convert from paths to Android friendly Parcelable Uri's
        for (File file : logFiles) {
            Uri temp = FileProvider.getUriForFile(ctx,strAppPackage+".logFileProvider",file);
            uris.add(temp);
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //ctx.startActivity(Intent.createChooser(emailIntent, "Sent mail"));
        ctx.startActivity(emailIntent);
    }//sendLogByMail


    /**
     * if the attachment has a size bigger than this
     */
    private static final int WARNING_MAIL_SIZE_BYTE  = 5* (1024*1024);

    /**
     * Show a dialog for ask to the used to send a mail with the logs
     * @param a activity used for send the mail intent
     * @param folder folder where the log file are stored
     * @param logs log files to send as attached
     * @param forceClose close the activity when the dialog is dismiss
     */
    public static  void exportDataByMail(final Activity a, final String folder, final File[] logs,
                                         final boolean forceClose) {
        long totalSize = 0;
        for (File f : logs)
                totalSize += f.length();

        String message;
        if (totalSize < WARNING_MAIL_SIZE_BYTE)
            message = a.getString(R.string.askExprotByMailMessage);
        else
            message = a.getString(R.string.askExprotByMailMessageExtraSize, totalSize);

        a.runOnUiThread(()-> {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(a);
            builder.setTitle(forceClose?R.string.askExprotByMailForceStop:R.string.askExprotByMailTitle)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            sendLogByMail(a,folder,logs);
                            if (forceClose)
                                a.onBackPressed();
                        }//onClick
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            if (forceClose)
                                a.onBackPressed();

                        }
                    });
            // Create the AlertDialog object and return it
            builder.create().show();
        });
    }//exportDataByMail

}
