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

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.util.DialogUtil;

/**
 * Display a progress dialog to show the upload status
 */
public class FwUpgradeServiceActionReceiver extends BroadcastReceiver{


    private ProgressDialog mUploadFileProgressDialog;
    private ProgressDialog mFormattingProgressDialog;

    private Context mContext;


    FwUpgradeServiceActionReceiver(Context c){
        mContext=c;
    }

    /**
     * hide all the dialog
     */
    public void releaseDialog(){
        DialogUtil.releaseDialog(mUploadFileProgressDialog);
        DialogUtil.releaseDialog(mFormattingProgressDialog);
        mUploadFileProgressDialog = null;
        mFormattingProgressDialog = null;
    }

    private ProgressDialog createUpgradeProgressDialog(Context c){
        ProgressDialog dialog = new ProgressDialog(c);
        dialog.setTitle(R.string.fwUpgrade_uploading);
        dialog.setCancelable(false);
        dialog.setProgressNumberFormat(c.getString(R.string.fwUpgrade_upgradeNumberFormat));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        return dialog;
    }

    private ProgressDialog createFormatProgressDialog(Context c){
        ProgressDialog dialog = new ProgressDialog(c);
        dialog.setTitle(R.string.fwUpgrade_formatting);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if(action==null)
            return;
        if(action.equals(FwUpgradeService.FW_UPLOAD_STARTED_ACTION))
            onUploadStarted();
        if(action.equals(FwUpgradeService.FW_UPLOAD_STATUS_UPGRADE_ACTION)){
            long total = intent.getLongExtra(FwUpgradeService
                    .FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE_EXTRA, Integer.MAX_VALUE);
            long upload = intent.getLongExtra(FwUpgradeService
                    .FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE_EXTRA,0);
            onUpgradeUploadStatus(upload,total);
        }else if(action.equals(FwUpgradeService.FW_UPLOAD_FINISHED_ACTION)){
            float timeS = intent.getFloatExtra(FwUpgradeService
                    .FW_UPLOAD_FINISHED_TIME_S_EXTRA,0.0f);
            onUploadFinished(timeS);
        }else if(action.equals(FwUpgradeService.FW_UPLOAD_ERROR_ACTION)){
            String message = intent.getStringExtra(FwUpgradeService
                    .FW_UPLOAD_ERROR_MESSAGE_EXTRA);
            onUploadError(message);
        }
    }

    protected void onUploadStarted() {
        mFormattingProgressDialog = createFormatProgressDialog(mContext);
        mFormattingProgressDialog.show();
    }

    protected void onUpgradeUploadStatus(long uploadBytes, long totalBytes){
        if(mUploadFileProgressDialog==null){
            mUploadFileProgressDialog= createUpgradeProgressDialog(mContext);
            mUploadFileProgressDialog.setMax((int)totalBytes);
            DialogUtil.releaseDialog(mFormattingProgressDialog);
            mFormattingProgressDialog=null;
        }
        mUploadFileProgressDialog.show();
        mUploadFileProgressDialog.setProgress((int)uploadBytes);
    }

    protected void onUploadFinished(float timeS){
        releaseDialog();
    }

    protected void onUploadError(String msg){
        releaseDialog();
    }
}

