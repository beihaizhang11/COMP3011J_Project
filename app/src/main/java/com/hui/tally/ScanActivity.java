package com.hui.tally;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hui.tally.db.AccountBean;
import com.hui.tally.db.DBManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class ScanActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://www.mxnzp.com/api/barcode/goods/details";
    private static final String APP_ID = "nrmef1l2tlflbknw";  // 替换成你申请的APP_ID
    private static final String APP_SECRET = "meIQIXYnGX7KaY49YE1cFjmsqqBaYhOi"; // 替换成你申请的APP_SECRET
    private OkHttpClient client;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        client = new OkHttpClient();
        startScan();
    }
    
    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.PRODUCT_CODE_TYPES);
        integrator.setPrompt("将条形码放入框内扫描");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "扫描取消", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                queryProductInfo(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void queryProductInfo(String barcode) {
        String url = BASE_URL + "?barcode=" + barcode 
                    + "&app_id=" + APP_ID 
                    + "&app_secret=" + APP_SECRET;
                    
        Request request = new Request.Builder()
            .url(url)
            .build();
            
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScanActivity.this, "获取商品信息失败", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        
                        if (json.getInt("code") == 1) { // 检查API返回状态
                            JSONObject data = json.getJSONObject("data");
                            final String name = data.getString("goodsName");
                            final float price = Float.parseFloat(data.getString("price"));
                            
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    saveToDatabase(price, name);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ScanActivity.this, "未找到商品信息", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ScanActivity.this, "解析商品信息失败", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                }
            }
        });
    }
    
    private void saveToDatabase(float price, String productName) {
        AccountBean bean = new AccountBean();
        bean.setTypename("其他");
        bean.setsImageId(R.mipmap.ic_qita_fs);
        bean.setBeizhu(productName);
        bean.setMoney(price);
        
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String time = sdf.format(date);
        bean.setTime(time);
        
        Calendar calendar = Calendar.getInstance();
        bean.setYear(calendar.get(Calendar.YEAR));
        bean.setMonth(calendar.get(Calendar.MONTH) + 1);
        bean.setDay(calendar.get(Calendar.DAY_OF_MONTH));
        bean.setKind(0);  // 支出类型
        
        DBManager.insertItemToAccounttb(bean);
        Toast.makeText(this, "记账成功!", Toast.LENGTH_SHORT).show();
        finish();
    }
}