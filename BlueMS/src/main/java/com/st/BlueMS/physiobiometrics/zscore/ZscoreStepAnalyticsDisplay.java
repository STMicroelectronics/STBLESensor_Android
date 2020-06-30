package com.st.BlueMS.physiobiometrics.zscore;

import android.content.Context;
import android.graphics.Color;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ZscoreStepAnalyticsDisplay {

    TableRow.LayoutParams labelColumn;
    TableRow.LayoutParams dataColumn;
    TableRow.LayoutParams unitsColumn;
    DecimalFormat df2;

    public ZscoreStepAnalyticsDisplay() {
        labelColumn = new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT,1f);
        //labelColumn.setMargins(5,5,5,5);
        labelColumn.weight = 13;
        dataColumn = new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT,1f);
        //dataColumn.setMargins(5,5,5,5);
        dataColumn.weight = 3;
        unitsColumn = new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT,1f);
        //unitsColumn.setMargins(5,5,5,5);
        unitsColumn.weight = 4;

        df2 = new DecimalFormat("#.##");
    }

    private TableRow tableHeader(Context context) {
        TableRow tr_head = new TableRow(context);
        tr_head.setBackgroundColor(Color.GRAY);
        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        TextView labelName = new TextView(context);
        labelName.setText("Analysis");
        labelName.setTextColor(Color.WHITE);
        labelName.setPadding(1, 1, 1, 1);
        tr_head.addView(labelName,labelColumn);// add the column to the table row here

        TextView value = new TextView(context);
        value.setText("Value"); // set the text for the header
        value.setTextColor(Color.WHITE); // set the color
        value.setPadding(1, 1, 1, 1); // set the padding (if required)
        tr_head.addView(value,dataColumn); // add the column to the table row here

        TextView units = new TextView(context);
        units.setText("Units"); // set the text for the header
        units.setTextColor(Color.WHITE); // set the color
        units.setPadding(1, 1, 1, 1);// set the padding (if required)
        tr_head.addView(units,unitsColumn); // add the column to the table row here
        return tr_head;
    }
    private TableRow tableData(Context context, String label, String val, String u) {
        TableRow tr_data = new TableRow(context);
        tr_data.setBackgroundColor(Color.WHITE);
        tr_data.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        TextView labelName = new TextView(context);
        labelName.setText(label);
        labelName.setTextColor(Color.BLACK);
        labelName.setTextSize(10);
        labelName.setPadding(1, 1, 1, 1);
        tr_data.addView(labelName,labelColumn);// add the column to the table row here

        TextView value = new TextView(context);
        value.setText(val); // set the text for the header
        value.setTextSize(10);
        value.setTextColor(Color.BLACK); // set the color
        value.setPadding(1, 1, 1, 1); // set the padding (if required)
        tr_data.addView(value,dataColumn); // add the column to the table row here

        TextView units = new TextView(context);
        units.setText(u); // set the text for the header
        units.setTextSize(10);
        units.setTextColor(Color.BLACK); // set the color
        units.setPadding(1, 1, 1, 1); // set the padding (if required)
        tr_data.addView(units,unitsColumn); // add the column to the table row here
        return tr_data;
    }

    private TableRow tableSingleRow(Context context, String val) {
        TableRow tr_data = new TableRow(context);
        tr_data.setBackgroundColor(Color.WHITE);
        tr_data.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        TextView labelName = new TextView(context);
        labelName.setText(val);
        labelName.setTextSize(10);
        labelName.setTextColor(Color.BLACK);
        labelName.setPadding(5, 5, 5, 5);
        tr_data.addView(labelName);// add the column to the table row here
        return tr_data;
    }

    public void results(Context context, TableLayout tl,  ZscoreStepCalculations sc,
                        double threshold, int sessionLength, String filename) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = Calendar.getInstance().getTime();

        tl.setShrinkAllColumns(true);
        tl.setStretchAllColumns(true);

        tl.addView(this.tableSingleRow(context, today.toString()));
        tl.addView(this.tableSingleRow(context, "file: " + filename));
        tl.addView(this.tableHeader(context));
        tl.addView(this.tableData(context,"sampling rate",String.valueOf(sc.freqHZ), "Hz"));
        tl.addView(this.tableData(context,"Session length",df2.format(sessionLength),"s"));
        tl.addView(this.tableData(context,"Good Step Threshold",df2.format(threshold),"d/s"));
        tl.addView(this.tableData(context,"Walking time",df2.format(sc.timeWalk),"s"));
        tl.addView(this.tableData(context,"Total Steps",String.valueOf(sc.nSteps),"count"));
        tl.addView(this.tableData(context,"Good Steps",String.valueOf(sc.ngood),df2.format(sc.pcgood)+"%"));
        tl.addView(this.tableData(context,"Bad Steps",String.valueOf(sc.nbad),df2.format(sc.pcbad)+"%"));
        tl.addView(this.tableData(context,"Average step",df2.format(sc.stepmeantime),"s"));
        tl.addView(this.tableData(context,"Cadence",df2.format(sc.cadmean),"step/m"));
        tl.addView(this.tableData(context,"Heel Strike Angular Velocity (AV)",df2.format(sc.HeelStrikeAV),"d/s"));
        tl.addView(this.tableData(context,"Heel Strike AV Standard deviation",df2.format(sc.HeelStrikeAVSTD),"d/s"));
        tl.addView(this.tableData(context,"Heel Strike Coefficient of variation",df2.format(sc.HeelStrikeAVCV),"CV"));
        tl.addView(this.tableData(context,"Foot Swing Angular Velocity (AV)",df2.format(sc.FootSwingAV),"d/s"));
        tl.addView(this.tableData(context,"Foot Swing AV Standard deviation",df2.format(sc.FootSwingAVSTD),"d/s"));
        tl.addView(this.tableData(context,"Foot Swing Coefficient of variation",df2.format(sc.FootSwingAVCV),"CV"));
    }

    public String results(ZscoreStepCalculations sc, double threshold, int sessionLength, String filename) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = Calendar.getInstance().getTime();

        String results = today.toString() + System.getProperty("line.separator"); //+ " file: " + filename));
        results += "sampling rate," + String.valueOf(sc.freqHZ)+",Hz"+ System.getProperty("line.separator");
        //results += "file name,"+df2.format(sessionLength)+",s"+ System.getProperty("line.separator");
        results += "Session length,"+df2.format(sessionLength)+",s"+ System.getProperty("line.separator");
        results += "Threshold,"+df2.format(threshold)+",d/s"+ System.getProperty("line.separator");
        results += "Walking time,"+df2.format(sc.timeWalk)+",s"+ System.getProperty("line.separator");
        results += "Total Steps,"+String.valueOf(sc.nSteps)+",count"+ System.getProperty("line.separator");
        results += "Good Steps,"+String.valueOf(sc.ngood)+df2.format(sc.pcgood)+",%"+ System.getProperty("line.separator");
        results += "Bad Steps,"+String.valueOf(sc.nbad)+df2.format(sc.pcbad)+",%"+ System.getProperty("line.separator");
        results += "Average step,"+df2.format(sc.stepmeantime)+",s"+ System.getProperty("line.separator");
        results += "Cadence,"+df2.format(sc.cadmean)+",step/m"+ System.getProperty("line.separator");
        results += "Heel Strike Angular Velocity (AV),"+df2.format(sc.HeelStrikeAV)+",d/s"+ System.getProperty("line.separator");
        results += "Heel Strike AV Standard deviation,"+df2.format(sc.HeelStrikeAVSTD)+",d/s"+ System.getProperty("line.separator");
        results += "Heel Strike Coefficient of variation,"+df2.format(sc.HeelStrikeAVCV)+",CV"+ System.getProperty("line.separator");
        results += "Foot Swing Angular Velocity (AV),"+df2.format(sc.FootSwingAV)+",d/s"+ System.getProperty("line.separator");
        results += "Foot Swing AV Standard deviation,"+df2.format(sc.FootSwingAVSTD)+",d/s"+ System.getProperty("line.separator");
        results += "Foot Swing Coefficient of variation,"+df2.format(sc.FootSwingAVCV)+",CV"+ System.getProperty("line.separator");
        return results;
    }
}
