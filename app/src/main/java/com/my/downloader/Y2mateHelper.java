package com.my.downloader;

import android.os.Handler;
import android.os.Looper;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Iterator;

public class Y2mateHelper {
    private static final OkHttpClient client = new OkHttpClient();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Tên miền mới theo yêu cầu của bạn
    private static final String DOMAIN = "https://wwv-y2mate.com"; 

    public interface ApiCallback { void onSuccess(VideoItem item); void onError(String msg); }
    public interface ConvertCallback { void onSuccess(String dlink); void onError(String msg); }
    public interface PingCallback { void onResult(boolean isOnline, String log); }

    // 1. AUTO TEST API khi mở app (Bắt log chi tiết)
    public static void testApi(PingCallback cb) {
        Request req = new Request.Builder().url(DOMAIN).build();
        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { 
                mainHandler.post(()->cb.onResult(false, "Lỗi kết nối mạng: " + e.getMessage())); 
            }
            @Override public void onResponse(Call call, Response response) throws IOException { 
                int code = response.code();
                mainHandler.post(()->cb.onResult(response.isSuccessful(), "Mã HTTP: " + code)); 
            }
        });
    }

    // 2. Lấy danh sách chất lượng (Analyze)
    public static void analyze(VideoItem item, ApiCallback cb) {
        FormBody body = new FormBody.Builder().add("k_query", item.url).add("q_auto", "1").build();
        Request req = new Request.Builder()
                .url(DOMAIN + "/mates/analyzeV2/ajax")
                .post(body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)") // Giả lập máy tính
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Origin", DOMAIN)
                .addHeader("Referer", DOMAIN + "/vi/")
                .build();

        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { 
                mainHandler.post(() -> cb.onError("Analyze Request Lỗi: " + e.getMessage())); 
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String err = response.body() != null ? response.body().string() : "Rỗng";
                    mainHandler.post(() -> cb.onError("HTTP Lỗi " + response.code() + "\nChi tiết: " + err));
                    return;
                }
                
                String resBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(resBody);
                    item.vid = json.getString("vid");
                    item.title = json.getString("title");
                    item.thumbUrl = "https://i.ytimg.com/vi/" + item.vid + "/0.jpg";

                    JSONObject links = json.getJSONObject("links");
                    if(links.has("mp4")) {
                        JSONObject mp4 = links.getJSONObject("mp4");
                        Iterator<String> keys = mp4.keys();
                        while(keys.hasNext()) {
                            JSONObject format = mp4.getJSONObject(keys.next());
                            item.mp4Formats.put(format.getString("q") + " (" + format.getString("size") + ")", format.getString("k"));
                        }
                    }
                    if(links.has("mp3")) {
                        JSONObject mp3 = links.getJSONObject("mp3");
                        Iterator<String> keys = mp3.keys();
                        while(keys.hasNext()) {
                            JSONObject format = mp3.getJSONObject(keys.next());
                            item.mp3Formats.put(format.getString("q") + " (" + format.getString("size") + ")", format.getString("k"));
                        }
                    }
                    item.isReady = true;
                    mainHandler.post(() -> cb.onSuccess(item));
                } catch (Exception e) { 
                    mainHandler.post(() -> cb.onError("Lỗi Parse JSON Analyze:\n" + e.getMessage() + "\n\nServer trả về:\n" + resBody)); 
                }
            }
        });
    }

    // 3. Gọi Token để lấy Direct Link thật (Convert)
    public static void convert(String vid, String token, ConvertCallback cb) {
        FormBody body = new FormBody.Builder().add("vid", vid).add("k", token).build();
        Request req = new Request.Builder()
                .url(DOMAIN + "/mates/convertV2/index")
                .post(body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Origin", DOMAIN)
                .addHeader("Referer", DOMAIN + "/vi/")
                .build();

        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { 
                mainHandler.post(() -> cb.onError("Convert Request Lỗi: " + e.getMessage())); 
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String err = response.body() != null ? response.body().string() : "Rỗng";
                    mainHandler.post(() -> cb.onError("HTTP Lỗi " + response.code() + "\nChi tiết: " + err));
                    return;
                }
                
                String resBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(resBody);
                    if(json.has("dlink")) {
                        String dlink = json.getString("dlink");
                        mainHandler.post(() -> cb.onSuccess(dlink));
                    } else {
                        mainHandler.post(() -> cb.onError("Token hết hạn hoặc server không có dlink.\nServer trả về: " + resBody));
                    }
                } catch (Exception e) { 
                    mainHandler.post(() -> cb.onError("Lỗi Parse JSON Convert:\n" + e.getMessage() + "\n\nServer trả về:\n" + resBody)); 
                }
            }
        });
    }
}