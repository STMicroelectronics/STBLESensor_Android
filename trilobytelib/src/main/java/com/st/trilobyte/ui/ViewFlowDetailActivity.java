package com.st.trilobyte.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.helper.DeviceHelperKt;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.Function;
import com.st.trilobyte.models.Output;
import com.st.trilobyte.models.Sensor;
import com.st.trilobyte.services.Session;

import java.util.Collections;

public class ViewFlowDetailActivity extends TrilobyteActivity {

    public final static String EXTRA_FLOW = "extra-flow";

    public final static String CAN_BE_EDITABLE = "extra-example-flag";

    public final static String EXTRA_BOARD_TYPE = "extra-board_type";

    public final static int EDIT_FLOW_RESULT_CODE = 100;

    private Flow mFlow;
    private Node.Type mBoard;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_flow_detail);

        getSupportActionBar().setTitle(getString(R.string.flow));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() == null || getIntent().getExtras() == null) {
            finish();
            return;
        }

        Bundle bundle = getIntent().getExtras();
        mFlow = (Flow) bundle.getSerializable(EXTRA_FLOW);
        mBoard = (Node.Type) bundle.getSerializable(EXTRA_BOARD_TYPE);
        boolean isEditable = bundle.getBoolean(CAN_BE_EDITABLE, false);

        findViewById(R.id.edit_button).setVisibility(isEditable ? View.GONE : View.VISIBLE);
        findViewById(R.id.edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                setResult(EDIT_FLOW_RESULT_CODE, getIntent());
                finish();
            }
        });

        findViewById(R.id.upload_button).setVisibility(mFlow.canBeUploaded() ? View.VISIBLE : View.GONE);
        findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                sendToBoard();
            }
        });

        fillView();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillView() {

        TextView flowNameTextview = findViewById(R.id.flow_name);
        flowNameTextview.setText(mFlow.getDescription());

        TextView outputTextView = findViewById(R.id.output_textview);
        StringBuilder outputs = new StringBuilder();
        int counter = 0;
        for (Output output : mFlow.getOutputs()) {
            outputs.append(output.getDescription());
            if (counter != mFlow.getOutputs().size() - 1) {
                outputs.append(", ");
            }
        }
        outputTextView.setText(outputs.toString());

        TextView noteTextview = findViewById(R.id.note_textview);
        noteTextview.setText(mFlow.getNotes());

        LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) DeviceHelperKt.convertDpToPixel(1, this));

        LinearLayout inputContainer = findViewById(R.id.viewer_input_container);
        for (int i = 0; i < mFlow.getSensors().size(); i++) {
            Sensor sensor = mFlow.getSensors().get(i);
            View row = getLayoutInflater().inflate(R.layout.flow_viewer_row_item, inputContainer, false);
            fillRowDetail(row, R.drawable.ic_sensor, sensor.getDescription());
            inputContainer.addView(row);

            if (i < mFlow.getSensors().size() - 1) {
                addCellSeparator(separatorParams, inputContainer);
            }
        }

        for (int i = 0; i < mFlow.getFlows().size(); i++) {

            addCellSeparator(separatorParams, inputContainer);

            Flow flow = mFlow.getFlows().get(i);
            View row = getLayoutInflater().inflate(R.layout.flow_viewer_row_item, inputContainer, false);
            fillRowDetail(row, R.drawable.ic_input, flow.getDescription());
            inputContainer.addView(row);
        }

        LinearLayout functionContainer = findViewById(R.id.viewer_function_container);
        for (Function function : mFlow.getFunctions()) {
            View functionItem = getLayoutInflater().inflate(R.layout.flow_viewer_function_item, functionContainer, false);
            View row = functionItem.findViewById(R.id.viewer_row);
            fillRowDetail(row, R.drawable.ic_function, function.getDescription());
            functionContainer.addView(functionItem);
        }

        LinearLayout outputContainer = findViewById(R.id.viewer_output_container);
        for (int i = 0; i < mFlow.getOutputs().size(); i++) {
            Output output = mFlow.getOutputs().get(i);
            View row = getLayoutInflater().inflate(R.layout.flow_viewer_row_item, outputContainer, false);
            int resourceId = getResources().getIdentifier(output.getIcon(), "drawable", getPackageName());
            fillRowDetail(row, resourceId, output.getDescription());
            outputContainer.addView(row);

            if (i < mFlow.getOutputs().size() - 1) {
                addCellSeparator(separatorParams, outputContainer);
            }
        }
    }

    private void addCellSeparator(LinearLayout.LayoutParams separatorParams, ViewGroup inputContainer) {
        View separator = new View(this);
        separator.setLayoutParams(separatorParams);
        separator.setBackgroundColor(getResources().getColor(R.color.subTitleColor));
        inputContainer.addView(separator);
    }

    private void sendToBoard() {

        Session.INSTANCE.setSession(Collections.singletonList(mFlow), null);
        
        Intent intent = UploadFlowActivity.Companion.provideIntent(this,mBoard);
        startActivity(intent);
    }

    private void fillRowDetail(View row, int resId, String description) {
        ImageView imageView = row.findViewById(R.id.viewer_row_imageview);
        imageView.setImageResource(resId);

        TextView nameTextview = row.findViewById(R.id.viewer_row_Textview);
        nameTextview.setText(description);
    }
}
