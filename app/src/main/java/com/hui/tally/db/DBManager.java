package com.hui.tally.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hui.tally.utils.FloatUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
* 负责管理数据库的类
*   主要对于表当中的内容进行操作，增删改查
* */
public class DBManager {

    private static SQLiteDatabase db;
    /* 初始化数据库对象*/
    public static void initDB(Context context){
        DBOpenHelper helper = new DBOpenHelper(context);  //得到帮助类对象
        db = helper.getWritableDatabase();      //得到数据库对象
    }

    /**
     * 读取数据库当中的数据，写入内存集合里
     *   kind :表示收入或者支出
     * */
    public static List<TypeBean>getTypeList(int kind){
        List<TypeBean>list = new ArrayList<>();
        //读取typetb表当中的数据
        String sql = "select * from typetb where kind = "+kind;
        Cursor cursor = db.rawQuery(sql, null);
//        循环读取游标内容，存储到对象当中
        while (cursor.moveToNext()) {
            String typename = cursor.getString(cursor.getColumnIndex("typename"));
            int imageId = cursor.getInt(cursor.getColumnIndex("imageId"));
            int sImageId = cursor.getInt(cursor.getColumnIndex("sImageId"));
            int kind1 = cursor.getInt(cursor.getColumnIndex("kind"));
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            TypeBean typeBean = new TypeBean(id, typename, imageId, sImageId, kind);
            list.add(typeBean);
        }
        return list;
    }

    /*
    * 向记账表当中插入一条元素
    * */
    public static void insertItemToAccounttb(AccountBean bean){
        Log.d("DBManager", "Saving location: " + bean.getLatitude() + ", " + bean.getLongitude());
        ContentValues values = new ContentValues();
        values.put("typename",bean.getTypename());
        values.put("sImageId",bean.getsImageId());
        values.put("beizhu",bean.getBeizhu());
        values.put("money",bean.getMoney());
        values.put("time",bean.getTime());
        values.put("year",bean.getYear());
        values.put("month",bean.getMonth());
        values.put("day",bean.getDay());
        values.put("kind",bean.getKind());
        values.put("latitude",bean.getLatitude());
        values.put("longitude",bean.getLongitude());
        db.insert("accounttb",null,values);
    }
    /*
    * 获取记账表当中某一天的所有支出或者收入情况
    * */
    public static List<AccountBean>getAccountListOneDayFromAccounttb(int year,int month,int day){
        List<AccountBean>list = new ArrayList<>();
        String sql = "select * from accounttb where year=? and month=? and day=? order by id desc";
        Cursor cursor = db.rawQuery(sql, new String[]{year + "", month + "", day + ""});
        //遍历符合要求的每一行数据
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String typename = cursor.getString(cursor.getColumnIndex("typename"));
            String beizhu = cursor.getString(cursor.getColumnIndex("beizhu"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            int sImageId = cursor.getInt(cursor.getColumnIndex("sImageId"));
            int kind = cursor.getInt(cursor.getColumnIndex("kind"));
            float money = cursor.getFloat(cursor.getColumnIndex("money"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            AccountBean accountBean = new AccountBean(id, typename, sImageId, beizhu, money, time, year, month, day, kind);
            accountBean.setLatitude(latitude);
            accountBean.setLongitude(longitude);
            list.add(accountBean);
        }
        return list;
    }

    /*
     * 获取记账表当中某一月的所有支出或者收入情况
     * */
    public static List<AccountBean>getAccountListOneMonthFromAccounttb(int year,int month){
        List<AccountBean>list = new ArrayList<>();
        String sql = "select * from accounttb where year=? and month=? order by id desc";
        Cursor cursor = db.rawQuery(sql, new String[]{year + "", month + ""});
        
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String typename = cursor.getString(cursor.getColumnIndex("typename"));
            String beizhu = cursor.getString(cursor.getColumnIndex("beizhu"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            int sImageId = cursor.getInt(cursor.getColumnIndex("sImageId"));
            int kind = cursor.getInt(cursor.getColumnIndex("kind"));
            float money = cursor.getFloat(cursor.getColumnIndex("money"));
            int day = cursor.getInt(cursor.getColumnIndex("day"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            AccountBean accountBean = new AccountBean(id, typename, sImageId, beizhu, money, time, year, month, day, kind);
            accountBean.setLatitude(latitude);
            accountBean.setLongitude(longitude);
            list.add(accountBean);
        }
        return list;
    }
    /**
     * 获取某一天的支出或者收入的总金额   kind：支出==0    收入===1
     * */
    public static float getSumMoneyOneDay(int year,int month,int day,int kind){
        float total = 0.0f;
        String sql = "select sum(money) from accounttb where year=? and month=? and day=? and kind=?";
        Cursor cursor = db.rawQuery(sql, new String[]{year + "", month + "", day + "", kind + ""});
        // 遍历
        if (cursor.moveToFirst()) {
            float money = cursor.getFloat(cursor.getColumnIndex("sum(money)"));
            total = money;
        }
        return total;
    }
    /**
     * 获取某一月的支出或者收入的总金额   kind：支出==0    收入===1
     * */
    public static float getSumMoneyOneMonth(int year,int month,int kind){
        float total = 0.0f;
        String sql = "select sum(money) from accounttb where year=? and month=? and kind=?";
        Cursor cursor = db.rawQuery(sql, new String[]{year + "", month + "", kind + ""});
        // 遍历
        if (cursor.moveToFirst()) {
            float money = cursor.getFloat(cursor.getColumnIndex("sum(money)"));
            total = money;
        }
        return total;
    }
    /** 统计某月份支出或者收入情况有多少条  收入-1   支出-0*/
    public static int getCountItemOneMonth(int year,int month,int kind){
        int total = 0;
        String sql = "select count(money) from accounttb where year=? and month=? and kind=?";
        Cursor cursor = db.rawQuery(sql, new String[]{year + "", month + "", kind + ""});
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(cursor.getColumnIndex("count(money)"));
            total = count;
        }
        return total;
    }
    /**
     * 获取某一年的支出或者收入的总金额   kind：支出==0    收入===1
     * */
    public static float getSumMoneyOneYear(int year,int kind){
        float total = 0.0f;
        String sql = "select sum(money) from accounttb where year=? and kind=?";
        Cursor cursor = db.rawQuery(sql, new String[]{year + "", kind + ""});
        // 遍历
        if (cursor.moveToFirst()) {
            float money = cursor.getFloat(cursor.getColumnIndex("sum(money)"));
            total = money;
        }
        return total;
    }

    /*
    * 根据传入的id，删除accounttb表当中的一条数据
    * */
    public static int deleteItemFromAccounttbById(int id){
        int i = db.delete("accounttb", "id=?", new String[]{id + ""});
        return i;
    }
    /**
     * 根据备注搜索收入或者支出的情况列表
     * */
    public static List<AccountBean>getAccountListByRemarkFromAccounttb(String beizhu){
        List<AccountBean>list = new ArrayList<>();
        String sql = "select * from accounttb where beizhu like '%"+beizhu+"%'";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String typename = cursor.getString(cursor.getColumnIndex("typename"));
            String bz = cursor.getString(cursor.getColumnIndex("beizhu"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            int sImageId = cursor.getInt(cursor.getColumnIndex("sImageId"));
            int kind = cursor.getInt(cursor.getColumnIndex("kind"));
            float money = cursor.getFloat(cursor.getColumnIndex("money"));
            int year = cursor.getInt(cursor.getColumnIndex("year"));
            int month = cursor.getInt(cursor.getColumnIndex("month"));
            int day = cursor.getInt(cursor.getColumnIndex("day"));
            AccountBean accountBean = new AccountBean(id, typename, sImageId, bz, money, time, year, month, day, kind);
            list.add(accountBean);
        }
        return list;
    }

    /**
     * 查询记账的表当中有几个年份信息
     * */
    public static List<Integer>getYearListFromAccounttb(){
        List<Integer>list = new ArrayList<>();
        String sql = "select distinct(year) from accounttb order by year asc";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            int year = cursor.getInt(cursor.getColumnIndex("year"));
            list.add(year);
        }
        return list;
    }

    /*
    * 删除accounttb表格当中的所有数据
    * */
    public static void deleteAllAccount(){
        String sql = "delete from accounttb";
        db.execSQL(sql);
    }

    /**
     * 查询指定年份和月份的收入或者支出每一种类型的总钱数
     * */
    public static List<ChartItemBean>getChartListFromAccounttb(int year,int month,int kind){
        List<ChartItemBean>list = new ArrayList<>();
        float sumMoneyOneMonth = getSumMoneyOneMonth(year, month, kind);  //求出支出或者收入总钱数
        String sql = "select typename,sImageId,sum(money)as total from accounttb where year=? and month=? and kind=? group by typename " +
                "order by total desc";
        Cursor cursor = db.rawQuery(sql, new String[]{year + "", month + "", kind + ""});
        while (cursor.moveToNext()) {
            int sImageId = cursor.getInt(cursor.getColumnIndex("sImageId"));
            String typename = cursor.getString(cursor.getColumnIndex("typename"));
            float total = cursor.getFloat(cursor.getColumnIndex("total"));
            //计算所占百分比  total /sumMonth
            float ratio = FloatUtils.div(total,sumMoneyOneMonth);
            ChartItemBean bean = new ChartItemBean(sImageId, typename, ratio, total);
            list.add(bean);
        }
        return list;
    }

    /**
    * 获取这个月当中某一天收入支出最大的金额，金额是多少
     * */
    public static float getMaxMoneyOneDayInMonth(int year,int month,int kind){
        String sql = "select sum(money) from accounttb where year=? and month=? and kind=? group by day order by sum(money) desc";
        Cursor cursor = db.rawQuery(sql, new String[]{year + "", month + "", kind + ""});
        if (cursor.moveToFirst()) {
            float money = cursor.getFloat(cursor.getColumnIndex("sum(money)"));
            return money;
        }
        return 0;
    }

    /** 根据指定月份每一日收入或者支出的总钱数的集合*/
    public static List<BarChartItemBean>getSumMoneyOneDayInMonth(int year,int month,int kind){
        String sql = "select day,sum(money) from accounttb where year=? and month=? and kind=? group by day";
        Cursor cursor = db.rawQuery(sql, new String[]{year + "", month + "", kind + ""});
        List<BarChartItemBean>list = new ArrayList<>();
        while (cursor.moveToNext()) {
            int day = cursor.getInt(cursor.getColumnIndex("day"));
            float smoney = cursor.getFloat(cursor.getColumnIndex("sum(money)"));
            BarChartItemBean itemBean = new BarChartItemBean(year, month, day, smoney);
            list.add(itemBean);
        }
        return list;
    }

    public static void insertBudget(String typename, float money, int year, int month) {
        ContentValues values = new ContentValues();
        values.put("typename", typename);
        values.put("money", money);
        values.put("year", year);
        values.put("month", month);
        db.insert("budgettb", null, values);
    }

    public static float getTypeBudget(String typename, int year, int month) {
        String sql = "select money from budgettb where typename=? and year=? and month=?";
        Cursor cursor = db.rawQuery(sql, new String[]{typename, year+"", month+""});
        if (cursor.moveToFirst()) {
            return cursor.getFloat(cursor.getColumnIndex("money"));
        }
        return 0;
    }

    public static List<BudgetBean> getTypeBudgetList(int year, int month) {
        List<BudgetBean> list = new ArrayList<>();
        String sql = "select typename, money from budgettb where year=? and month=?";
        Cursor cursor = db.rawQuery(sql, new String[]{year+"", month+""});
        while (cursor.moveToNext()) {
            BudgetBean bean = new BudgetBean();
            bean.setTypename(cursor.getString(cursor.getColumnIndex("typename")));
            bean.setMoney(cursor.getFloat(cursor.getColumnIndex("money")));
            list.add(bean);
        }
        cursor.close();
        return list;
    }

    public static void deleteBudget(String typename, int year, int month) {
        String sql = "delete from budgettb where typename=? and year=? and month=?";
        db.execSQL(sql, new Object[]{typename, year, month});
    }

    public static TypeBean getTypeFromTypetb(String typename) {
        String sql = "select * from typetb where typename=?";
        Cursor cursor = db.rawQuery(sql, new String[]{typename});
        TypeBean bean = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("typename"));
            int imageId = cursor.getInt(cursor.getColumnIndex("imageId"));
            int sImageId = cursor.getInt(cursor.getColumnIndex("sImageId"));
            int kind = cursor.getInt(cursor.getColumnIndex("kind"));
            bean = new TypeBean(id, name, imageId, sImageId, kind);
        }
        cursor.close();
        return bean;
    }

    public static List<BudgetAnalysisBean> getTypeBudgetAnalysisList(int year, int month) {
        List<BudgetAnalysisBean> list = new ArrayList<>();
        // 获取所有支出类型
        List<TypeBean> typeList = getTypeList(0);
        
        for (TypeBean type : typeList) {
            BudgetAnalysisBean analysisBean = new BudgetAnalysisBean();
            analysisBean.setTypename(type.getTypename());
            
            // 获取该类型的预算金额
            float budgetMoney = getTypeBudget(type.getTypename(), year, month);
            analysisBean.setBudgetMoney(budgetMoney);
            
            // 获取该类型的实际支出
            float spendMoney = getSumMoneyOneMonthByType(year, month, type.getTypename());
            analysisBean.setSpendMoney(spendMoney);
            
            list.add(analysisBean);
        }
        return list;
    }

    // 获取某年某月某一类的支出总金额
    public static float getSumMoneyOneMonthByType(int year, int month, String typename) {
        float total = 0.0f;
        String sql = "select sum(money) from accounttb where year=? and month=? and typename=?";
        Cursor cursor = db.rawQuery(sql, new String[]{year+"", month+"", typename});
        if (cursor.moveToFirst()) {
            total = cursor.getFloat(0);
        }
        return total;
    }

    public static List<Float> getRecentSixMonthAmount(String typename) {
        List<Float> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        
        for (int i = 0; i < 6; i++) {
            float amount = getSumMoneyOneMonthByType(currentYear, currentMonth, typename);
            list.add(0, amount); // 添加到列表开头，保持时间顺序
            
            currentMonth--;
            if (currentMonth == 0) {
                currentMonth = 12;
                currentYear--;
            }
        }
        return list;
    }

    public static int getCountItemOneDay(int year, int month, int day, int kind) {
        int count = 0;
        String sql = "select count(*) from accounttb where year=? and month=? and day=? and kind=?";
        Cursor cursor = db.rawQuery(sql, new String[]{year+"", month+"", day+"", kind+""});
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        return count;
    }

    public static int getCountItemOneYear(int year, int kind) {
        int count = 0;
        String sql = "select count(*) from accounttb where year=? and kind=?";
        Cursor cursor = db.rawQuery(sql, new String[]{year+"", kind+""});
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        return count;
    }

    public static List<AccountBean> getAccountListOneYearFromAccounttb(int year) {
        List<AccountBean> list = new ArrayList<>();
        String sql = "select * from accounttb where year=? order by id desc";
        Cursor cursor = db.rawQuery(sql, new String[]{year + ""});
        
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String typename = cursor.getString(cursor.getColumnIndex("typename"));
            String beizhu = cursor.getString(cursor.getColumnIndex("beizhu"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            int sImageId = cursor.getInt(cursor.getColumnIndex("sImageId"));
            int kind = cursor.getInt(cursor.getColumnIndex("kind"));
            float money = cursor.getFloat(cursor.getColumnIndex("money"));
            int month = cursor.getInt(cursor.getColumnIndex("month"));
            int day = cursor.getInt(cursor.getColumnIndex("day"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            AccountBean accountBean = new AccountBean(id, typename, sImageId, beizhu, money, time, year, month, day, kind);
            accountBean.setLatitude(latitude);
            accountBean.setLongitude(longitude);
            list.add(accountBean);
        }
        return list;
    }

}
