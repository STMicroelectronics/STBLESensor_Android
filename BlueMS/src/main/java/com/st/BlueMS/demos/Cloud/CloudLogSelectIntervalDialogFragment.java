package com.st.BlueMS.demos.Cloud;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.st.BlueMS.R;

import java.util.Arrays;


public class CloudLogSelectIntervalDialogFragment extends DialogFragment {

    private static final String CURRENT_INTERVAL_INDEX = CloudLogSelectIntervalDialogFragment.class.getName()+".CURRENT_INTERVAL_INDEX";

    public interface CloudLogSelectIntervalDialogCallback{
        void onNewUpdateIntervalSelected(int newUpdateInterval);
    }

    private static int getCurrentTimeIndex(int possibleValues[],int currentTime){
        return Arrays.binarySearch(possibleValues,currentTime);
    }

    public static DialogFragment create(Context context,int currentInterval){
        int[] sampleIntevals = context.getResources().getIntArray(R.array
                .cloudLog_updateIntervalValues);

        DialogFragment fragment = new CloudLogSelectIntervalDialogFragment();

        Bundle args = new Bundle();
        args.putInt(CURRENT_INTERVAL_INDEX,getCurrentTimeIndex(sampleIntevals,currentInterval));

        fragment.setArguments(args);
        return fragment;
    }


    private CloudLogSelectIntervalDialogCallback getCloudFwUpgradeRequestCallback() {
        Context ctx = getActivity();
        if(ctx instanceof CloudLogSelectIntervalDialogCallback) {
            return (CloudLogSelectIntervalDialogCallback) ctx;
        }

        Fragment parent = getParentFragment();
        if(parent!=null && parent instanceof CloudLogSelectIntervalDialogCallback ){
            return (CloudLogSelectIntervalDialogCallback) parent;
        }

        throw new IllegalStateException("CloudLogSelectIntervalDialogFragment must attach to something "+
                "implementing CloudLogSelectIntervalDialogCallback");

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int checkedItem = getArguments().getInt(CURRENT_INTERVAL_INDEX,3);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.cloudLog_updateIntervalMsg);
        //onClick
        dialog.setSingleChoiceItems(R.array.cloudLog_updateIntervalString,checkedItem,
                  (dialog1, which) -> {
                        int[] sampleIntevals = getResources().getIntArray(R.array
                              .cloudLog_updateIntervalValues);
                        getCloudFwUpgradeRequestCallback()
                                .onNewUpdateIntervalSelected(sampleIntevals[which]);
                        dismiss();
                  }
            );
        return dialog.create();
    }

}
