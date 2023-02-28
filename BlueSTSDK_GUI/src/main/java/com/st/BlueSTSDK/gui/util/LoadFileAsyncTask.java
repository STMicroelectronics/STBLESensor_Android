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

import android.content.res.Resources;
import android.os.AsyncTask;
import android.widget.TextView;

import androidx.annotation.RawRes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

/**
 * read the file content in a background thread
 */
public class LoadFileAsyncTask extends AsyncTask<Integer,Void, CharSequence> {

    private WeakReference<TextView> mTargetView;
    private Resources resources;

    /**
     * load the content of the file into the text view
     * @param res object used to open the resource file
     * @param targetView view where display the file content
     */
    public LoadFileAsyncTask(Resources res , TextView targetView){
        resources=res;
        mTargetView = new WeakReference<>(targetView);
    }

    /**
     * read all the file into a string
     * @param files resource id of the file to read
     * @return the concatenation of the file content
     */
    @Override
    protected CharSequence doInBackground(Integer... files) {
        StringBuffer fileContent = new StringBuffer();
        String line;
        for (@RawRes int fileId : files) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(resources.openRawResource(fileId)));
                while ((line = in.readLine()) != null) {
                    fileContent.append(line);
                    fileContent.append('\n');
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }//for
        return fileContent;
    }

    /**
     * show the file content into the text view
     * @param content file content
     */
    @Override
    protected void onPostExecute(CharSequence content) {
        TextView temp = mTargetView.get();
        if(temp!=null)
            temp.setText(content);
    }
}
