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
package com.st.BlueSTSDK.gui.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.widget.Toast;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.R;

/**
 * Progress dialog that is displayed while the mobile is connecting with the node
 * you can directly use this class as nodeStateListener for aromatically display/dismiss the dialog
 * when the node state change.
 */
@Deprecated
public class ConnectProgressDialog extends ProgressDialog implements Node.NodeStateListener {

    //main thread where run the command for change the gui
    private Handler mMainThread;

    private String mNodeName;

    /**
     * create the dialog
     * @param ctx context where the dialog is displayed
     * @param nodeName node name
     */
    public ConnectProgressDialog(Context ctx, String nodeName) {
        super(ctx,ProgressDialog.STYLE_SPINNER);
        setTitle(R.string.progressDialogConnTitle);
        setNodeName(nodeName);
        mMainThread = new Handler(ctx.getMainLooper());
    }

    /**
     * change the dialog state in function of the node state:
     * show the dialog when the node is connecting, dismiss the dialog when the node is connected
     * display a toast message when we lost the connection
     * @param node note that change its status
     * @param newState new node status
     * @param prevState previous node status
     */
    @Override
    public void onStateChange(@NonNull final Node node, @NonNull final Node.State newState, @NonNull Node.State prevState) {
        if(mMainThread!=null){
            mMainThread.post(() -> setState(newState,prevState));
        }
    }

    public void setNodeName(String name){
        mNodeName=name;
        setMessage(String.format(getContext().getString(R.string.progressDialogConnMsg), mNodeName));
    }

    public void setState(Node.State currentState, Node.State prevState){
        switch (currentState){
            case Init:
            case Idle:
            case Connected:
            case Disconnecting:
                if(isShowing())
                    dismiss();
                return;
            case Connecting:
            case Unreachable:
                if(!isShowing())
                    show();
                return;
            case Lost:
            case Dead:
                if(prevState != Node.State.Idle && prevState!= Node.State.Init) {
                    final String msg = getErrorString(currentState, mNodeName);
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
        }
    }

    private String getErrorString(Node.State state, String nodeName){
        Context ctx = getContext();
        switch (state) {
            case Dead:
                return String.format(ctx.getString(R.string.progressDialogConnMsgDeadNodeError),
                        nodeName);
            case Unreachable:
                return String.format(ctx.getString(R.string.progressDialogConnMsgUnreachableNodeError),
                        nodeName);
            case Lost:
            default:
                return String.format(ctx.getString(R.string
                                .progressDialogConnMsgLostNodeError),nodeName);
        }//switch
    }

}
