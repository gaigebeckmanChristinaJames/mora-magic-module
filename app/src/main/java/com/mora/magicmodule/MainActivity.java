package com.mora.magicmodule;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
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
        rootLayout.setBackgroundColor(Color.parseColor("#F5F7FA"));
        
        // === 顶部状态区域 ===
        LinearLayout statusLayout = new LinearLayout(this);
        statusLayout.setOrientation(LinearLayout.VERTICAL);
        statusLayout.setPadding(48, 56, 48, 32);
        statusLayout.setBackgroundColor(Color.parseColor("#6750A4"));
        
        // 标题
        TextView title = new TextView(this);
        title.setText("魔姬增强模块");
        title.setTextSize(22);
        title.getPaint().setFakeBoldText(true);
        title.setTextColor(Color.WHITE);
        title.setPadding(0, 0, 0, 8);
        statusLayout.addView(title);
        
        // 版本信息
        TextView version = new TextView(this);
        version.setText("v11.2.2 | com.zte.aiassistant");
        version.setTextSize(13);
        version.setTextColor(Color.parseColor("#EADDFF"));
        statusLayout.addView(version);
        
        rootLayout.addView(statusLayout);
        
        // === Hook 状态卡片 ===
        LinearLayout hookCard = new LinearLayout(this);
        hookCard.setOrientation(LinearLayout.VERTICAL);
        hookCard.setPadding(32, 28, 32, 28);
        hookCard.setBackgroundColor(Color.WHITE);
        hookCard.setElevation(2);
        LinearLayout.LayoutParams hookParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        hookParams.setMargins(32, 32, 32, 16);
        hookCard.setLayoutParams(hookParams);
        
        TextView hookTitle = new TextView(this);
        hookTitle.setText("Hook 状态");
        hookTitle.setTextSize(15);
        hookTitle.getPaint().setFakeBoldText(true);
        hookTitle.setTextColor(Color.parseColor("#1D1B20"));
        hookTitle.setPadding(0, 0, 0, 12);
        hookCard.addView(hookTitle);
        
        TextView hookContent = new TextView(this);
        hookContent.setTextSize(13);
        hookContent.setTextColor(Color.parseColor("#49454F"));
        
        StringBuilder hookSb = new StringBuilder();
        hookSb.append("✓ PopupWindow Hook - 菜单注入\n");
        hookSb.append("✓ OkHttp Hook - 网络拦截\n");
        hookSb.append("✓ 消息改写 - chat 接口\n");
        
        hookContent.setText(hookSb.toString());
        hookCard.addView(hookContent);
        
        rootLayout.addView(hookCard);
        
        // === 日志区域 ===
        TextView logTitle = new TextView(this);
        logTitle.setText("运行日志");
        logTitle.setTextSize(15);
        logTitle.getPaint().setFakeBoldText(true);
        logTitle.setTextColor(Color.parseColor("#1D1B20"));
        LinearLayout.LayoutParams logTitleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        logTitleParams.setMargins(32, 16, 32, 8);
        logTitle.setLayoutParams(logTitleParams);
        rootLayout.addView(logTitle);
        
        // 日志内容卡片
        LinearLayout logCard = new LinearLayout(this);
        logCard.setOrientation(LinearLayout.VERTICAL);
        logCard.setPadding(24, 24, 24, 24);
        logCard.setBackgroundColor(Color.parseColor("#1E1E1E"));
        LinearLayout.LayoutParams logCardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        logCardParams.setMargins(32, 0, 32, 32);
        logCard.setLayoutParams(logCardParams);
        
        TextView logView = new TextView(this);
        logView.setTextSize(12);
        logView.setTextColor(Color.parseColor("#A5D6A7"));
        logView.setTypeface(Typeface.MONOSPACE);
        logView.setMovementMethod(new ScrollingMovementMethod());
        
        // 读取日志文件
        StringBuilder logSb = new StringBuilder();
        try {
            File logFile = new File(android.os.Environment.getExternalStorageDirectory(), 
                "Android/data/com.mora.magicmodule/files/logs.txt");
            if (logFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null && count < 200) {
                    logSb.append(line).append("\n");
                    count++;
                }
                reader.close();
            } else {
                logSb.append("// 暂无日志\n");
                logSb.append("// 打开魔姬助手触发操作后刷新查看\n");
            }
        } catch (Exception e) {
            logSb.append("// 读取日志失败: " + e.getMessage());
        }
        
        logView.setText(logSb.toString());
        logCard.addView(logView);
        
        rootLayout.addView(logCard);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(rootLayout);
        setContentView(scrollView);
    }
}
