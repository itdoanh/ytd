package com.my.downloader;

import android.os.Handler;
import android.os.Looper;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class Y2mateHelper {
    // Khởi tạo OkHttpClient với timeout và Cloudflare handling
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Hai domain chính hiện đang hoạt động tốt
    private static final String DOMAIN = "https://www.y2mate.com";
    private static final String DOMAIN_BACKUP = "https://y2mate.me"; 

    public interface ApiCallback { void onSuccess(VideoItem item); void onError(String msg); }
    public interface ConvertCallback { void onSuccess(String dlink); void onError(String msg); }
    public interface PingCallback { void onResult(boolean isOnline, String log); }

    // 1. AUTO TEST API khi mở app
    public static void testApi(PingCallback cb) {
        Request req = new Request.Builder()
                .url(DOMAIN)
                .addHeader("User-Agent", "Mozilla/5.0 (Android 12)")
                .build();
        
        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override 
            public void onFailure(Call call, IOException e) { 
                mainHandler.post(() -> cb.onResult(false, "❌ Lỗi kết nối: " + e.getMessage())); 
            }
            
            @Override 
            public void onResponse(Call call, Response response) throws IOException { 
                int code = response.code();
                boolean isOnline = code == 200 || code == 403; // 403 có thể do Cloudflare
                String msg = isOnline ? "✅ API ONLINE (HTTP " + code + ")" : "❌ Lỗi HTTP " + code;
                mainHandler.post(() -> cb.onResult(isOnline, msg)); 
                response.close();
            }
        });
    }

    // 2. Lấy danh sách chất lượng (Analyze) - xử lý Cloudflare
    public static void analyze(VideoItem item, ApiCallback cb) {
        FormBody body = new FormBody.Builder()
                .add("k_query", item.url)
                .add("q_auto", "1")
                .build();
        
        Request req = new Request.Builder()
                .url(DOMAIN + "/mates/analyzeV2/ajax")
                .post(body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Origin", DOMAIN)
                .addHeader("Referer", DOMAIN + "/")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override 
            public void onFailure(Call call, IOException e) { 
                mainHandler.post(() -> cb.onError("❌ Analyze Lỗi: " + e.getMessage())); 
            }
            
            @Override 
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String err = response.body() != null ? response.body().string() : "Unknown";
                        int code = response.code();
                        String errMsg = (code == 403 || code == 429) 
                            ? "⚠️ Cloudflare chặn. Thử lại sau..."
                            : "HTTP Lỗi " + code;
                        mainHandler.post(() -> cb.onError(errMsg + "\n" + err));
                        response.close();
                        return;
                    }
                    
                    String resBody = response.body().string();
                    if (resBody == null || resBody.isEmpty()) {
                        mainHandler.post(() -> cb.onError("❌ Response rỗng từ server"));
                        response.close();
                        return;
                    }
                    
                    JSONObject json = new JSONObject(resBody);
                    
                    // Kiểm tra status
                    if (!json.optString("status", "").equals("ok")) {
                        String mess = json.optString("mess", "Server trả về lỗi");
                        mainHandler.post(() -> cb.onError("❌ " + mess));
                        response.close();
                        return;
                    }
                    
                    item.vid = json.getString("vid");
                    item.title = json.getString("title");
                    item.thumbUrl = "https://i.ytimg.com/vi/" + item.vid + "/0.jpg";

                    JSONObject links = json.getJSONObject("links");
                    
                    // Parse MP4 formats
                    if (links.has("mp4")) {
                        JSONObject mp4 = links.getJSONObject("mp4");
                        Iterator<String> keys = mp4.keys();
                        while (keys.hasNext()) {
                            JSONObject format = mp4.getJSONObject(keys.next());
                            String quality = format.getString("q");
                            String size = format.getString("size");
                            String token = format.getString("k");
                            item.mp4Formats.put(quality + " (" + size + ")", token);
                        }
                    }
                    
                    // Parse MP3 formats
                    if (links.has("mp3")) {
                        JSONObject mp3 = links.getJSONObject("mp3");
                        Iterator<String> keys = mp3.keys();
                        while (keys.hasNext()) {
                            JSONObject format = mp3.getJSONObject(keys.next());
                            String quality = format.getString("q");
                            String size = format.getString("size");
                            String token = format.getString("k");
                            item.mp3Formats.put(quality + " (" + size + ")", token);
                        }
                    }
                    
                    item.isReady = true;
                    mainHandler.post(() -> cb.onSuccess(item));
                    response.close();
                    
                } catch (JSONException e) {
                    mainHandler.post(() -> cb.onError("❌ Lỗi Parse JSON:\n" + e.getMessage())); 
                    response.close();
                } catch (Exception e) {
                    mainHandler.post(() -> cb.onError("❌ Lỗi Analyze:\n" + e.getMessage())); 
                    response.close();
                }
            }
        });
    }

    // 3. Gọi Token để lấy Direct Link thật (Convert)
    public static void convert(String vid, String token, ConvertCallback cb) {
        FormBody body = new FormBody.Builder()
                .add("vid", vid)
                .add("k", token)
                .build();
        
        Request req = new Request.Builder()
                .url(DOMAIN + "/mates/convertV2/index")
                .post(body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Origin", DOMAIN)
                .addHeader("Referer", DOMAIN + "/")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override 
            public void onFailure(Call call, IOException e) { 
                mainHandler.post(() -> cb.onError("❌ Convert Lỗi\Timeout: " + e.getMessage())); 
            }
            
            @Override 
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String err = response.body() != null ? response.body().string() : "Unknown";
                        int code = response.code();
                        String errMsg = (code == 403 || code == 429) 
                            ? "⚠️ Cloudflare chặn. Thử lại..."
                            : "HTTP Lỗi " + code;
                        mainHandler.post(() -> cb.onError(errMsg + "\n" + err));
                        response.close();
                        return;
                    }
                    
                    String resBody = response.body().string();
                    if (resBody == null || resBody.isEmpty()) {
                        mainHandler.post(() -> cb.onError("❌ Response rỗng từ server"));
                        response.close();
                        return;
                    }
                    
                    JSONObject json = new JSONObject(resBody);
                    
                    // Kiểm tra status
                    String status = json.optString("c_status", "");
                    if ("CONVERTING".equals(status)) {
                        mainHandler.post(() -> cb.onError("⏳ Video đang được convert...\nThử lại sau 5 giây"));
                        response.close();
                        return;
                    }
                    
                    if (json.has("dlink")) {
                        String dlink = json.getString("dlink");
                        if (dlink != null && !dlink.isEmpty()) {
                            mainHandler.post(() -> cb.onSuccess(dlink));
                        } else {
                            mainHandler.post(() -> cb.onError("❌ Link tải không hợp lệ"));
                        }
                    } else {
                        String mess = json.optString("mess", "Token hết hạn");
                        mainHandler.post(() -> cb.onError("❌ " + mess));
                    }
                    response.close();
                    
                } catch (JSONException e) {
                    mainHandler.post(() -> cb.onError("❌ Lỗi Parse JSON:\n" + e.getMessage())); 
                    response.close();
                } catch (Exception e) {
                    mainHandler.post(() -> cb.onError("❌ Lỗi Convert:\n" + e.getMessage())); 
                    response.close();
                }
            }
        });
    }