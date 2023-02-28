package com.st.trilobyte.ui.fragment.flow_builder;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.helper.DeviceHelperKt;
import com.st.trilobyte.helper.DialogHelper;
import com.st.trilobyte.helper.FlowHelper;
import com.st.trilobyte.helper.SensorFilterHelper;
import com.st.trilobyte.helper.SensorHelper;
import com.st.trilobyte.helper.TextWatcherImpl;
import com.st.trilobyte.models.CutOff;
import com.st.trilobyte.models.Filter;
import com.st.trilobyte.models.PowerMode;
import com.st.trilobyte.models.Sensor;
import com.st.trilobyte.models.SensorConfiguration;
import com.st.trilobyte.models.SensorConfigurationKt;
import com.st.trilobyte.models.SensorFilter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlowBuilderSensorOption extends BuilderFragment {

    private final static int GET_OBJECT_MLC = 0;
    private final static int GET_OBJECT_FSM = 1;
    private final static int REQUEST_BROWSE_FILE_MLC = 1000;
    private final static int REQUEST_BROWSE_FILE_FSM = 1001;

    //private Context mContext;
    private String mSensorId;
    private Sensor mSensor;
    private SensorConfiguration mConfiguration;

    private int nLabels = 0;

    private Node.Type mBoard;

    public static FlowBuilderSensorOption getInstance(String sensorId, Node.Type board) {
        FlowBuilderSensorOption fragment = new FlowBuilderSensorOption();
        fragment.setSensorId(sensorId);
        fragment.setBoardType(board);
        return fragment;
    }

    private void setBoardType(Node.Type board) {
        mBoard = board;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor_option, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView actionbarText = view.findViewById(R.id.actionbar_text);
        actionbarText.setText(getString(R.string.input_options));

        view.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                getActivity().onBackPressed();
            }
        });

        for (Sensor sensor : getCurrentFlow().getSensors()) {
            if (sensor.getId().equals(mSensorId)) {
                mSensor = sensor;
                break;
            }
        }

        TextView sensorName = view.findViewById(R.id.sensor_description);
        sensorName.setText(mSensor.getDescription());

        cloneSensorConfig();
        fillView();
    }

    public void setSensorId(final String sensorId) {
        mSensorId = sensorId;
    }

    private void cloneSensorConfig() {
        SensorConfiguration configuration = FlowHelper.searchSensorConfigurationInFlow(getCurrentFlow(), mSensorId);
        if (configuration != null) {
            mConfiguration = configuration;
        } else {
            mConfiguration = mSensor.getConfiguration();
        }
    }

    @Override
    public boolean onBackPressed() {

        if (!validateCustomOdrValue()) {
            DialogHelper.showDialog(getActivity(), getString(R.string.error_entered_odr_not_allowed), null);
            return false;
        }

        return true;
    }

    private void fillView() {
        fillRegConfig();
        fillAcquisitionTime();
        fillPowerModeSection();
        fillOdr();
        fillFilter();
        fillFullScale();
    }

    private void fillRegConfig() {

        if (mSensor.getConfiguration().getRegConfig() != null) {
            getView().findViewById(R.id.ucf_file_container).setVisibility(View.VISIBLE);

            String mlcLabels = mSensor.getConfiguration().getMlcLabels();
            String fsmLabels = mSensor.getConfiguration().getFsmLabels();

            if (mlcLabels != null) {
                View layout = getView().findViewById(R.id.mlc_layout);
                layout.setVisibility(View.VISIBLE);
                registerButtons(GET_OBJECT_MLC);
                loadUI(GET_OBJECT_MLC, mlcLabels);
            }
            else if (fsmLabels != null) {
                View layout = getView().findViewById(R.id.fsm_layout);
                layout.setVisibility(View.VISIBLE);
                registerButtons(GET_OBJECT_FSM);
                loadUI(GET_OBJECT_FSM, fsmLabels);
            }
        }
    }

    private void registerButtons(final int feature)
    {
        if (feature == GET_OBJECT_MLC)
        {
            Button ucfFileSelectionButton = getView().findViewById(R.id.ucf_file_selection_mlc_button);
            ucfFileSelectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(intent, REQUEST_BROWSE_FILE_MLC);
                }
            });

            ImageView mlcDectree0Expand = getView().findViewById(R.id.mlc_dectree0_expand);
            mlcDectree0Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 0);
                    ImageView mlcDectree0Expand = getView().findViewById(R.id.mlc_dectree0_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        mlcDectree0Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        mlcDectree0Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView mlcDectree0AddLabel = getView().findViewById(R.id.mlc_dectree0_add_label);
            mlcDectree0AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 0, "0", "label");
                }
            });

            ImageView mlcDectree1Expand = getView().findViewById(R.id.mlc_dectree1_expand);
            mlcDectree1Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 1);
                    ImageView mlcDectree1Expand = getView().findViewById(R.id.mlc_dectree1_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        mlcDectree1Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        mlcDectree1Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView mlcDectree1AddLabel = getView().findViewById(R.id.mlc_dectree1_add_label);
            mlcDectree1AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 1, "0", "label");
                }
            });

            ImageView mlcDectree2Expand = getView().findViewById(R.id.mlc_dectree2_expand);
            mlcDectree2Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 2);
                    ImageView mlcDectree2Expand = getView().findViewById(R.id.mlc_dectree2_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        mlcDectree2Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        mlcDectree2Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView mlcDectree2AddLabel = getView().findViewById(R.id.mlc_dectree2_add_label);
            mlcDectree2AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 2, "0", "label");
                }
            });

            ImageView mlcDectree3Expand = getView().findViewById(R.id.mlc_dectree3_expand);
            mlcDectree3Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 3);
                    ImageView mlcDectree3Expand = getView().findViewById(R.id.mlc_dectree3_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        mlcDectree3Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        mlcDectree3Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView mlcDectree3AddLabel = getView().findViewById(R.id.mlc_dectree3_add_label);
            mlcDectree3AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 3, "0", "label");
                }
            });

            ImageView mlcDectree4Expand = getView().findViewById(R.id.mlc_dectree4_expand);
            mlcDectree4Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 4);
                    ImageView mlcDectree4Expand = getView().findViewById(R.id.mlc_dectree4_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        mlcDectree4Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        mlcDectree4Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView mlcDectree4AddLabel = getView().findViewById(R.id.mlc_dectree4_add_label);
            mlcDectree4AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 4, "0", "label");
                }
            });

            ImageView mlcDectree5Expand = getView().findViewById(R.id.mlc_dectree5_expand);
            mlcDectree5Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 5);
                    ImageView mlcDectree5Expand = getView().findViewById(R.id.mlc_dectree5_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        mlcDectree5Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        mlcDectree5Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView mlcDectree5AddLabel = getView().findViewById(R.id.mlc_dectree5_add_label);
            mlcDectree5AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 5, "0", "label");
                }
            });

            ImageView mlcDectree6Expand = getView().findViewById(R.id.mlc_dectree6_expand);
            mlcDectree6Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 6);
                    ImageView mlcDectree6Expand = getView().findViewById(R.id.mlc_dectree6_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        mlcDectree6Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        mlcDectree6Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView mlcDectree6AddLabel = getView().findViewById(R.id.mlc_dectree6_add_label);
            mlcDectree6AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 6, "0", "label");
                }
            });

            ImageView mlcDectree7Expand = getView().findViewById(R.id.mlc_dectree7_expand);
            mlcDectree7Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 7);
                    ImageView mlcDectree7Expand = getView().findViewById(R.id.mlc_dectree7_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        mlcDectree7Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        mlcDectree7Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView mlcDectree7AddLabel = getView().findViewById(R.id.mlc_dectree7_add_label);
            mlcDectree7AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 7, "0", "label");
                }
            });
        }
        else if (feature == GET_OBJECT_FSM)
        {
            Button ucfFileSelectionButton = getView().findViewById(R.id.ucf_file_selection_fsm_button);
            ucfFileSelectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(intent, REQUEST_BROWSE_FILE_FSM);
                }
            });
            /*
            ImageView fsmProgram0Expand = getView().findViewById(R.id.fsm_program0_expand);
            fsmProgram0Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 0);
                    ImageView fsmProgram0Expand = getView().findViewById(R.id.fsm_program0_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram0Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram0Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram0AddLabel = getView().findViewById(R.id.fsm_program0_add_label);
            fsmProgram0AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 0, "0", "label");
                }
            });

            ImageView fsmProgram1Expand = getView().findViewById(R.id.fsm_program1_expand);
            fsmProgram1Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 1);
                    ImageView fsmProgram1Expand = getView().findViewById(R.id.fsm_program1_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram1Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram1Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram1AddLabel = getView().findViewById(R.id.fsm_program1_add_label);
            fsmProgram1AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 1, "0", "label");
                }
            });

            ImageView fsmProgram2Expand = getView().findViewById(R.id.fsm_program2_expand);
            fsmProgram2Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 2);
                    ImageView fsmProgram2Expand = getView().findViewById(R.id.fsm_program2_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram2Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram2Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram2AddLabel = getView().findViewById(R.id.fsm_program2_add_label);
            fsmProgram2AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 2, "0", "label");
                }
            });

            ImageView fsmProgram3Expand = getView().findViewById(R.id.fsm_program3_expand);
            fsmProgram3Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 3);
                    ImageView fsmProgram3Expand = getView().findViewById(R.id.fsm_program3_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram3Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram3Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram3AddLabel = getView().findViewById(R.id.fsm_program3_add_label);
            fsmProgram3AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 3, "0", "label");
                }
            });

            ImageView fsmProgram4Expand = getView().findViewById(R.id.fsm_program4_expand);
            fsmProgram4Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 4);
                    ImageView fsmProgram4Expand = getView().findViewById(R.id.fsm_program4_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram4Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram4Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram4AddLabel = getView().findViewById(R.id.fsm_program4_add_label);
            fsmProgram4AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 4, "0", "label");
                }
            });

            ImageView fsmProgram5Expand = getView().findViewById(R.id.fsm_program5_expand);
            fsmProgram5Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 5);
                    ImageView fsmProgram5Expand = getView().findViewById(R.id.fsm_program5_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram5Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram5Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram5AddLabel = getView().findViewById(R.id.fsm_program5_add_label);
            fsmProgram5AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 5, "0", "label");
                }
            });

            ImageView fsmProgram6Expand = getView().findViewById(R.id.fsm_program6_expand);
            fsmProgram6Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 6);
                    ImageView fsmProgram6Expand = getView().findViewById(R.id.fsm_program6_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram6Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram6Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram6AddLabel = getView().findViewById(R.id.fsm_program6_add_label);
            fsmProgram6AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 6, "0", "label");
                }
            });

            ImageView fsmProgram7Expand = getView().findViewById(R.id.fsm_program7_expand);
            fsmProgram7Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 7);
                    ImageView fsmProgram7Expand = getView().findViewById(R.id.fsm_program7_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram7Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram7Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram7AddLabel = getView().findViewById(R.id.fsm_program7_add_label);
            fsmProgram7AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 7, "0", "label");
                }
            });

            ImageView fsmProgram8Expand = getView().findViewById(R.id.fsm_program8_expand);
            fsmProgram8Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 8);
                    ImageView fsmProgram8Expand = getView().findViewById(R.id.fsm_program8_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram8Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram8Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram8AddLabel = getView().findViewById(R.id.fsm_program8_add_label);
            fsmProgram8AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 8, "0", "label");
                }
            });

            ImageView fsmProgram9Expand = getView().findViewById(R.id.fsm_program9_expand);
            fsmProgram9Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 9);
                    ImageView fsmProgram9Expand = getView().findViewById(R.id.fsm_program9_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram9Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram9Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram9AddLabel = getView().findViewById(R.id.fsm_program9_add_label);
            fsmProgram9AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 9, "0", "label");
                }
            });

            ImageView fsmProgram10Expand = getView().findViewById(R.id.fsm_program10_expand);
            fsmProgram10Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 10);
                    ImageView fsmProgram10Expand = getView().findViewById(R.id.fsm_program10_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram10Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram10Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram10AddLabel = getView().findViewById(R.id.fsm_program10_add_label);
            fsmProgram10AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 10, "0", "label");
                }
            });

            ImageView fsmProgram11Expand = getView().findViewById(R.id.fsm_program11_expand);
            fsmProgram11Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 11);
                    ImageView fsmProgram11Expand = getView().findViewById(R.id.fsm_program11_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram11Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram11Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram11AddLabel = getView().findViewById(R.id.fsm_program11_add_label);
            fsmProgram11AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 11, "0", "label");
                }
            });

            ImageView fsmProgram12Expand = getView().findViewById(R.id.fsm_program12_expand);
            fsmProgram12Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 12);
                    ImageView fsmProgram12Expand = getView().findViewById(R.id.fsm_program12_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram12Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram12Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram12AddLabel = getView().findViewById(R.id.fsm_program12_add_label);
            fsmProgram12AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 12, "0", "label");
                }
            });

            ImageView fsmProgram13Expand = getView().findViewById(R.id.fsm_program13_expand);
            fsmProgram13Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 13);
                    ImageView fsmProgram13Expand = getView().findViewById(R.id.fsm_program13_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram13Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram13Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram13AddLabel = getView().findViewById(R.id.fsm_program13_add_label);
            fsmProgram13AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 13, "0", "label");
                }
            });

            ImageView fsmProgram14Expand = getView().findViewById(R.id.fsm_program14_expand);
            fsmProgram14Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(feature, 14);
                    ImageView fsmProgram14Expand = getView().findViewById(R.id.fsm_program14_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram14Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram14Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram14AddLabel = getView().findViewById(R.id.fsm_program14_add_label);
            fsmProgram14AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 14, "0", "label");
                }
            });

            ImageView fsmProgram15Expand = getView().findViewById(R.id.fsm_program15_expand);
            fsmProgram15Expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = getLabelLayout(GET_OBJECT_FSM, 15);
                    ImageView fsmProgram15Expand = getView().findViewById(R.id.fsm_program15_expand);
                    if (layout.getVisibility() == View.VISIBLE) {
                        layout.setVisibility(View.GONE);
                        fsmProgram15Expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    }
                    else {
                        layout.setVisibility(View.VISIBLE);
                        fsmProgram15Expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });

            ImageView fsmProgram15AddLabel = getView().findViewById(R.id.fsm_program15_add_label);
            fsmProgram15AddLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSingleLabelRow(feature, 15, "0", "label");
                }
            });
            */
        }
    }

    private void loadUI(int feature, String mlcLabels)
    {
        if (feature == GET_OBJECT_MLC)
        {
            if (mlcLabels.contains("<MLC") && mlcLabels.contains("_SRC>"))
            {
                Button ucfFileNameButton = getView().findViewById(R.id.ucf_file_selection_mlc_button);
                ucfFileNameButton.setText(mConfiguration.getUcfFilename());

                // Divide the string
                String[] myDataRow = mlcLabels.split(";");

                int nDecTrees = myDataRow.length;

                for (int j = 0; j < nDecTrees; j++) {
                    int decTreeNumber;
                    String decTreeName;
                    ArrayList<String> decTreeOutputs = new ArrayList<>();
                    ArrayList<String> decTreeLabels = new ArrayList<>();

                    String[] separated = myDataRow[j].split(",");

                    // DecTree Number
                    decTreeNumber = Character.getNumericValue(separated[0].charAt(4));
                    // DecTree Name
                    decTreeName = separated[0].substring(10);
                    // DecTree Outputs/Labels
                    for (int i = 1; i < separated.length; i++) {
                        String[] separated2 = separated[i].split("=");
                        decTreeOutputs.add(separated2[0]);
                        decTreeLabels.add(separated2[1].substring(1, separated2[1].length() - 1));
                    }

                    fillMlcFsmView(feature, decTreeNumber, decTreeName, decTreeOutputs, decTreeLabels);
                }
            }
        }
        else if (feature == GET_OBJECT_FSM)
        {
            if (mlcLabels.contains("<FSM_OUTS") && mlcLabels.contains(">"))
            {
                Button ucfFileNameButton = getView().findViewById(R.id.ucf_file_selection_fsm_button);
                ucfFileNameButton.setText(mConfiguration.getUcfFilename());

                // Divide the string
                String[] myDataRow = mlcLabels.split(";");

                int nPrograms = myDataRow.length;

                for (int j = 0; j < nPrograms; j++) {
                    int programNumber;
                    String programName;
                    ArrayList<String> programOutputs = new ArrayList<>();
                    ArrayList<String> programLabels = new ArrayList<>();

                    String[] separated = myDataRow[j].split(",");

                    // Program Number / Name
                    if (separated[0].charAt(10) >= '0' && separated[0].charAt(10) <= '9') {
                        programNumber = Character.getNumericValue(separated[0].charAt(9)) * 10 + Character.getNumericValue(separated[0].charAt(10));
                        programName = separated[0].substring(12);
                    }
                    else {
                        programNumber = Character.getNumericValue(separated[0].charAt(9));
                        programName = separated[0].substring(11);
                    }
                    programNumber--;

                    // Program Outputs/Labels
                    for (int i = 1; i < separated.length; i++) {
                        String[] separated2 = separated[i].split("=");
                        programOutputs.add(separated2[0]);
                        programLabels.add(separated2[1].substring(1, separated2[1].length() - 1));
                    }

                    fillMlcFsmView(feature, programNumber, programName, programOutputs, programLabels);
                }
            }
        }
    }

    private LinearLayout getLabelRowLayout(int feature, int algoNumber)
    {
        LinearLayout labelsRowLayout = null;
        if (feature == GET_OBJECT_MLC)
        {
            switch (algoNumber)
            {
                case 0: labelsRowLayout = getView().findViewById(R.id.mlc_dectree0_labels_row); break;
                case 1: labelsRowLayout = getView().findViewById(R.id.mlc_dectree1_labels_row); break;
                case 2: labelsRowLayout = getView().findViewById(R.id.mlc_dectree2_labels_row); break;
                case 3: labelsRowLayout = getView().findViewById(R.id.mlc_dectree3_labels_row); break;
                case 4: labelsRowLayout = getView().findViewById(R.id.mlc_dectree4_labels_row); break;
                case 5: labelsRowLayout = getView().findViewById(R.id.mlc_dectree5_labels_row); break;
                case 6: labelsRowLayout = getView().findViewById(R.id.mlc_dectree6_labels_row); break;
                case 7: labelsRowLayout = getView().findViewById(R.id.mlc_dectree7_labels_row); break;
                default: break;
            }
        }
        else if (feature == GET_OBJECT_FSM)
        {
            switch (algoNumber) {
                case 0: labelsRowLayout = getView().findViewById(R.id.fsm_program0_labels_row); break;
                case 1: labelsRowLayout = getView().findViewById(R.id.fsm_program1_labels_row); break;
                case 2: labelsRowLayout = getView().findViewById(R.id.fsm_program2_labels_row); break;
                case 3: labelsRowLayout = getView().findViewById(R.id.fsm_program3_labels_row); break;
                case 4: labelsRowLayout = getView().findViewById(R.id.fsm_program4_labels_row); break;
                case 5: labelsRowLayout = getView().findViewById(R.id.fsm_program5_labels_row); break;
                case 6: labelsRowLayout = getView().findViewById(R.id.fsm_program6_labels_row); break;
                case 7: labelsRowLayout = getView().findViewById(R.id.fsm_program7_labels_row); break;
                case 8: labelsRowLayout = getView().findViewById(R.id.fsm_program8_labels_row); break;
                case 9: labelsRowLayout = getView().findViewById(R.id.fsm_program9_labels_row); break;
                case 10: labelsRowLayout = getView().findViewById(R.id.fsm_program10_labels_row); break;
                case 11: labelsRowLayout = getView().findViewById(R.id.fsm_program11_labels_row); break;
                case 12: labelsRowLayout = getView().findViewById(R.id.fsm_program12_labels_row); break;
                case 13: labelsRowLayout = getView().findViewById(R.id.fsm_program13_labels_row); break;
                case 14: labelsRowLayout = getView().findViewById(R.id.fsm_program14_labels_row); break;
                case 15: labelsRowLayout = getView().findViewById(R.id.fsm_program15_labels_row); break;
                default: break;
            }
        }

        return labelsRowLayout;
    }

    private LinearLayout getLabelLayout(int feature, int algoNumber)
    {
        LinearLayout labelsLayout = null;
        if (feature == GET_OBJECT_MLC)
        {
            switch (algoNumber)
            {
                case 0: labelsLayout = getView().findViewById(R.id.mlc_dectree0_labels); break;
                case 1: labelsLayout = getView().findViewById(R.id.mlc_dectree1_labels); break;
                case 2: labelsLayout = getView().findViewById(R.id.mlc_dectree2_labels); break;
                case 3: labelsLayout = getView().findViewById(R.id.mlc_dectree3_labels); break;
                case 4: labelsLayout = getView().findViewById(R.id.mlc_dectree4_labels); break;
                case 5: labelsLayout = getView().findViewById(R.id.mlc_dectree5_labels); break;
                case 6: labelsLayout = getView().findViewById(R.id.mlc_dectree6_labels); break;
                case 7: labelsLayout = getView().findViewById(R.id.mlc_dectree7_labels); break;
                default: break;
            }
        }
        else if (feature == GET_OBJECT_FSM)
        {
            switch (algoNumber)
            {
                case 0: labelsLayout = getView().findViewById(R.id.fsm_program0_labels); break;
                case 1: labelsLayout = getView().findViewById(R.id.fsm_program1_labels); break;
                case 2: labelsLayout = getView().findViewById(R.id.fsm_program2_labels); break;
                case 3: labelsLayout = getView().findViewById(R.id.fsm_program3_labels); break;
                case 4: labelsLayout = getView().findViewById(R.id.fsm_program4_labels); break;
                case 5: labelsLayout = getView().findViewById(R.id.fsm_program5_labels); break;
                case 6: labelsLayout = getView().findViewById(R.id.fsm_program6_labels); break;
                case 7: labelsLayout = getView().findViewById(R.id.fsm_program7_labels); break;
                case 8: labelsLayout = getView().findViewById(R.id.fsm_program8_labels); break;
                case 9: labelsLayout = getView().findViewById(R.id.fsm_program9_labels); break;
                case 10: labelsLayout = getView().findViewById(R.id.fsm_program10_labels); break;
                case 11: labelsLayout = getView().findViewById(R.id.fsm_program11_labels); break;
                case 12: labelsLayout = getView().findViewById(R.id.fsm_program12_labels); break;
                case 13: labelsLayout = getView().findViewById(R.id.fsm_program13_labels); break;
                case 14: labelsLayout = getView().findViewById(R.id.fsm_program14_labels); break;
                case 15: labelsLayout = getView().findViewById(R.id.fsm_program15_labels); break;
                default: break;
            }
        }

        return labelsLayout;
    }

    private EditText getAlgoNameEditText(int feature, int algoNumber)
    {
        EditText algoNameEditText = null;
        if (feature == GET_OBJECT_MLC)
        {
            switch (algoNumber)
            {
                case 0: algoNameEditText = getView().findViewById(R.id.mlc_dectree0_name); break;
                case 1: algoNameEditText = getView().findViewById(R.id.mlc_dectree1_name); break;
                case 2: algoNameEditText = getView().findViewById(R.id.mlc_dectree2_name); break;
                case 3: algoNameEditText = getView().findViewById(R.id.mlc_dectree3_name); break;
                case 4: algoNameEditText = getView().findViewById(R.id.mlc_dectree4_name); break;
                case 5: algoNameEditText = getView().findViewById(R.id.mlc_dectree5_name); break;
                case 6: algoNameEditText = getView().findViewById(R.id.mlc_dectree6_name); break;
                case 7: algoNameEditText = getView().findViewById(R.id.mlc_dectree7_name); break;
                default: break;
            }
        }
        else if (feature == GET_OBJECT_FSM)
        {
            switch (algoNumber)
            {
                case 0: algoNameEditText = getView().findViewById(R.id.fsm_program0_name); break;
                case 1: algoNameEditText = getView().findViewById(R.id.fsm_program1_name); break;
                case 2: algoNameEditText = getView().findViewById(R.id.fsm_program2_name); break;
                case 3: algoNameEditText = getView().findViewById(R.id.fsm_program3_name); break;
                case 4: algoNameEditText = getView().findViewById(R.id.fsm_program4_name); break;
                case 5: algoNameEditText = getView().findViewById(R.id.fsm_program5_name); break;
                case 6: algoNameEditText = getView().findViewById(R.id.fsm_program6_name); break;
                case 7: algoNameEditText = getView().findViewById(R.id.fsm_program7_name); break;
                case 8: algoNameEditText = getView().findViewById(R.id.fsm_program8_name); break;
                case 9: algoNameEditText = getView().findViewById(R.id.fsm_program9_name); break;
                case 10: algoNameEditText = getView().findViewById(R.id.fsm_program10_name); break;
                case 11: algoNameEditText = getView().findViewById(R.id.fsm_program11_name); break;
                case 12: algoNameEditText = getView().findViewById(R.id.fsm_program12_name); break;
                case 13: algoNameEditText = getView().findViewById(R.id.fsm_program13_name); break;
                case 14: algoNameEditText = getView().findViewById(R.id.fsm_program14_name); break;
                case 15: algoNameEditText = getView().findViewById(R.id.fsm_program15_name); break;
                default: break;
            }
        }


        return algoNameEditText;
    }

    private void addSingleLabelRow(final int feature, final int algoNumber, String output, String label)
    {
        LinearLayout decTreeLabelRowLayout = getLabelRowLayout(feature, algoNumber);

        View labelView = View.inflate(getContext(), R.layout.mlc_fsm_label_item, null);
        labelView.setId(nLabels);
        nLabels++;

        // DECTREE_OUTPUT_EDITTEXT
        EditText outputEditText = labelView.findViewById(R.id.output_edittext);
        outputEditText.setId(nLabels);
        nLabels++;
        outputEditText.setText(String.valueOf(output));
        outputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (feature == GET_OBJECT_MLC)
                    mConfiguration.setMlcLabels(generateLabelString(GET_OBJECT_MLC));
                else if (feature == GET_OBJECT_FSM)
                    mConfiguration.setFsmLabels(generateLabelString(GET_OBJECT_FSM));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // DECTREE_LABEL_EDITTEXT
        EditText labelEditText = labelView.findViewById(R.id.label_edittext);
        labelEditText.setId(nLabels);
        nLabels++;
        labelEditText.setText(label);
        labelEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (feature == GET_OBJECT_MLC)
                    mConfiguration.setMlcLabels(generateLabelString(GET_OBJECT_MLC));
                else if (feature == GET_OBJECT_FSM)
                    mConfiguration.setFsmLabels(generateLabelString(GET_OBJECT_FSM));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // REMOVE_LABEL_BUTTON
        ImageView removeLabelImageView = labelView.findViewById(R.id.remove_label_imageview);
        removeLabelImageView.setId(nLabels);
        nLabels++;
        removeLabelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                LinearLayout decTreeLabelRowLayout = getLabelRowLayout(feature, algoNumber);
                View labelView = getView().findViewById(id - 3); // get row view
                decTreeLabelRowLayout.removeView(labelView);
                if (feature == GET_OBJECT_MLC)
                    mConfiguration.setMlcLabels(generateLabelString(GET_OBJECT_MLC));
                else if (feature == GET_OBJECT_FSM)
                    mConfiguration.setFsmLabels(generateLabelString(GET_OBJECT_FSM));
            }
        });

        decTreeLabelRowLayout.addView(labelView);

        if (feature == GET_OBJECT_MLC)
            mConfiguration.setMlcLabels(generateLabelString(GET_OBJECT_MLC));
        else if (feature == GET_OBJECT_FSM)
            mConfiguration.setFsmLabels(generateLabelString(GET_OBJECT_FSM));
    }

    private void fillMlcFsmView(int feature, int algoNumber, String algoName, ArrayList<String> algoOutputs, ArrayList<String> algoLabels)
    {
        if (feature == GET_OBJECT_MLC)
        {
            EditText algoNameEditText = getAlgoNameEditText(feature, algoNumber);
            algoNameEditText.setText(algoName);
            for (int i = 0; i < algoOutputs.size(); i++)
            {
                addSingleLabelRow(feature, algoNumber, algoOutputs.get(i), algoLabels.get(i));
            }
        }
        else if (feature == GET_OBJECT_FSM)
        {
            EditText algoNameEditText = getAlgoNameEditText(feature, algoNumber);
            algoNameEditText.setText(algoName);
            /*
            for (int i = 0; i < algoOutputs.size(); i++)
            {
                addSingleLabelRow(feature, algoNumber, algoOutputs.get(i), algoLabels.get(i));
            }
            */
        }

    }

    private String generateLabelString(int feature)
    {
        String labels = "";
        int nAlgos = 0;

        if (feature == GET_OBJECT_MLC)
            nAlgos = 8;
        else if (feature == GET_OBJECT_FSM)
            nAlgos = 16;

        for (int i = 0; i < nAlgos; i++) {
            LinearLayout labelRowLayout = getLabelRowLayout(feature, i);
            int nLabels = labelRowLayout.getChildCount();

            if (nLabels > 0) {
                String algoName = getAlgoNameEditText(feature, i).getText().toString();
                String label_tmp = "";
                if (feature == GET_OBJECT_MLC)
                    label_tmp = "<MLC" + i + "_SRC>" + algoName;
                if (feature == GET_OBJECT_FSM)
                    label_tmp = "<FSM_OUTS" + i + ">" + algoName;

                for(int j = 0; j < nLabels; j++) {
                    if(labelRowLayout.getChildAt(j) instanceof LinearLayout) {
                        LinearLayout labelRow = (LinearLayout) labelRowLayout.getChildAt(j);
                        int nElements = labelRow.getChildCount();
                        for(int k = 0; k < nElements; k++) {
                            // this is the output
                            if(labelRow.getChildAt(k) instanceof EditText) {
                                String item = ((EditText) labelRow.getChildAt(k)).getText().toString();
                                label_tmp = label_tmp.concat(",");
                                label_tmp = label_tmp.concat(item);
                                label_tmp = label_tmp.concat("=");
                                k++;
                            }
                            // this is the label
                            if (labelRow.getChildAt(k) instanceof EditText) {
                                String item = ((EditText) labelRow.getChildAt(k)).getText().toString();
                                label_tmp = label_tmp.concat("'");
                                label_tmp = label_tmp.concat(item);
                                label_tmp = label_tmp.concat("'");
                                k++;
                                k++;
                            }
                        }
                    }
                }

                labels = labels.concat(label_tmp);
                labels = labels.concat(";");
            }
        }

        return labels;
    }

    private void eraseLabelViews(int feature)
    {
        for (int i = 0; i < 8; i++) {
            LinearLayout labelRowLayout = getLabelRowLayout(feature, i);
            if((labelRowLayout).getChildCount() > 0)
                (labelRowLayout).removeAllViews();
        }

        nLabels = 0;
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void parseUcfFile(int feature, Uri uri) {
        String regConfig = "";
        String labels = "";
        String ucfFilename;
        boolean steval_supported = false;
        boolean stmc_page = false;
        boolean mlc_enabled = false;
        boolean fsm_enabled = false;

        // erase json regConfig field
        mConfiguration.setRegConfig("");

        // Parse config file
        InputStream inputStream = null;
        try {
            inputStream = getContext().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (inputStream != null) {
            BufferedReader myReader = new BufferedReader( new InputStreamReader(inputStream));
            String myDataRow;

            try
            {
                while ((myDataRow = myReader.readLine()) != null)
                {
                    // Check steval is correct
                    if (myDataRow.contains("LSM6DSOX")) {
                        steval_supported = true;
                    }
                    // MLC labels in ucf header
                    if (myDataRow.contains("<MLC") && myDataRow.contains("_SRC>")) {
                        myDataRow = myDataRow.substring(3);
                        labels = labels.concat(myDataRow);
                        labels = labels.concat(";");
                    }
                    // FSM labels in ucf header
                    if (myDataRow.contains("<FSM_OUTS") && myDataRow.contains(">")) {
                        myDataRow = myDataRow.substring(3);
                        labels = labels.concat(myDataRow);
                        labels = labels.concat(";");
                    }

                    // Valid ucf file row
                    if (myDataRow.contains("Ac"))
                    {
                        String[] separated = myDataRow.split(" ");
                        String regAddress = separated[1];
                        String regValue = separated[2];
                        regConfig = regConfig.concat(regAddress);
                        regConfig = regConfig.concat(regValue);

                        if (regAddress.equals("01") && regValue.equals("80")) {
                            stmc_page = true;
                        }
                        else if (myDataRow.contains("01 00"))  {
                            stmc_page = false;
                        }

                        if (stmc_page) {
                            if (regAddress.equals("05")) {
                                byte reg = Byte.parseByte(regValue, 16);
                                reg &= 0x01;
                                fsm_enabled = reg > 0;
                            }
                            if (regAddress.equals("05")) {
                                byte reg = Byte.parseByte(regValue, 16);
                                reg &= 0x10;
                                mlc_enabled = reg > 0;
                            }
                        }
                    }
                }
                myReader.close();
            }
            catch (Exception e) {
                steval_supported = false;
            }
        }

        if (steval_supported) {
            if (feature == GET_OBJECT_MLC) {
                if (mlc_enabled) {
                    ucfFilename = getFileName(uri);

                    try {
                        mConfiguration.setUcfFilename(ucfFilename);
                        mConfiguration.setRegConfig(regConfig);
                        if (labels.isEmpty()) {
                            labels = generateDefaultLabels(GET_OBJECT_MLC);
                        }
                        mConfiguration.setMlcLabels(labels);
                        eraseLabelViews(GET_OBJECT_MLC);
                        loadUI(GET_OBJECT_MLC, labels);
                    } catch (Exception ignored) {
                        mConfiguration.setUcfFilename("");
                        mConfiguration.setRegConfig("");
                        mConfiguration.setMlcLabels("");
                        eraseLabelViews(GET_OBJECT_MLC);
                    }
                } else {
                    DialogHelper.showDialog(getActivity(), getString(R.string.ucf_file_mlc_disabled), null);
                }
            }
            else if (feature == GET_OBJECT_FSM) {
                if (fsm_enabled) {
                    ucfFilename = getFileName(uri);

                    try {
                        mConfiguration.setUcfFilename(ucfFilename);
                        mConfiguration.setRegConfig(regConfig);
                        if (labels.isEmpty()) {
                            labels = generateDefaultLabels(GET_OBJECT_FSM);
                        }
                        mConfiguration.setFsmLabels(labels);
                        eraseLabelViews(GET_OBJECT_FSM);
                        loadUI(GET_OBJECT_FSM, labels);
                    } catch (Exception ignored) {
                        mConfiguration.setUcfFilename("");
                        mConfiguration.setRegConfig("");
                        mConfiguration.setFsmLabels("");
                        eraseLabelViews(GET_OBJECT_FSM);
                    }
                } else {
                    DialogHelper.showDialog(getActivity(), getString(R.string.ucf_file_fsm_disabled), null);
                }
            }
        }
        else {
            DialogHelper.showDialog(getActivity(), getString(R.string.ucf_file_steval_unsupported), null);
        }
    }

    private String generateDefaultLabels(int feature) {
        String labels = "";
        if (feature == GET_OBJECT_MLC) {
            String registerA = "<MLC";
            String registerB = "_SRC>";
            String dectree = "DT";
            for (int i = 0; i < 8; i++) {
                labels = labels.concat(registerA).concat("" + i).concat(registerB).concat(dectree).concat("" + (i + 1)).concat(";");
            }
        } else if (feature == GET_OBJECT_FSM) {
            String registerA = "<FSM_OUTS";
            String registerB = ">";
            String dectree = "FSM";
            for (int i = 0; i < 16; i++) {
                labels = labels.concat(registerA).concat("" + (i + 1)).concat(registerB).concat(dectree).concat("" + (i + 1)).concat(";");
            }
        }
        return labels;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == REQUEST_BROWSE_FILE_MLC)
                parseUcfFile(GET_OBJECT_MLC, uri);
            else if (requestCode == REQUEST_BROWSE_FILE_FSM)
                parseUcfFile(GET_OBJECT_FSM, uri);
        }
    }

    private void fillAcquisitionTime() {

        if (mSensor.getAcquisitionTime() != null) {
            getView().findViewById(R.id.acquisition_time_container).setVisibility(View.VISIBLE);
            EditText acquisitionTimeEditText = getView().findViewById(R.id.acquisition_time_value);
            acquisitionTimeEditText.setText(String.format(Locale.UK, "%.1f", SensorConfigurationKt.getAcquisitionTimeMin(mConfiguration)));
            acquisitionTimeEditText.addTextChangedListener(new TextWatcherImpl() {
                @Override
                public void afterTextChanged(@org.jetbrains.annotations.Nullable final Editable editable) {
                    try {
                        SensorConfigurationKt.setAcquisitionTimeMin(mConfiguration,Double.parseDouble(editable.toString()));
                    } catch (Exception ignored) {
                        SensorConfigurationKt.setAcquisitionTimeMin(mConfiguration,0d);
                    }
                }
            });
        }
    }

    private void fillPowerModeSection() {

        if (mSensor.getPowerModes() != null) {

            if (mConfiguration.getPowerMode() == null) {
                mConfiguration.setPowerMode(mSensor.getPowerModes().get(0).getMode());
            }

            if (mSensor.getPowerModes().size() > 1) {
                getView().findViewById(R.id.power_mode_container).setVisibility(View.VISIBLE);

                RadioGroup pmRadioGroup = getView().findViewById(R.id.power_mode_radiogroup);
                pmRadioGroup.removeAllViews();

                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int spacing = (int) DeviceHelperKt.convertDpToPixel(8, getContext());
                params.setMargins(spacing, spacing, spacing, spacing);

                for (final PowerMode powerMode : mSensor.getPowerModes()) {
                    RadioButton radioButton = new RadioButton(getContext());
                    radioButton.setLayoutParams(params);
                    radioButton.setText(powerMode.getLabel());
                    radioButton.setId(powerMode.getMode().getId());
                    radioButton.setChecked(powerMode.getMode().getId() == mConfiguration.getPowerMode().getId());
                    pmRadioGroup.addView(radioButton);

                    radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
                            if (isChecked) {
                                PowerMode.Mode newMode = PowerMode.Mode.values()[compoundButton.getId()];
                                if (mConfiguration.getPowerMode() != newMode) {
                                    mConfiguration.setPowerMode(newMode);
                                    mConfiguration.setOdr(null);
                                    mConfiguration.getFilters().lowPass = null;
                                    mConfiguration.getFilters().highPass = null;
                                    fillView();
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private void fillOdr() {

        getView().findViewById(R.id.odr_container).setVisibility(mSensor.getPowerModes() != null ? View.VISIBLE : View.GONE);

        if (mSensor.getPowerModes() != null) {

            AppCompatSpinner spinner = getView().findViewById(R.id.odr_spinner);
            spinner.setOnItemSelectedListener(null);

            PowerMode.Mode mode = mConfiguration.getPowerMode();
            final PowerMode selectedMode = SensorHelper.getPowerModeBySelectedMode(mSensor, mode);

            if (selectedMode != null) {

                final boolean hasCustomOdr = selectedMode.getMinCustomOdr() != null;
                getView().findViewById(R.id.custom_odr_container).setVisibility(hasCustomOdr ? View.VISIBLE : View.GONE);

                CheckBox customOdrCheckbox = getView().findViewById(R.id.custom_odr_checkbox);
                customOdrCheckbox.setOnCheckedChangeListener(null);

                final EditText customOdrEditText = getView().findViewById(R.id.custom_odr_value);
                customOdrEditText.removeTextChangedListener(customOdrTextWatcher);

                if (hasCustomOdr) {

                    if (mConfiguration.getOneShotTime() != null) {
                        customOdrCheckbox.setChecked(true);
                        customOdrEditText.setText(String.format(Locale.UK, "%.2f", mConfiguration.getOneShotTime()));
                    }

                    customOdrEditText.setEnabled(customOdrCheckbox.isChecked());

                    TextView customOdrHintTextView = getView().findViewById(R.id.custom_odr_hint);
                    customOdrHintTextView.setText(String.format(Locale.UK, getString(R.string.custom_odr_min_value), selectedMode.getMinCustomOdr()));

                    customOdrCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
                            if (!isChecked) {
                                mConfiguration.setOneShotTime(null);
                            }

                            fillView();
                        }
                    });

                    customOdrEditText.addTextChangedListener(customOdrTextWatcher);
                }

                List<String> adapterItems = getSpinnerVoices(selectedMode.getOdrs(), "Hz");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_row, R.id.row_value, adapterItems);
                spinner.setAdapter(adapter);
                spinner.setEnabled(!customOdrCheckbox.isChecked());

                if (mConfiguration.getOdr() == null) {
                    mConfiguration.setOdr(selectedMode.getOdrs().get(0));
                }

                int selectedPosition = getSelectedIndexFromValue(selectedMode.getOdrs(), mConfiguration.getOdr());
                spinner.setSelection(selectedPosition);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(final AdapterView<?> adapterView, final View view, final int position, final long l) {
                        double newValue = selectedMode.getOdrs().get(position);
                        if (mConfiguration.getOdr() != newValue) {
                            mConfiguration.setOdr(newValue);
                            mConfiguration.getFilters().lowPass = null;
                            mConfiguration.getFilters().highPass = null;
                            fillView();
                        }
                    }

                    @Override
                    public void onNothingSelected(final AdapterView<?> adapterView) {
                        //nothing to do
                    }
                });
            }
        }
    }

    private TextWatcherImpl customOdrTextWatcher = new TextWatcherImpl() {
        @Override
        public void afterTextChanged(@org.jetbrains.annotations.Nullable final Editable editable) {
            try {
                mConfiguration.setOneShotTime(Double.parseDouble(editable.toString()));
            } catch (Exception e) {
                mConfiguration.setOneShotTime(null);
            }

            mConfiguration.getFilters().lowPass = null;
            mConfiguration.getFilters().highPass = null;
            fillFilter();
        }
    };

    private void fillFilter() {

        SensorFilter sensorFilter = SensorFilterHelper.getSensorFiltersBySensorId(getContext(), mSensorId,mBoard);
        getView().findViewById(R.id.lowpass_filters_container).setVisibility(View.GONE);
        getView().findViewById(R.id.high_filters_container).setVisibility(View.GONE);

        if (sensorFilter == null) {
            return;
        }

        final PowerMode.Mode powerMode = mConfiguration.getPowerMode() != null ? mConfiguration.getPowerMode() : PowerMode.Mode.NONE;

        boolean hasCustomOdr = mConfiguration.getOneShotTime() != null;

        double odr = mConfiguration.getOdr() != null ? mConfiguration.getOdr() : -1;
        final Filter filter = SensorFilterHelper.getAvailableFilter(sensorFilter, powerMode, odr);

        if (filter == null) {
            if (!hasCustomOdr) {
                return;
            }
        }

        final List<CutOff> lowPassCutoffs = filter != null ? filter.getLowPass() : sensorFilter.getValues().get(0).getFilters().get(0).getLowPass();
        if (!lowPassCutoffs.isEmpty()) {
            getView().findViewById(R.id.lowpass_filters_container).setVisibility(View.VISIBLE);
            prepareFilterSpinner(R.id.lowpass_filters_spinner, lowPassCutoffs,
                    mConfiguration.getFilters().lowPass, new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(final AdapterView<?> adapterView, final View view, final int position, final long l) {
                            mConfiguration.getFilters().lowPass = position == 0 ? null : lowPassCutoffs.get(position - 1);
                        }

                        @Override
                        public void onNothingSelected(final AdapterView<?> adapterView) {
                            //nothing to do
                        }
                    });
        }

        if (filter != null && !filter.getHighPass().isEmpty()) {
            getView().findViewById(R.id.high_filters_container).setVisibility(View.VISIBLE);
            prepareFilterSpinner(R.id.high_filters_spinner, filter.getHighPass(), mConfiguration.getFilters().highPass, new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> adapterView, final View view, final int position, final long l) {
                    mConfiguration.getFilters().highPass = position == 0 ? null : filter.getHighPass().get(position - 1);
                }

                @Override
                public void onNothingSelected(final AdapterView<?> adapterView) {
                    // nothing to do
                }
            });
        }
    }

    private void prepareFilterSpinner(int spinnerId, List<CutOff> cutOffs, CutOff selectedCutoff, AdapterView.OnItemSelectedListener listener) {
        AppCompatSpinner spinner = getView().findViewById(spinnerId);
        spinner.setOnItemSelectedListener(null);

        List<String> cutOffLabels = new ArrayList<>();
        cutOffLabels.add(getString(R.string.no_filter));
        for (CutOff cutOff : cutOffs) {
            cutOffLabels.add(getString(R.string.sensor_adapter_row_s, cutOff.getLabel(), "Hz"));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_filter_row, R.id.filter_value, cutOffLabels);
        spinner.setAdapter(adapter);

        spinner.setSelection(getSelectedCutOffValueIndex(cutOffs, selectedCutoff));
        spinner.setOnItemSelectedListener(listener);
    }

    private void fillFullScale() {

        if (mSensor.getFullScales() != null) {
            getView().findViewById(R.id.fs_container).setVisibility(View.VISIBLE);

            AppCompatSpinner spinner = getView().findViewById(R.id.fs_spinner);
            spinner.setOnItemSelectedListener(null);

            if (mConfiguration.getFullScale() == null) {
                mConfiguration.setFullScale(mSensor.getFullScales().get(0));
            }

            List<String> adapterItems = getSpinnerVoices(mSensor.getFullScales(), mSensor.getFullScaleUm());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_row, R.id.row_value, adapterItems);
            spinner.setAdapter(adapter);

            spinner.setSelection(getSelectedIndexFromValue(mSensor.getFullScales(), mConfiguration.getFullScale()));
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                    int newValue = mSensor.getFullScales().get(i);
                    if (mConfiguration.getFullScale() != newValue) {
                        mConfiguration.setFullScale(newValue);
                    }
                }

                @Override
                public void onNothingSelected(final AdapterView<?> adapterView) {
                    //nothing to do
                }
            });
        }
    }

    private boolean validateCustomOdrValue() {

        CheckBox customOdrCheckbox = getView().findViewById(R.id.custom_odr_checkbox);
        if (customOdrCheckbox.isChecked()) {
            final PowerMode selectedMode = SensorHelper.getPowerModeBySelectedMode(mSensor, mConfiguration.getPowerMode());
            return mConfiguration.getOneShotTime() != null && mConfiguration.getOneShotTime() >= selectedMode.getMinCustomOdr();
        }

        return true;
    }

    private List<String> getSpinnerVoices(List<? extends Number> values, String um) {
        List<String> strValues = new ArrayList<>();
        for (Number value : values) {
            strValues.add(getString(R.string.sensor_adapter_row_s, value.toString(), um));
        }

        return strValues;
    }

    private int getSelectedIndexFromValue(List<? extends Number> values, Number selectedValue) {
        for (int i = 0; i < values.size(); i++) {
            Number value = values.get(i);
            if (value.equals(selectedValue)) {
                return i;
            }
        }

        return -1;
    }

    private int getSelectedCutOffValueIndex(List<CutOff> cutoffs, CutOff selectedCutOff) {
        if (selectedCutOff != null) {
            for (int i = 0; i < cutoffs.size(); i++) {
                CutOff cutOff = cutoffs.get(i);
                if (cutOff.getValue() == selectedCutOff.getValue()) {
                    return i + 1;
                }
            }
        }

        return -1;
    }
}
