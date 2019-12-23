package com.st.BlueMS.physiobiometrics;

import android.app.Activity;
import android.content.Intent;

public class FilePicker {

    private static final int REQUEST_CODE_PICK_FILE = 300;

    public FilePicker() {
    }

    public static void pickCsvFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        activity.startActivityForResult(intent, 1);
    }

    public static String parseFilePath(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_FILE) {
                return parseFilePath(intent);
            }
        }
        return null;
    }

    private static String parseFilePath(Intent intent) {
        if (intent != null && intent.getData() != null) {
            return intent.getData().getPath();
        }
        return null;
    }
}