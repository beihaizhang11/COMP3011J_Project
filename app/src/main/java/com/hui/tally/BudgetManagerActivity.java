package com.hui.tally;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hui.tally.db.BudgetBean;
import com.hui.tally.adapter.BudgetAdapter;
import com.hui.tally.db.DBManager;
import com.hui.tally.utils.BudgetDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetManagerActivity extends AppCompatActivity {
    private ListView budgetLv;
    private List<BudgetBean> mDatas;
    private BudgetAdapter adapter;
    private int year, month;
    private TextView dateTv;
    private ImageView backIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_manager);
        initTime();
        initView();
        loadData();
    }

    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
    }

    private void initView() {
        budgetLv = findViewById(R.id.budget_manager_lv);
        dateTv = findViewById(R.id.budget_manager_tv_date);
        backIv = findViewById(R.id.budget_manager_iv_back);
        Button addBtn = findViewById(R.id.budget_manager_btn_add);
        
        dateTv.setText(year + "年" + month + "月预算");
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mDatas = new ArrayList<>();
        adapter = new BudgetAdapter(this, mDatas);
        budgetLv.setAdapter(adapter);

        // 设置ListView的长按事件
        budgetLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
                return true;
            }
        });
        
        // 设置添加按钮点击事件
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBudgetDialog();
            }
        });

        // 设置编辑按钮的点击事件
        adapter.setOnEditClickListener(new BudgetAdapter.OnEditClickListener() {
            @Override
            public void onEditClick(int position) {
                showBudgetDialog();
                BudgetBean bean = mDatas.get(position);
                // 删除旧的预算
                DBManager.deleteBudget(bean.getTypename(), year, month);
            }
        });
    }

    private void loadData() {
        List<BudgetBean> list = DBManager.getTypeBudgetList(year, month);
        mDatas.clear();
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
    }

    private void showDeleteDialog(final int position) {
        final BudgetBean budgetBean = mDatas.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("WARNING")
                .setMessage("Do you sure to delete budget？")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Approve", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBManager.deleteBudget(budgetBean.getTypename(), year, month);
                        mDatas.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });
        builder.create().show();
    }

    private void showBudgetDialog() {
        BudgetDialog dialog = new BudgetDialog(this);
        dialog.show();
        dialog.setOnEnsureListener(new BudgetDialog.OnEnsureListener() {
            @Override
            public void onEnsure(float money, String typename) {
                DBManager.insertBudget(typename, money, year, month);
                loadData();
            }
        });
    }
} 