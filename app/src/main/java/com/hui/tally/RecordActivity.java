package com.hui.tally;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.location.Location;
import android.location.LocationManager;
import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;

import com.google.android.material.tabs.TabLayout;
import com.hui.tally.adapter.RecordPagerAdapter;
import com.hui.tally.frag_record.IncomeFragment;
import com.hui.tally.frag_record.BaseRecordFragment;
import com.hui.tally.frag_record.OutcomeFragment;

import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        
        tabLayout = findViewById(R.id.record_tabs);
        viewPager = findViewById(R.id.record_vp);
        initPager();
    }

    private void initPager() {
        List<Fragment> fragmentList = new ArrayList<>();
        OutcomeFragment outFrag = new OutcomeFragment();
        IncomeFragment inFrag = new IncomeFragment();
        
        // 获取位置信息并传递给Fragment
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", location.getLatitude());
                bundle.putDouble("longitude", location.getLongitude());
                outFrag.setArguments(bundle);
                inFrag.setArguments(bundle);
            }
        }
        
        fragmentList.add(outFrag);
        fragmentList.add(inFrag);
        
        RecordPagerAdapter pagerAdapter = new RecordPagerAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.record_iv_back:
                finish();
                break;
        }
    }
}
