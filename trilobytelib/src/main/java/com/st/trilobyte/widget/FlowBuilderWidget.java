package com.st.trilobyte.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.daimajia.swipe.SwipeLayout;
import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.helper.SensorFilterHelper;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.Function;
import com.st.trilobyte.models.Output;
import com.st.trilobyte.models.Sensor;

public class FlowBuilderWidget extends LinearLayout {

    private LayoutInflater inflater;

    private View view;

    private String title;

    private String emptyMessage;

    private WidgetClickListener clickListener;

    public FlowBuilderWidget(final Context context) {
        super(context);
        init();
    }

    public FlowBuilderWidget(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlowBuilderWidget, 0, 0);

        try {
            title = a.getString(R.styleable.FlowBuilderWidget_title);
            emptyMessage = a.getString(R.styleable.FlowBuilderWidget_empty_message);
        } finally {
            a.recycle();
        }

        init();
    }

    public FlowBuilderWidget(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(R.layout.flow_builder_widget, this, false);

        TextView emptyTextview = view.findViewById(R.id.empty_textview);
        emptyTextview.setText(emptyMessage);

        emptyTextview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (clickListener != null) {
                    clickListener.onWidgetSelected();
                }
            }
        });

        TextView titleTextView = view.findViewById(R.id.title);
        titleTextView.setText(title);

        addView(view);
    }

    public void addSensor(final Sensor sensor, Node.Type board) {
        boolean hasSettings = sensor.hasSettings() || SensorFilterHelper.hasFilter(getContext(), sensor.getId(),board);
        addItem(sensor.getDescription(), R.drawable.ic_sensor, hasSettings, false);
    }

    public void addFlow(final Flow flow) {
        addItem(flow.getDescription(), R.drawable.ic_input, false, false);
    }

    public void addFunction(final Function function) {
        addItem(function.getDescription(), R.drawable.ic_function, function.getHasSettings(), true);
    }

    public void addOutput(final Output output) {
        int resourceId = getResources().getIdentifier(output.getIcon(), "drawable", getContext().getPackageName());
        boolean hasSettings = output.getProperties() != null;
        addItem(output.getDescription(), resourceId, hasSettings, false);
    }

    private void addItem(String description, int resId, boolean hasSettings, boolean swipable) {

        view.findViewById(R.id.empty_textview).setVisibility(GONE);

        LinearLayout itemContainer = view.findViewById(R.id.item_container);

        int layoutId = swipable ? R.layout.flow_widget_swipable_cell : R.layout.flow_widget_cell;
        View row = inflater.inflate(layoutId, this, false);
        if (swipable) {
            SwipeLayout swipeLayout = row.findViewById(R.id.swipable_widget_row);
            swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        }

        row.findViewById(R.id.item_settings).setVisibility(hasSettings ? VISIBLE : GONE);

        final int index = itemContainer.getChildCount();

        if (swipable) {
            row.findViewById(R.id.delete_container).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (clickListener != null) {
                        clickListener.onDeleteItemSelected(index);
                    }
                }
            });
        } else {
            row.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (clickListener != null) {
                        clickListener.onItemSelected(index);
                    }
                }
            });
        }

        if (hasSettings) {
            row.findViewById(R.id.item_settings).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (clickListener != null) {
                        clickListener.onSettingSelected(index);
                    }
                }
            });
        }

        ImageView itemImage = row.findViewById(R.id.item_image);
        itemImage.setImageResource(resId);

        TextView descriptionTextView = row.findViewById(R.id.item_textview);
        descriptionTextView.setText(description);

        itemContainer.addView(row);
    }

    public void clear() {
        LinearLayout itemContainer = view.findViewById(R.id.item_container);
        itemContainer.removeAllViews();
        view.findViewById(R.id.empty_textview).setVisibility(VISIBLE);
        setCompleted(false);
    }

    public void setCompleted(boolean isCompleted) {
        int color = isCompleted ? getResources().getColor(R.color.widgetCompletedHeaderBgColor) : getResources().getColor(R.color.widgetHeaderBgColor);
        view.findViewById(R.id.widget_header).setBackgroundColor(color);
        view.findViewById(R.id.complete_icon).setVisibility(isCompleted ? VISIBLE : GONE);
    }

    public void setWidgetListener(final WidgetClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // interfaces

    public interface WidgetClickListener {

        void onWidgetSelected();

        void onItemSelected(int index);

        void onSettingSelected(int index);

        void onDeleteItemSelected(int index);
    }
}
