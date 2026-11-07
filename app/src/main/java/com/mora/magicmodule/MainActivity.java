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
        layout.setBackgroundColor(Color.parseColor("#F5F5F5"));
        
        // 标题
        TextView title = new TextView(this);
        title.setText("魔姬增强模块");
        title.setTextSize(24);
        title.getPaint().setFakeBoldText(true);
        title.setTextColor(Color.parseColor("#1A1A1A"));
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);
        
        // 版本信息
        TextView version = new TextView(this);
        version.setText("版本: v11.2 | 目标: com.zte.aiassistant");
        version.setTextSize(13);
        version.setTextColor(Color.parseColor("#888888"));
        version.setPadding(0, 0, 0, 24);
        layout.addView(version);
        
        // Hook 点状态
        TextView hookTitle = new TextView(this);
        hookTitle.setText("=== Hook 点状态 ===");
        hookTitle.setTextSize(16);
        hookTitle.getPaint().setFakeBoldText(true);
        hookTitle.setTextColor(Color.parseColor("#1A1A1A"));
        hookTitle.setPadding(0, 0, 0, 12);
        layout.addView(hookTitle);
        
        TextView hooks = new TextView(this);
        hooks.setTextSize(14);
        hooks.setTextColor(Color.parseColor("#333333"));
        hooks.setPadding(32, 24, 32, 24);
        hooks.setBackgroundColor(Color.WHITE);
        
        StringBuilder sb = new StringBuilder();
        sb.append("✓ PopupWindow Hook\n");
        sb.append("  - 作用: 注入三点菜单入口\n");
        sb.append("  - 状态: 已安装\n\n");
        
        sb.append("✓ OkHttp Hook\n");
        sb.append("  - 作用: 拦截网络请求\n");
        sb.append("  - 状态: 已安装\n");
        sb.append("  - 拦截接口:\n");
        sb.append("    • /aimoe/service/api/v1/chat\n");
        sb.append("    • /aigcapi/oss/file/presignedurl\n");
        sb.append("    • /aigcapi/oss/config\n\n");
        
        sb.append("=== 功能列表 ===\n");
        sb.append("• 菜单注入: 三点菜单加入口\n");
        sb.append("• 消息改写: 修改聊天请求\n");
        sb.append("• 文件附件: OSS 上传\n");
        sb.append("• 调试日志: 记录所有请求\n");
        
        hooks.setText(sb.toString());
        layout.addView(hooks);
        
        // 使用说明
        TextView usageTitle = new TextView(this);
        usageTitle.setText("=== 使用说明 ===");
        usageTitle.setTextSize(16);
        usageTitle.getPaint().setFakeBoldText(true);
        usageTitle.setTextColor(Color.parseColor("#1A1A1A"));
        usageTitle.setPadding(0, 24, 0, 12);
        layout.addView(usageTitle);
        
        TextView usage = new TextView(this);
        usage.setTextSize(14);
        usage.setTextColor(Color.parseColor("#333333"));
        usage.setPadding(32, 24, 32, 24);
        usage.setBackgroundColor(Color.WHITE);
        usage.setMovementMethod(new ScrollingMovementMethod());
        
        StringBuilder usageSb = new StringBuilder();
        usageSb.append("1. 打开魔姬助手\n");
        usageSb.append("2. 点击右上角三点菜单\n");
        usageSb.append("3. 点击「魔姬增强设置」\n");
        usageSb.append("4. 查看详细日志\n\n");
        usageSb.append("日志内容包括:\n");
        usageSb.append("• 所有网络请求 URL\n");
        usageSb.append("• 请求体和响应\n");
        usageSb.append("• Hook 触发记录\n");
        
        usage.setText(usageSb.toString());
        layout.addView(usage);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        setContentView(scrollView);
    }
}
