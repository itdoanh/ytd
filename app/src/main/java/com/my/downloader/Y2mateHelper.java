package com.my.downloader;

import android.os.Handler;
import android.os.Looper;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Y2mateHelper {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();
    
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Multiple API endpoints for fallback
    private static final String[] API_DOMAINS = {
        "https://yt1s.ltd",           // Y2mate alternative 1
        "https://www.yt1s.com",       // Y2mate alternative 2
        "https://ytmp3.nu",           // Y2mate alternative 3
        "https://api.cobalt.tools",   // Cobalt API
    };
    
    private static int currentDomainIndex = 0;

    public interface ApiCallback { void onSuccess(VideoItem item); void onError(String msg); }
    public interface ConvertCallback { void onSuccess(String dlink); void onError(String msg); }
    public interface PingCallback { void onResult(boolean isOnline, String log); }

    // Extract video ID from YouTube URL
    private static String extractVideoId(String url) {
        Pattern pattern = Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // 1. Test API availability
    public static void testApi(PingCallback cb) {
        testDomain(0, cb);
    }
    
    private static void testDomain(int index, PingCallback cb) {
        if (index >= API_DOMAINS.length) {
            mainHandler.post(() -> cb.onResult(false, "❌ Tất cả API đều offline"));
            return;
        }
        
        Request req = new Request.Builder()
                .url(API_DOMAINS[index])
                .addHeader("User-Agent", "Mozilla/5.0 (Android 12)")
                .build();
        
        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override 
            public void onFailure(Call call, IOException e) { 
                testDomain(index + 1, cb);
            }
            
            @Override 
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                response.close();
                if (code == 200 || code == 403) {
                    currentDomainIndex = index;
                    mainHandler.post(() -> cb.onResult(true, "✅ API ONLINE: " + API_DOMAINS[index]));
                } else {
                    testDomain(index + 1, cb);
                }
            }
        });
    }

    // 2. Analyze video using multiple API methods
    public static void analyze(VideoItem item, ApiCallback cb) {
        String videoId = extractVideoId(item.url);
        if (videoId == null) {
            mainHandler.post(() -> cb.onError("❌ URL không hợp lệ"));
            return;
        }
        
        // Try Cobalt API first (most reliable for 2026)
        if (currentDomainIndex == 3) {
            analyzeCobalt(item, videoId, cb);
        } else {
            analyzeYT1S(item, videoId, cb);
        }
    }
    
    // Method 1: YT1S API (Y2mate alternative)
    private static void analyzeYT1S(VideoItem item, String videoId, ApiCallback cb) {
        String domain = API_DOMAINS[currentDomainIndex];
        
        FormBody body = new FormBody.Builder()
                .add("url", "https://www.youtube.com/watch?v=" + videoId)
                .add("ajax", "1")
                .add("lang", "en")
                .build();
        
        Request req = new Request.Builder()
                .url(domain + "/api/ajaxSearch")
                .post(body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override 
            public void onFailure(Call call, IOException e) { 
                tryNextDomain(item, cb);
            }
            
            @Override 
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        response.close();
                        tryNextDomain(item, cb);
                        return;
                    }
                    
                    String resBody = response.body().string();
                    response.close();
                    
                    JSONObject json = new JSONObject(resBody);
                    
                    if (!json.optString("status", "").equals("ok")) {
                        tryNextDomain(item, cb);
                        return;
                    }
                    
                    item.vid = videoId;
                    item.title = json.optString("title", "Unknown Video");
                    item.thumbUrl = "https://i.ytimg.com/vi/" + videoId + "/maxresdefault.jpg";

                    // Parse MP4 links
                    JSONObject links = json.optJSONObject("links");
                    if (links != null) {
                        JSONObject mp4 = links.optJSONObject("mp4");
                        if (mp4 != null) {
                            Iterator<String> keys = mp4.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject format = mp4.getJSONObject(key);
                                String quality = format.optString("q", key);
                                String size = format.optString("size", "");
                                String k = format.optString("k", "");
                                if (!k.isEmpty()) {
                                    item.mp4Formats.put(quality + " (" + size + ")", k);
                                }
                            }
                        }
                        
                        JSONObject mp3 = links.optJSONObject("mp3");
                        if (mp3 != null) {
                            Iterator<String> keys = mp3.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject format = mp3.getJSONObject(key);
                                String quality = format.optString("q", key);
                                String size = format.optString("size", "");
                                String k = format.optString("k", "");
                                if (!k.isEmpty()) {
                                    item.mp3Formats.put(quality + " (" + size + ")", k);
                                }
                            }
                        }
                    }
                    
                    if (item.mp4Formats.isEmpty() && item.mp3Formats.isEmpty()) {
                        tryNextDomain(item, cb);
                        return;
                    }
                    
                    item.isReady = true;
                    mainHandler.post(() -> cb.onSuccess(item));
                    
                } catch (Exception e) {
                    tryNextDomain(item, cb);
                }
            }
        });
    }
    
    // Method 2: Cobalt API (modern, reliable)
    private static void analyzeCobalt(VideoItem item, String videoId, ApiCallback cb) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("url", "https://www.youtube.com/watch?v=" + videoId);
            requestBody.put("vCodec", "h264");
            requestBody.put("vQuality", "1080");
            requestBody.put("aFormat", "mp3");
        } catch (JSONException e) {
            mainHandler.post(() -> cb.onError("❌ Lỗi tạo request"));
            return;
        }
        
        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json")
        );
        
        Request req = new Request.Builder()
                .url("https://api.cobalt.tools/api/json")
                .post(body)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override 
            public void onFailure(Call call, IOException e) { 
                mainHandler.post(() -> cb.onError("❌ Cobalt API lỗi: " + e.getMessage())); 
            }
            
            @Override 
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String resBody = response.body().string();
                    response.close();
                    
                    JSONObject json = new JSONObject(resBody);
                    
                    item.vid = videoId;
                    item.title = json.optString("filename", "Video_" + videoId);
                    item.thumbUrl = "https://i.ytimg.com/vi/" + videoId + "/maxresdefault.jpg";
                    
                    String url = json.optString("url", "");
                    if (!url.isEmpty()) {
                        // Direct download link
                        item.mp4Formats.put("Best Quality", url);
                    }
                    
                    // Audio link
                    JSONObject audio = json.optJSONObject("audio");
                    if (audio != null) {
                        String audioUrl = audio.optString("url", "");
                        if (!audioUrl.isEmpty()) {
                            item.mp3Formats.put("Audio (128kbps)", audioUrl);
                        }
                    }
                    
                    if (item.mp4Formats.isEmpty() && item.mp3Formats.isEmpty()) {
                        mainHandler.post(() -> cb.onError("❌ Không lấy được link download"));
                        return;
                    }
                    
                    item.isReady = true;
                    mainHandler.post(() -> cb.onSuccess(item));
                    
                } catch (Exception e) {
                    mainHandler.post(() -> cb.onError("❌ Lỗi parse Cobalt: " + e.getMessage()));
                }
            }
        });
    }
    
    private static void tryNextDomain(VideoItem item, ApiCallback cb) {
        currentDomainIndex++;
        if (currentDomainIndex >= API_DOMAINS.length) {
            currentDomainIndex = 0;
            mainHandler.post(() -> cb.onError("❌ Tất cả API đều thất bại. Thử lại sau."));
        } else {
            analyze(item, cb);
        }
    }

    // 3. Convert to get download link
    public static void convert(String vid, String token, ConvertCallback cb) {
        // If token is already a direct URL (from Cobalt)
        if (token.startsWith("http")) {
            mainHandler.post(() -> cb.onSuccess(token));
            return;
        }
        
        String domain = API_DOMAINS[currentDomainIndex];
        
        FormBody body = new FormBody.Builder()
                .add("vid", vid)
                .add("k", token)
                .build();
        
        Request req = new Request.Builder()
                .url(domain + "/api/ajaxConvert")
                .post(body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .build();

        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override 
            public void onFailure(Call call, IOException e) { 
                mainHandler.post(() -> cb.onError("❌ Convert lỗi: " + e.getMessage())); 
            }
            
            @Override 
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String resBody = response.body().string();
                    response.close();
                    
                    JSONObject json = new JSONObject(resBody);
                    
                    String status = json.optString("status", "");
                    if ("success".equals(status) || "ok".equals(status)) {
                        String dlink = json.optString("dlink", "");
                        if (!dlink.isEmpty()) {
                            mainHandler.post(() -> cb.onSuccess(dlink));
                            return;
                        }
                    }
                    
                    String c_status = json.optString("c_status", "");
                    if ("CONVERTING".equals(c_status)) {
                        mainHandler.post(() -> cb.onError("⏳ Đang convert... Thử lại sau 5s"));
                        return;
                    }
                    
                    mainHandler.post(() -> cb.onError("❌ Không lấy được link: " + json.optString("mess", "Unknown")));
                    
                } catch (Exception e) {
                    mainHandler.post(() -> cb.onError("❌ Lỗi parse response: " + e.getMessage()));
                }
            }
        });
    }
}