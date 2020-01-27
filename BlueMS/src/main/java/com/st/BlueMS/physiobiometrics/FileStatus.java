package com.st.BlueMS.physiobiometrics;

public class FileStatus {
    public boolean success;
    public String  reason;

    FileStatus () {
        success = false;
        reason = null;
    }
    FileStatus (boolean s, String r) {
        success = s;
        reason = r;
    }
}
