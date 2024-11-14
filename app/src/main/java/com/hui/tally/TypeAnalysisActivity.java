package com.hui.tally;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.hui.tally.db.DBManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TypeAnalysisActivity extends AppCompatActivity {
    private LineChart lineChart;
    private String typename;
    private ImageView backIv;
    private TextView titleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_analysis);
        typename = getIntent().getStringExtra("typename");
        initView();
        setChart();
    }

    private void initView() {
        lineChart = findViewById(R.id.type_analysis_chart);
        backIv = findViewById(R.id.type_analysis_iv_back);
        titleTv = findViewById(R.id.type_analysis_tv_title);
        
        titleTv.setText(typename + " Spending Trend");
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setChart() {
        List<Entry> entries = new ArrayList<>();
        List<Float> list = DBManager.getRecentSixMonthAmount(typename);
        
        float maxMoney = 0;
        for (Float amount : list) {
            if (amount > maxMoney) {
                maxMoney = amount;
            }
        }
        float max = (float) Math.ceil(maxMoney);
        
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMaximum(max);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setEnabled(true);
        
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
        
        for (int i = 0; i < list.size(); i++) {
            entries.add(new Entry(i, list.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, typename + "消费金额");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4D0000FF"));
        
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (value == 0) {
                    return "";
                }
                return value + "";
            }
        });
        
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, (int)value - 5);
                return (calendar.get(Calendar.MONTH) + 1) + "月";
            }
        });
        
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }
} 