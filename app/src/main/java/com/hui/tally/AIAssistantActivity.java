package com.hui.tally;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hui.tally.adapter.ChatAdapter;
import com.hui.tally.bean.ChatMessage;
import com.hui.tally.utils.WenXin;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AIAssistantActivity extends AppCompatActivity {
    private EditText inputEt;
    private Button sendBtn;
    private ImageView backIv;
    private RecyclerView chatRv;
    private WenXin wenXin;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);
        
        initView();
        wenXin = new WenXin();
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);
        
        // 设置RecyclerView的适配器
        chatRv.setAdapter(chatAdapter);
        
        // 添加欢迎消息
        chatAdapter.addMessage(new ChatMessage("AI Assistant", "Hi！I am your AI assistant，what can I help you today？", true));
    }

    private void initView() {
        inputEt = findViewById(R.id.ai_assistant_et_input);
        sendBtn = findViewById(R.id.ai_assistant_btn_send);
        backIv = findViewById(R.id.ai_assistant_iv_back);
        chatRv = findViewById(R.id.ai_assistant_rv_chat);
        
        // 设置RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRv.setLayoutManager(layoutManager);
        
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String input = inputEt.getText().toString().trim();
                if (!input.isEmpty()) {
                    // 添加用户消息
                    chatAdapter.addMessage(new ChatMessage("Me", input, false));
                    inputEt.setText("");
                    
                    // 添加加载提示消息
                    chatAdapter.addMessage(new ChatMessage("AI Assistant", "Thinking...", true));
                    
                    // 滚动到底部
                    chatRv.scrollToPosition(chatMessages.size() - 1);
                    
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String response = wenXin.getAnswer(input);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 移除加载提示消息
                                        chatAdapter.removeMessage(chatMessages.size() - 1);
                                        // 添加AI回复
                                        chatAdapter.addMessage(new ChatMessage("AI Assistant", response, true));
                                        chatRv.scrollToPosition(chatMessages.size() - 1);
                                    }
                                });
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 移除加载提示消息
                                        chatAdapter.removeMessage(chatMessages.size() - 1);
                                        chatAdapter.addMessage(new ChatMessage("AI Assistant", "Sorry，problems occurs：" + e.getMessage(), true));
                                        chatRv.scrollToPosition(chatMessages.size() - 1);
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        });
    }
} 