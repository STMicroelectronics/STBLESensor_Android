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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;
import android.view.MenuItem;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.preferences.LogPreferenceFragment;
import com.st.BlueSTSDK.gui.preferences.PreferenceFragmentWithNode;
import com.st.BlueSTSDK.gui.util.AppCompatPreferenceActivity;

import java.util.List;

/**
 * This Preference settings will keep the node information an pass to the PreferenceFragmentWithNode
 * that will open the connection with the node
 *
 * This class will show the log preference fragment in
 */
public class SettingsActivityWithNode extends AppCompatPreferenceActivity {

    private final static String NODE_TAG = SettingsActivityWithNode.class.getCanonicalName()
            + ".NODE_TAG";

    private final static String KEEP_CONNECTION_OPEN = SettingsActivityWithNode.class.getCanonicalName() +
            ".KEEP_CONNECTION_OPEN";

    private String nodeTag;
    private boolean mKeepConnectionOpen;

    /**
     * create an intent for start the activity that will log the information from the node
     *
     * @param c    context used for create the intent
     * @param node note that will be used by the activity
     * @return intent for start this activity
     */
    protected static Intent getStartIntent(Context c, @NonNull Class<? extends Activity> activity, @NonNull Node
            node, boolean keepConnectionOpen) {
        Intent i = new Intent(c, activity);
        i.putExtra(NODE_TAG, node.getTag());
        i.putExtra(KEEP_CONNECTION_OPEN, keepConnectionOpen);
        return i;
    }

    public static Intent getStartIntent(Context c, @NonNull Node node,boolean keepConnectionOpen) {
        return SettingsActivityWithNode.getStartIntent(c,SettingsActivityWithNode.class,node,keepConnectionOpen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState==null) {
            Intent i = getIntent();
            nodeTag = i.getStringExtra(NODE_TAG);
            mKeepConnectionOpen = i.getBooleanExtra(KEEP_CONNECTION_OPEN,false);
        }else{
            nodeTag = savedInstanceState.getString(NODE_TAG);
            mKeepConnectionOpen= savedInstanceState.getBoolean(KEEP_CONNECTION_OPEN,false);
        }
        // recover the node

    }//onCreate


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NODE_TAG,nodeTag);
        outState.putBoolean(KEEP_CONNECTION_OPEN,mKeepConnectionOpen);
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @CallSuper
    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.pref_headers_log, target);
    }

    @CallSuper
    @Override
    protected  boolean isValidFragment (String fragmentName){
        return fragmentName.equals(LogPreferenceFragment.class.getName()) ||
                super.isValidFragment(fragmentName) ;
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        header.fragmentArguments = PreferenceFragmentWithNode.addStartArgs(header.fragmentArguments,nodeTag,mKeepConnectionOpen);
        super.onHeaderClick(header,position);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
