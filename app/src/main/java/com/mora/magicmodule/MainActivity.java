package com.mora.magicmodule;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        layout.setBackgroundColor(Color.parseColor("#F5F5F5"));
        
        // 标题
        TextView title = new TextView(this);
        title.setText("魔姬增强模块");
        title.setTextSize(24);
        title.getPaint().setFakeBoldText(true);
        title.setTextColor(Color.parseColor("#1A1A1A"));
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);
        
        // 状态卡片
        TextView status = new TextView(this);
        status.setTextSize(15);
        status.setTextColor(Color.parseColor("#333333"));
        status.setPadding(32, 28, 32, 28);
        status.setBackgroundColor(Color.WHITE);
        status.setElevation(2);
        
        StringBuilder sb = new StringBuilder();
        sb.append("模块版本: v11.2\n");
        sb.append("目标包名: com.zte.aiassistant\n\n");
        
        sb.append("=== Hook 状态 ===\n");
        sb.append("✓ PopupWindow Hook: 已安装\n");
        sb.append("✓ OkHttp Hook: 已安装\n\n");
        
        status.setText(sb.toString);
        layout.addView(status);
        
        // 日志标题
        TextView logTitle = new TextView(this);
        logTitle.setText("运行日志");
        logTitle.setTextSize(18);
        logTitle.getPaint().setFakeBoldText(true);
        logTitle.setTextColor(Color.parseColor("#1A1A1A"));
        logTitle.setPadding(0, 24, 0, 12);
        layout.addView(logTitle);
        
        // 日志内容
        TextView logView = new TextView(this);
        logView.setTextSize(13);
        logView.setTextColor(Color.parseColor("#555555"));
        logView.setPadding(32, 28, 32, 28);
        logView.setBackgroundColor(Color.WHITE);
        logView.setMovementMethod(new ScrollingMovementMethod());
        
        // 尝试读取日志文件
        StringBuilder logSb = new StringBuilder();
        try {
            File logFile = new File(getExternalFilesDir(null), "mora_logs.txt");
            if (logFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    logSb.append(line).append("\n");
                }
                reader.close();
            } else {
                logSb.append("暂无日志\n\n");
                logSb.append("请打开魔姬助手应用，\n");
                logSb.append("触发一些操作后再回来查看。");
            }
        } catch (Exception e) {
            logSb.append("读取日志失败: ").append(e.getMessage());
        }
        
        logView.setText(logSb.toString());
        layout.addView(logView);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        setContentView(scrollView);
    }
}
