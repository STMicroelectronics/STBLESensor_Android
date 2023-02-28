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

package com.st.blesensor.cloud.proprietary.STAzureIot.AuthKey.Storage;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.st.blesensor.cloud.proprietary.STAzureIot.AuthKey.AuthToken;
import com.st.blesensor.cloud.proprietary.STAzureIot.AuthKey.Storage.AzureAuthKeyDbContract.AuthTokenEntry;

import java.util.ArrayList;
import java.util.List;


public class AzureAuthKeyDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "AzureAuthKeyDb.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_AUTH_KEY_TABLE =
            "CREATE TABLE " + AuthTokenEntry.TABLE_NAME + " (" +
                    AuthTokenEntry._ID + " INTEGER PRIMARY KEY," +
                    AuthTokenEntry.COLUMN_NAME_DEVICE_ID + TEXT_TYPE + COMMA_SEP +
                    AuthTokenEntry.COLUMN_NAME_TOKEN + TEXT_TYPE+" )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AuthTokenEntry.TABLE_NAME;


    private static AzureAuthKeyDbHelper mInstance = null;

    public static AzureAuthKeyDbHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new AzureAuthKeyDbHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }


    private AzureAuthKeyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_AUTH_KEY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Insert a new license into the db
     * @param entry license to add
     */
    public static void insert(Context ctx, AuthToken entry){

        SQLiteDatabase db = getInstance(ctx).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AuthTokenEntry.COLUMN_NAME_DEVICE_ID, entry.getDeviceID());
        values.put(AuthTokenEntry.COLUMN_NAME_TOKEN, entry.getToken());

        // Insert the new row, returning the primary key value of the new row
        db.insert(AuthTokenEntry.TABLE_NAME,null, values);

        db.close();

    }//insert


    /**
     * Return a cursor loader for query the database and returning all the license associated with a
     * specific board id
     * @param c context to use for open the db
     * @param deviceId board to search
     * @return loader that will select all the license that can be apply to the specific board id
     */
    static public Loader<Cursor> getLicenseForBoard(final Context c, final String deviceId){

        return new AsyncTaskLoader<Cursor>(c) {

            @Override
            public Cursor loadInBackground() {

                SQLiteDatabase db = getInstance(c).getReadableDatabase();
                String[] projection = {
                        AuthTokenEntry._ID,
                        AuthTokenEntry.COLUMN_NAME_DEVICE_ID,
                        AuthTokenEntry.COLUMN_NAME_TOKEN,
                };

                // How you want the results sorted in the resulting Cursor
                String sortOrder = AuthTokenEntry.COLUMN_NAME_DEVICE_ID;

                String selection = AuthTokenEntry.COLUMN_NAME_DEVICE_ID + " LIKE ?";
                // Specify arguments in placeholder order.
                String[] selectionArgs = {deviceId};

                return db.query(
                        AuthTokenEntry.TABLE_NAME,  // The table to query
                        projection,               // The columns to return
                        selection,                // The columns for the WHERE clause
                        selectionArgs,            // The values for the WHERE clause
                        null,                     // don't group the rows
                        null,                     // don't filter by row groups
                        sortOrder                 // The sort order
                );
            }
        };
    }//getLicenseForBoard

    /**
     * build a LicenseEntry from a cursor line
     * @param c cursor to use for build the entry
     * @return license entry build with the data inside the cursor
     */
    public static AuthTokenEntry buildAuthToken(Cursor c){
        long id = c.getLong(c.getColumnIndex(AuthTokenEntry._ID));
        String deviceId = c.getString(c.getColumnIndex(AuthTokenEntry.COLUMN_NAME_DEVICE_ID));
        String token = c.getString(c.getColumnIndex(AuthTokenEntry.COLUMN_NAME_TOKEN));

        return new AuthTokenEntry(id,deviceId,token);
    }

    /**
     * read all the entry in the cursor and build a list of license
     * @param c cursor where extract the data
     * @return list of license inside the cursor
     */
    public static List<AuthTokenEntry> buildAuthTokenEntryList(Cursor c){
        List<AuthTokenEntry> list = new ArrayList<>(c.getCount());
        c.moveToFirst();
        while(!c.isAfterLast()){
            list.add(buildAuthToken(c));
            c.moveToNext();
        }//while
        return list;
    }

    /**
     * remove all the license in the db
     */
    public void deleteAllTokens(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(AuthTokenEntry.TABLE_NAME, null, null);
    }

    /**
     * remove all the license for a specific board
     * @param boardId board to delete
     */
    public void deleteToken(String boardId){
        SQLiteDatabase db = getWritableDatabase();

        String selection = AuthTokenEntry.COLUMN_NAME_DEVICE_ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { boardId };

        db.delete(AuthTokenEntry.TABLE_NAME, selection, selectionArgs);
    }
}
