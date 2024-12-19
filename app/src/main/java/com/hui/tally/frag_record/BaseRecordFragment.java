package com.hui.tally.frag_record;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.hui.tally.R;
import com.hui.tally.db.AccountBean;
import com.hui.tally.db.DBManager;
import com.hui.tally.db.TypeBean;
import com.hui.tally.utils.BeiZhuDialog;
import com.hui.tally.utils.KeyBoardUtils;
import com.hui.tally.utils.SelectTimeDialog;
import com.hui.tally.utils.CurrencyConverter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.os.Handler;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * 记录页面当中的支出模块
 */
public abstract class BaseRecordFragment extends Fragment implements View.OnClickListener {

    KeyboardView keyboardView;
    EditText moneyEt;
    ImageView typeIv;
    TextView typeTv,beizhuTv,timeTv;
    GridView typeGv;
    List<TypeBean>typeList;
    TypeBaseAdapter adapter;
    AccountBean accountBean;   //��需要插入到记账本当中的数据保存成对象的形式
    private LocationClient locationClient;
    private BDAbstractLocationListener locationListener;
    private LocationManager locationManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountBean = new AccountBean();   //创建对象
        accountBean.setTypename("其他");
        accountBean.setsImageId(R.mipmap.ic_qita_fs);
        
        // 初始化位置管理器
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        
        try {
            LocationClient.setAgreePrivacy(true);
            locationClient = new LocationClient(getActivity().getApplicationContext());
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            option.setCoorType("bd09ll");
            option.setScanSpan(1000);
            option.setOpenGps(true);
            option.setLocationNotify(true);
            option.setIgnoreKillProcess(false);
            option.setIsNeedLocationDescribe(true);
            option.setIsNeedAddress(true);
            locationClient.setLocOption(option);
            
            locationListener = new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation location) {
                    if (location != null) {
                        accountBean.setLatitude(location.getLatitude());
                        accountBean.setLongitude(location.getLongitude());
                        Log.d("BaseRecordFragment", "Baidu location: " + location.getLatitude() + ", " + location.getLongitude());
                    }
                }
            };
            
            locationClient.registerLocationListener(locationListener);
            locationClient.start();
        } catch (Exception e) {
            Log.e("BaseRecordFragment", "Baidu location init error: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationClient != null) {
            locationClient.stop();
            locationClient.unRegisterLocationListener(locationListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_outcome, container, false);
        initView(view);
        setInitTime();
        //给GridView填充数据的方法
        loadDataToGV();
        setGVListener(); //设置GridView每一项的点击事件
        return view;
    }
    /* 获取当前时间，显示在timeTv上*/
    private void setInitTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String time = sdf.format(date);
        timeTv.setText(time);
        accountBean.setTime(time);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        accountBean.setYear(year);
        accountBean.setMonth(month);
        accountBean.setDay(day);
    }

    /* 设置GridView每一项的点击事件*/
    private void setGVListener() {
        typeGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.selectPos = position;
                adapter.notifyDataSetInvalidated();  //提示绘制发生变化了
                TypeBean typeBean = typeList.get(position);
                String typename = typeBean.getTypename();
                typeTv.setText(typename);
                accountBean.setTypename(typename);
                int simageId = typeBean.getsImageId();
                typeIv.setImageResource(simageId);
                accountBean.setsImageId(simageId);
            }
        });
    }

    /* 给GridView填出数据的方法*/
    public void loadDataToGV() {
        typeList = new ArrayList<>();
        adapter = new TypeBaseAdapter(getContext(), typeList);
        typeGv.setAdapter(adapter);
    }

    private void initView(View view) {
        keyboardView = view.findViewById(R.id.frag_record_keyboard);
        moneyEt = view.findViewById(R.id.frag_record_et_money);
        typeIv = view.findViewById(R.id.frag_record_iv);
        typeGv = view.findViewById(R.id.frag_record_gv);
        typeTv = view.findViewById(R.id.frag_record_tv_type);
        beizhuTv = view.findViewById(R.id.frag_record_tv_beizhu);
        timeTv = view.findViewById(R.id.frag_record_tv_time);
        beizhuTv.setOnClickListener(this);
        timeTv.setOnClickListener(this);

        //让自定义软键盘显示出来
        KeyBoardUtils boardUtils = new KeyBoardUtils(keyboardView, moneyEt);
        boardUtils.showKeyboard();
        //设置接口，监听确定按钮按钮被点击了
        boardUtils.setOnEnsureListener(new KeyBoardUtils.OnEnsureListener() {
            @Override
            public void onEnsure() {
                String moneyStr = moneyEt.getText().toString();
                if (TextUtils.isEmpty(moneyStr) || moneyStr.equals("0")) {
                    getActivity().finish();
                    return;
                }
                final float inputMoney = Float.parseFloat(moneyStr);
                
                // 在后台线程中执行货币转换
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final float convertedMoney;
                            // 如果不是人民币，进行转换
                            if (!accountBean.getCurrency().equals("CNY")) {
                                convertedMoney = CurrencyConverter.convertToCNY(inputMoney, accountBean.getCurrency());
                            } else {
                                convertedMoney = inputMoney;
                            }
                            
                            // 在UI线程中更新UI并保存数据
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountBean.setMoney(convertedMoney);
                                    // 在保存前更新时间和位置信息
                                    updateTimeAndLocation();
                                    // 保存到数据库
                                    saveAccountToDB();
                                    // 返回上一级页面
                                    getActivity().finish();
                                }
                            });
                        } catch (Exception e) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "货币转换失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        
        // 添加货币选择器
        Spinner currencySpinner = view.findViewById(R.id.currency_spinner);
        if (currencySpinner != null) {
            ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item,
                    CurrencyConverter.getAvailableCurrencies());
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinner.setAdapter(currencyAdapter);
            currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedCurrency = (String) parent.getItemAtPosition(position);
                    accountBean.setCurrency(selectedCurrency);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    accountBean.setCurrency("CNY");
                }
            });
        }
    }
    /* 让子类一定要重写这个方法*/
    public abstract void saveAccountToDB();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.frag_record_tv_time:
                showTimeDialog();
                break;
            case R.id.frag_record_tv_beizhu:
                showBZDialog();
                break;
        }
    }
    /* 弹出显示时间的对话框*/
    private void showTimeDialog() {
        SelectTimeDialog dialog = new SelectTimeDialog(getContext());
        dialog.show();
        //设定确定按钮被点击了的监听器
        dialog.setOnEnsureListener(new SelectTimeDialog.OnEnsureListener() {
            @Override
            public void onEnsure(String time, int year, int month, int day) {
                timeTv.setText(time);
                accountBean.setTime(time);
                accountBean.setYear(year);
                accountBean.setMonth(month);
                accountBean.setDay(day);
            }
        });
    }

    /* 弹出备注对话框*/
    public  void showBZDialog(){
        final BeiZhuDialog dialog = new BeiZhuDialog(getContext());
        dialog.show();
        dialog.setDialogSize();
        dialog.setOnEnsureListener(new BeiZhuDialog.OnEnsureListener() {
            @Override
            public void onEnsure() {
                String msg = dialog.getEditText();
                if (!TextUtils.isEmpty(msg)) {
                    beizhuTv.setText(msg);
                    accountBean.setBeizhu(msg);
                }
                dialog.cancel();
            }
        });
    }

    private void updateTimeAndLocation() {
        // 更新时间信息
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String time = sdf.format(date);
        accountBean.setTime(time);
        
        Calendar calendar = Calendar.getInstance();
        accountBean.setYear(calendar.get(Calendar.YEAR));
        accountBean.setMonth(calendar.get(Calendar.MONTH) + 1);
        accountBean.setDay(calendar.get(Calendar.DAY_OF_MONTH));
        
        // 只在没有位置信息时尝试获取
        if (accountBean.getLatitude() == 0 && accountBean.getLongitude() == 0) {
            updateLocation();
        }
    }

    private void updateLocation() {
        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                // 先尝试网络定位
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (networkLocation != null) {
                        accountBean.setLatitude(networkLocation.getLatitude());
                        accountBean.setLongitude(networkLocation.getLongitude());
                        Log.d("BaseRecordFragment", "Got network location: " + networkLocation.getLatitude() + ", " + networkLocation.getLongitude());
                        return;
                    }
                }

                // 再尝试GPS定位
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (gpsLocation != null) {
                        accountBean.setLatitude(gpsLocation.getLatitude());
                        accountBean.setLongitude(gpsLocation.getLongitude());
                        Log.d("BaseRecordFragment", "Got GPS location: " + gpsLocation.getLatitude() + ", " + gpsLocation.getLongitude());
                        return;
                    }
                }

                Log.d("BaseRecordFragment", "No location available from either provider");
            } catch (SecurityException e) {
                Log.e("BaseRecordFragment", "Security Exception: " + e.getMessage());
            }
        }
    }

}
