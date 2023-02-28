package com.st.trilobyte.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.helper.SensorHelper;
import com.st.trilobyte.models.Sensor;

public class SensorDetailActivity extends AppCompatActivity {

    public final static String EXTRA_SENSOR_KEY = "extra-sensor-key";

    public final static String EXTRA_BOARD_TYPE = "extra-board_type";

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_detail);

        getSupportActionBar().setTitle(getString(R.string.sensor));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() == null || getIntent().getExtras() == null) {
            finish();
            return;
        }

        Bundle bundle = getIntent().getExtras();
        Sensor sensor = (Sensor) bundle.getSerializable(EXTRA_SENSOR_KEY);
        Node.Type board = (Node.Type) bundle.getSerializable(EXTRA_BOARD_TYPE);
        fillView(sensor,board);
    }

    private void fillView(final Sensor sensor, final Node.Type board) {

        ImageView sensorImage = findViewById(R.id.sensor_image);
        int resourceId = getResources().getIdentifier(sensor.getIcon(), "drawable", getPackageName());
        sensorImage.setImageResource(resourceId);

        TextView sensorName = findViewById(R.id.sensor_description);
        sensorName.setText(sensor.getDescription());

        TextView outputTextview = findViewById(R.id.output_textview);
        outputTextview.setText(sensor.getOutput());

        TextView unitTextview = findViewById(R.id.unit_textview);
        unitTextview.setText(sensor.getUm());

        TextView dataTypeTextview = findViewById(R.id.data_type_textview);
        dataTypeTextview.setText(sensor.getDataType());

        TextView propertiesTextview = findViewById(R.id.properties_textview);
        propertiesTextview.setText(SensorHelper.getSensorPropertiesDescription(this, sensor,board));

        TextView notesTextview = findViewById(R.id.notes_textview);
        notesTextview.setText(sensor.getNotes());

        TextView modelTextview = findViewById(R.id.model_textview);
        modelTextview.setText(sensor.getModel());

        TextView datasheetTextview = findViewById(R.id.datasheet);
        datasheetTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent openUrlIntent = new Intent(Intent.ACTION_VIEW);
                openUrlIntent.setData(Uri.parse(sensor.getDatasheetLink()));
                startActivity(openUrlIntent);
            }
        });
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
}
