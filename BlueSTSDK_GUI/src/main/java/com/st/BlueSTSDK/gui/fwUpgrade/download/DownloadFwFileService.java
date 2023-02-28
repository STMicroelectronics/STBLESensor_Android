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

package com.st.BlueSTSDK.gui.fwUpgrade.download;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeService;


/**
 * Service that request to the download manager to download the fw file
 * the service will throw an local broadcast with the downloaded file location when the download ends
 */
public class DownloadFwFileService extends IntentService {
    private static final int FW_UPGRADE_NOTIFICATION_ID = DownloadFwFileService.class.hashCode();

    private static final String ACTION_DOWNLOAD = DownloadFwFileService.class.getCanonicalName() + ".ACTION_DOWNLOAD";

    private static final String EXTRA_FW_LOCATION = DownloadFwFileService.class.getCanonicalName() + ".EXTRA_FW_LOCATION";

    public static final String ACTION_DOWNLOAD_COMPLETE = DownloadFwFileService.class.getCanonicalName() + ".ACTION_DOWNLOAD_COMPLETE";
    public static final String EXTRA_DOWNLOAD_LOCATION = DownloadFwFileService.class.getCanonicalName() + ".EXTRA_DOWNLOAD_LOCATION";

    /**
     * display the notification that informs the user that an update is available, when the action is
     * pressed the service is executed
     * @param c context
     * @param firmwareRemoteLocation location to download
     */
    public static void displayAvailableFwNotification(Context c, Uri firmwareRemoteLocation){
        Context appContext = c.getApplicationContext();
        NotificationManager notificationManager = (NotificationManager)
                appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager==null)
            return;

        PendingIntent startDwService = PendingIntent.getService(appContext,0,
                downloadFwFile(appContext,firmwareRemoteLocation),PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Action download = new NotificationCompat.Action.Builder(
                R.drawable.ic_fw_upgrade_check_24dp,
                appContext.getString(R.string.cloudLog_fwUpgrade_startUpgrade),
                startDwService)
                .build();

        String notificationDesc = appContext.getString(R.string.cloudLog_fwUpgrade_notification_desc,
                firmwareRemoteLocation.getLastPathSegment());

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(appContext,
                FwUpgradeService.createNotificationChannel(appContext,notificationManager))
                .setContentTitle(appContext.getString(R.string.cloudLog_fwUpgrade_notification_title))
                .setContentText(notificationDesc)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setColor(ContextCompat.getColor(appContext, com.st.BlueSTSDK.gui.R.color.colorPrimary))

                .addAction(download)
                .setSmallIcon(R.drawable.ic_cloud_fw_upgrade_24dp);

        notificationManager.notify(FW_UPGRADE_NOTIFICATION_ID, notificationBuilder.build());
        Log.i("check","notify Manager");
    }


    /**
     * Display one notification where there is one error during the FW download process
     * @param c Context
     */
    public static void displayFwDownloadErrorNotification(Context c){
        Context appContext = c.getApplicationContext();
        NotificationManager notificationManager = (NotificationManager)
                appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager==null)
            return;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(appContext,
                FwUpgradeService.createNotificationChannel(appContext,notificationManager))
                .setContentTitle("Problem Downloading Fw")
                .setContentText("Try later")
                .setAutoCancel(false)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(appContext, R.color.debugConsole_errorMsg))
                .setSmallIcon(R.drawable.ic_error_24);

        notificationManager.notify(FW_UPGRADE_NOTIFICATION_ID, notificationBuilder.build());
    }

    public static DialogFragment buildAvailableFwNotificationDialog(Context c, Uri firmwareRemoteLocation, boolean forceFwUpgrade){
        DownloadFwFileService.removeNotification(c);
        return DownloadNewFwDialog.buildDialogForUri(firmwareRemoteLocation,forceFwUpgrade);
    }

    /**
     * remove the notificaiton that ask to upgrade the fw
     * @param c context used to show the notification
     */
    private static void removeNotification(Context c){
        NotificationManagerCompat.from(c.getApplicationContext()).cancel(FW_UPGRADE_NOTIFICATION_ID);
    }

    public DownloadFwFileService() {
        super("DownloadFwFileService");
    }

    /**
     * create the intent to start the service and download the file
     * @param context context used to create the intent
     * @param location location of the file to download
     * @return intent to start the service
     */
    public static Intent downloadFwFile(Context context, Uri location) {
        Intent intent = new Intent(context, DownloadFwFileService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_FW_LOCATION, location);
        return intent;
    }


    public static void startDownloadFwFile(Context context, Uri location) {
        context.startService(downloadFwFile(context,location));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                final Uri location = intent.getParcelableExtra(EXTRA_FW_LOCATION);
                handleDownloadFwFileAction(location);
            }
        }
    }

    private static boolean canBeDownloaded(Uri file){
        String scheme = file.getScheme();
        if(scheme == null){
            return false;
        }
        scheme = scheme.toLowerCase();
        return scheme.equals("http") || scheme.equals("https");
    }

    /**
     * remove the notification and start the download manager
     * @param file file that is going to be downloaded
     */
    private void handleDownloadFwFileAction(Uri file) {
        removeNotification(this);
        if(!canBeDownloaded(file)){
            return;
        }
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if(manager==null)
            return;
        DownloadManager.Request dwRequest = new DownloadManager.Request(file);
        dwRequest.setTitle(file.getLastPathSegment());
        dwRequest.setDescription(getString(R.string.cloudLog_fw_upgrade_download_desc));
        dwRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        //dwRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        long requestId = manager.enqueue(dwRequest);
        //register the broadcast receiver to know when the file is downloaded
        getApplicationContext().registerReceiver(new DownloadFinishedBroadcastReceiver(requestId),
                DownloadFinishedBroadcastReceiver.getIntentFilter());
    }



    private static Intent getCompleteDownloadIntent(Uri localFile){
        Intent action = new Intent(ACTION_DOWNLOAD_COMPLETE);
        action.putExtra(EXTRA_DOWNLOAD_LOCATION,localFile);
        return action;
    }


    /**
     * class that wait the broadcast throw when the file our file downloads complete
     */
    private static class DownloadFinishedBroadcastReceiver extends BroadcastReceiver{

        public static IntentFilter getIntentFilter(){
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
            return filter;
        }

        private long mDownloadRequestId;

        public DownloadFinishedBroadcastReceiver(long downloadId){
            mDownloadRequestId = downloadId;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle extra = intent.getExtras();
            if(action!=null && action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
                if(extra!=null && ourDownloadIsComplete(extra,mDownloadRequestId)){
                    Boolean completedSuccessful=false;
                    //tell to the app that the fw download is complete
                    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    if(manager==null)
                        return;

                    {
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(mDownloadRequestId);
                        Cursor c = manager.query(query);

                        if (c != null) {
                            if (c.moveToFirst()) {
                                int statusColumnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                int downloadManagerDownloadStatus = c.getInt(statusColumnIndex);

                                if (DownloadManager.STATUS_SUCCESSFUL == downloadManagerDownloadStatus) {
                                    completedSuccessful=true;
                                }
                                else if (DownloadManager.STATUS_FAILED == downloadManagerDownloadStatus) {
                                    completedSuccessful=false;
                                }
                            }
                            c.close();
                        }
                    }

                    // Only if everything is ok
                    if(completedSuccessful) {
                        Uri file = manager.getUriForDownloadedFile(mDownloadRequestId);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(getCompleteDownloadIntent(file));
                    } else {
                        displayFwDownloadErrorNotification(context);
                    }

                    context.unregisterReceiver(this);
                }
            }
            //download canceled
            if(action!=null && action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)){
                context.unregisterReceiver(this);
            }
        }

        private static boolean ourDownloadIsComplete(Bundle extra, long downloadId){
            //the default value is != from what we are waiting -> handle the case when is not present
            return extra.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId +1)== downloadId;
        }

    }


}

