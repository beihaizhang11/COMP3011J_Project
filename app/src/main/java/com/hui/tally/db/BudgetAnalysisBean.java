package com.hui.tally.db;

public class BudgetAnalysisBean {
    private String typename;    // 类型名称
    private float budgetMoney;  // 预算金额
    private float spendMoney;   // 已花费金额
    
    public String getTypename() {
        return typename;
    }
    
    public void setTypename(String typename) {
        this.typename = typename;
    }
    
    public float getBudgetMoney() {
        return budgetMoney;
    }
    
    public void setBudgetMoney(float budgetMoney) {
        this.budgetMoney = budgetMoney;
    }
    
    public float getSpendMoney() {
        return spendMoney;
    }
    
    public void setSpendMoney(float spendMoney) {
        this.spendMoney = spendMoney;
    }
} 