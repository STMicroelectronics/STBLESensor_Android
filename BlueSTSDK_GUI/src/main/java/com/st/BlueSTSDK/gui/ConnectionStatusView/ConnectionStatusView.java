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

package com.st.BlueSTSDK.gui.ConnectionStatusView;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.st.BlueSTSDK.gui.R;

public class ConnectionStatusView extends FrameLayout implements ConnectionStatusContract.View{
    public ConnectionStatusView(Context context) {
        super(context);
        init();
    }

    public ConnectionStatusView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConnectionStatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ConnectionStatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private TextView mStatus;
    private TextView mError;
    private Handler mGuiThread;


    private void init() {
        inflate(getContext(), R.layout.view_connection_status, this);

        mStatus = findViewById(R.id.conStatus_satusText);
        mError = findViewById(R.id.conStatus_errorText);
        //the object is build by the ui thread
        mGuiThread = new Handler(Looper.getMainLooper());
        setVisibility(GONE);
    }

    @Override
    public void showConnecting(final String nodeName) {
        String connectString = String.format(getContext().getString(com.st.BlueSTSDK.gui.R.string.progressDialogConnMsg), nodeName);
        mGuiThread.post(() -> {
            mStatus.setText(connectString);
            mStatus.setVisibility(VISIBLE);
            setVisibility(VISIBLE);
        });

    }

    @Override
    public void showConnected() {
        mGuiThread.post(() -> {
            mError.setVisibility(GONE);
            setVisibility(GONE);
        });
    }

    @Override
    public void showDeadNodeError(String nodeName) {
        final String error = String.format(getContext().getString(com.st.BlueSTSDK.gui.R.string.progressDialogConnMsgDeadNodeError), nodeName);
        mGuiThread.post(() -> showError(error));

    }

    @Override
    public void showLostNodeError(String nodeName) {
        final String error = String.format(getContext().getString(com.st.BlueSTSDK.gui.R.string.progressDialogConnMsgLostNodeError), nodeName);
        mGuiThread.post(() -> showError(error));
    }

    @Override
    public void showUnreachableNodeError(String nodeName) {
        final String error = String.format(getContext().getString(com.st.BlueSTSDK.gui.R.string.progressDialogConnMsgUnreachableNodeError), nodeName);
        mGuiThread.post(() -> showError(error));
    }


    private void showError(String message) {
        mError.setText(message);
        mError.setVisibility(VISIBLE);
        mStatus.setVisibility(GONE);
        setVisibility(VISIBLE);
    }
}
