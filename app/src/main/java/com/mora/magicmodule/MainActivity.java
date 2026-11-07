package com.mora.magicmodule;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        
        // 标题
        TextView title = new TextView(this);
        title.setText("魔姬增强模块");
        title.setTextSize(24);
        title.getPaint().setFakeBoldText(true);
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);
        
        // 状态信息
        TextView status = new TextView(this);
        status.setTextSize(16);
        status.setPadding(0, 0, 0, 24);
        
        StringBuilder sb = new StringBuilder();
        sb.append("模块版本: v11.0\n");
        sb.append("目标包名: com.zte.aiassistant\n\n");
        
        sb.append("=== Hook 状态 ===\n");
        sb.append("PopupWindow Hook: 已安装\n");
        sb.append("OkHttp Hook: 已安装\n\n");
        
        sb.append("=== 运行日志 ===\n");
        for (String log : MoraHooker.logs) {
            sb.append(log).append("\n");
        }
        
        status.setText(sb.toString());
        status.setMovementMethod(new ScrollingMovementMethod());
        
        layout.addView(status);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        setContentView(scrollView);
    }
}
