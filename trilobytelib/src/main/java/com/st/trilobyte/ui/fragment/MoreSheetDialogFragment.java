package com.st.trilobyte.ui.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;

import java.util.HashMap;
import java.util.Map;

public class MoreSheetDialogFragment extends BottomSheetDialogFragment {

    private DismissListener mDismissListener;
    private Node.Type mBoard;

    public static MoreSheetDialogFragment getInstance(Node.Type board, DismissListener listener) {
        MoreSheetDialogFragment df = new MoreSheetDialogFragment();
        df.setDismissListener(listener);
        df.setBoardType(board);
        return df;
    }

    private void setBoardType(Node.Type board) {
        mBoard = board;
    }

    private static Map<Node.Type, String> linksToBoard = new HashMap<>();

    static {
        linksToBoard.put(Node.Type.SENSOR_TILE_BOX, "https://www.st.com/SensorTilebox");
        linksToBoard.put(Node.Type.SENSOR_TILE_BOX_PRO, "https://www.st.com/SensorTilebox-Pro");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.more_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {

        TextView boardName = view.findViewById(R.id.open_about_layout_text);
        if (mBoard == Node.Type.SENSOR_TILE_BOX) {
            boardName.setText(getText(R.string.about_sensortile_box));
        } else if (mBoard == Node.Type.SENSOR_TILE_BOX_PRO) {
            boardName.setText(getText(R.string.about_sensortile_box_pro));
        } else {
            boardName.setText("Board Not Supported");
        }

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                dismiss();
            }
        });

        view.findViewById(R.id.open_doc_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                openUrl(linksToBoard.get(mBoard));
            }
        });

        view.findViewById(R.id.open_help_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                openUrl(linksToBoard.get(mBoard));
            }
        });

        view.findViewById(R.id.open_about_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                openUrl(linksToBoard.get(mBoard));
            }
        });

        view.findViewById(R.id.open_stm_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                openUrl("https://www.st.com/");
            }
        });
    }

    private void openUrl(String url) {
        //Intent i = new Intent(Intent.ACTION_VIEW);
        //i.setData(Uri.parse(url));
        //this.startActivity(i);
        Activity activity = requireActivity();
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity,"impossible to open an external webpage", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    public void setDismissListener(final DismissListener dismissListener) {
        mDismissListener = dismissListener;
    }

    // listener

    public interface DismissListener {
        void onDismiss();
    }
}
