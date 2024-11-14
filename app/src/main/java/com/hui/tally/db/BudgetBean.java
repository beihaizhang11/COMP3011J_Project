package com.hui.tally.db;

public class BudgetBean {
    private String typename;
    private float money;
    private int year;
    private int month;

    public BudgetBean() {}  // 无参构造函数

    public BudgetBean(String typename, float money, int year, int month) {
        this.typename = typename;
        this.money = money;
        this.year = year;
        this.month = month;
    }

    public String getTypename() {
        return typename;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }
}