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
        
        // 顶部状态栏卡片
        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(48, 40, 48, 40);
        statusCard.setBackgroundColor(Color.parseColor("#6750A4"));
        
        TextView title = new TextView(this);
        title.setText("魔姬增强模块");
        title.setTextSize(22);
        title.getPaint().setFakeBoldText(true);
        title.setTextColor(Color.WHITE);
        title.setPadding(0, 0, 0, 8);
        statusCard.addView(title);
        
        TextView version = new TextView(this);
        version.setText("v11.3 | com.zte.aiassistant");
        version.setTextSize(13);
        version.setTextColor(Color.parseColor("#EADDFF"));
        statusCard.addView(version);
        
        rootLayout.addView(statusCard);
        
        // Hook 状态标签
        LinearLayout hookRow = new LinearLayout(this);
        hookRow.setOrientation(LinearLayout.HORIZONTAL);
        hookRow.setPadding(32, 24, 32, 24);
        
        TextView hook1 = createBadge("PopupWindow", "#4CAF50");
        TextView hook2 = createBadge("OkHttp", "#4CAF50");
        TextView hook3 = createBadge("日志", "#FF9800");
        
        hookRow.addView(hook1);
        hookRow.addView(hook2);
        hookRow.addView(hook3);
        rootLayout.addView(hookRow);
        
        // 日志区域标题
        TextView logTitle = new TextView(this);
        logTitle.setText("运行日志");
        logTitle.setTextSize(16);
        logTitle.getPaint().setFakeBoldText(true);
        logTitle.setTextColor(Color.parseColor("#1D1B20"));
        logTitle.setPadding(32, 16, 32, 12);
        rootLayout.addView(logTitle);
        
        // 日志内容卡片
        TextView logView = new TextView(this);
        logView.setTextSize(13);
        logView.setTextColor(Color.parseColor("#333333"));
        logView.setPadding(28, 24, 28, 24);
        logView.setBackgroundColor(Color.WHITE);
        logView.setMovementMethod(new ScrollingMovementMethod());
        
        // 读取日志文件
        StringBuilder logSb = new StringBuilder();
        try {
            File logFile = new File(android.os.Environment.getExternalStorageDirectory(), 
                "Android/data/com.mora.magicmodule/files/logs.txt");
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
                logSb.append("触发操作后日志会自动显示在这里。\n\n");
                logSb.append("=== Hook 点列表 ===\n");
                logSb.append("✓ PopupWindow Hook\n");
                logSb.append("  - 注入三点菜单入口\n\n");
                logSb.append("✓ OkHttp Hook\n");
                logSb.append("  - 拦截聊天接口\n");
                logSb.append("  - 拦截 OSS 上传接口\n");
            }
        } catch (Exception e) {
            logSb.append("读取日志失败: ").append(e.getMessage());
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
    
    private TextView createBadge(String text, String color) {
        TextView badge = new TextView(this);
        badge.setText(text);
        badge.setTextSize(12);
        badge.setTextColor(Color.WHITE);
        badge.setPadding(20, 10, 20, 10);
        
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(20);
        badge.setBackground(drawable);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 12, 0);
        badge.setLayoutParams(params);
        
        return badge;
    }
}
