package com.mora.magicmodule;

import android.app.AlertDialog;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * 魔姬助手增强模块 v7.0
 * 三级菜单结构
 */
public class MoraHooker implements IXposedHookLoadPackage {

    private static final String TAG = "MoraModule";
    private static final String TARGET_PACKAGE = "com.zte.aiassistant";
    private static final int MENU_ITEM_ID = 9999;

    public static final ArrayList<String> logs = new ArrayList<>();
    private static final int MAX_LOGS = 200;

    // 设置
    private static boolean rewriteEnabled = true;
    private static ArrayList<String> customFragments = new ArrayList<>(); // "名字:内容"
    private static String messageTemplate = "{msg}";
    private static int logLevel = 2; // 0=错误 1=警告 2=信息 3=调试

    // 从请求里提取的 API 信息
    private static String lastChatUrl = "";
    private static String lastApiKey = "";
    private static String lastBotId = "";
    private static String lastUserId = "";

    private static void addLog(String msg) {
        addLog(2, msg); // 默认信息级
    }

    private static void addLog(int level, String msg) {
        if (level > logLevel) return;
        String levelTag = new String[]{"E", "W", "I", "D"}[level];
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String line = time + " [" + levelTag + "] " + msg;
        logs.add(line);
        if (logs.size() > MAX_LOGS) logs.remove(0);
        Log.i(TAG, msg);
        
        // 写日志到魔姬助手内部目录
        try {
            android.content.Context ctx = de.robv.android.xposed.AndroidAppHelper.currentApplication();
            if (ctx != null) {
                java.io.File dir = new java.io.File(ctx.getFilesDir(), "mora_logs");
                dir.mkdirs();
                java.io.File logFile = new java.io.File(dir, "logs.txt");
                java.io.FileWriter fw = new java.io.FileWriter(logFile, true);
                fw.write(line + "\n");
                fw.close();
            }
        } catch (Exception e) {
            // 忽略写文件错误
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) return;
        addLog("=== v12.0 模块加载 ===");
        hookPopupWindow();
        hookOkHttp(lpparam);
    }

    private void hookPopupWindow() {
        XposedHelpers.findAndHookMethod(PopupWindow.class, "showAsDropDown",
            View.class, int.class, int.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    injectMenu((PopupWindow) param.thisObject);
                }
            });
        XposedHelpers.findAndHookMethod(PopupWindow.class, "showAtLocation",
            View.class, int.class, int.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    injectMenu((PopupWindow) param.thisObject);
                }
            });
    }

    private void injectMenu(PopupWindow popup) {
        try {
            addLog("检测到 PopupWindow 弹出");
            View content = popup.getContentView();
            if (content == null) {
                addLog("  contentView 为 null，跳过");
                return;
            }
            Context ctx = content.getContext();
            if (ctx == null) {
                addLog("  context 为 null，跳过");
                return;
            }

            // 用资源 id 定位"清除历史记录"
            int clearId = ctx.getResources().getIdentifier("clear_history", "id", "com.zte.aiassistant");
            addLog("  clear_history 资源 id: " + clearId);
            TextView target = null;
            if (clearId != 0) {
                target = content.findViewById(clearId);
                addLog("  通过资源 id 找到: " + (target != null));
            }
            // 兜底：文本匹配
            if (target == null) {
                target = findText(content, "清除历史记录");
                addLog("  通过文本匹配找到: " + (target != null));
            }
            if (target == null) {
                addLog("  未找到清除历史记录菜单项，跳过");
                return;
            }

            ViewGroup parent = (ViewGroup) target.getParent();
            if (parent == null) return;
            if (parent.findViewById(MENU_ITEM_ID) != null) return;

            int targetIndex = parent.indexOfChild(target);
            
            // 1. 在"清除历史记录"上面加分隔线
            View divider1 = new View(ctx);
            divider1.setId(MENU_ITEM_ID + 1);
            divider1.setBackgroundColor(0x1F000000);
            LinearLayout.LayoutParams divider1Lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
            divider1.setLayoutParams(divider1Lp);
            parent.addView(divider1, targetIndex);
            
            // 2. 加"魔姬增强设置"菜单项
            TextView item = new TextView(ctx);
            item.setId(MENU_ITEM_ID);
            item.setText("魔姬增强设置");
            item.setTextSize(16);
            item.setPadding(target.getPaddingLeft(), target.getPaddingTop(),
                target.getPaddingRight(), target.getPaddingBottom());
            item.setTextColor(target.getTextColors());
            
            ViewGroup.LayoutParams lp = target.getLayoutParams();
            if (lp != null) {
                LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(lp.width, lp.height);
                item.setLayoutParams(itemLp);
            }
            
            item.setOnClickListener(v -> showMainDialog(ctx));
            parent.addView(item, targetIndex); // 插在分隔线上面
            
            // 3. 在"魔姬增强设置"下面也加分隔线，这样上下都有空间
            View divider2 = new View(ctx);
            divider2.setId(MENU_ITEM_ID + 2);
            divider2.setBackgroundColor(0x1F000000);
            LinearLayout.LayoutParams divider2Lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
            divider2.setLayoutParams(divider2Lp);
            parent.addView(divider2, targetIndex + 2); // 插在菜单项下面
            
            addLog("菜单注入成功");
        } catch (Exception e) {
            addLog("injectMenu: " + e.getMessage());
        }
    }

    private TextView findText(View v, String text) {
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            if (text.equals(tv.getText().toString().trim())) return tv;
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                TextView r = findText(vg.getChildAt(i), text);
                if (r != null) return r;
            }
        }
        return null;
    }

    private void hookOkHttp(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> clientCls = XposedHelpers.findClass("okhttp3.OkHttpClient", lpparam.classLoader);
            Class<?> reqCls = XposedHelpers.findClass("okhttp3.Request", lpparam.classLoader);
            Class<?> bufCls = XposedHelpers.findClass("okio.Buffer", lpparam.classLoader);

            XposedHelpers.findAndHookMethod(clientCls, "newCall", reqCls, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        Object req = param.args[0];
                        String url = String.valueOf(XposedHelpers.callMethod(
                            XposedHelpers.callMethod(req, "url"), "url"));
                        addLog(3, "网络请求: " + url);
                        lastChatUrl = url;

                        // 提取 header 里的 token
                        Object headers = XposedHelpers.callMethod(req, "headers");
                        if (headers != null) {
                            try {
                                Object auth = XposedHelpers.callMethod(headers, "get", "Authorization");
                                if (auth != null) lastApiKey = auth.toString();
                            } catch (Exception e) {}
                        }

                        // 打印 presignedurl 接口的请求体
                        if (url.contains("presignedurl") || url.contains("oss/")) {
                            Object body = XposedHelpers.callMethod(req, "body");
                            if (body != null) {
                                Object buf = bufCls.newInstance();
                                XposedHelpers.callMethod(body, "writeTo", buf);
                                String orig = (String) XposedHelpers.callMethod(buf, "readUtf8");
                                addLog(3, "[文件接口] 请求体: " + orig);
                            }
                            return;
                        }

                        if (!url.contains("/chat")) return;

                        Object body = XposedHelpers.callMethod(req, "body");
                        if (body == null) return;

                        Object buf = bufCls.newInstance();
                        XposedHelpers.callMethod(body, "writeTo", buf);
                        String orig = (String) XposedHelpers.callMethod(buf, "readUtf8");
                        addLog(3, "原始请求体: " + orig.substring(0, Math.min(200, orig.length())) + "...");

                        // 提取 botId、userId
                        extractApiInfo(orig);

                        String modified = doRewrite(orig);

                        Object ct = XposedHelpers.callMethod(body, "contentType");
                        Class<?> bodyCls = XposedHelpers.findClass("okhttp3.RequestBody", lpparam.classLoader);
                        Object newBody = XposedHelpers.callStaticMethod(bodyCls, "create", ct, modified);

                        Object builder = XposedHelpers.callMethod(req, "newBuilder");
                        XposedHelpers.callMethod(builder, "method", XposedHelpers.callMethod(req, "method"), newBody);
                        param.args[0] = XposedHelpers.callMethod(builder, "build");
                    } catch (Exception e) {
                        addLog("hookOkHttp: " + e.getMessage());
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Object call = param.thisObject;
                        // 不处理响应，先只抓请求
                    } catch (Exception e) {}
                }
            });
            addLog("OkHttp Hook 已安装");
        } catch (Exception e) {
            addLog("hookOkHttp 失败: " + e.getMessage());
        }
    }

    private String doRewrite(String orig) {
        if (!rewriteEnabled || messageTemplate.isEmpty()) return orig;

        int userPos = orig.lastIndexOf("\"role\":\"user\"");
        if (userPos < 0) return orig;

        int contentPos = orig.lastIndexOf("\"content\":\"", userPos);
        if (contentPos < 0) return orig;

        int start = contentPos + "\"content\":\"".length();
        int end = findStrEnd(orig, start);
        if (end <= start) return orig;

        String msg = orig.substring(start, end);
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        String result = messageTemplate;
        result = result.replace("{msg}", msg);
        result = result.replace("{time}", time);
        for (String frag : customFragments) {
            int idx = frag.indexOf(":");
            if (idx > 0) {
                result = result.replace("{" + frag.substring(0, idx) + "}", frag.substring(idx + 1));
            }
        }

        orig = orig.substring(0, start) + result + orig.substring(end);
        addLog("改写: " + msg + " -> " + result);
        return orig;
    }

    private int findStrEnd(String s, int start) {
        int i = start;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\') i += 2;
            else if (c == '"') return i;
            else i++;
        }
        return -1;
    }

    private void extractApiInfo(String body) {
        // 简单提取几个字段
        lastBotId = extractJsonField(body, "bot_id");
        lastUserId = extractJsonField(body, "user_id");
        addLog(3, "提取API信息: bot=" + lastBotId + " user=" + lastUserId);
    }

    private String extractJsonField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int idx = json.indexOf(key);
        if (idx < 0) return "";
        int start = idx + key.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return json.substring(start, end);
    }

    // ========== 三级菜单 ==========

    /** 第一层: 主菜单 */
    private void showMainDialog(Context ctx) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);

        // 标题
        TextView title = new TextView(ctx);
        title.setText("魔姬增强");
        title.setTextSize(24);
        title.getPaint().setFakeBoldText(true);
        title.setPadding(0, 0, 0, 12);
        layout.addView(title);

        TextView subtitle = new TextView(ctx);
        subtitle.setText("增强魔姬助手体验");
        subtitle.setTextSize(14);
        subtitle.setTextColor(0xFF666666);
        subtitle.setPadding(0, 0, 0, 36);
        layout.addView(subtitle);

        // 功能卡片 1: 消息改写
        View card1 = createSettingCard(ctx, "✏️", "消息改写", "自动在消息中添加时间戳", v -> showFormatDialog(ctx));
        layout.addView(card1);

        // 间距
        View space1 = new View(ctx);
        space1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 16));
        layout.addView(space1);

        // 功能卡片 2: 文件附件
        View card2 = createSettingCard(ctx, "📎", "文件附件", "按原生 OSS 方式发送文件", v -> showFileDialog(ctx));
        layout.addView(card2);

        // 间距
        View space2 = new View(ctx);
        space2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 16));
        layout.addView(space2);

        // 功能卡片 3: 日志与状态
        View card3 = createSettingCard(ctx, "📊", "日志与状态", "查看运行日志和模块状态", v -> showSettingsDialog2(ctx));
        layout.addView(card3);

        new AlertDialog.Builder(ctx)
            .setView(layout)
            .setPositiveButton("关闭", null)
            .show();
    }

    /** 创建一个设置卡片条目 */
    private View createSettingCard(Context ctx, String icon, String title, String desc, android.view.View.OnClickListener onClick) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(32, 28, 32, 28);
        card.setBackgroundColor(0xFFF5F5F5);
        card.setClickable(true);
        card.setOnClickListener(onClick);

        // 图标
        TextView iconView = new TextView(ctx);
        iconView.setText(icon);
        iconView.setTextSize(24);
        iconView.setPadding(0, 0, 24, 0);
        card.addView(iconView);

        // 文字区域
        LinearLayout textArea = new LinearLayout(ctx);
        textArea.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textArea.setLayoutParams(lp);

        TextView titleView = new TextView(ctx);
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.getPaint().setFakeBoldText(true);
        textArea.addView(titleView);

        TextView descView = new TextView(ctx);
        descView.setText(desc);
        descView.setTextSize(13);
        descView.setTextColor(0xFF666666);
        descView.setPadding(0, 4, 0, 0);
        textArea.addView(descView);

        card.addView(textArea);

        return card;
    }

    /** 第一层: 文件附件 */
    private void showFileDialog(Context ctx) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(ctx);
        title.setText("文件附件");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        TextView hint = new TextView(ctx);
        hint.setText("选择文件后，按原生 OSS 方式上传并发送");
        hint.setTextSize(14);
        hint.setPadding(0, 0, 0, 30);
        layout.addView(hint);

        Button btnPick = new Button(ctx);
        btnPick.setText("选择文件并发送");
        btnPick.setOnClickListener(v -> {
            showFilePicker(ctx, file -> {
                Toast.makeText(ctx, "已选择: " + file.getName() + "\n开始上传...", Toast.LENGTH_SHORT).show();
                addLog("选择文件: " + file.getAbsolutePath());
                // TODO: 实现 OSS 上传流程
                uploadFileAndSend(ctx, file);
            });
        });
        layout.addView(btnPick);

        new AlertDialog.Builder(ctx).setView(layout).setPositiveButton("返回", null).show();
    }
    
    /** 上传文件并发送（通过反射调用 classes2.dex 里的 FileUploadHelper） */
    private void uploadFileAndSend(Context ctx, java.io.File file) {
        addLog("开始上传文件: " + file.getName());
        try {
            // 反射调用 FileUploadHelper.upload(ctx, file, lastApiKey)
            Class<?> helperClass = Class.forName("com.mora.magicmodule.FileUploadHelper");
            java.lang.reflect.Method method = helperClass.getMethod("upload", Context.class, java.io.File.class, String.class);
            method.invoke(null, ctx, file, lastApiKey);
        } catch (Exception e) {
            addLog("反射调用 FileUploadHelper 失败: " + e.getMessage());
            Toast.makeText(ctx, "文件上传功能未加载，请检查 classes2.dex", Toast.LENGTH_LONG).show();
        }
    }

    /** 第一层: 设置（日志、状态） */
    private void showSettingsDialog2(Context ctx) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(ctx);
        title.setText("设置");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        // 状态信息
        TextView status = new TextView(ctx);
        status.setText("模块状态:\n" +
            "  版本: v7.3\n" +
            "  消息改写: " + (rewriteEnabled ? "开启" : "关闭") + "\n" +
            "  自定义片段: " + customFragments.size() + " 个\n" +
            "  日志条数: " + logs.size() + " 条\n\n" +
            "API 信息:\n" +
            "  接口: " + (lastChatUrl.isEmpty() ? "未获取" : lastChatUrl) + "\n" +
            "  Token: " + (lastApiKey.isEmpty() ? "未获取" : lastApiKey.substring(0, Math.min(20, lastApiKey.length())) + "...") + "\n" +
            "  Bot ID: " + (lastBotId.isEmpty() ? "未获取" : lastBotId) + "\n" +
            "  User ID: " + (lastUserId.isEmpty() ? "未获取" : lastUserId));
        status.setTextSize(13);
        status.setPadding(20, 20, 20, 20);
        status.setBackgroundColor(0xFFEEEEEE);
        layout.addView(status);

        // 日志等级
        TextView lLabel = new TextView(ctx);
        lLabel.setText("日志等级:");
        lLabel.setTextSize(14);
        lLabel.setPadding(0, 25, 0, 10);
        layout.addView(lLabel);

        final String[] levels = {"错误", "警告", "信息", "调试"};
        LinearLayout levelRow = new LinearLayout(ctx);
        levelRow.setOrientation(LinearLayout.HORIZONTAL);
        levelRow.setWeightSum(4);
        for (int i = 0; i < 4; i++) {
            Button btn = new Button(ctx);
            btn.setText(levels[i]);
            final int lv = i;
            btn.setOnClickListener(v -> {
                logLevel = lv;
                Toast.makeText(ctx, "日志等级: " + levels[lv], Toast.LENGTH_SHORT).show();
            });
            levelRow.addView(btn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        layout.addView(levelRow);

        Button btnLog = new Button(ctx);
        btnLog.setText("查看日志 (" + logs.size() + "条)");
        btnLog.setOnClickListener(v -> showLogs(ctx));
        layout.addView(btnLog);

        Button btnClearLog = new Button(ctx);
        btnClearLog.setText("清空日志");
        btnClearLog.setOnClickListener(v -> logs.clear());
        layout.addView(btnClearLog);

        // 复制 API 信息
        Button btnCopyApi = new Button(ctx);
        btnCopyApi.setText("复制 API 信息");
        btnCopyApi.setOnClickListener(v -> {
            String info = "接口: " + lastChatUrl + "\n" +
                "Token: " + lastApiKey + "\n" +
                "Bot ID: " + lastBotId + "\n" +
                "User ID: " + lastUserId;
            android.content.ClipboardManager cm = (android.content.ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(android.content.ClipData.newPlainText("MoraAPI", info));
            Toast.makeText(ctx, "已复制 API 信息", Toast.LENGTH_SHORT).show();
        });
        layout.addView(btnCopyApi);

        new AlertDialog.Builder(ctx).setView(layout).setPositiveButton("返回", null).show();
    }

    /** 第二层: 消息格式 */
    private void showFormatDialog(Context ctx) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(ctx);
        title.setText("消息格式");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        Switch sw = new Switch(ctx);
        sw.setText("启用消息改写");
        sw.setChecked(rewriteEnabled);
        layout.addView(sw);

        TextView label = new TextView(ctx);
        label.setText("当前模板:");
        label.setTextSize(14);
        label.setPadding(0, 25, 0, 5);
        layout.addView(label);

        final EditText etTpl = new EditText(ctx);
        etTpl.setText(messageTemplate);
        etTpl.setTextSize(16);
        etTpl.setPadding(20, 15, 20, 15);
        etTpl.setBackgroundColor(0xFFEEEEEE);
        layout.addView(etTpl);

        TextView quickLabel = new TextView(ctx);
        quickLabel.setText("快速插入:");
        quickLabel.setTextSize(13);
        quickLabel.setPadding(0, 20, 0, 10);
        layout.addView(quickLabel);

        LinearLayout row1 = new LinearLayout(ctx);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setWeightSum(2);

        Button btnMsg = new Button(ctx);
        btnMsg.setText("{msg}");
        btnMsg.setOnClickListener(v -> {
            messageTemplate = etTpl.getText().toString() + "{msg}";
            etTpl.setText(messageTemplate);
            etTpl.setSelection(messageTemplate.length());
        });
        row1.addView(btnMsg, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button btnTime = new Button(ctx);
        btnTime.setText("{time}");
        btnTime.setOnClickListener(v -> {
            messageTemplate = etTpl.getText().toString() + "{time}";
            etTpl.setText(messageTemplate);
            etTpl.setSelection(messageTemplate.length());
        });
        row1.addView(btnTime, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        layout.addView(row1);

        if (!customFragments.isEmpty()) {
            LinearLayout row2 = new LinearLayout(ctx);
            row2.setOrientation(LinearLayout.HORIZONTAL);
            row2.setWeightSum(customFragments.size());
            for (String frag : customFragments) {
                int idx = frag.indexOf(":");
                String name = idx > 0 ? frag.substring(0, idx) : "?";
                Button btn = new Button(ctx);
                btn.setText("{" + name + "}");
                btn.setOnClickListener(v -> {
                    messageTemplate = etTpl.getText().toString() + "{" + name + "}";
                    etTpl.setText(messageTemplate);
                    etTpl.setSelection(messageTemplate.length());
                });
                row2.addView(btn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            }
            layout.addView(row2);
        }

        LinearLayout rowOps = new LinearLayout(ctx);
        rowOps.setOrientation(LinearLayout.HORIZONTAL);
        rowOps.setWeightSum(3);

        Button btnClear = new Button(ctx);
        btnClear.setText("清空");
        btnClear.setOnClickListener(v -> {
            etTpl.setText("");
        });
        rowOps.addView(btnClear, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button btnAdd = new Button(ctx);
        btnAdd.setText("加片段");
        btnAdd.setOnClickListener(v -> showEditFragmentDialog(ctx, -1));
        rowOps.addView(btnAdd, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button btnManage = new Button(ctx);
        btnManage.setText("管理");
        btnManage.setOnClickListener(v -> showManageFragmentsDialog(ctx));
        rowOps.addView(btnManage, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        layout.addView(rowOps);

        Button btnSave = new Button(ctx);
        btnSave.setText("保存并返回");
        btnSave.setOnClickListener(v -> {
            rewriteEnabled = sw.isChecked();
            messageTemplate = etTpl.getText().toString();
            Toast.makeText(ctx, "已保存", Toast.LENGTH_SHORT).show();
        });
        layout.addView(btnSave);

        ScrollView sv = new ScrollView(ctx);
        sv.addView(layout);
        new AlertDialog.Builder(ctx).setView(sv).setNegativeButton("返回", null).show();
    }

    /** 第三层: 管理片段列表 */
    private void showManageFragmentsDialog(Context ctx) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(ctx);
        title.setText("管理自定义片段");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        for (int i = 0; i < customFragments.size(); i++) {
            final int idx = i;
            String frag = customFragments.get(i);
            int cIdx = frag.indexOf(":");
            String name = cIdx > 0 ? frag.substring(0, cIdx) : frag;

            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView label = new TextView(ctx);
            label.setText(name);
            label.setTextSize(14);
            label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(label);

            Button btnEdit = new Button(ctx);
            btnEdit.setText("编辑");
            btnEdit.setOnClickListener(v -> showEditFragmentDialog(ctx, idx));
            row.addView(btnEdit);

            Button btnDel = new Button(ctx);
            btnDel.setText("删除");
            btnDel.setOnClickListener(v -> { customFragments.remove(idx); showManageFragmentsDialog(ctx); });
            row.addView(btnDel);

            layout.addView(row);
        }

        Button btnAdd = new Button(ctx);
        btnAdd.setText("+ 添加新片段");
        btnAdd.setOnClickListener(v -> showEditFragmentDialog(ctx, -1));
        layout.addView(btnAdd);

        ScrollView sv = new ScrollView(ctx);
        sv.addView(layout);
        new AlertDialog.Builder(ctx).setView(sv).setNegativeButton("返回", null).show();
    }

    /** 第三层: 编辑/新建片段 */
    private void showEditFragmentDialog(Context ctx, int editIdx) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(ctx);
        title.setText(editIdx >= 0 ? "编辑片段" : "新建片段");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        TextView lName = new TextView(ctx);
        lName.setText("名字:");
        lName.setTextSize(14);
        layout.addView(lName);

        final EditText etName = new EditText(ctx);
        if (editIdx >= 0) {
            String frag = customFragments.get(editIdx);
            int idx = frag.indexOf(":");
            etName.setText(idx > 0 ? frag.substring(0, idx) : "");
        }
        etName.setHint("比如: 问候语");
        layout.addView(etName);

        TextView lContent = new TextView(ctx);
        lContent.setText("内容:");
        lContent.setTextSize(14);
        lContent.setPadding(0, 20, 0, 5);
        layout.addView(lContent);

        final EditText etContent = new EditText(ctx);
        if (editIdx >= 0) {
            String frag = customFragments.get(editIdx);
            int idx = frag.indexOf(":");
            etContent.setText(idx > 0 ? frag.substring(idx + 1) : "");
        }
        etContent.setHint("输入内容");
        layout.addView(etContent);

        ScrollView sv = new ScrollView(ctx);
        sv.addView(layout);

        new AlertDialog.Builder(ctx)
            .setView(sv)
            .setPositiveButton("保存", (d, i) -> {
                String name = etName.getText().toString();
                String content = etContent.getText().toString();
                if (editIdx >= 0) customFragments.set(editIdx, name + ":" + content);
                else customFragments.add(name + ":" + content);
                Toast.makeText(ctx, "已保存", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    /** 文件浏览器 */
    private void showFilePicker(Context ctx, final OnFilePicked callback) {
        addLog("打开文件选择器");
        try {
            // 用应用的外部存储目录作为默认目录
            File defaultDir = ctx.getExternalFilesDir(null);
            if (defaultDir == null || !defaultDir.exists()) {
                defaultDir = new File("/sdcard");
            }
            addLog("默认目录: " + defaultDir.getAbsolutePath());
            showFilePicker(ctx, defaultDir, callback);
        } catch (Exception e) {
            addLog("文件选择器出错: " + e.getMessage());
            Toast.makeText(ctx, "文件选择器出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showFilePicker(Context ctx, File dir, final OnFilePicked callback) {
        try {
            if (!dir.exists()) {
                addLog("目录不存在: " + dir.getAbsolutePath());
                return;
            }
            addLog("浏览目录: " + dir.getAbsolutePath());

            File[] files = dir.listFiles();
            final ArrayList<File> list = new ArrayList<>();
            if (files != null) {
                list.addAll(Arrays.asList(files));
                // 文件夹在前，文件在后
                Collections.sort(list, (a, b) -> {
                    if (a.isDirectory() && !b.isDirectory()) return -1;
                    if (!a.isDirectory() && b.isDirectory()) return 1;
                    return a.getName().compareToIgnoreCase(b.getName());
                });
            }
            addLog("找到 " + list.size() + " 个文件/文件夹");

            LinearLayout layout = new LinearLayout(ctx);
            layout.setOrientation(LinearLayout.VERTICAL);

            // 当前路径
            TextView pathView = new TextView(ctx);
            pathView.setText("当前: " + dir.getAbsolutePath());
            pathView.setTextSize(12);
            pathView.setPadding(20, 10, 20, 10);
            layout.addView(pathView);

            // 返回上一级
            final File parentDir = dir.getParentFile();
            if (parentDir != null) {
                Button btnUp = new Button(ctx);
                btnUp.setText(".. 返回上一级");
                btnUp.setOnClickListener(v -> showFilePicker(ctx, parentDir, callback));
                layout.addView(btnUp);
            }

            // 文件列表
            ListView lv = new ListView(ctx);
            ArrayList<String> names = new ArrayList<>();
            for (File f : list) {
                names.add((f.isDirectory() ? "📁 " : "📄 ") + f.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_list_item_1, names);
            lv.setAdapter(adapter);
            layout.addView(lv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 600));

            lv.setOnItemClickListener((parent, view, position, id) -> {
                File selected = list.get(position);
                addLog("点击了: " + selected.getName() + (selected.isDirectory() ? " (文件夹)" : " (文件)"));
                if (selected.isDirectory()) {
                    showFilePicker(ctx, selected, callback);
                } else {
                    addLog("选择文件: " + selected.getAbsolutePath());
                    callback.onPicked(selected);
                }
            });

            addLog("显示文件选择对话框");
            new AlertDialog.Builder(ctx)
                .setTitle("选择文件")
                .setView(layout)
                .setNegativeButton("取消", null)
                .show();
        } catch (Exception e) {
            addLog("文件浏览器失败: " + e.getMessage());
            Toast.makeText(ctx, "文件浏览器失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    interface OnFilePicked {
        void onPicked(File file);
    }

    private void showLogs(Context ctx) {
        StringBuilder sb = new StringBuilder();
        for (String l : logs) sb.append(l).append("\n");
        String text = sb.toString();

        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setPadding(20, 20, 20, 20);

        ScrollView sv = new ScrollView(ctx);
        sv.addView(tv);

        new AlertDialog.Builder(ctx)
            .setTitle("模块日志 (" + logs.size() + "条)")
            .setView(sv)
            .setPositiveButton("关闭", null)
            .setNegativeButton("复制", (d, i) -> {
                android.content.ClipboardManager cm = (android.content.ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(android.content.ClipData.newPlainText("Mora", text));
                Toast.makeText(ctx, "已复制", Toast.LENGTH_SHORT).show();
            })
            .setNeutralButton("清空", (d, i) -> logs.clear())
            .show();
    }
}
