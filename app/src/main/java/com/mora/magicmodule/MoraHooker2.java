package com.mora.magicmodule;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MoraHooker2 implements IXposedHookLoadPackage {

    private static final String TAG = "MoraHooker2";
    private static Context appContext = null;
    private static File selectedFile = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!"com.zte.aiassistant".equals(lpparam.packageName)) return;

        addLog("MoraHooker2: 初始化完成");
        
        // 测试 Toast - 确认 Hook 有没有运行
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            android.widget.Toast.makeText(android.app.ActivityThread.currentApplication(), "MoraHooker2 已加载", android.widget.Toast.LENGTH_LONG).show();
        });

        // Hook PopupWindow 注入菜单
        try {
            XposedHelpers.findAndHookMethod(
                "android.widget.PopupWindow",
                lpparam.classLoader,
                "showAtLocation",
                View.class, int.class, int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            View parent = (View) param.args[0];
                            Context ctx = parent.getContext();
                            injectSendFileMenu(ctx, parent);
                        } catch (Exception e) {
                            addLog("菜单注入异常: " + e.getMessage());
                        }
                    }
                }
            );
            addLog("Hook PopupWindow 成功");
        } catch (Exception e) {
            addLog("Hook PopupWindow 失败: " + e.getMessage());
        }
    }

    private void injectSendFileMenu(Context ctx, View parent) {
        try {
            View root = parent.getRootView();
            if (root == null) return;

            // 找 LinearLayout 根布局
            ViewGroup linearLayout = findLinearLayout(root);
            if (linearLayout == null) return;

            // 检查有没有"清除历史记录"
            boolean hasClearHistory = false;
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                View child = linearLayout.getChildAt(i);
                if (child instanceof TextView) {
                    String text = ((TextView) child).getText().toString();
                    if (text.contains("清除历史")) {
                        hasClearHistory = true;
                        break;
                    }
                }
            }

            if (!hasClearHistory) return;

            // 检查有没有已经注入过
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                View child = linearLayout.getChildAt(i);
                if (child instanceof TextView) {
                    String text = ((TextView) child).getText().toString();
                    if (text.equals("发送文件")) return; // 已经注入过
                }
            }

            // 创建新菜单项
            TextView menuItem = new TextView(ctx);
            menuItem.setText("发送文件");
            menuItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            menuItem.setPadding(dp(48), dp(12), dp(16), dp(12));
            menuItem.setTextColor(Color.BLACK);
            menuItem.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

            // 点击事件
            menuItem.setOnClickListener(v -> {
                addLog("点击了发送文件菜单");
                showFileChooserDialog(ctx);
            });

            // 添加到 LinearLayout 末尾
            linearLayout.addView(menuItem);
            addLog("菜单注入成功: 发送文件");
        } catch (Exception e) {
            addLog("injectSendFileMenu 异常: " + e.getMessage());
        }
    }

    private ViewGroup findLinearLayout(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            if (vg instanceof LinearLayout) {
                return vg;
            }
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                ViewGroup result = findLinearLayout(child);
                if (result != null) return result;
            }
        }
        return null;
    }

    private void showFileChooserDialog(Context ctx) {
        try {
            appContext = ctx;
            selectedFile = null;

            LinearLayout layout = new LinearLayout(ctx);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(dp(20), dp(20), dp(20), dp(20));

            TextView title = new TextView(ctx);
            title.setText("发送文件");
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            title.setGravity(Gravity.CENTER);
            title.setPadding(0, 0, 0, dp(20));
            layout.addView(title);

            TextView statusText = new TextView(ctx);
            statusText.setText("未选择文件");
            statusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            statusText.setPadding(0, dp(10), 0, dp(10));
            layout.addView(statusText);

            // 选择文件按钮
            TextView selectBtn = new TextView(ctx);
            selectBtn.setText("选择文件");
            selectBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            selectBtn.setGravity(Gravity.CENTER);
            selectBtn.setPadding(0, dp(12), 0, dp(12));
            selectBtn.setBackgroundColor(0xFFE0E0FF);
            layout.addView(selectBtn);

            selectBtn.setOnClickListener(v -> {
                showFilePicker(ctx, statusText);
            });

            // 发送按钮
            TextView sendBtn = new TextView(ctx);
            sendBtn.setText("发送");
            sendBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            sendBtn.setGravity(Gravity.CENTER);
            sendBtn.setPadding(0, dp(12), 0, dp(12));
            sendBtn.setBackgroundColor(0xFFB0B0FF);
            sendBtn.setEnabled(false);
            layout.addView(sendBtn);

            sendBtn.setOnClickListener(v -> {
                if (selectedFile != null) {
                    addLog("开始上传文件: " + selectedFile.getName());
                    new Thread(() -> {
                        try {
                            String ossUrl = uploadFileToOSS(selectedFile);
                            addLog("上传成功: " + ossUrl);
                            showToast(ctx, "上传成功: " + selectedFile.getName());
                        } catch (Exception e) {
                            addLog("上传失败: " + e.getMessage());
                            showToast(ctx, "上传失败: " + e.getMessage());
                        }
                    }).start();
                }
            });

            new AlertDialog.Builder(ctx)
                .setView(layout)
                .setCancelable(true)
                .show();
        } catch (Exception e) {
            addLog("showFileChooserDialog 异常: " + e.getMessage());
        }
    }

    private void showFilePicker(Context ctx, TextView statusText) {
        try {
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File[] files = downloadDir.listFiles();

            LinearLayout layout = new LinearLayout(ctx);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(dp(20), dp(20), dp(20), dp(20));

            TextView title = new TextView(ctx);
            title.setText("选择文件");
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            title.setGravity(Gravity.CENTER);
            title.setPadding(0, 0, 0, dp(16));
            layout.addView(title);

            TextView pathText = new TextView(ctx);
            pathText.setText("当前: " + downloadDir.getAbsolutePath());
            pathText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            pathText.setPadding(0, 0, 0, dp(12));
            layout.addView(pathText);

            ScrollView scrollView = new ScrollView(ctx);
            LinearLayout fileList = new LinearLayout(ctx);
            fileList.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(fileList);
            layout.addView(scrollView);

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        TextView fileItem = new TextView(ctx);
                        fileItem.setText(file.getName());
                        fileItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        fileItem.setPadding(dp(8), dp(10), dp(8), dp(10));
                        fileItem.setOnClickListener(v -> {
                            selectedFile = file;
                            statusText.setText("已选择: " + file.getName());
                            showToast(ctx, "已选择: " + file.getName());
                        });
                        fileList.addView(fileItem);
                    }
                }
            }

            new AlertDialog.Builder(ctx)
                .setView(layout)
                .setCancelable(true)
                .show();
        } catch (Exception e) {
            addLog("showFilePicker 异常: " + e.getMessage());
        }
    }

    private String uploadFileToOSS(File file) throws Exception {
        // 1. 获取 presigned URL
        URL url = new URL("https://aigc.ztems.com/aigcapi/oss/file/presignedurl");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String requestBody = "{\"fileName\":\"" + file.getName() + "\",\"filePath\":\"tempfiles/\"}";
        conn.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));

        int responseCode = conn.getResponseCode();
        addLog("presignedurl 响应码: " + responseCode);

        if (responseCode != 200) {
            throw new Exception("presignedurl 请求失败: " + responseCode);
        }

        // 读取响应
        InputStream is = conn.getInputStream();
        byte[] buffer = new byte[4096];
        StringBuilder sb = new StringBuilder();
        int len;
        while ((len = is.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
        }
        is.close();

        String response = sb.toString();
        addLog("presignedurl 响应: " + response);

        // 解析上传 URL
        String uploadUrl = parseUploadUrl(response);
        if (uploadUrl == null) {
            throw new Exception("无法解析上传 URL");
        }

        // 2. 上传文件到 OSS
        URL ossUrl = new URL(uploadUrl);
        HttpURLConnection ossConn = (HttpURLConnection) ossUrl.openConnection();
        ossConn.setRequestMethod("PUT");
        ossConn.setDoOutput(true);

        FileInputStream fis = new FileInputStream(file);
        OutputStream os = ossConn.getOutputStream();
        byte[] buf = new byte[4096];
        int l;
        while ((l = fis.read(buf)) != -1) {
            os.write(buf, 0, l);
        }
        fis.close();
        os.close();

        int ossResponseCode = ossConn.getResponseCode();
        addLog("OSS 上传响应码: " + ossResponseCode);

        if (ossResponseCode != 200) {
            throw new Exception("OSS 上传失败: " + ossResponseCode);
        }

        // 返回可访问的 URL
        return "https://zaigc-oss.oss-cn-shenzhen.aliyuncs.com/tempfiles/1249941/" + file.getName();
    }

    private String parseUploadUrl(String response) {
        // 简单解析 JSON 里的 url 字段
        try {
            int start = response.indexOf("\"url\":\"") + 7;
            int end = response.indexOf("\"", start);
            if (start > 6 && end > start) {
                return response.substring(start, end).replace("\\/", "/");
            }
        } catch (Exception e) {
            addLog("解析上传 URL 失败: " + e.getMessage());
        }
        return null;
    }

    private void addLog(String message) {
        try {
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String logLine = time + " [MoraHooker2] " + message + "\n";

            // 写到文件
            if (appContext != null) {
                File logFile = new File(appContext.getFilesDir(), "mora_logs/logs.txt");
                logFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(logFile, true);
                fos.write(logLine.getBytes(StandardCharsets.UTF_8));
                fos.close();
            }
        } catch (Exception e) {
            // 忽略
        }
    }

    private void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    private int dp(int dp) {
        return (int) (dp * appContext.getResources().getDisplayMetrics().density);
    }
}
