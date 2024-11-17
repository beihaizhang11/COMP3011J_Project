package com.hui.tally;

import android.app.Application;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.location.LocationClient;
import com.hui.tally.db.DBManager;

/* 表示全局应用的类*/
public class UniteApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化数据库
        DBManager.initDB(getApplicationContext());
        
        try {
            // 百度定位SDK需要在使用前初始化
            LocationClient.setAgreePrivacy(true);
            // 设置百度地图SDK隐私合规
            SDKInitializer.setAgreePrivacy(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        SDKInitializer.initialize(this);
        // 自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        // 包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }
}
