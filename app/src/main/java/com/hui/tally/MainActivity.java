package com.hui.tally;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hui.tally.adapter.AccountAdapter;
import com.hui.tally.db.AccountBean;
import com.hui.tally.db.BudgetBean;
import com.hui.tally.db.DBManager;
import com.hui.tally.db.TypeBean;
import com.hui.tally.utils.BudgetDialog;
import com.hui.tally.utils.MoreDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView todayLv;  //展示今日收支情况的ListView
    private ImageView searchIv;
    private Button editBtn;
    private ImageButton moreBtn;
    private Button budgetAnalysisBtn;
    private Button scanBtn;
    private Button heatMapBtn;
    private Button aiAssistantBtn;
    private List<AccountBean> mDatas;
    private AccountAdapter adapter;
    private int year,month,day;
    private View headerView;
    private TextView topOutTv,topInTv,topbudgetTv,topConTv;
    private ImageView topShowIv;
    private SharedPreferences preferences;
    private TextView warningTv;
    private TextView suggestionTv;
    private boolean isShow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        checkLocationPermission();
        initTime();
        initView();
        preferences = getSharedPreferences("budget", Context.MODE_PRIVATE);
        addLVHeaderView();
        initData();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void initData() {
        mDatas = new ArrayList<>();
        adapter = new AccountAdapter(this, mDatas);
        todayLv.setAdapter(adapter);
        setLVLongClickListener();
    }

    private void initView() {
        todayLv = findViewById(R.id.main_lv);
        editBtn = findViewById(R.id.main_btn_edit);
        moreBtn = findViewById(R.id.main_btn_more);
        searchIv = findViewById(R.id.main_iv_search);
        budgetAnalysisBtn = findViewById(R.id.main_btn_budget_analysis);
        scanBtn = findViewById(R.id.main_btn_scan);
        warningTv = findViewById(R.id.main_tv_warning);
        suggestionTv = findViewById(R.id.main_tv_suggestion);
        heatMapBtn = findViewById(R.id.main_btn_heatmap);
        aiAssistantBtn = findViewById(R.id.main_btn_ai_assistant);
        
        initClickListeners();
    }

    private void initClickListeners() {
        editBtn.setOnClickListener(this);
        moreBtn.setOnClickListener(this);
        searchIv.setOnClickListener(this);
        budgetAnalysisBtn.setOnClickListener(this);
        
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });
        
        heatMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HeatMapActivity.class);
                startActivity(intent);
            }
        });
        
        aiAssistantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AIAssistantActivity.class);
                startActivity(intent);
            }
        });
    }

    /** 设置ListView的长按事件*/
    private void setLVLongClickListener() {
        todayLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {  //点击了头布局
                    return false;
                }
                int pos = position-1;
                AccountBean clickBean = mDatas.get(pos);  //获取正在被点击的这条信息

                //弹出提示用户是否删除的对话框
                showDeleteItemDialog(clickBean);
                return false;
            }
        });
    }
    /* 弹出是否删除某一条记录的对话框*/
    private void showDeleteItemDialog(final  AccountBean clickBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示信息").setMessage("您确定要删除这条记录么？")
                .setNegativeButton("取消",null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int click_id = clickBean.getId();
                        //执行删除的操作
                        DBManager.deleteItemFromAccounttbById(click_id);
                        mDatas.remove(clickBean);   //实时刷新，移除集合当中的对象
                        adapter.notifyDataSetChanged();   //提示适配器更新数据
                        setTopTvShow();   //改变头布局TextView显示的内容
                    }
                });
        builder.create().show();   //显示对话框
    }

    /** 给ListView添加头布局的方法*/
    private void addLVHeaderView() {
        //将布局转换成View对象
        headerView = getLayoutInflater().inflate(R.layout.item_mainlv_top,null);
        //查找头布局可用控件
        topOutTv = headerView.findViewById(R.id.item_mainlv_top_tv_out);
        topInTv = headerView.findViewById(R.id.item_mainlv_top_tv_in);
        topbudgetTv = headerView.findViewById(R.id.item_mainlv_top_tv_budget);
        topConTv = headerView.findViewById(R.id.item_mainlv_top_tv_day);
        topShowIv = headerView.findViewById(R.id.item_mainlv_top_iv_hide);

        // 添加预算文本的点击事件
        topbudgetTv.setOnClickListener(this);

        // 设置头布局点击事件
        headerView.setOnClickListener(this);
        todayLv.addHeaderView(headerView);
    }
    /* 获取今日的具体时间*/
    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    // 当activity获取焦点时，会调用的方法
    @Override
    protected void onResume() {
        super.onResume();
        loadDBData();
        setTopTvShow();
        checkBudgetWarning();
        checkSpendingSuggestion();
    }
    /* 设置头布局当中文本内容的显示*/
    private void setTopTvShow() {
        //获取今日支出和收入总金额，显示在view当中
        float incomeOneDay = DBManager.getSumMoneyOneDay(year, month, day, 1);
        float outcomeOneDay = DBManager.getSumMoneyOneDay(year, month, day, 0);
        String infoOneDay = "Payment today ￥"+outcomeOneDay+"  Income ￥"+incomeOneDay;
        topConTv.setText(infoOneDay);
        //获取本月收入和支出总金额
        float incomeOneMonth = DBManager.getSumMoneyOneMonth(year, month, 1);
        float outcomeOneMonth = DBManager.getSumMoneyOneMonth(year, month, 0);
        topInTv.setText("￥"+incomeOneMonth);
        topOutTv.setText("￥"+outcomeOneMonth);

        // 获取本月所有类型预算
        List<BudgetBean> budgetList = DBManager.getTypeBudgetList(year, month);
        float totalBudget = 0;
        for (BudgetBean budget : budgetList) {
            totalBudget += budget.getMoney();
        }

        // 如果没有设置预算，显示0
        if (totalBudget == 0) {
            topbudgetTv.setText("￥ 0");
        } else {
            // 计算预算剩余
            float syMoney = totalBudget - outcomeOneMonth;
            topbudgetTv.setText("￥"+syMoney);
        }
    }


    // 加载数据库数据
    private void loadDBData() {
        List<AccountBean> list = DBManager.getAccountListOneDayFromAccounttb(year, month, day);
        mDatas.clear();
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_iv_search:
                Intent it = new Intent(this, SearchActivity.class);  //跳转界面
                startActivity(it);
                break;
            case R.id.main_btn_edit:
                Intent intent = new Intent(this, RecordActivity.class);
                // 获取当前位置并传递给RecordActivity
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        intent.putExtra("latitude", location.getLatitude());
                        intent.putExtra("longitude", location.getLongitude());
                    }
                }
                startActivity(intent);
                break;
            case R.id.main_btn_more:
                MoreDialog moreDialog = new MoreDialog(this);
                moreDialog.show();
                moreDialog.setDialogSize();
                break;
            case R.id.item_mainlv_top_tv_budget:
                Intent intent1 = new Intent(this, BudgetManagerActivity.class);
                startActivity(intent1);
                break;
            case R.id.item_mainlv_top_iv_hide:
                // 切换TextView明文和密文
                toggleShow();
                break;
            case R.id.main_btn_budget_analysis:
                Intent intent2 = new Intent(this, BudgetAnalysisActivity.class);
                startActivity(intent2);
                break;
        }
        if (v == headerView) {
            //头布局被点击了
            Intent intent = new Intent();
            intent.setClass(this, MonthChartActivity.class);
            startActivity(intent);
        }
    }
    /** 显示预算设置对话框*/
    private void showBudgetDialog() {
        BudgetDialog dialog = new BudgetDialog(this);
        dialog.show();
        dialog.setDialogSize();
        dialog.setOnEnsureListener(new BudgetDialog.OnEnsureListener() {
            @Override
            public void onEnsure(float money, String typename) {
                //将预算金额写入到数据库
                DBManager.insertBudget(typename, money, year, month);
                //重新计算并显示预算
                setTopTvShow();
            }
        });
    }

    /**
     * 点击头布局眼睛时，如果原来是明文，就加密，如果是密文，就显示出来
     * */
    private void toggleShow() {
        if (isShow) {   //明文====》密文
            PasswordTransformationMethod passwordMethod = PasswordTransformationMethod.getInstance();
            topInTv.setTransformationMethod(passwordMethod);   //设置隐藏
            topOutTv.setTransformationMethod(passwordMethod);   //设置隐藏
            topbudgetTv.setTransformationMethod(passwordMethod);   //设置隐藏
            topShowIv.setImageResource(R.mipmap.ih_hide);
            isShow = false;   //设置标志位为隐藏状态
        }else{  //密文---》明文
            HideReturnsTransformationMethod hideMethod = HideReturnsTransformationMethod.getInstance();
            topInTv.setTransformationMethod(hideMethod);   //设置隐藏
            topOutTv.setTransformationMethod(hideMethod);   //设置隐藏
            topbudgetTv.setTransformationMethod(hideMethod);   //设置隐藏
            topShowIv.setImageResource(R.mipmap.ih_show);
            isShow = true;   //设置标志位为隐藏状态
        }
    }

    private void checkBudgetWarning() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        
        int lastMonth = currentMonth - 1;
        int lastYear = currentYear;
        if (lastMonth == 0) {
            lastMonth = 12;
            lastYear--;
        }
        
        List<TypeBean> outTypes = DBManager.getTypeList(0);
        float maxOverBudget = 0;
        String maxOverType = "";
        boolean isDoubleOver = false;
        
        for (TypeBean type : outTypes) {
            String typename = type.getTypename();
            float currentMonthMoney = DBManager.getSumMoneyOneMonthByType(currentYear, currentMonth, typename);
            float budgetMoney = DBManager.getTypeBudget(typename, currentYear, currentMonth);
            float lastMonthMoney = DBManager.getSumMoneyOneMonthByType(lastYear, lastMonth, typename);
            
            if (budgetMoney > 0 && currentMonthMoney > budgetMoney 
                && currentMonthMoney > lastMonthMoney) {
                float overAmount = currentMonthMoney - budgetMoney;
                if (overAmount > maxOverBudget) {
                    maxOverBudget = overAmount;
                    maxOverType = typename;
                    isDoubleOver = currentMonthMoney > lastMonthMoney * 2;
                }
            }
        }
        
        if (!maxOverType.isEmpty()) {
            if (isDoubleOver) {
                warningTv.setText("Your spending on " + maxOverType + " is extremely high this month");
            } else {
                warningTv.setText("Your spending on " + maxOverType + " is high this month");
            }
        } else {
            warningTv.setText("");
        }
    }

    private void checkSpendingSuggestion() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        
        List<TypeBean> outTypes = DBManager.getTypeList(0);
        float maxOverAverage = 0;
        String maxOverType = "";
        
        for (TypeBean type : outTypes) {
            String typename = type.getTypename();
            float currentMonthMoney = DBManager.getSumMoneyOneMonthByType(currentYear, currentMonth, typename);
            
            // 计算前三个月的平均值
            float threeMonthTotal = 0;
            for (int i = 1; i <= 3; i++) {
                int targetMonth = currentMonth - i;
                int targetYear = currentYear;
                if (targetMonth <= 0) {
                    targetMonth += 12;
                    targetYear--;
                }
                threeMonthTotal += DBManager.getSumMoneyOneMonthByType(targetYear, targetMonth, typename);
            }
            float averageMoney = threeMonthTotal / 3;
            
            if (currentMonthMoney > averageMoney) {
                float overAmount = currentMonthMoney - averageMoney;
                if (overAmount > maxOverAverage) {
                    maxOverAverage = overAmount;
                    maxOverType = typename;
                }
            }
        }
        
        if (!maxOverType.isEmpty()) {
            suggestionTv.setText("Consider reducing spending on " + maxOverType + " this month");
        } else {
            suggestionTv.setText("");
        }
    }
}
