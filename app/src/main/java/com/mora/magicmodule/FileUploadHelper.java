package com.mora.magicmodule;

import android.content.Context;
import android.widget.Toast;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 文件上传辅助类（放在 classes2.dex 里）
 * 通过反射被 MoraHooker 调用
 */
public class FileUploadHelper {
    
    private static final String TAG = "MoraMagic";
    
    public static void upload(Context ctx, File file, String apiKey) {
        // 写日志到魔姬助手内部目录
        log(ctx, "FileUploadHelper: 开始上传文件: " + file.getName());
        
        new Thread(() -> {
            try {
                // 1. 获取 presigned URL
                log(ctx, "正在获取上传地址...");
                URL presignedUrl = new URL("https://aigc.ztems.com/aigcapi/oss/file/presignedurl");
                HttpURLConnection conn = (HttpURLConnection) presignedUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", apiKey);
                conn.setDoOutput(true);
                
                // 根据 OSS 路径格式 /tempfiles/1249941/文件名 推断请求参数
                String requestBody = "{\"fileName\":\"" + file.getName() + "\",\"filePath\":\"tempfiles/\"}";
                conn.getOutputStream().write(requestBody.getBytes("UTF-8"));
                
                int responseCode = conn.getResponseCode();
                log(ctx, "presignedurl 响应码: " + responseCode);
                
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    String respStr = response.toString();
                    log(ctx, "presignedurl 响应: " + respStr);
                    
                    // 解析响应，获取上传 URL 和 OSS URL
                    org.json.JSONObject json = new org.json.JSONObject(respStr);
                    String uploadUrl = json.optString("uploadUrl", "");
                    String ossUrl = json.optString("ossUrl", "");
                    log(ctx, "上传 URL: " + uploadUrl);
                    log(ctx, "OSS URL: " + ossUrl);
                    
                    if (!uploadUrl.isEmpty()) {
                        // 2. PUT 上传文件到 OSS
                        log(ctx, "正在上传文件到 OSS...");
                        URL putUrl = new URL(uploadUrl);
                        HttpURLConnection putConn = (HttpURLConnection) putUrl.openConnection();
                        putConn.setRequestMethod("PUT");
                        putConn.setDoOutput(true);
                        
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[4096];
                        int len;
                        OutputStream os = putConn.getOutputStream();
                        while ((len = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        fis.close();
                        os.close();
                        
                        int putCode = putConn.getResponseCode();
                        log(ctx, "OSS 上传响应码: " + putCode);
                        putConn.disconnect();
                        
                        if (putCode == 200) {
                            log(ctx, "文件上传成功！OSS URL: " + ossUrl);
                            // TODO: 把 OSS URL 拼在消息里发送
                        } else {
                            log(ctx, "OSS 上传失败: " + putCode);
                        }
                    }
                } else {
                    log(ctx, "获取上传地址失败: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                log(ctx, "上传文件出错: " + e.getMessage());
            }
        }).start();
        
        Toast.makeText(ctx, "开始上传文件...", Toast.LENGTH_SHORT).show();
    }
    
    private static void log(Context ctx, String msg) {
        android.util.Log.i(TAG, msg);
        try {
            File dir = new File(ctx.getFilesDir(), "mora_logs");
            dir.mkdirs();
            File logFile = new File(dir, "logs.txt");
            FileWriter fw = new FileWriter(logFile, true);
            fw.write(System.currentTimeMillis() + " [D] " + msg + "\n");
            fw.close();
        } catch (Exception e) {
            // 忽略写文件错误
        }
    }
}
