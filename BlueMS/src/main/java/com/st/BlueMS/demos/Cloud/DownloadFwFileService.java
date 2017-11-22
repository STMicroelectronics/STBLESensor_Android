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

package com.st.BlueMS.demos.Cloud;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.st.BlueMS.R;

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
     * display the notification that inform the user that an update is available, when the action is
     * pressed the service is lanced
     * @param c context
     * @param firmwareRemoteLocation location to download
     */
    public static void displayAvailableFwNotification(Context c, Uri firmwareRemoteLocation){
        Context appContext = c.getApplicationContext();
        PendingIntent startDwService = PendingIntent.getService(appContext,0,
                downloadFwFile(appContext,firmwareRemoteLocation),0);

        NotificationCompat.Action download = new NotificationCompat.Action.Builder(
                R.drawable.ic_fw_upgrade_check_24dp,
                appContext.getString(R.string.cloudLog_fwUpgrade_startUpgrade),
                startDwService)
                .build();

        String notificationDesc = appContext.getString(R.string.cloudLog_fwUpgrade_notification_desc,
                firmwareRemoteLocation.getLastPathSegment());

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(appContext)
                .setContentTitle(appContext.getString(R.string.cloudLog_fwUpgrade_notification_title))
                .setContentText(notificationDesc)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setColor(ContextCompat.getColor(appContext, com.st.BlueSTSDK.gui.R.color.colorPrimary))

                .addAction(download)
                .setSmallIcon(R.drawable.ic_cloud_fw_upgrade_24dp);

        NotificationManagerCompat.from(appContext).notify(FW_UPGRADE_NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * remove the notificaiton that ask to upgrade the fw
     * @param c
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

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
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

    /**
     * remove the notification and start the download manager
     * @param file
     */
    private void handleDownloadFwFileAction(Uri file) {
        removeNotification(this);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request dwRequest = new DownloadManager.Request(file);
        dwRequest.setTitle(file.getLastPathSegment());
        dwRequest.setDescription(getString(R.string.cloudLog_fw_upgrade_download_desc));
        dwRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
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

            if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
                if(ourDownloadIsComplete(intent.getExtras(),mDownloadRequestId)){
                    //tell to the app that the fw download is complete
                    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri file = manager.getUriForDownloadedFile(mDownloadRequestId);

                    LocalBroadcastManager.getInstance(context).sendBroadcast(getCompleteDownloadIntent(file));

                    context.unregisterReceiver(this);
                }
            }
            //download canceled
            if(intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)){
                context.unregisterReceiver(this);
            }
        }

        private static boolean ourDownloadIsComplete(Bundle extra, long downloadId){
            //the default value is != from what we are waiting -> handle the case when is not present
            return extra.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId +1)== downloadId;
        }

    }


}

