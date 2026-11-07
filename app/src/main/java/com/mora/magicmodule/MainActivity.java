package com.mora.magicmodule;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
        
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#F8F9FA"));
        
        // 读取日志文件内容
        StringBuilder logSb = new StringBuilder();
        boolean hookRunning = false;
        try {
            File logFile = new File("/data/local/tmp/mora_logs.txt");
            if (logFile.exists() && logFile.length() > 0) {
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    logSb.append(line).append("\n");
                }
                reader.close();
                hookRunning = true;
            }
        } catch (Exception e) {
            logSb.append("读取日志失败: ").append(e.getMessage());
        }
        
        // 顶部状态栏卡片
        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(48, 40, 48, 40);
        
        int statusColor;
        String statusText;
        if (hookRunning) {
            statusColor = Color.parseColor("#4CAF50");
            statusText = "● 已启用 - Hook 正在运行";
        } else {
            statusColor = Color.parseColor("#9E9E9E");
            statusText = "○ 未启用 - 未检测到 Hook 运行";
        }
        statusCard.setBackgroundColor(statusColor);
        
        TextView title = new TextView(this);
        title.setText("魔姬增强模块");
        title.setTextSize(22);
        title.getPaint().setFakeBoldText(true);
        title.setTextColor(Color.WHITE);
        title.setPadding(0, 0, 0, 8);
        statusCard.addView(title);
        
        TextView status = new TextView(this);
        status.setText(statusText);
        status.setTextSize(14);
        status.setTextColor(Color.WHITE);
        statusCard.addView(status);
        
        rootLayout.addView(statusCard);
        
        // 日志区域标题
        TextView logTitle = new TextView(this);
        logTitle.setText("运行日志");
        logTitle.setTextSize(16);
        logTitle.getPaint().setFakeBoldText(true);
        logTitle.setTextColor(Color.parseColor("#1D1B20"));
        logTitle.setPadding(32, 24, 32, 12);
        rootLayout.addView(logTitle);
        
        // 日志内容
        TextView logView = new TextView(this);
        logView.setTextSize(13);
        logView.setTextColor(Color.parseColor("#333333"));
        logView.setPadding(28, 24, 28, 24);
        logView.setBackgroundColor(Color.WHITE);
        logView.setMovementMethod(new ScrollingMovementMethod());
        
        if (!hookRunning) {
            logSb.append("暂无日志\n\n");
            logSb.append("=== 如何启用 ===");
            logSb.append("\n1. 打开 LSPosed\n");
            logSb.append("2. 找到「魔姬增强模块」\n");
            logSb.append("3. 启用模块，勾选魔姬助手\n");
            logSb.append("4. 重启魔姬助手\n");
            logSb.append("5. 回到这个界面查看日志\n");
        }
        
        logView.setText(logSb.toString());
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(logView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.setMargins(32, 0, 32, 32);
        scrollView.setLayoutParams(params);
        
        rootLayout.addView(scrollView);
        setContentView(rootLayout);
    }
}
