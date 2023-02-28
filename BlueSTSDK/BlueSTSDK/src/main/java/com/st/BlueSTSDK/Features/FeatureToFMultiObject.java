package com.st.BlueSTSDK.Features;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class FeatureToFMultiObject extends Feature {

    public static final String FEATURE_NAME = "ToF Multi Object";
    public static final String FEATURE_UNIT = "mm";
    public static final String FEATURE_DATA_NAME = "Obj";
    public static final short DATA_MAX = 4000;
    public static final short DATA_MIN = 0;

    public static final short MAX_OBJ_NUMBER = 4;

    public static final String OBJECT_FOUND_UNIT = "num";
    public static final String OBJECT_FOUND_DATA_NAME = "Num";

    public static final String PRESENCE_FOUND_UNIT = "num";
    public static final String PRESENCE_FOUND_DATA_NAME = "Pres";

    public static final byte ENABLE_PRESENCE_DETECTION_COMMAND = 0x01;
    public static final byte DISABLE_PRESENCE_DETECTION_COMMAND = 0x00;

    protected static final Field TOF_FIELD =
            new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt16, DATA_MAX, DATA_MIN,true);

    protected static final Field OBJECT_FOUND_FIELD =
            new Field(OBJECT_FOUND_DATA_NAME, OBJECT_FOUND_UNIT, Field.Type.Int8, MAX_OBJ_NUMBER,0,false);

    protected static final Field PRESENCE_FOUND_FIELD =
            new Field(PRESENCE_FOUND_DATA_NAME, PRESENCE_FOUND_UNIT, Field.Type.Int8, MAX_OBJ_NUMBER, 0,false);


    /**
     * build a TimeOfFlight feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureToFMultiObject(Node n) {
        super(FEATURE_NAME, n, new Field[] {
                TOF_FIELD,
                TOF_FIELD,
                TOF_FIELD,
                TOF_FIELD,
                OBJECT_FOUND_FIELD,
                PRESENCE_FOUND_FIELD});
    }

    /**
     *  return the distance for the one object
     * @param sample data sample
     * @param obj_num object number
     * @return
     */
    public static int getDistance (Sample sample, int obj_num) {
        if((sample!=null) & (obj_num<MAX_OBJ_NUMBER))
            if(sample.data.length>0)
                if (sample.data[obj_num] != null)
                    return sample.data[obj_num].intValue();
        return 0;
    }

    /**
     * Return the formatted string for one object distance
     * @param sample data sample
     * @param obj_num object number
     * @return
     */
    public static String getDistanceToString (Sample sample, int obj_num) {
        if((sample!=null) & (obj_num<MAX_OBJ_NUMBER))
            if(sample.data.length>0)
                if (sample.data[obj_num] != null) {
                    int distance = sample.data[obj_num].intValue();
                    if(distance!=0) {
                        return String.format("Distance %04d %s", distance, FEATURE_UNIT);
                    } else {
                        return String.format("Distance NaN %s",  FEATURE_UNIT);
                    }
                }
        //else
        return null;
    }

    /**
     *  Return the number found objects
     * @param sample data sample
     * @return
     */
    public static int getNumObjects (Sample sample) {
        if(sample!=null)
            if(sample.data.length>0)
                if (sample.data[MAX_OBJ_NUMBER] != null)
                    return sample.data[MAX_OBJ_NUMBER].intValue();
        //else
        return 0;
    }

    /**
     *  Return the number of found presences
     * @param sample data sample
     * @return
     */
    public static int getNumPresence (Sample sample) {
        if(sample!=null)
            if(sample.data.length>0)
                if (sample.data[MAX_OBJ_NUMBER+1] != null)
                    return sample.data[MAX_OBJ_NUMBER+1].intValue();
        //else
        return 0;
    }

    /**
     * return the formatted string for the number of found presences
     * @param sample
     * @return
     */
    public static String  getNumPresenceToString (Sample sample) {
        if(sample!=null)
            if(sample.data.length>0)
                if (sample.data[4] != null) {
                    int numPres = sample.data[MAX_OBJ_NUMBER+1].intValue();
                    if (numPres==1) {
                        return "Found 1 person";
                    } else if (numPres>1) {
                        return String.format("Found %d people", sample.data[MAX_OBJ_NUMBER+1].intValue());
                    } else {
                        return "No Presence found";
                    }
                }
        //else
        return null;
    }

    /**
     * return the formatted string for the number of found objects
     * @param sample
     * @return
     */
    public static String  getNumObjectsToString (Sample sample) {
        if(sample!=null)
            if(sample.data.length>0)
                if (sample.data[4] != null) {
                    int numObj = sample.data[MAX_OBJ_NUMBER].intValue();
                    if (numObj==1) {
                        return "Found 1 object";
                    } else if (numObj>1) {
                        return String.format("Found %d objects", sample.data[MAX_OBJ_NUMBER].intValue());
                    } else {
                        return "No objects found";
                    }
                }
        //else
        return null;
    }

    public static void enablePresenceRecognition(Feature f){
        f.getParentNode().writeFeatureData(f,new byte[]{ENABLE_PRESENCE_DETECTION_COMMAND});
    }

    public static void disablePresenceRecognition(Feature f){
        f.getParentNode().writeFeatureData(f,new byte[]{DISABLE_PRESENCE_DETECTION_COMMAND});
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
//        if((data.length-dataOffset)!=(2*4+1)){
//            throw new IllegalArgumentException("Too few bytes for extracting the Time-Of-Flight Feature");
//        }

        //Number of valid distances
        int nObj = (data.length-dataOffset)/2;
        Field temp[] = new Field[MAX_OBJ_NUMBER+2];
        for(int i=0;i<MAX_OBJ_NUMBER;i++){
            temp[i]= new Field(FEATURE_DATA_NAME+(i+1), FEATURE_UNIT, Field.Type.UInt16,
                    DATA_MAX, DATA_MIN,true);
        }//for
        temp[MAX_OBJ_NUMBER] = new Field(OBJECT_FOUND_DATA_NAME, OBJECT_FOUND_UNIT, Field.Type.Int8,
                MAX_OBJ_NUMBER, 0,false);

        temp[MAX_OBJ_NUMBER+1] = new Field(PRESENCE_FOUND_DATA_NAME, PRESENCE_FOUND_UNIT, Field.Type.Int8,
                MAX_OBJ_NUMBER, 0,false);

        mDataDesc=temp;

        Number results[] = new Number[MAX_OBJ_NUMBER+2];


        for(int i=0;i<nObj;i++){
            results[i]=NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset+2*i);
        }//for
        //Fill the remaining distances
        for(int i=nObj;i<MAX_OBJ_NUMBER;i++){
            results[i]=0; // Not a valid measure
        }//for
        results[MAX_OBJ_NUMBER] = nObj;

        if(((data.length-dataOffset)&0x1)==1) {
            results[MAX_OBJ_NUMBER + 1] = NumberConversion.byteToUInt8(data, dataOffset + 2 * nObj);
        } else {
            results[MAX_OBJ_NUMBER + 1] = 0;
        }

        return new ExtractResult(new Sample(timestamp,results,getFieldsDesc()),(nObj*2)+1);
    }//extractData
}
