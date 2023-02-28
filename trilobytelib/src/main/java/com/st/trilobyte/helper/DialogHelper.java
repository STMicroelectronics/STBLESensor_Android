package com.st.trilobyte.helper;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.st.trilobyte.R;

public class DialogHelper {

    private DialogHelper() {
    }

    public static Dialog showDialog(@NonNull Activity activity,@NonNull String message,@Nullable DialogInterface.OnClickListener listener) {
        return showDialog(activity, activity.getString(R.string.app_name), message, activity.getString(android.R.string.ok), null, null, listener);
    }

    public static Dialog showDialog(Activity activity, String message, String positiveText, String negativeText, DialogInterface.OnClickListener listener) {
        return showDialog(activity, activity.getString(R.string.app_name), message, positiveText, negativeText, null, listener);
    }

    public static Dialog showDialog(Activity activity, String title, String content, String positiveText, String negativeText, String neutralText, DialogInterface.OnClickListener listener) {

        if (title == null) {
            title = activity.getString(R.string.app_name);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setCancelable(false);
        builder.setPositiveButton(positiveText, listener);

        if (negativeText != null) {
            builder.setNegativeButton(negativeText, listener);
        }

        if (neutralText != null) {
            builder.setNeutralButton(neutralText, listener);
        }

        final Dialog dialog = builder.create();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });

        return dialog;
    }

    public static void showFragmentDialog(DialogFragment dialog, FragmentManager manager) {
        dialog.show(manager, null);
    }
}
