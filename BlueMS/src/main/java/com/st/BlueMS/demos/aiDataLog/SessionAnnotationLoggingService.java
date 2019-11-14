/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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
package com.st.BlueMS.demos.aiDataLog;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import androidx.annotation.NonNull;
import android.util.Log;

import com.st.BlueMS.demos.aiDataLog.repository.Annotation;
import com.st.BlueMS.demos.aiDataLog.viewModel.SessionAnnotationEvent;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Service that write the current selected label in a csv file
 */
public class SessionAnnotationLoggingService extends IntentService {

    private final static SimpleDateFormat FILE_DATE_FORMAT_PREFIX = new SimpleDateFormat("yyyyMMdd_HHmmss",
            Locale.getDefault());

    private static final String STORE_ANNOTATION_ACTION = SessionAnnotationLoggingService.class.getName()+".STORE_ANNOTATION_ACTION";

    private static final String STORE_ANNOTATION_EXTRA = SessionAnnotationLoggingService.class.getName()+".STORE_ANNOTATION_EXTRA";

    public SessionAnnotationLoggingService() {
        super("SessionAnnotationLoggingService");
    }

    /**
     * log the current labels into a csv file
     * @param context context to use to start the service
     * @param annotation label to store into the file
     */
    public static void log(@NonNull Context context,@NonNull SessionAnnotationEvent annotation) {
        Intent intent = new Intent(context, SessionAnnotationLoggingService.class);
        intent.setAction(STORE_ANNOTATION_ACTION);
        intent.putExtra(STORE_ANNOTATION_EXTRA, annotation);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (STORE_ANNOTATION_ACTION.equals(action)) {
                final SessionAnnotationEvent annotation = intent.getParcelableExtra(STORE_ANNOTATION_EXTRA);
                write(annotation);
            }
        }
    }


    public static String getLogFileName(@NonNull SessionAnnotationEvent sessionAnnotation){
        String fileName = FILE_DATE_FORMAT_PREFIX.format(sessionAnnotation.sessionStart);
        return String.format("%s/AI_%s.csv", LogFeatureActivity.getLogDirectory(),fileName);
    }

    private void writeHeader(File f) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
        out.write("Timestamp,Label\n");
        out.flush();
        out.close();
    }

    private void write(SessionAnnotationEvent annotation) {
        File logFile = new File(getLogFileName(annotation));
        try {
            if(!logFile.exists())
                writeHeader(logFile);
            BufferedWriter out = new BufferedWriter(new FileWriter(logFile, true));
            out.write(Long.toString(annotation.eventTime.getTime()));
            out.write(",");
            for(Annotation a : annotation.labels){
                out.write(a.label);
                out.write(',');
            }
            out.write('\n');
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e(SessionAnnotationLoggingService.class.getName(),"IO Error:"+e.getMessage());
            e.printStackTrace();
        }
    }
}
