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
package com.st.BlueSTSDK.gui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Activity that show a console with the stdout and stderr message from the board and permit to
 * send string to the stdin
 */
public class DebugConsoleActivity extends ActivityWithNode {

    private final static String PREFERENCE_AUTO_SCROLL_KEY = "prefDebugActivityAutoScroll";

    private final static String WESU_HELP_MESSAGE="?\n";
    private final static String NUCLEO_HELP_MESSAGE="help";

    private enum ConsoleType{
        OUTPUT,
        INPUT,
        ERROR;

        public @ColorRes int getColorID(){
            switch (this){
                case ERROR:
                    return  R.color.debugConsole_errorMsg;
                case OUTPUT:
                    return R.color.debugConsole_outMsg;
                case INPUT:
                    return R.color.debugConsole_inMsg;
                default:
                    return R.color.debugConsole_inMsg;
            }
        }

        public String getPrefix(){
            String prefix =">";
            if (this != INPUT)
                prefix = "<";
            return  prefix;
        }
    }
    ConsoleType mTargetConsole = null;
    private static Date mLastMessageReceived = new Date();
    private static final long PAUSE_DETECTION_TIME_MS = 100; //ms

    private static Date mLastMessageSending = new Date();
    private static final long SENDING_TIME_OUT_MS = 1000; //ms

    /**
     * text view where we will dump the console out
     */
    private TextView mConsole;

    /**
     * scroll view attached to the text view, we use it for keep visualized the last line
     */
    private ScrollView mConsoleView;

    /**
     * text edit where the user will write its commands
     */
    private EditText mUserInput;


    /** object that will send/receive commands from the node */
    private Debug mDebugService;
    private Debug.DebugOutputListener mDebugListener = new UpdateConsole();
    private String mToSent=null;
    private int mNextPartToSent = -1;



    private boolean mAutoScroll = true;

    /** create an intent for start the activity that will log the information from the node
     *
     * @param c context used for create the intent
     * @param node note that will be used by the activity
     * @return intent for start this activity
     */
    public static Intent getStartIntent(Context c,@NonNull Node node){
        return getStartIntent(c,DebugConsoleActivity.class,node,true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_debug_console, menu);

        menu.findItem(R.id.action_device_auto_scroll).setTitle(mAutoScroll ? R.string.debugConsole_deviceAutoScroll : R.string.debugConsole_deviceAutoScrollOff);
        menu.findItem(R.id.action_device_auto_scroll).setIcon(mAutoScroll ? R.drawable.ic_auto_scroll_down_24dp :R.drawable.ic_auto_scroll_off_24dp);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_debug_console);
        mConsoleView = findViewById(R.id.consoleView);
        mConsole = findViewById(R.id.deviceConsole);
        mUserInput =  findViewById(R.id.inputText);

        /*
         * when the user click on send we remove the text that it send
         */
        //onEditorAction
        mUserInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {

                String toSend = v.getText().toString();
                v.setText(""); //reset the string
                if (!toSend.isEmpty())
                    if (!sendMessage(toSend + "\n")) {
                        mToSent = null;
                        mNextPartToSent = -1;
                        //Notify.message(DebugConsoleActivity.this, "Error on write message");
                    }

                handled = true;
            }
            return handled;
        });

    }

    /**
     * write a message to the stdIn of the debug console prepare the string to sent and check
     * if there is a current message in queue to be sent
     *
     * @param message message to send
     * @return false if there is a message current sending else true
     */
    private boolean sendMessage(String message) {
        boolean bRet=false;
        Date  now = new Date();
        //not message already sending or time out
        if ((mToSent == null) || (now.getTime() - mLastMessageSending.getTime() > SENDING_TIME_OUT_MS)) {
            bRet = true;
            resetMessageToSend();
            if (message != null && !message.isEmpty()) {
                mToSent = message;
                mNextPartToSent = 0;
                bRet = writeNextMessage();
            }
        }
        return bRet;
    }

    private void resetMessageToSend(){
        mToSent = null;
        mNextPartToSent = -1;
    }

    /**
     * write the next part of the current message to the input characteristic
     */
    private boolean writeNextMessage() {

        int startIndex = mNextPartToSent * Debug.DEFAULT_MAX_STRING_SIZE_TO_SENT;

        if (mToSent != null && (startIndex < mToSent.length())) {
            int endIndex = Math.min(mToSent.length(), (mNextPartToSent + 1) * Debug.DEFAULT_MAX_STRING_SIZE_TO_SENT);
            mNextPartToSent++;

            String partToSent = mToSent.substring(startIndex, endIndex);
            if(mDebugService!=null)
                return (mDebugService.write(partToSent) == partToSent.length());
        }//else
        return false;
    }

    /**
     * when the node connected check the presence of the debug service and enable the gui if it
     * present otherwise it will show an error message
     * @param debugService debug service return from the node, null if not present
     */
    private void setUpConsoleService(Debug debugService){
        mDebugService=debugService;
        if(mDebugService!=null) {
            mDebugService.addDebugOutputListener(mDebugListener);
            DebugConsoleActivity.this.runOnUiThread(() -> mUserInput.setEnabled(true));
        }/*else{
            Notify.message(DebugConsoleActivity.this, R.string.DebugNotAvailable);
        }*/
        invalidateOptionsMenu();
    }


    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAutoScroll = preferences.getBoolean(PREFERENCE_AUTO_SCROLL_KEY, true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Node node = getNode();
        if(node!=null && node.isConnected()) {
            setUpConsoleService(node.getDebug());
        }
    }

    @Override
    public void onDestroy(){
        if(mDebugService!=null)
            mDebugService.removeDebugOutputListener(mDebugListener);

        super.onDestroy();
    }


     /**
     * call when the user press the back button on the menu bar, we are leaving this activity so
     * we disconnect the node
     * @param item  menu item clicked
     * @return true if the item is handle by this function
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId =item.getItemId();

        if (itemId == R.id.action_device_auto_scroll ){
            setAutoScrollPolicy(!mAutoScroll);
            return true;
        }
        if (itemId == R.id.action_device_clear_debug){
            mConsole.setText(""); //clear
            return true;
        }
        if(itemId == R.id.action_send_help){
            sendHelpMessage();
            return true;
        }
        //else

        return super.onOptionsItemSelected(item);
    }

    /**
     *help a message for request the list of command available
     */
    private void sendHelpMessage() {
        String msg = getHelpMessage(getNode());
        if(msg==null)
            return;
        //else
        sendMessage(msg);
    }

    /**
     * return the command that return the help information
     * @param node node where the help message will be sent
     * @return null if the command is not available/known otherwise the command for display the
     * help information
     */
    protected @Nullable String getHelpMessage(Node node) {
        switch (node.getType()){
            case STEVAL_WESU1:
                return WESU_HELP_MESSAGE;
            case GENERIC:
                return null;
            default:
                return NUCLEO_HELP_MESSAGE;
        }
    }

    private void setAutoScrollPolicy(boolean enableAutoScroll){
        mAutoScroll = enableAutoScroll;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit()
            .putBoolean(PREFERENCE_AUTO_SCROLL_KEY, mAutoScroll)
            .apply();
        invalidateOptionsMenu();
    }

    /**
     * class that receive the debug message and post it on the activity main thread a runnable
     * for update the textview that contains the massage.
     * <p>
     *     The different message will be show with different color
     * </p>
     */
    private class UpdateConsole implements Debug.DebugOutputListener{
        final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyMMdd HH:mm:ss.SSS",
                Locale.getDefault());

        private String appendDateTime(boolean append){
            String str = "";
            Date  now = new Date();
            if (append || (now.getTime() - mLastMessageReceived.getTime() > PAUSE_DETECTION_TIME_MS)) {
                str +="\n[" + DATE_FORMAT.format(now) + mTargetConsole.getPrefix() +"]";
            }
            mLastMessageReceived = now;

            return str;
        }

        private void appendMessage(String message, ConsoleType std) {
            boolean forceAppendPrefix = (mTargetConsole != std);
            mTargetConsole = std;

            final SpannableStringBuilder displayText = new SpannableStringBuilder();

            displayText.append(appendDateTime(forceAppendPrefix));
            displayText.append(message);

            displayText.setSpan(new ForegroundColorSpan(getResources().getColor(mTargetConsole.getColorID())), 0, displayText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            DebugConsoleActivity.this.runOnUiThread(() -> {
                mConsole.append(displayText);
                if (mAutoScroll)
                    //mConsoleView.fullScroll(View.FOCUS_DOWN);
                    mConsoleView.post(() -> mConsoleView.fullScroll(View.FOCUS_DOWN));
                mUserInput.requestFocus();
            });

        }

        @Override
        public void onStdOutReceived(Debug debug, final String message) {
            appendMessage(message, ConsoleType.OUTPUT);
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {
            appendMessage(message, ConsoleType.ERROR);
        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            appendMessage(message, ConsoleType.INPUT);

            if (!writeResult) {
                resetMessageToSend();
            } else {
                if (!writeNextMessage()) {
                    resetMessageToSend();
                }//if
            }//if
        }//onStdInSent

    }//UpdateConsole
}//DebugConsoleActivity