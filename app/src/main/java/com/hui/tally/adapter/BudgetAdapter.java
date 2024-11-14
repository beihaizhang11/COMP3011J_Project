package com.hui.tally.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hui.tally.R;
import com.hui.tally.db.BudgetBean;
import com.hui.tally.db.DBManager;
import com.hui.tally.db.TypeBean;

import java.util.List;

public class BudgetAdapter extends BaseAdapter {
    private Context context;
    private List<BudgetBean> mDatas;
    private LayoutInflater inflater;
    
    public BudgetAdapter(Context context, List<BudgetBean> mDatas) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_budget_lv, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        BudgetBean bean = mDatas.get(position);
        holder.typenameTv.setText(bean.getTypename());
        holder.budgetTv.setText("￥" + bean.getMoney());
        
        // 获取对应类型的图片
        TypeBean typeBean = DBManager.getTypeFromTypetb(bean.getTypename());
        if (typeBean != null) {
            holder.typeIv.setImageResource(typeBean.getsImageId());
        }
        
        // 设置编辑按钮点击事件
        holder.editIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEditClickListener != null) {
                    onEditClickListener.onEditClick(position);
                }
            }
        });
        
        return convertView;
    }
    
    static class ViewHolder {
        TextView typenameTv, budgetTv;
        ImageView typeIv, editIv;
        
        public ViewHolder(View view) {
            typenameTv = view.findViewById(R.id.item_budget_tv_typename);
            budgetTv = view.findViewById(R.id.item_budget_tv_money);
            typeIv = view.findViewById(R.id.item_budget_iv);
            editIv = view.findViewById(R.id.item_budget_iv_edit);
        }
    }
    
    // 编辑按钮的点击监听接口
    public interface OnEditClickListener {
        void onEditClick(int position);
    }
    
    private OnEditClickListener onEditClickListener;
    
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }
} 