package com.mora.magicmodule;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
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
        layout.setBackgroundColor(Color.parseColor("#F5F5F5"));
        
        TextView title = new TextView(this);
        title.setText("魔姬增强模块");
        title.setTextSize(24);
        title.getPaint().setFakeBoldText(true);
        title.setTextColor(Color.parseColor("#1A1A1A"));
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);
        
        TextView status = new TextView(this);
        status.setTextSize(15);
        status.setTextColor(Color.parseColor("#333333"));
        status.setPadding(32, 28, 32, 28);
        status.setBackgroundColor(Color.WHITE);
        
        StringBuilder sb = new StringBuilder();
        sb.append("模块版本: v11.2\n");
        sb.append("目标包名: com.zte.aiassistant\n\n");
        sb.append("=== Hook 状态 ===\n");
        sb.append("✓ PopupWindow Hook: 已安装\n");
        sb.append("✓ OkHttp Hook: 已安装\n\n");
        sb.append("=== 使用说明 ===\n");
        sb.append("打开魔姬助手，点击三点菜单，\n");
        sb.append("即可看到「魔姬增强设置」入口。\n");
        
        status.setText(sb.toString());
        layout.addView(status);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        setContentView(scrollView);
    }
}
