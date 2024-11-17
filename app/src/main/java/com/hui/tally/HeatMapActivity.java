package com.hui.tally;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.Gradient;
import com.baidu.mapapi.map.WeightedLatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.hui.tally.db.AccountBean;
import com.hui.tally.db.DBManager;

public class HeatMapActivity extends AppCompatActivity implements View.OnClickListener {
    private MapView mapView;
    private BaiduMap baiduMap;
    private HeatMap heatMap;
    private LocationClient locationClient;
    private Button dayBtn,monthBtn,yearBtn;
    private ImageView backIv;
    private TextView dateTv, countTv, amountTv;
    private int year,month,day;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        
        try {
            locationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initTime();
        initView();
        initBaiduMap();
        initStatistics(year,month,day);
        startLocation();
    }

    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void initView() {
        mapView = findViewById(R.id.heat_map_view);
        backIv = findViewById(R.id.heat_map_iv_back);
        dayBtn = findViewById(R.id.heat_map_btn_day);
        monthBtn = findViewById(R.id.heat_map_btn_month);
        yearBtn = findViewById(R.id.heat_map_btn_year);
        dateTv = findViewById(R.id.heat_map_tv_date);
        countTv = findViewById(R.id.heat_map_tv_count);
        amountTv = findViewById(R.id.heat_map_tv_amount);
        
        baiduMap = mapView.getMap();
        
        backIv.setOnClickListener(this);
        dayBtn.setOnClickListener(this);
        monthBtn.setOnClickListener(this);
        yearBtn.setOnClickListener(this);
        
        dateTv.setText(year + "年" + month + "月" + day + "日");
    }

    private void initBaiduMap() {
        // 参考JS代码初始化地图
        baiduMap = mapView.getMap();
        LatLng point = new LatLng(39.921984, 116.418261); // 默认北京中心
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(10));
        baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                loadHeatMapData();
            }
        });
        
        // 允许缩放
        UiSettings settings = baiduMap.getUiSettings();
        settings.setZoomGesturesEnabled(true);
    }

    private void initStatistics(int year, int month, int day) {
        float outMoneyOneDay = DBManager.getSumMoneyOneDay(year, month, day, 0);
        int outcountItemOneDay = DBManager.getCountItemOneDay(year, month, day, 0);
        countTv.setText("共" + outcountItemOneDay + "笔支出");
        amountTv.setText("总计: ￥" + outMoneyOneDay);
    }

    private void loadHeatMapData() {
        List<AccountBean> records = DBManager.getAccountListOneDayFromAccounttb(year, month, day);
        updateHeatMap(records);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.heat_map_iv_back:
                finish();
                break;
            case R.id.heat_map_btn_day:
                dateTv.setText(year + "年" + month + "月" + day + "日");
                initStatistics(year, month, day);
                loadHeatMapData();
                break;
            case R.id.heat_map_btn_month:
                dateTv.setText(year + "年" + month + "月");
                float outMoneyOneMonth = DBManager.getSumMoneyOneMonth(year, month, 0);
                int outcountItemOneMonth = DBManager.getCountItemOneMonth(year, month, 0);
                countTv.setText("共" + outcountItemOneMonth + "笔支出");
                amountTv.setText("总计: ￥" + outMoneyOneMonth);
                List<AccountBean> monthRecords = DBManager.getAccountListOneMonthFromAccounttb(year, month);
                updateHeatMap(monthRecords);
                break;
            case R.id.heat_map_btn_year:
                dateTv.setText(year + "年");
                float outMoneyOneYear = DBManager.getSumMoneyOneYear(year, 0);
                int outcountItemOneYear = DBManager.getCountItemOneYear(year, 0);
                countTv.setText("共" + outcountItemOneYear + "笔支出");
                amountTv.setText("总计: ￥" + outMoneyOneYear);
                List<AccountBean> yearRecords = DBManager.getAccountListOneYearFromAccounttb(year);
                updateHeatMap(yearRecords);
                break;
        }
    }

    private void updateHeatMap(List<AccountBean> records) {
        if (records == null || records.isEmpty()) {
            Log.d("HeatMap", "No records found");
            return;
        }

        if (heatMap != null) {
            heatMap.removeHeatMap();
        }

        List<LatLng> points = new ArrayList<>();
        for (AccountBean record : records) {
            if (record.getLatitude() != 0 && record.getLongitude() != 0) {
                LatLng point = new LatLng(record.getLatitude(), record.getLongitude());
                points.add(point);
                Log.d("HeatMap", "Added point: " + record.getLatitude() + ", " + record.getLongitude());
            }
        }

        Log.d("HeatMap", "Total points: " + points.size());

        if (!points.isEmpty()) {
            HeatMap.Builder builder = new HeatMap.Builder();
            builder.data(points)
                   .radius(150)  // 进一步增大热力点半径
                   .opacity(1.0f)  // 设置最大不透明度
                   .gradient(new Gradient(
                       new int[] {
                           Color.rgb(0, 255, 255),
                           Color.rgb(255, 255, 0),
                           Color.rgb(255, 0, 0)
                       },
                       new float[] {
                           0.2f, 0.5f, 1.0f
                       }
                   ));
            
            heatMap = builder.build();
            baiduMap.addHeatMap(heatMap);
            Log.d("HeatMap", "Heat map added to map");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private void startLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.setWifiCacheTimeOut(5*60*1000);
        option.setEnableSimulateGps(false);
        
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (location == null || mapView == null) {
                    return;
                }
                
                LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(point).zoom(15.0f);
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                
                // 在定位成功后加载热力图数据
                loadHeatMapData();
                
                locationClient.stop();
            }
        });
        
        locationClient.start();
    }
} 