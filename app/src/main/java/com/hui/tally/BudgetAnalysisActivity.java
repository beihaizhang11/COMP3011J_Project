package com.hui.tally;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.hui.tally.adapter.BudgetAnalysisAdapter;
import com.hui.tally.db.BudgetAnalysisBean;
import com.hui.tally.db.DBManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetAnalysisActivity extends AppCompatActivity {
    private ListView analysisLv;
    private ImageView backIv;
    private TextView dateTv;
    private List<BudgetAnalysisBean> mDatas;
    private BudgetAnalysisAdapter adapter;
    private int year, month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_analysis);
        initTime();
        initView();
        loadData();
        setLVClickListener();
    }

    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
    }

    private void initView() {
        analysisLv = findViewById(R.id.analysis_lv);
        backIv = findViewById(R.id.analysis_iv_back);
        dateTv = findViewById(R.id.analysis_tv_date);
        
        dateTv.setText("Budget Analysis " + year + "/" + month);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mDatas = new ArrayList<>();
        adapter = new BudgetAnalysisAdapter(this, mDatas);
        analysisLv.setAdapter(adapter);
    }

    private void loadData() {
        List<BudgetAnalysisBean> list = DBManager.getTypeBudgetAnalysisList(year, month);
        mDatas.clear();
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
    }

    private void setLVClickListener() {
        analysisLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BudgetAnalysisBean bean = mDatas.get(position);
                Intent intent = new Intent(BudgetAnalysisActivity.this, TypeAnalysisActivity.class);
                intent.putExtra("typename", bean.getTypename());
                startActivity(intent);
            }
        });
    }
} 