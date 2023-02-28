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
package com.st.BlueSTSDK.gui.preferences;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.gui.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fragment that contains the log preference -> where and how store the data
 */

public class LogPreferenceFragment extends PreferenceFragment {

    private static final Pattern PATTERN_FILE_NAME_SESSION = Pattern.compile("^(\\d{8})_(\\d{6})_.*(csv|wav)$");

    public final static String KEY_PREF_LOG_STORE="prefLog_logStore";
    private static final String TAG = LogPreferenceFragment.class.getCanonicalName();

    /** preference widget */
    private Preference mClearLog;
    private Preference mExportSessionLog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_logging);
        mClearLog = findPreference("prefLog_clearLog");
        mExportSessionLog = findPreference("prefLog_exportSessionLog");

    }

    /**
     * get all the log file in the directory
     * @param directoryPath path where search the file
     * @return all file in the directory with an extension .csv
     */
    static public File[] getLogFiles(String directoryPath, final String session){
        File directory = new File(directoryPath);
        //find all the log files files
        //accept
        final FileFilter isLogFile = pathname -> {
            final String fileName = pathname.getName();
            return PATTERN_FILE_NAME_SESSION.matcher(fileName).matches();
        };
        return directory.listFiles(isLogFile);
    }//getLogFiles

    /**
     * remove all the csv file in the directory
     * @param c context where the file were created
     * @param directoryPath directory where this class dumped the feature data
     */
    static public void deleteSession(Context c, String directoryPath, String session){
        File files[] =getLogFiles(directoryPath, session);
        if(files==null || files.length==0) //nothing to do
            return;

        for(File f: files ){
            if(!f.delete())
                Log.e(TAG, "Error deleting the file " + f.getAbsolutePath());
            c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
        }//for
    }//clean

    private String getLogPath(){
        return LogFeatureActivity.getLogDirectory();
    }


    private String[] getSessions(){
        File files[] = getLogFiles(getLogPath(), null);
        if(files==null)
            return new String[0];
        List<String> strSessions = new ArrayList<>();

        for (File f: files){
            Matcher matcher = PATTERN_FILE_NAME_SESSION.matcher(f.getName());
            if (matcher.matches()) {
                String session = matcher.group(1) + " " + matcher.group(2);
                if (strSessions.indexOf(session)<0)
                    strSessions.add(session);
            }
        }
        return strSessions.toArray(new String[strSessions.size()]);
    }

    private String [] mSessionsLocal;
    private boolean [] mCheckedSession;

    private void createSessionDialog(String title, int resId,  DialogInterface.OnClickListener actionListener ){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mSessionsLocal = getSessions();
        if(mSessionsLocal.length==0)
            return;
        mCheckedSession = new boolean[mSessionsLocal.length];
        for (int i = 0; i< mCheckedSession.length; i++)
        {
            mCheckedSession[i] = title.startsWith("Export");
        }
        builder.setTitle(title);

        builder.setIcon(resId);
        builder.setCancelable(false);
        builder.setMultiChoiceItems(mSessionsLocal, mCheckedSession,
                (dialogInterface, i, b) -> mCheckedSession[i]=b);

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, actionListener);
        builder.create().show();
    }

        /**
         * if we click on the clear log preference we remove the generated file
         * @param preferenceScreen
         * @param preference preference that the user clicked
         * @return true if the user click the clearLog preference
         */

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                             @NonNull Preference preference) {
            if(preference==mClearLog){
                createSessionDialog("Remove Sessions Log", android.R.drawable.ic_delete, (dialogInterface, i) -> {
                    if (mCheckedSession != null && mCheckedSession.length > 0) {
                        for (int j = 0; j < mCheckedSession.length; j++)
                        {
                            if (mCheckedSession[j])
                                deleteSession(getActivity(), getLogPath(), mSessionsLocal[j].replace(" ","_"));
                        }
                    }
                });
                //clearLog();
                //Notify.message(getActivity(),R.string.pref_logClearDone);
                return true;
            }
            if(preference==mExportSessionLog){
                //TODO CHECK + SHOW ONLY IF THERE ARE SOMETHING TO EXPORT
                createSessionDialog("Export Sessions Log", android.R.drawable.ic_dialog_email, (dialogInterface, i) -> {
                    if (mCheckedSession != null && mCheckedSession.length > 0) {
                        List<File> mFileToExport = new ArrayList<>();
                        for (int j = 0; j < mCheckedSession.length; j++)
                        {
                            if (mCheckedSession[j]) {
                                File[] files = getLogFiles(getLogPath(), mSessionsLocal[j]
                                        .replace(" ", "_"));
                                mFileToExport.addAll(Arrays.asList(files));
                            }

                        }

                        if (mFileToExport.size()> 0)
                            LogFeatureActivity.exportDataByMail(getActivity(),
                                    getLogPath(),
                                    mFileToExport.toArray(new File[mFileToExport.size()]),
                                    false);
                    }
                });
                //clearLog();
                //Notify.message(getActivity(),R.string.pref_logClearDone);
                return true;
            }
            return false;
        }//onPreferenceTreeClick
    }//LogPreferenceFragment