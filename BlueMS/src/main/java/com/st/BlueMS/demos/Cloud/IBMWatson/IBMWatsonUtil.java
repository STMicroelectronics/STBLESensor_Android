package com.st.BlueMS.demos.Cloud.IBMWatson;


import android.support.annotation.NonNull;

import java.util.regex.Pattern;

public class IBMWatsonUtil {

    static final Pattern VALID_NAME_CHARACTER = Pattern.compile("[a-zA-Z0-9\\.\\-_]*");


    static boolean isValidString(String str){
        if(str!=null){
            String trimStr = str.trim();
            return !trimStr.isEmpty() && VALID_NAME_CHARACTER.matcher(trimStr).matches();
        }else{
            return false;
        }
    }

}
