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
package com.st.BlueSTSDK.gui.fwUpgrade;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.FwFileDescriptor;

/**
 * Service that will upload the file as a background task, it will notify to the user the progres
 * using a LocalBroadcast message.
 */
public class FwUpgradeService extends IntentService implements FwUpgradeConsole.FwUpgradeCallback {

    /**
     * action send when the service start the uploading
     */
    public static final String FW_UPLOAD_STARTED_ACTION = FwUpgradeService.class
            .getCanonicalName() + "action.FW_UPLOAD_STARTED_ACTION";

    /**
     * action send when the service notify a progress in the uploading
     */
    public static final String FW_UPLOAD_STATUS_UPGRADE_ACTION = FwUpgradeService.class
            .getCanonicalName() + "action.FW_UPLOAD_STATUS_UPGRADE";
    /**
     * key used in the upload status intent for store the file size
     */
    public static final String FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE_EXTRA = FwUpgradeService.class
            .getCanonicalName() + "extra.FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE";
    /**
     * key used in the upload status intent for store the number of bytes stored
     */
    public static final String FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE_EXTRA = FwUpgradeService.class
            .getCanonicalName() + "extra.FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE";

    /**
     * action send when the upload end correctly
     */
    public static final String FW_UPLOAD_FINISHED_ACTION = FwUpgradeService.class
            .getCanonicalName() + "action.FW_UPLOAD_FINISHED_ACTION";
    /**
     * key used in the upload finished intent for store the seconds needed for upload the file
     */
    public static final String FW_UPLOAD_FINISHED_TIME_S_EXTRA = FwUpgradeService.class
            .getCanonicalName() + "extra.FW_UPLOAD_FINISHED_TIME_S";

    /**
     * action send when an error happen
     */
    public static final String FW_UPLOAD_ERROR_ACTION = FwUpgradeService.class
            .getCanonicalName() + "action.FW_UPLOAD_ERROR_ACTION";
    /**
     * key used in the error intent for store the error message
     */
    public static final String FW_UPLOAD_ERROR_MESSAGE_EXTRA = FwUpgradeService.class
            .getCanonicalName() + "extra.FW_UPLOAD_ERROR_MESSAGE";

    /**
     * id used for display the notification from this service
     */
    private static final int NOTIFICATION_ID = FwUpgradeService.class.hashCode();

    private static final String CONNECTION_NOTIFICATION_CHANNEL =
            FwUpgradeService.class.getCanonicalName()+"FwUpgradeNotification";
    /**
     * action used in the intent for create this service
     */
    private static final String UPLOAD_FW =
            FwUpgradeService.class.getCanonicalName() + "action.uploadFw";
    /**
     * key used in the intent for create this service, to store the file to upload
     */
    private static final String FW_FILE_URI =
            FwUpgradeService.class.getCanonicalName() + "extra.fwUri";

    /**
     * key used in the intent for create this service, to store the node where upload the file
     */
    private static final String NODE_TAG =
            FwUpgradeService.class.getCanonicalName() + "extra.nodeTag";

    /**
     * key used in the intent for create this service, to store the node where upload the file
     */
    private static final String FW_VERSION_TAG =
            FwUpgradeService.class.getCanonicalName() + "extra.currentFwVersion";

    /**
     * key used in the intent for create this service, to store the node where upload the file
     */
    private static final String FW_ADDRESS_DESTINATION =
            FwUpgradeService.class.getCanonicalName() + "extra.fwDestinationAddresss";

    private static final String FW_NB_SECTORS_TO_ERASE =
            FwUpgradeService.class.getCanonicalName() + "extra.fwNbSectorsToErase";

    /**
     * key used in the intent for create this service, to store the file to upload
     */
    private static final String FW_TYPE =
            FwUpgradeService.class.getCanonicalName() + "extra.fwType";


    private static Context ctx;


    /**
     * object used for send the broadcast message
     */
    private LocalBroadcastManager mBroadcastManager;

    /**
     * object used for publish the upload notification
     */
    private NotificationManager mNotificationManager;

    /**
     * object for create the notification to display
     */
    private NotificationCompat.Builder mNotification;

    /**
     * timestamp when the upload start
     */
    private long mStartUploadTime = -1;
    /**
     * size of the file that the service will upload
     */
    private long mFileLength = Long.MAX_VALUE;

    public FwUpgradeService() {
        super(FwUpgradeService.class.getSimpleName());
    }

    /**
     * convert an error code into a string
     * @param errorCode code to convert
     * @return string to display when the error happen
     */
    private String getErrorMessage(@UpgradeErrorType int errorCode) {
        switch (errorCode) {
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_CORRUPTED_FILE:
                return getString(R.string.fwUpgrade_error_corrupted_file);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_TRANSMISSION:
                return getString(R.string.fwUpgrade_error_transmission);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_WRONG_SDK_VERSION:
                return getString(R.string.fwUpgrade_error_wrong_sdk);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_WRONG_SDK_VERSION_OR_ERROR_TRANSMISSION:
                return getString(R.string.fwUpgrade_error_wrong_sdk_or_error_transmission);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_UNKNOWN:
                return getString(R.string.fwUpgrade_error_unknown);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_INVALID_FW_FILE:
                return getString(R.string.fwUpgrade_error_invalid_file);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_NOT_READY_TO_RECEIVE:
                return "File Upload Indication characteristic with value 0x03 received from the board";
        }
        return "";
    }

    /**
     * start this service
     * @param context context used for start the service
     * @param node node where upload the file
     * @param fwFile file to upload
     */
    public static void startUploadService(@NonNull Context context, @NonNull  Node node, @NonNull  Uri fwFile,
                                          @FirmwareType int fwType, @Nullable Long address, @Nullable FwVersion currentVersion) {
        Intent intent = new Intent(context, FwUpgradeService.class);
        ctx = context;
        intent.setAction(UPLOAD_FW);
        intent.putExtra(FW_FILE_URI, fwFile);
        intent.putExtra(NODE_TAG, node.getTag());
        intent.putExtra(FW_TYPE,fwType);
        if(currentVersion!=null) {
            intent.putExtra(FW_VERSION_TAG, currentVersion);
        }
        if(address!=null){
            intent.putExtra(FW_ADDRESS_DESTINATION,address);
        }
        context.startService(intent);
    }

    /**
     * create an intent filter that will select all the action send by this service
     * @return IntentFilter for select all the intent send by this service
     */
    public static IntentFilter getServiceActionFilter() {
        IntentFilter filter = new IntentFilter(FW_UPLOAD_FINISHED_ACTION);
        filter.addAction(FW_UPLOAD_STATUS_UPGRADE_ACTION);
        filter.addAction(FW_UPLOAD_STARTED_ACTION);
        filter.addAction(FW_UPLOAD_ERROR_ACTION);
        return filter;
    }

    /**
     * create an action intent for notify that the upload is finished
     * @param durationS time spent for upload the file
     * @return intent for notify that the upload finished
     */
    private static Intent getFwUpgradeCompleteIntent(float durationS) {
        return new Intent(FW_UPLOAD_FINISHED_ACTION)
                .putExtra(FW_UPLOAD_FINISHED_TIME_S_EXTRA, durationS);
    }

    private static Intent getFwUpgradeErrorIntent(String errorMessage) {
        return new Intent(FW_UPLOAD_ERROR_ACTION)
                .putExtra(FW_UPLOAD_ERROR_MESSAGE_EXTRA, errorMessage);
    }

    private static Intent getFwUpgradeStatusIntent(long uploadBytes, long totalBytes) {
        return new Intent(FW_UPLOAD_STATUS_UPGRADE_ACTION)
                .putExtra(FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE_EXTRA, totalBytes)
                .putExtra(FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE_EXTRA, uploadBytes);
    }

    private static Intent getFwUpgradeStartIntent() {
        return new Intent(FW_UPLOAD_STARTED_ACTION);
    }

    /**
     * Create a notification channel to use for the fw upgrade
     * @param manager manager where create the notification channel
     * @return id for the notification channel
     */
    public static String createNotificationChannel(Context ctx,NotificationManager manager){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String desc = ctx.getString(R.string.fwUpgrade_channelName);
            String name = ctx.getString(R.string.fwUpgrade_channelDesc);
            NotificationChannel channel = new NotificationChannel(CONNECTION_NOTIFICATION_CHANNEL,
                    name, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(desc);
            manager.createNotificationChannel(channel);
        }

        return CONNECTION_NOTIFICATION_CHANNEL;
    }

    /**
     * crate the notification for display the upload status
     * @return object that will build the notification
     * @param notificationManager object to use to display the notification
     */
    private NotificationCompat.Builder buildUploadNotification(NotificationManager notificationManager) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                createNotificationChannel(this,notificationManager))
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)

                .setContentTitle(getString(R.string.fwUpgrade_notificationTitle));

        if(Build.VERSION.SDK_INT>=21){
            builder.setSmallIcon(R.drawable.ic_upload_license_white_24dp);
        }else{
            builder.setSmallIcon(android.R.drawable.ic_menu_upload);
        }
        return builder;
    }

    @Override
    public void onLoadFwError(FwUpgradeConsole console, FwFileDescriptor fwFile,
                              @UpgradeErrorType int error) {
        String errorMessage = getErrorMessage(error);
        mBroadcastManager.sendBroadcast(getFwUpgradeErrorIntent(errorMessage));
        mNotification.setContentTitle(getString(R.string.fwUpgrade_errorNotificationTitle))
                .setContentText(errorMessage);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
    }

    @Override
    public void onLoadFwComplete(FwUpgradeConsole console, FwFileDescriptor fwFile) {
        long totalTimeMs = System.currentTimeMillis() - mStartUploadTime;
        float totalTimeS = totalTimeMs / 1000.0f;
        mBroadcastManager.sendBroadcast(getFwUpgradeCompleteIntent(totalTimeS));
        mNotification.setContentTitle(getString(R.string.fwUpgrade_upgradeCompleteNotificationTitle))
                .setContentText(getString(R.string.fwUpgrade_upgradeCompleteNotificationContent));
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
    }

    @Override
    public void onLoadFwProgressUpdate(FwUpgradeConsole console, FwFileDescriptor fwFile, final long remainingBytes) {
        if (mStartUploadTime < 0) { //if is the first time
            mStartUploadTime = System.currentTimeMillis();
            mFileLength = remainingBytes;
        }
        mNotification.setProgress((int) mFileLength, (int) (mFileLength - remainingBytes), false);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
        mBroadcastManager.sendBroadcast(
                getFwUpgradeStatusIntent(mFileLength - remainingBytes, mFileLength));
    }

    private static @Nullable Long extractAddress(Intent intent){
        if(intent.hasExtra(FW_ADDRESS_DESTINATION))
            return intent.getLongExtra(FW_ADDRESS_DESTINATION,0);
        else
            return null;
    }

    /**
     * check that the intent is correct and start the service
     * @param intent inent for start the service
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (UPLOAD_FW.equals(action)) {
                final Uri file = intent.getParcelableExtra(FW_FILE_URI);
                final Node node = getNode(intent.getStringExtra(NODE_TAG));
                final Long address = extractAddress(intent);
                final FwVersion currentVersion = extractCurrentFwVersion(intent);
                final @FirmwareType int fwType = intent.getIntExtra(FW_TYPE,FirmwareType.BOARD_FW);
                handleActionUpload(file, node,fwType,address,currentVersion);
            }
        }
    }

    private @Nullable FwVersion extractCurrentFwVersion(Intent intent) {
        if(intent.hasExtra(FW_VERSION_TAG))
            return intent.getParcelableExtra(FW_VERSION_TAG);
        else
            return null;
    }

    /**
     * extract the node from the manager
     * @param tag node unique name
     * @return tat with that tag
     */
    private Node getNode(String tag) {
        return Manager.getSharedInstance().getNodeWithTag(tag);
    }

    /**
     * Service constructor
     * @param file file to upload
     * @param node node where upload the file
     * @param fwType type of fw to load into the node
     * @param currentVersion
     */
    void handleActionUpload(Uri file, Node node, @FirmwareType int fwType,
                            @Nullable Long address,@Nullable FwVersion currentVersion) {
        if(node == null)
            return;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mNotification = buildUploadNotification(mNotificationManager);
        FwUpgradeConsole console = FwUpgradeConsole.getFwUpgradeConsole(ctx,node,currentVersion);
        if (console != null) {
            console.setLicenseConsoleListener(this);
            mBroadcastManager.sendBroadcast(getFwUpgradeStartIntent());
            mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
            if(address!=null) {
                console.loadFw(fwType, new FwFileDescriptor(getContentResolver(),
                        file), address);
            }else{
                console.loadFw(fwType, new FwFileDescriptor(getContentResolver(),
                        file));
            }
        }//if console
    }


}
