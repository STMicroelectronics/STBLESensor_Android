/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.Log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Store the feature data into a Db in ram, the data can be exported in a csv file when the
 * logging is finished
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureLogDB extends  FeatureLogBase {

    private static final String TAG = FeatureLogDB.class.getCanonicalName();

    /** context used for open the db */
    private Context mLogContext;

    /** class used for create the db */
    private FeatureLogDBOpenHelper mDbHelper;
    /** db where store the data */
    private SQLiteDatabase mDb;

    private List<Feature>  mAvailableFeatures;

    /**
     * create a db for store the features list
     * @param c context to use for open the db
     * @param dumpDirectoryPath directory where store the file
     * @param nodes list of nodes to log
     */
    public FeatureLogDB(Context c, String dumpDirectoryPath, List<Node> nodes){
        super(dumpDirectoryPath,nodes);
        this.mLogContext = c;
        mAvailableFeatures=getAllFeatures();
        mDbHelper = new FeatureLogDBOpenHelper(c);
        mDb = mDbHelper.getWritableDatabase();
    }//FeatureLogDb

    @Override
    public void logFeatureUpdate(@NonNull Feature feature, @NonNull byte[] rawData, @Nullable Feature.Sample data) {
        if(data == null)
            return;
        mDb.insert(sanitizeString(feature.getName()),null,
                getFeatureRow(feature, rawData, data));
    }
    /**
     * prepare the feature data for be inserted in the db row
     * @param feature feature that we have to dump
     * @param rawData raw data used for extract the feature data
     * @param sample data extracted from the feature
     * @return object ready to be instead in the db row
     */
    public ContentValues getFeatureRow(Feature feature, byte[] rawData,@NonNull Feature.Sample sample){
        Field[] fields = feature.getFieldsDesc();
        ContentValues cv = new ContentValues();
        cv.put(HOST_TIMESTAMP_COLUMN, System.currentTimeMillis() - mStartLog.getTime());
        cv.put(NODE_NAME_COLUMN, feature.getParentNode().getFriendlyName());
        cv.put(NODE_TIMESTAMP_COLUMN, sample.timestamp);
        cv.put(NODE_RAW_DATA_COLUMN,rawData);
        int nFeature = fields.length;
        for(int i=0;i<nFeature;i++){
            String fieldName = sanitizeString(fields[i].getName());
            switch (fields[i].getType()){
                case Float:
                    cv.put(fieldName, sample.data[i].floatValue());
                    break;
                case Int64:
                case UInt32:
                    cv.put(fieldName,sample.data[i].longValue());
                    break;
                case Int32:
                case UInt16:
                    cv.put(fieldName,sample.data[i].intValue());
                    break;
                case Int16:
                case UInt8:
                    cv.put(fieldName,sample.data[i].shortValue());
                    break;
                case Int8:
                    cv.put(fieldName,sample.data[i].byteValue());
                    break;
            }//switch
        }//for
        return cv;
    }//getFeatureRow


    /**
     * check if a feature is present in the list
     * @param f feature to search
     * @param lf list of current registered features
     * @return true if available else false
     */
    private boolean isAvailable(Feature f, List<Feature> lf ){
        for (Feature ft: lf) {
            if (f.getName().compareTo(ft.getName()) == 0)
                return true;
        }
        return  false;
    }

    /**
     * Get all features in the list of connected nodes
     * @return the list of features
     */
    private List<Feature> getAllFeatures()
    {

        List<Feature> feat = new ArrayList<>();
        for(Node n : mNodeList) {
            for(Feature f:n.getFeatures()) {
                if(!isAvailable(f, feat))
                    feat.add(f);
            }
        }
        return feat;
    }
    /**
     * remove non-word characters from the feature name
     * @param featureName feature name
     * @return feature name containing only letter or number
     */
    public static String sanitizeString(String featureName){
        return featureName.replaceAll("\\W","");
    }
    /**
     * create the sql code for a table that can contain the feature data
     * <p>
     *     the table will have the name = feature name
     *     a column with the device name that update the feature
     *     a column with the raw data received by the node
     *     a column for each feature field
     * </p>
     * @param feature feature that we have to dump
     * @return sql code for create a table that will contain the feature data
     */
    public static String getFeatureTable(Feature feature){
        StringBuilder sqlTable = new StringBuilder();
        sqlTable.append("CREATE TABLE ").append(sanitizeString(feature.getName())).append("(\n");
        sqlTable.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,\n");
        sqlTable.append(HOST_TIMESTAMP_COLUMN).append(" INTEGER NOT NULL,\n");
        sqlTable.append(NODE_NAME_COLUMN).append(" TEXT,\n");
        sqlTable.append(NODE_TIMESTAMP_COLUMN).append(" INTEGER NOT NULL,\n");
        sqlTable.append(NODE_RAW_DATA_COLUMN).append(" BLOB,\n");
        Field featureDesc[] = feature.getFieldsDesc();
        int nFeature = featureDesc.length;
        for(int i=0;i<nFeature;i++){
            sqlTable.append(sanitizeString(featureDesc[i].getName()));
            switch (featureDesc[i].getType()){
                case Float:
                    sqlTable.append(" REAL NOT NULL");
                    break;
                case Int64:
                case Int32:
                case Int16:
                case UInt32:
                case UInt16:
                case UInt8:
                    sqlTable.append(" INTEGER NOT NULL");
                    break;
                case Int8:
                    sqlTable.append(" INT8 NOT NULL");
                    break;
            }//switch
            if(i!=nFeature-1)
                sqlTable.append(",\n");
        }//for i
        sqlTable.append(");\n");
        return sqlTable.toString();
    }//getFeatureTable


    /**
     * async task that dump the db tables in a file, each table will have a different file
     */
    private class ExportDatabaseCSVTask extends AsyncTask<Feature, Void, File[]> {

        /** directory where store the files */
        private String baseDirName;
        /* context used for notify that the task is finish*/
        private Context context;

        ExportDatabaseCSVTask(Context c,String baseDirName){
            this.baseDirName=baseDirName;
            context =c;
        }

        /** for each feature find all the db row in the feature table and store the data in a
         *  csv file
         * @param args list of feature to dump
         * @return true if all the data are dump correctly
         */
        @Override
        protected File[] doInBackground(final Feature... args) {
            final SQLiteDatabase db = mDbHelper.getReadableDatabase();
            File createdFile[] = new File[args.length];
            int featureIdx=0;
            for(Feature f:args) {
                Field featureDesc[] = f.getFieldsDesc();
                ArrayList<String> columName = new ArrayList<>(featureDesc.length+1);
                columName.add(HOST_TIMESTAMP_COLUMN);
                columName.add(NODE_NAME_COLUMN);
                columName.add(NODE_TIMESTAMP_COLUMN);
                columName.add(NODE_RAW_DATA_COLUMN);
                for (Field desc: featureDesc){
                    columName.add(sanitizeString(desc.getName()));
                }

                Cursor res = db.query(sanitizeString(f.getName()),
                        //all the column + device name
                        columName.toArray(new String[columName.size()]), //select
                        null, null, //where
                        null, //group by
                        null, //having
                        BaseColumns._ID, //sort by
                        null //limit
                );

                Formatter out;
                //Writer outW = new OutputStreamWriter(new FileOutputStream(logFeatureFileName(f),true));
                try {
                    createdFile[featureIdx]=new File(logFeatureFileName(f));
                    out = new Formatter(createdFile[featureIdx]);
                    featureIdx++;
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Impossible open the dumpFile: " + e);
                    e.printStackTrace();
                    continue;
                }//try-catch

                if (!res.moveToFirst()) {
                    continue;
                }
                printHeader(out, f);

                final int nColum = columName.size();
                int columId[] = new int[nColum];
                int typeId[] = new int[nColum];
                for(int i=0;i<nColum;i++){
                    String name = columName.get(i);
                    columId[i] = res.getColumnIndex(name);
                    typeId[i] = res.getType(columId[i]);
                }
                out.flush();

                do {
                    for(int i=0;i<nColum;++i) {
                        switch (typeId[i]) {
                            case Cursor.FIELD_TYPE_FLOAT:
                                out.format("%f, ",res.getFloat(columId[i]));
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                out.format("%d, ",res.getInt(columId[i]));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                out.format("%s, ",res.getString(columId[i]));
                                break;
                            case Cursor.FIELD_TYPE_NULL:
                                out.format(",");
                                break;
                            case Cursor.FIELD_TYPE_BLOB:
                                storeBlobData(out, res.getBlob(columId[i]));
                                out.format(",");
                                break;
                        }//switch
                    }//for
                    out.format("\n");
                    out.flush();
                } while (res.moveToNext());
                res.close();
                out.flush();
                out.close();
            }
            if(featureIdx==args.length)
                return createdFile;
            else
                return null;
        }//doInBackground

        @Override
        protected void onPostExecute(File[] success) {
            if (success!=null) {
                Toast.makeText(context, "Export Success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context,"Export Error",Toast.LENGTH_SHORT).show();
            }//if-else
        }//onPostExecute
    }//doInBackground

    /**
     * dump the db in csv files
     * @param directoryPath directory where store the csv files
     * @return list of file where the db is dumped, each table will have its file
     */
    public File[] dumpToFile(String directoryPath){
        try {
            return new ExportDatabaseCSVTask(mLogContext,directoryPath).execute(
                    mAvailableFeatures.toArray(new Feature[mAvailableFeatures.size()])).get();
        } catch (InterruptedException e) {
            Log.e(TAG,"Error exporting the logs");
            return new File[0];
        } catch (ExecutionException e) {
            Log.e(TAG, "Error exporting the logs");
            return new File[0];
        }
    }

    /**
     * create the db where store the feature data, each feature will have a different table with the
     * table
     *
     * @author STMicroelectronics - Central Labs.
     * @version 1.0
     */
    public class FeatureLogDBOpenHelper extends SQLiteOpenHelper {


        private static final int DATABASE_VERSION = 1;


        FeatureLogDBOpenHelper(Context context){
      /*  super(context, String.format(DATABASE_NAME,features.get(0).getParentNode().getName()),
                null,DATABASE_VERSION);*/
            //TODO create a database with the node name and delete it each time?
            super(context, null,null,DATABASE_VERSION);
        }

        /**
         * create a table for each Feature passed to the constructor
         * @see SQLiteOpenHelper
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            for(Feature f: mAvailableFeatures){
                //Log.d(TAG,getFeatureTable(f));
                db.execSQL(FeatureLogDB.getFeatureTable(f));
            }//for
        }//onCreate

        /**
         * it is not implemented
         * @see SQLiteOpenHelper
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }


    }

}
