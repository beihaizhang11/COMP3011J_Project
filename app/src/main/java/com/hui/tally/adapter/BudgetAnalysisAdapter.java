package com.hui.tally.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hui.tally.R;
import com.hui.tally.db.BudgetAnalysisBean;
import com.hui.tally.db.DBManager;
import com.hui.tally.db.TypeBean;

import java.util.List;

public class BudgetAnalysisAdapter extends BaseAdapter {
    private Context context;
    private List<BudgetAnalysisBean> mDatas;
    private LayoutInflater inflater;
    
    public BudgetAnalysisAdapter(Context context, List<BudgetAnalysisBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_budget_analysis_lv, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        BudgetAnalysisBean bean = mDatas.get(position);
        holder.typenameTv.setText(bean.getTypename());
        holder.moneyTv.setText("￥" + bean.getSpendMoney() + "/￥" + bean.getBudgetMoney());
        
        // 设置进度条
        float progress = bean.getSpendMoney() / bean.getBudgetMoney() * 100;
        holder.progressBar.setProgress((int)progress);
        
        // 获取对应类型的图片
        TypeBean typeBean = DBManager.getTypeFromTypetb(bean.getTypename());
        if (typeBean != null) {
            holder.typeIv.setImageResource(typeBean.getsImageId());
        }
        
        return convertView;
    }
    
    static class ViewHolder {
        TextView typenameTv, moneyTv;
        ImageView typeIv;
        ProgressBar progressBar;
        
        public ViewHolder(View view) {
            typenameTv = view.findViewById(R.id.item_analysis_tv_typename);
            moneyTv = view.findViewById(R.id.item_analysis_tv_money);
            typeIv = view.findViewById(R.id.item_analysis_iv);
            progressBar = view.findViewById(R.id.item_analysis_pb);
        }
    }
} 